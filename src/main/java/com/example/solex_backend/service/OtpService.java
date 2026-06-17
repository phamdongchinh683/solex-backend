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

        switch (params.getField()) {
            case EMAIL -> {
                userOtpRepository.upsertOtpByEmail(
                        params.getValue(),
                        null,
                        otp,
                        expiresAt);
                emailPort.sendOtp(params.getValue(), otp);
            }
            case PHONE -> {
                userOtpRepository.upsertOtpByPhone(
                        null,
                        params.getValue(),
                        otp,
                        expiresAt);
                smsPort.sendOtp(params.getValue(), otp);
            }
        }
    }

    public boolean isOtpVerified(String email, String phone) {
        if (email != null && !email.isBlank()) {
            UserOtp otpRecord = userOtpRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Email OTP has not been verified"));
            if (!Boolean.TRUE.equals(otpRecord.getVerified())) {
                throw new BusinessException("Email has not been verified via OTP");
            }
        }
        if (phone != null && !phone.isBlank()) {
            UserOtp otpRecord = userOtpRepository.findByPhone(phone)
                    .orElseThrow(() -> new BusinessException("Phone OTP has not been verified"));
            if (!Boolean.TRUE.equals(otpRecord.getVerified())) {
                throw new BusinessException("Phone has not been verified via OTP");
            }
        }
        return true;
    }

    public boolean checkContactExists(ContactCheckRequest request) {
        switch (request.getField()) {
            case EMAIL -> {
                return userRepository.findByEmail(request.getValue()).isPresent();
            }
            case PHONE -> {
                return userRepository.findByPhone(request.getValue()).isPresent();
            }
            default -> throw new BusinessException("Invalid field type");
        }
    }

    public void verifyOtp(VerifyOtpRequest request) {
        UserOtp userOtp = switch (request.getField()) {
            case EMAIL -> userOtpRepository.findByEmail(request.getValue())
                    .orElseThrow(() -> new BusinessException("OTP not found for this email"));
            case PHONE -> userOtpRepository.findByPhone(request.getValue())
                    .orElseThrow(() -> new BusinessException("OTP not found for this phone"));
        };

        if (userOtp.getExpiresAt() == null || userOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("OTP has expired");
        }

        if (!request.getOtp().equals(userOtp.getOtp())) {
            throw new BusinessException("Invalid OTP");
        }

        userOtp.setOtp(null);
        userOtp.setExpiresAt(null);
        userOtp.setVerified(true);
        userOtpRepository.save(userOtp);
    }

}
