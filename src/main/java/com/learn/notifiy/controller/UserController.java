package com.learn.notifiy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @GetMapping("/protected")
    public String checkAuthenticatedUser() {
        return "Authenticated";
    }
}
