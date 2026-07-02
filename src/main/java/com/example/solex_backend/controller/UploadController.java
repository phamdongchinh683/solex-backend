package com.example.solex_backend.controller;

import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.PresignRequest;
import com.example.solex_backend.dto.response.PresignResponse;
import com.example.solex_backend.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Upload", description = "Cloudinary presigned upload")
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @Operation(summary = "Generate Cloudinary presigned params for direct client upload")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/presign")
    public ApiResponse<PresignResponse> presign(@RequestBody @Valid PresignRequest request) {
        return ApiResponse.ok("OK", cloudinaryService.generatePresign(request.folder()));
    }
}
