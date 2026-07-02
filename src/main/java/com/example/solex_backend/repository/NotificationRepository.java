package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n FROM Notification n
        WHERE n.user.id = :userId
          AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
        """)
    List<Notification> findByUserIdBeforeCursor(
        @Param("userId") Long userId,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    long countByUserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllReadByUserId(@Param("userId") Long userId);
}
