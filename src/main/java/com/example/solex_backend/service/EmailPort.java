package com.example.solex_backend.service;

public interface EmailPort {
    void sendOtp(String to, String otp);
}