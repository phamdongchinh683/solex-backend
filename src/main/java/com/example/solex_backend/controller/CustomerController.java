package com.example.solex_backend.controller;

import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.SignupRequest;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Customer", description = "Customer registration endpoints")
@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final AuthService authService;

    @Operation(summary = "Register a new customer account")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.<AuthResponse>builder()
            .message("OK")
            .data(authService.signup(request))
            .build();

    }


}