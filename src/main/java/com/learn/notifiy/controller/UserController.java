package com.learn.notifiy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/protected")
    public ResponseEntity<?> checkAuthenticatedUser() {
        return ResponseEntity.ok().build();
    }
}
