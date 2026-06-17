package com.example.solex_backend.service.notification;

public interface EmailPort {
    void sendOtp(String to, String otp);
}