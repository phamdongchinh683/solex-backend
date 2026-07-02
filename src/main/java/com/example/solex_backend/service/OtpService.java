package com.example.solex_backend.service;

import com.example.solex_backend.domain.UserOtp;
import com.example.solex_backend.dto.request.ContactCheckRequest;
import com.example.solex_backend.dto.request.SendOtpRequest;
import com.example.solex_backend.dto.request.VerifyOtpRequest;
import com.example.solex_backend.exception.BusinessException;
import com.example.solex_backend.repository.UserOtpRepository;
import com.example.solex_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class OtpService {

    private final UserOtpRepository userOtpRepository;
    private final UserRepository userRepository;
    private final EmailPort emailPort;
    private final SmsPort smsPort;

    private static final int OTP_EXPIRY_MINUTES = 2;
    private static final int OTP_LENGTH = 6;

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public void sendOtp(SendOtpRequest params) {
        String otp = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        switch (params.field()) {
            case EMAIL -> {
                userOtpRepository.upsertOtpByEmail(params.value(), null, otp, expiresAt);
                emailPort.sendOtp(params.value(), otp);
            }
            case PHONE -> {
                userOtpRepository.upsertOtpByPhone(null, params.value(), otp, expiresAt);
                smsPort.sendOtp(params.value(), otp);
            }
        }
    }

    public boolean isOtpVerified(String email, String phone) {
        if (email != null && !email.isBlank()) {
            UserOtp otpRecord = userOtpRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Email OTP not verified"));
            if (!Boolean.TRUE.equals(otpRecord.getVerified())) {
                throw new BusinessException("Email not verified via OTP");
            }
        }
        if (phone != null && !phone.isBlank()) {
            UserOtp otpRecord = userOtpRepository.findByPhone(phone)
                    .orElseThrow(() -> new BusinessException("Phone OTP not verified"));
            if (!Boolean.TRUE.equals(otpRecord.getVerified())) {
                throw new BusinessException("Phone number not verified via OTP");
            }
        }
        return true;
    }

    public boolean checkContactExists(ContactCheckRequest request) {
        return switch (request.field()) {
            case EMAIL -> userRepository.findByEmail(request.value()).isPresent();
            case PHONE -> userRepository.findByPhone(request.value()).isPresent();
        };
    }

    public void verifyOtp(VerifyOtpRequest request) {
        UserOtp userOtp = switch (request.field()) {
            case EMAIL -> userOtpRepository.findByEmail(request.value())
                    .orElseThrow(() -> new BusinessException("No OTP found for this email"));
            case PHONE -> userOtpRepository.findByPhone(request.value())
                    .orElseThrow(() -> new BusinessException("No OTP found for this phone number"));
        };

        if (userOtp.getExpiresAt() == null || userOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP has expired");
        }

        if (!request.otp().equals(userOtp.getOtp())) {
            throw new BusinessException("Invalid OTP");
        }

        userOtp.setOtp(null);
        userOtp.setExpiresAt(null);
        userOtp.setVerified(true);
        userOtpRepository.save(userOtp);
    }
}
