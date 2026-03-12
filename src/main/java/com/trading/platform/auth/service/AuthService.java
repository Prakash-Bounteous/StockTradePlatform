package com.trading.platform.auth.service;

import com.trading.platform.auth.dto.*;
import com.trading.platform.auth.security.JwtUtil;
import com.trading.platform.common.enums.Role;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public void register(RegisterRequest request) {

        if (userRepository
                .findByUsername(request.getUsername())
                .isPresent()) {

            throw new RuntimeException("Username already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .balance(BigDecimal.valueOf(100000))
                .marginMultiplier(BigDecimal.valueOf(5))
                .usedMargin(BigDecimal.ZERO)
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getUsername());

        return new AuthResponse(token);
    }
}