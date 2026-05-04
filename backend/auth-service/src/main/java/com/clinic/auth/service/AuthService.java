package com.clinic.auth.service;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
import com.clinic.auth.repository.UserRepository;
import com.clinic.auth.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

//        System.out.println("RAW password = [" + request.getPassword() + "]");
//        System.out.println("DB hash      = [" + user.getPassword() + "]");
//        boolean match = passwordEncoder.matches(
//                request.getPassword(),
//                user.getPassword()
//        );
//        System.out.println("PASSWORD MATCH = " + match);

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponse("Login successful", token, user.getRole().name());
    }

    public void forceCreateAdmin() {

        User user = userRepository.findByEmail("admin@clinic.com")
                .orElse(new User());

        user.setEmail("admin@clinic.com");
        user.setPassword(passwordEncoder.encode("admin123")); // ✅ regenerate hash
        user.setRole(Role.ADMIN);
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }


    // Optional: signup (keep if you want)
    public String signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ✅ patient signup flow: role is system decided
        user.setRole(Role.PATIENT);
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
        return "Signup successful";
    }

    public void registerDoctor(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor already exists"
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DOCTOR);
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }


}