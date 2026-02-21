package com.learn.notifiy.service;

import com.learn.notifiy.dto.LoginRequest;
import com.learn.notifiy.dto.SignupRequest;
import com.learn.notifiy.entity.User;
import com.learn.notifiy.enums.UserRole;
import com.learn.notifiy.repository.UserRepository;
import com.learn.notifiy.utils.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public void register(SignupRequest request) {
        // validate request payload
        String email = request.getEmail();
        String password = request.getPassword();
        UserRole role = determineRole(request.getRole());

        if (userRepository.existsByEmail(email)) {
            throw new BadCredentialsException("User Already Exists");
        }

        // register a new user
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        userRepository.save(user);
    }

    private UserRole determineRole(String role) {
        // if role is null, default to RULE_USER
        // otherwise make sure it's a valid value
        if (role == null || role.isBlank())
            return UserRole.ROLE_USER;

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException exc) {
            throw new RuntimeException("Invalid Role.");
        }
    }

    public Map<String, String> login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.getTokens(request.getEmail());
    }
}
