package com.portfolio.bank.user.internal;

import com.portfolio.bank.shared.security.JwtUtil;
import com.portfolio.bank.user.api.AuthResponse;
import com.portfolio.bank.user.api.LoginRequest;
import com.portfolio.bank.user.api.RegisterRequest;
import com.portfolio.bank.user.api.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);

        // Standard Spring Security User for JWT Generation
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                                                                            .username(user.getEmail())
                                                                            .password(user.getPassword())
                                                                            .roles(user.getRole().name())
                                                                            .build();

        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email()).orElseThrow();
        var userDetails = org.springframework.security.core.userdetails.User.builder()
                                                                            .username(user.getEmail())
                                                                            .password(user.getPassword())
                                                                            .roles(user.getRole().name())
                                                                            .build();

        String token = jwtUtil.generateToken(userDetails);
        return new AuthResponse(token, user.getEmail());
    }

    @Override
    public Long getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                             .map(UserEntity::getId)
                             .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}