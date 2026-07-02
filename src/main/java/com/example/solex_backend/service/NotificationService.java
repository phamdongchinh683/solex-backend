package com.example.solex_backend.service;

import com.example.solex_backend.domain.Notification;
import com.example.solex_backend.domain.Order;
import com.example.solex_backend.domain.User;
import com.example.solex_backend.domain.state.OrderState;
import com.example.solex_backend.dto.response.NotificationResponse;
import com.example.solex_backend.dto.response.SliceResponse;
import com.example.solex_backend.exception.ResourceNotFoundException;
import com.example.solex_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createOrderNotification(User user, Order order, OrderState type, String title, String body) {
        Notification notification = Notification.builder()
                .user(user)
                .order(order)
                .type(type.status())
                .title(title)
                .body(body)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public SliceResponse<NotificationResponse> getMyNotifications(User user, Long cursor, int size) {
        Long userId = user.getId();
        List<Notification> result = notificationRepository
                .findByUserIdBeforeCursor(userId, cursor, PageRequest.of(0, size + 1));
        boolean hasNext = result.size() > size;
        List<Notification> page = hasNext ? result.subList(0, size) : result;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        return new SliceResponse<>(page.stream().map(this::toResponse).toList(), nextCursor);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Transactional
    public NotificationResponse markOneRead(User user, Long notificationId) {
        Long userId = user.getId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found: " + notificationId);
        }
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getOrder() != null ? n.getOrder().getId() : null,
                n.getOrder() != null ? n.getOrder().getOrderCode() : null,
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getIsRead(),
                n.getCreatedAt());
    }
}
