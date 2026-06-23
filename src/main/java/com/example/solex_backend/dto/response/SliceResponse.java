package com.example.solex_backend.dto.response;

import java.util.List;

public record SliceResponse<T>(List<T> items, Long nextCursor) {}
