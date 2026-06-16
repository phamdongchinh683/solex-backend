package com.example.solex_backend.service;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.UserInfoResponse;
import com.example.solex_backend.dto.request.LoginRequest;
import com.example.solex_backend.dto.request.SignupRequest;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.repository.UserRepository;
import com.example.solex_backend.util.Enums;
import com.example.solex_backend.util.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;
    private final OtpService otpService;

    private UserInfoResponse toUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .isEmailVerified(user.getIsEmailVerified())
                .isPhoneVerified(user.getIsPhoneVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public AuthResponse signup(SignupRequest request) {

        otpService.isOtpVerified(request.getEmail(), request.getPhone());

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Enums.UserRole.CUSTOMER)
                .isEmailVerified(1)
                .isPhoneVerified(1)
                .isActive(1)
                .tokenVersion(0)
                .build();
        userRepository.save(user);

        String token = jwt.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(toUserInfoResponse(user))
                .build();
    }

    public void logout() {
        String token = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        Long userId = jwt.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        if ((request.getEmail() == null || request.getEmail().isBlank())
                && (request.getPhone() == null || request.getPhone().isBlank())) {
            throw new BusinessException("Either email or phone must be provided");
        }

        User user;
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user = userRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new BusinessException("Account not found for this phone number"));
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new BusinessException("Account not found for this email"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("Incorrect password");
        }

        if (user.getIsActive() == 0) {
            throw new BusinessException("Account has not been activated");
        }

        String token = jwt.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .user(toUserInfoResponse(user))
                .build();
    }

}