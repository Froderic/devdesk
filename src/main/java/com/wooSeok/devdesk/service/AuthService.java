package com.wooSeok.devdesk.service;

import com.wooSeok.devdesk.config.JwtService;
import com.wooSeok.devdesk.domain.entity.User;
import com.wooSeok.devdesk.dto.request.LoginRequest;
import com.wooSeok.devdesk.dto.response.AuthResponse;
import com.wooSeok.devdesk.exception.ResourceNotFoundException;
import com.wooSeok.devdesk.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
