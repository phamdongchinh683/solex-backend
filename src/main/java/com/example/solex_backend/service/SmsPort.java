package com.example.solex_backend.service;

public interface SmsPort {
    void sendOtp(String to, String otp);
}