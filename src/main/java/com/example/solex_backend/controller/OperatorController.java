package com.example.solex_backend.controller;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.dto.ApiResponse;
import com.example.solex_backend.dto.request.OperatorSignupRequest;
import com.example.solex_backend.dto.request.UpdateStatusRestaurantRequest;
import com.example.solex_backend.dto.response.AuthResponse;
import com.example.solex_backend.dto.response.OrderResponse;
import com.example.solex_backend.service.AuthService;
import com.example.solex_backend.service.OrderService;
import com.example.solex_backend.service.OrderStatusService;
import com.example.solex_backend.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Operator", description = "Operator management and order operations")
@RestController
@RequestMapping("/api/v1/operator")
@RequiredArgsConstructor
public class OperatorController {

    private final AuthService authService;
    private final OrderService orderService;
    private final OrderStatusService orderStatusService;
    private final RestaurantService restaurantService;

    @Operation(summary = "Operator sign-up — creates operator account and restaurant in one request")
    @PostMapping("/sign-up")
    public ApiResponse<AuthResponse> signUp(@RequestBody @Valid OperatorSignupRequest request) {
        return ApiResponse.ok("OK", authService.signupOperator(request));
    }

    @Operation(summary = "Confirm order")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @PutMapping("/orders/{id}/confirm")
    public ApiResponse<Void> confirmOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id) {
        orderStatusService.confirmOrder(id, operator);
        return ApiResponse.ok("Order confirmed", null);
    }

    @Operation(summary = "Advance order to next status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @PutMapping("/orders/{id}/advance")
    public ApiResponse<Void> advanceOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id) {
        orderStatusService.advanceOrder(id, operator);
        return ApiResponse.ok("Order status advanced", null);
    }

    @Operation(summary = "Cancel order")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @PutMapping("/orders/{id}/cancel")
    public ApiResponse<Void> cancelOrder(
            @AuthenticationPrincipal User operator,
            @PathVariable Long id,
            @RequestParam String reason) {
        orderStatusService.cancelOrder(id, operator, reason);
        return ApiResponse.ok("Order cancelled", null);
    }

    @Operation(summary = "Get order detail (operator view)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable Long id) {
        return ApiResponse.ok("OK", orderService.getOrderForOperator(id));
    }

    @Operation(summary = "Update restaurant open/close status")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('OPERATOR')")
    @PatchMapping("/restaurant/status")
    public ApiResponse<Void> updateRestaurantStatus(
            @AuthenticationPrincipal User operator,
            @RequestBody UpdateStatusRestaurantRequest request) {
        restaurantService.updateIsOpenRestaurant(request.restaurantId(), operator, request.status());
        return ApiResponse.ok(request.status() ? "Restaurant is now open" : "Restaurant is now closed", null);
    }

}