package com.learn.notifiy.dto;

import com.learn.notifiy.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SignupRequest {
    @NotBlank (message = "Email is required")
    @Email (message = "Invalid Email")
    private String email;

    @NotBlank (message = "Password is required")
    private String password;

    private UserRole role;
}
