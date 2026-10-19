package com.shopwave.controller;

import com.shopwave.dto.Dto.RegisterRequest;
import com.shopwave.dto.Dto.UserResponse;
import com.shopwave.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @Operation(summary = "Register a new customer account (public)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest r) { return auth.register(r); }

    @Operation(summary = "Current logged-in user")
    @GetMapping("/me")
    public UserResponse me(Principal principal) { return auth.me(principal.getName()); }
}
