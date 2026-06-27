package com.example.solex_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignResponse(
        @Schema(description = "Cloudinary cloud name") String cloudName,
        @Schema(description = "Cloudinary API key")    String apiKey,
        @Schema(description = "Unix timestamp used in signature calculation") long timestamp,
        @Schema(description = "SHA-1 signature to authenticate the direct upload") String signature,
        @Schema(description = "Target folder in Cloudinary") String folder,
        @Schema(description = "Direct upload URL") String uploadUrl
) {}
