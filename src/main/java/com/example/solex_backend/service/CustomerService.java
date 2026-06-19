package com.example.solex_backend.service;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.request.UpdateProfileRequest;
import com.example.solex_backend.dto.response.UserInfoResponse;
import com.example.solex_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final UserRepository userRepository;

    public UserInfoResponse getProfile(User user) {
        return toResponse(user);
    }

    public UserInfoResponse updateProfile(User user, UpdateProfileRequest request) {
        if (request.firstName() != null)
            user.setFirstName(request.firstName());
        if (request.lastName() != null)
            user.setLastName(request.lastName());
        if (request.phone() != null)
            user.setPhone(request.phone());
        userRepository.save(user);
        return toResponse(user);
    }

    private UserInfoResponse toResponse(User user) {
        return new UserInfoResponse(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), user.getRole(), user.getIsEmailVerified(),
                user.getIsPhoneVerified(), user.getIsActive(), user.getCreatedAt(),
                user.getLastChangeEmail(), user.getLastChangePhone());
    }
}
