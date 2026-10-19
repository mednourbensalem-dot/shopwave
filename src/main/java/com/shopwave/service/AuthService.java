package com.shopwave.service;

import com.shopwave.dto.Dto.RegisterRequest;
import com.shopwave.dto.Dto.UserResponse;
import com.shopwave.dto.Mapper;
import com.shopwave.exception.ApiException;
import com.shopwave.model.AppUser;
import com.shopwave.model.Role;
import com.shopwave.repo.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AuthService(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public UserResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        AppUser u = new AppUser();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.CUSTOMER); // public registration can never create admins
        return Mapper.user(users.save(u));
    }

    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return Mapper.user(users.findByEmail(email).orElseThrow(() -> ApiException.notFound("User")));
    }
}
