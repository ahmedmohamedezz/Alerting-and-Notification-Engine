package com.learn.notifiy.controller;

import com.learn.notifiy.dto.LoginRequest;
import com.learn.notifiy.dto.SignupRequest;
import com.learn.notifiy.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        userService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Map<String, String> tokens = userService.login(request);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody String refreshToken) {
        Map<String, String> token = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(token);
    }
}
