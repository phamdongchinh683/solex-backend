package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.CreateOrderRequest;
import com.example.solex_backend.dto.response.OrderResponse;
import com.example.solex_backend.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Customer order management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Create order from cart")
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@AuthenticationPrincipal User user, @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok("OK", orderService.createOrder(user, request));
    }

    @Operation(summary = "Get my orders")
    @GetMapping
    public ApiResponse<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", orderService.getMyOrders(user));
    }

    @Operation(summary = "Get order detail")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ApiResponse.ok("OK", orderService.getOrderById(id, user));
    }
}
