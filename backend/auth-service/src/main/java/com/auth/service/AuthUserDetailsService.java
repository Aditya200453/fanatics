package com.auth.service;

import com.auth.entity.AuthUser;
import com.auth.repository.AuthUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository repo;

    public AuthUserDetailsService(AuthUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = repo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Spring Security expects ROLE_ prefix
        List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }
}