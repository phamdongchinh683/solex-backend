package com.example.solex_backend.service.notification;

public interface SmsPort {
    void sendOtp(String to, String otp);
}