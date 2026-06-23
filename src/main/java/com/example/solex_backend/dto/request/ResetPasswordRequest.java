package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @Schema(description = "Email address or phone number") @NotBlank(message = "Vui lòng nhập email hoặc số điện thoại") String value,

        @Schema(description = "OTP code (6 digits)") @NotBlank(message = "Vui lòng nhập mã OTP") @Pattern(regexp = "^\\d{6}$", message = "OTP phải có 6 chữ số") String otp,

        @Schema(description = "Field type: EMAIL or PHONE") @NotNull(message = "Vui lòng chọn loại trường") Enums.OtpType field,

        @Schema(description = "New password") @NotBlank(message = "Vui lòng nhập mật khẩu mới") @Size(min = 8, max = 20, message = "Mật khẩu phải từ 8 đến 20 ký tự") String password) {
}