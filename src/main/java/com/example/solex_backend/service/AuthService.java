package com.example.solex_backend.service;

import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.domain.UserOtp;
import com.example.solex_backend.dto.request.LoginRequest;
import com.example.solex_backend.dto.request.OperatorSignupRequest;
import com.example.solex_backend.dto.request.SignupRequest;
import com.example.solex_backend.dto.request.UpdateContactRequest;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.UserInfoResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.repository.RestaurantRepository;
import com.example.solex_backend.repository.UserOtpRepository;
import com.example.solex_backend.repository.UserRepository;
import com.example.solex_backend.util.Enums;
import com.example.solex_backend.util.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserOtpRepository userOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;
    private final OtpService otpService;

    private static final long CONTACT_CHANGE_COOLDOWN_HOURS = 24;

    private UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getRole(), user.getIsEmailVerified(),
                user.getIsPhoneVerified(), user.getIsActive(), user.getCreatedAt(),
                user.getLastChangeEmail(), user.getLastChangePhone());
    }

    public AuthResponse signupOperator(OperatorSignupRequest request) {
        otpService.isOtpVerified(request.email(), request.phone());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BusinessException("Email already registered");
        }
        if (request.phone() != null && userRepository.findByPhone(request.phone()).isPresent()) {
            throw new BusinessException("Phone already registered");
        }

        User operator = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(Enums.UserRole.OPERATOR)
                .isEmailVerified(1)
                .isPhoneVerified(1)
                .isActive(1)
                .tokenVersion(0)
                .build();
        userRepository.save(operator);

        OperatorSignupRequest.RestaurantInfo ri = request.restaurant();
        Restaurant restaurant = Restaurant.builder()
                .operator(operator)
                .name(ri.name())
                .description(ri.description())
                .phone(ri.phone())
                .addressDetail(request.addressDetail())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .imageUrl(ri.imageUrl())
                .star1(0).star2(0).star3(0).star4(0).star5(0)
                .isOpen(false)
                .build();
        restaurantRepository.save(restaurant);

        String token = jwt.generateToken(operator);
        return new AuthResponse(token, toUserInfoResponse(operator));
    }

    public AuthResponse signup(SignupRequest request) {
        otpService.isOtpVerified(request.email(), request.phone());

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(Enums.UserRole.CUSTOMER)
                .isEmailVerified(1)
                .isPhoneVerified(1)
                .isActive(1)
                .tokenVersion(0)
                .build();
        userRepository.save(user);

        String token = jwt.generateToken(user);
        return new AuthResponse(token, toUserInfoResponse(user));
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

    public void updateContact(User user, UpdateContactRequest request) {
        LocalDateTime now = LocalDateTime.now();

        switch (request.field()) {
            case EMAIL -> {
                if (user.getLastChangeEmail() != null &&
                        user.getLastChangeEmail().plusHours(CONTACT_CHANGE_COOLDOWN_HOURS).isAfter(now)) {
                    throw new BusinessException("Email can only be changed once every 24 hours");
                }
            }
            case PHONE -> {
                if (user.getLastChangePhone() != null &&
                        user.getLastChangePhone().plusHours(CONTACT_CHANGE_COOLDOWN_HOURS).isAfter(now)) {
                    throw new BusinessException("Phone can only be changed once every 24 hours");
                }
            }
        }

        UserOtp userOtp = switch (request.field()) {
            case EMAIL -> userOtpRepository.findByEmail(request.value())
                    .orElseThrow(() -> new BusinessException("OTP not found for this email — send OTP first"));
            case PHONE -> userOtpRepository.findByPhone(request.value())
                    .orElseThrow(() -> new BusinessException("OTP not found for this phone — send OTP first"));
        };

        if (userOtp.getExpiresAt() == null || userOtp.getExpiresAt().isBefore(now)) {
            throw new BusinessException("OTP has expired");
        }
        if (!request.otp().equals(userOtp.getOtp())) {
            throw new BusinessException("Invalid OTP");
        }

        switch (request.field()) {
            case EMAIL -> {
                user.setEmail(request.value());
                user.setLastChangeEmail(now);
            }
            case PHONE -> {
                user.setPhone(request.value());
                user.setLastChangePhone(now);
            }
        }
        userOtp.setOtp(null);
        userOtp.setExpiresAt(null);
        userOtpRepository.save(userOtp);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        if ((request.email() == null || request.email().isBlank())
                && (request.phone() == null || request.phone().isBlank())) {
            throw new BusinessException("Either email or phone must be provided");
        }

        User user;
        if (request.phone() != null && !request.phone().isBlank()) {
            user = userRepository.findByPhone(request.phone())
                    .orElseThrow(() -> new BusinessException("Account not found for this phone number"));
        } else {
            user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BusinessException("Account not found for this email"));
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Incorrect password");
        }

        if (user.getIsActive() == 0) {
            throw new BusinessException("Account has not been activated");
        }

        String token = jwt.generateToken(user);
        return new AuthResponse(token, toUserInfoResponse(user));
    }
}
