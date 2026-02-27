package com.neurocart.service;

import com.neurocart.dto.AuthDTO;
import com.neurocart.entity.Role;
import com.neurocart.entity.User;
import com.neurocart.exception.BadRequestException;
import com.neurocart.exception.ResourceNotFoundException;
import com.neurocart.repository.RoleRepository;
import com.neurocart.repository.UserRepository;
import com.neurocart.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthDTO.AuthResponse register(AuthDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken: " + request.getUsername());
        }

        String roleName = (request.getRole() == null || request.getRole().isBlank())
                ? "ROLE_CUSTOMER"
                : "ROLE_" + request.getRole().toUpperCase();

        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleName))
                .orElseThrow(() -> new BadRequestException("Invalid role: " + roleName));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .roles(Set.of(role))
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getEmail(), roleName);
        return new AuthDTO.AuthResponse(token, saved.getId(), saved.getUsername(),
                saved.getEmail(), saved.getFullName(), roleName, saved.getAvatarUrl());
    }

    public AuthDTO.AuthResponse login(AuthDTO.LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (!user.isActive()) {
            throw new BadRequestException("Your account has been deactivated");
        }

        String roleName = user.getRoles().stream().findFirst()
                .map(r -> r.getName().name()).orElse("ROLE_CUSTOMER");

        String token = jwtUtil.generateToken(user.getEmail(), roleName);
        return new AuthDTO.AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getFullName(), roleName, user.getAvatarUrl());
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
