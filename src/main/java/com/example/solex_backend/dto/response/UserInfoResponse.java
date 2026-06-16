package com.example.solex_backend.dto.response;

import com.example.solex_backend.util.Enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    @Schema(description = "User ID")
    private Long id;

    @Schema(description = "User email address")
    private String email;

    @Schema(description = "First name")
    private String firstName;

    @Schema(description = "Last name")
    private String lastName;

    @Schema(description = "Phone number")
    private String phone;

    @Schema(description = "User role")
    private Enums.UserRole role;

    @Schema(description = "Email verified flag")
    private Integer isEmailVerified;

    @Schema(description = "Phone verified flag")
    private Integer isPhoneVerified;

    @Schema(description = "Account active flag")
    private Integer isActive;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;
}