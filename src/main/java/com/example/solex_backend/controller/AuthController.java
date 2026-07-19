package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.ContactCheckRequest;
import com.example.solex_backend.dto.request.LoginRequest;
import com.example.solex_backend.dto.request.LogoutRequest;
import com.example.solex_backend.dto.request.RegisterDeviceRequest;
import com.example.solex_backend.dto.request.ResetPasswordRequest;
import com.example.solex_backend.dto.request.SendOtpRequest;
import com.example.solex_backend.dto.request.UpdateContactRequest;
import com.example.solex_backend.dto.request.VerifyOtpRequest;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.DeviceResponse;
import com.example.solex_backend.service.AuthService;
import com.example.solex_backend.service.OtpService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Auth", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @Operation(summary = "Sign in with email or phone and password")
    @PostMapping("/sign-in")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.ok("OK", response);
    }

    @Operation(summary = "Send OTP code")
    @PostMapping("/otp/send")
    public ApiResponse<AuthResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.sendOtp(request);
        return ApiResponse.ok("OTP has been sent", null);
    }

    @Operation(summary = "Check if email or phone already exists")
    @PostMapping("/contact/check")
    public ApiResponse<Boolean> checkContact(@Valid @RequestBody ContactCheckRequest request) {
        boolean exists = otpService.checkContactExists(request);
        return ApiResponse.ok("OK", exists);
    }

    @Operation(summary = "Verify OTP code")
    @PostMapping("/otp/verify")
    public ApiResponse<Void> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request);
        return ApiResponse.ok("OTP verification OK", null);
    }

    @Operation(summary = "Update email or phone — requires OTP sent to the new value first, 24h cooldown applies")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/contact")
    public ApiResponse<AuthResponse> updateContact(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateContactRequest request) {
        AuthResponse response = authService.updateContact(user, request);
        return ApiResponse.ok("Contact info updated OK", response);
    }

    @Operation(summary = "Reset password using OTP — requires OTP sent to email/phone first")
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.ok("Password reset OK", null);
    }

    @Operation(summary = "Logout and invalidate current token, optionally delete device FCM token")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody(required = false) LogoutRequest request) {
        String fcmToken = request != null ? request.fcmToken() : null;
        authService.logout(fcmToken);
        return ApiResponse.ok("Logged out OK", null);
    }

    @Operation(summary = "Get list of registered devices")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/devices")
    public ApiResponse<List<DeviceResponse>> getDevices(@AuthenticationPrincipal User user) {
        List<DeviceResponse> devices = authService.getDevices(user);
        return ApiResponse.ok("Device list", devices);
    }

    @Operation(summary = "Register or update device FCM token with OS info")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/devices")
    public ApiResponse<Void> registerDevice(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody RegisterDeviceRequest request) {
        authService.registerDevice(user, request);
        return ApiResponse.ok("Device has been registered", null);
    }

}
