package com.learn.notifiy.controller;

import com.learn.notifiy.dto.LoginRequest;
import com.learn.notifiy.dto.SignupRequest;
import com.learn.notifiy.enums.UserRole;
import com.learn.notifiy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // Loads the full application context
@AutoConfigureMockMvc // Injects MockMvc to simulate HTTP requests
@ActiveProfiles("test") // Uses the application-test.properties (H2 DB)
@Transactional // Rolls back the database changes after every test
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JsonMapper jsonMapper; // Jackson tool to convert Object to JSON

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Registration: Should return 200 and save user to DB")
    void register_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest("test@example.com", "password123", UserRole.ROLE_USER.toString());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // Verify the user actually exists in our In-Memory DB
        assert (userRepository.findByEmail("test@example.com").isPresent());
    }

    @Test
    @DisplayName("Registration: Should return 400 for invalid email (Validation Test)")
    void register_InvalidEmail() throws Exception {
        SignupRequest badRequest = new SignupRequest("not-an-email", "password", null);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").value("Invalid Email"));
    }

    @Test
    @DisplayName("Login: Should return 200 and tokens for valid credentials")
    void login_Success() throws Exception {
        // First, manually register a user
        SignupRequest signup = new SignupRequest("login@test.com", "password123", UserRole.ROLE_USER.toString());
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(signup)));

        // Now, attempt to log in
        LoginRequest loginRequest = new LoginRequest("login@test.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("Login: Should return 401 for wrong password")
    void login_WrongPassword() throws Exception {
        // Register user
        SignupRequest signup = new SignupRequest("wrong@pass.com", "correctPassword", UserRole.ROLE_USER.toString());
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(signup)));

        // Login with wrong password
        LoginRequest loginRequest = new LoginRequest("wrong@pass.com", "wrongPassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Credentials"));
    }
}