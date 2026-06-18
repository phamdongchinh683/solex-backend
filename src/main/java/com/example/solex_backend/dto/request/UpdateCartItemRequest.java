package com.example.solex_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCartItemRequest(
        @NotBlank(message = "Action cannot be empty")
        @Pattern(regexp = "[+-]", message = "Action must be '+' or '-'")
        String action
) {
}