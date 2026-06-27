package com.example.solex_backend.service;

import com.example.solex_backend.config.CloudinaryConfig;
import com.example.solex_backend.dto.response.PresignResponse;
import com.example.solex_backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final CloudinaryConfig config;

    public PresignResponse generatePresign(String folder) {
        long timestamp = System.currentTimeMillis() / 1000;
        String toSign = "folder=" + folder + "&timestamp=" + timestamp + config.getApiSecret();
        String signature = sha1Hex(toSign);
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + config.getCloudName() + "/image/upload";
        return new PresignResponse(config.getCloudName(), config.getApiKey(), timestamp, signature, folder, uploadUrl);
    }

    private String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("Failed to generate upload signature");
        }
    }
}
