package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreateAddressRequest;
import com.example.solex_backend.dto.request.SignupRequest;
import com.example.solex_backend.dto.request.UpdateProfileRequest;
import com.example.solex_backend.dto.response.AddressResponse;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.UserInfoResponse;
import com.example.solex_backend.service.AddressService;
import com.example.solex_backend.service.AuthService;
import com.example.solex_backend.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Customer", description = "Customer endpoints")
@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final AuthService authService;
    private final CustomerService customerService;
    private final AddressService addressService;

    @Operation(summary = "Register a new customer account")
    @PreAuthorize("permitAll()")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
                return ApiResponse.ok("OK", authService.signup(request));


    }

    @Operation(summary = "Get current user profile")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/profile")
    public ApiResponse<UserInfoResponse> getProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", customerService.getProfile(user));
    }

    @Operation(summary = "Update user profile")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ApiResponse<UserInfoResponse> updateProfile(@AuthenticationPrincipal User user, @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok("Profile updated", customerService.updateProfile(user, request));
    }

    @Operation(summary = "Get my addresses")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/addresses")
    public ApiResponse<List<AddressResponse>> getMyAddresses(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", addressService.getMyAddresses(user));
    }

    @Operation(summary = "Create new address")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/addresses")
    public ApiResponse<AddressResponse> createAddress(@AuthenticationPrincipal User user, @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Address created", addressService.createAddress(user, request));
    }

    @Operation(summary = "Update address")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/addresses/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request) {
        return ApiResponse.ok("Address updated", addressService.updateAddress(id, user, request));
    }

    @Operation(summary = "Delete address")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@AuthenticationPrincipal User user, @PathVariable Long id) {
        addressService.deleteAddress(id, user);
        return ApiResponse.ok("Address deleted", null);
    }

    @Operation(summary = "Set default address")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/addresses/{id}/default")
    public ApiResponse<AddressResponse> setDefaultAddress(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ApiResponse.ok("Default address set", addressService.setDefaultAddress(id, user));
    }
}
