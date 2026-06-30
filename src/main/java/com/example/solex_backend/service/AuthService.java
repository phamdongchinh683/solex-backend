package com.example.solex_backend.service;

import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.domain.UserDevice;
import com.example.solex_backend.domain.UserOtp;
import com.example.solex_backend.dto.request.LoginRequest;
import com.example.solex_backend.dto.request.OperatorSignupRequest;
import com.example.solex_backend.dto.request.RegisterDeviceRequest;
import com.example.solex_backend.dto.request.ResetPasswordRequest;
import com.example.solex_backend.dto.request.SignupRequest;
import com.example.solex_backend.dto.request.UpdateContactRequest;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.DeviceResponse;
import com.example.solex_backend.dto.response.UserInfoResponse;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.repository.RestaurantRepository;
import com.example.solex_backend.repository.UserDeviceRepository;
import com.example.solex_backend.repository.UserOtpRepository;
import com.example.solex_backend.repository.UserRepository;
import com.example.solex_backend.util.Enums;
import com.example.solex_backend.util.Jwt;
import lombok.RequiredArgsConstructor;
import com.example.solex_backend.service.UserCacheService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserOtpRepository userOtpRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final Jwt jwt;
    private final OtpService otpService;
    private final UserCacheService userCacheService;

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
            throw new BusinessException("Email đã được đăng ký");
        }
        if (request.phone() != null && userRepository.findByPhone(request.phone()).isPresent()) {
            throw new BusinessException("Số điện thoại đã được đăng ký");
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

    public void logout(String fcmToken) {
        String token = (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getDetails();
        Long userId = jwt.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (fcmToken != null && !fcmToken.isBlank()) {
            userDeviceRepository.findByUserAndFcmToken(user, fcmToken)
                    .ifPresent(userDeviceRepository::delete);
        }

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        userCacheService.evict(userId);
    }

    public List<DeviceResponse> getDevices(User user) {
        return userDeviceRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(d -> new DeviceResponse(
                        d.getId(), d.getFcmToken(), d.getDeviceOs()))
                .toList();
    }

    public void registerDevice(User user, RegisterDeviceRequest request) {
        userDeviceRepository.upsertDevice(user.getId(), request.token(), request.deviceOs().name());
    }

    public User updateContact(User user, UpdateContactRequest request) {
        LocalDateTime now = LocalDateTime.now();

        switch (request.field()) {
            case EMAIL -> {
                if (user.getLastChangeEmail() != null &&
                        user.getLastChangeEmail().plusHours(CONTACT_CHANGE_COOLDOWN_HOURS).isAfter(now)) {
                    throw new BusinessException("Email chỉ có thể được thay đổi mỗi 24 giờ một lần");
                }
            }
            case PHONE -> {
                if (user.getLastChangePhone() != null &&
                        user.getLastChangePhone().plusHours(CONTACT_CHANGE_COOLDOWN_HOURS).isAfter(now)) {
                    throw new BusinessException("Số điện thoại chỉ có thể được thay đổi mỗi 24 giờ một lần");
                }
            }
        }

        UserOtp userOtp = switch (request.field()) {
            case EMAIL -> userOtpRepository.findByEmail(request.value())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy OTP cho email này — hãy gửi OTP trước"));
            case PHONE -> userOtpRepository.findByPhone(request.value())
                    .orElseThrow(() -> new BusinessException(
                            "Không tìm thấy OTP cho số điện thoại này — hãy gửi OTP trước"));
        };

        if (userOtp.getExpiresAt() == null || userOtp.getExpiresAt().isBefore(now)) {
            throw new BusinessException("OTP đã hết hạn");
        }
        if (!request.otp().equals(userOtp.getOtp())) {
            throw new BusinessException("OTP không hợp lệ");
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
        User userUpdated = userRepository.save(user);
        userCacheService.evict(userUpdated.getId());
        return userUpdated;
    }

    public void resetPassword(ResetPasswordRequest request) {
        UserOtp userOtp = switch (request.field()) {
            case EMAIL -> userOtpRepository.findByEmail(request.value())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy OTP cho email này — hãy gửi OTP trước"));
            case PHONE -> userOtpRepository.findByPhone(request.value())
                    .orElseThrow(() -> new BusinessException(
                            "Không tìm thấy OTP cho số điện thoại này — hãy gửi OTP trước"));
        };

        if (userOtp.getExpiresAt() == null || userOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP đã hết hạn");
        }
        if (!request.otp().equals(userOtp.getOtp())) {
            throw new BusinessException("OTP không hợp lệ");
        }

        User user = switch (request.field()) {
            case EMAIL -> userRepository.findByEmail(request.value())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản cho email này"));
            case PHONE -> userRepository.findByPhone(request.value())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản cho số điện thoại này"));
        };

        user.setPassword(passwordEncoder.encode(request.password()));
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
        userCacheService.evict(user.getId());

        userOtp.setOtp(null);
        userOtp.setExpiresAt(null);
        userOtpRepository.save(userOtp);
    }

    public void updateFcmToken(User user, String token) {
        user.setFcmToken(token);
        userRepository.save(user);
        userCacheService.evict(user.getId());
    }

    public AuthResponse login(LoginRequest request) {
        if ((request.email() == null || request.email().isBlank())
                && (request.phone() == null || request.phone().isBlank())) {
            throw new BusinessException("Phải cung cấp email hoặc số điện thoại");
        }

        User user;
        if (request.phone() != null && !request.phone().isBlank()) {
            user = userRepository.findByPhone(request.phone())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản cho số điện thoại này"));
        } else {
            user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản cho email này"));
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Mật khẩu không đúng");
        }

        if (user.getIsActive() == 0) {
            throw new BusinessException("Tài khoản chưa được kích hoạt");
        }

        String token = jwt.generateToken(user);
        return new AuthResponse(token, toUserInfoResponse(user));
    }
}
