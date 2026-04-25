package com.auth.config;

import com.auth.entity.AuthUser;
import com.auth.entity.ReferenceType;
import com.auth.entity.Role;
import com.auth.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final AuthUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin.email}")
    private String adminEmail;

    @Value("${bootstrap.admin.password}")
    private String adminPassword;

    public AdminBootstrap(AuthUserRepository repository,
                          PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (repository.existsByEmail(adminEmail)) {
            return; // ✅ Admin already exists → do nothing
        }

        AuthUser admin = new AuthUser();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setReferenceType(ReferenceType.SYSTEM);
        admin.setReferenceId(null);
        admin.setEnabled(true);

        repository.save(admin);

        System.out.println("✅ Default ADMIN user created: " + adminEmail);
    }
}