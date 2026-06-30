package com.example.solex_backend.service.notification;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FirebaseNotificationAdapter {

    public void notifyNewOrderToRestaurant(String fcmToken, Long orderId, String orderCode) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("Đơn hàng mới!")
                        .setBody("Bạn có đơn hàng " + orderCode + " cần xác nhận")
                        .build())
                .putData("type", "NEW_ORDER")
                .putData("orderId", String.valueOf(orderId))
                .putData("orderCode", orderCode)
                .build();
        send(message);
    }

    public void notifyOrderStatusToCustomer(String fcmToken, String orderCode, String status) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("Cập nhật đơn hàng")
                        .setBody(buildStatusMessage(orderCode, status))
                        .build())
                .putData("type", "ORDER_STATUS")
                .putData("orderCode", orderCode)
                .putData("status", status)
                .build();
        send(message);
    }

    private void send(Message message) {
        if (FirebaseApp.getApps().isEmpty()) {
            return;
        }
        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            log.warn("FCM send failed: {}", e.getMessage());
        }
    }

    private String buildStatusMessage(String orderCode, String status) {
        return switch (status) {
            case "CONFIRMED"  -> "Đơn hàng " + orderCode + " đã được xác nhận";
            case "PREPARING"  -> "Đơn hàng " + orderCode + " đang được chuẩn bị";
            case "READY"      -> "Đơn hàng " + orderCode + " đã sẵn sàng, cửa hàng chuẩn bị giao đến bạn";
            case "DELIVERING" -> "Đơn hàng " + orderCode + " đang trên đường giao đến bạn";
            case "DELIVERED"  -> "Đơn hàng " + orderCode + " đã giao thành công. Cảm ơn bạn!";
            case "CANCELLED"  -> "Đơn hàng " + orderCode + " đã bị hủy";
            default           -> "Trạng thái đơn hàng " + orderCode + " đã được cập nhật";
        };
    }
}
