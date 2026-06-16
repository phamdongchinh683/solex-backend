package com.example.solex_backend.dto.request;

import com.example.solex_backend.util.Enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SendOtpRequest {

    @Schema(description = "Field type to send OTP: EMAIL or PHONE")
    private Enums.OtpType field;

    @Schema(description = "Email address or phone number to receive OTP")
    private String value;
}
