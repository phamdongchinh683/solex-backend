package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByOperator(User operator);
    boolean existsByIdAndOperator(Long id, User operator);

    @Query("SELECT r FROM Restaurant r WHERE r.isOpen = true AND r.id > :cursor ORDER BY r.id ASC")
    List<Restaurant> findOpenAfterCursor(@Param("cursor") Long cursor, Pageable pageable);

    @Query(value = "SELECT * FROM restaurants WHERE is_open = true AND id > :cursor " +
            "AND unaccent(lower(name)) LIKE '%' || unaccent(lower(:name)) || '%' ORDER BY id", nativeQuery = true)
    List<Restaurant> searchOpenByNameAfterCursor(@Param("name") String name, @Param("cursor") Long cursor, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Restaurant r where r.id = :id")
    Optional<Restaurant> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT * FROM restaurants
            WHERE is_open = true
              AND (6371 * acos(LEAST(1.0,
                    cos(radians(:lat)) * cos(radians(latitude))
                    * cos(radians(longitude) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(latitude))
                  ))) <= :radiusKm
            ORDER BY (6371 * acos(LEAST(1.0,
                    cos(radians(:lat)) * cos(radians(latitude))
                    * cos(radians(longitude) - radians(:lng))
                    + sin(radians(:lat)) * sin(radians(latitude))
                  ))) ASC
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Restaurant> findNearbyOpen(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("size") int size,
            @Param("offset") int offset);

    @Modifying
    @Query("UPDATE Restaurant rs SET rs.isOpen = :status WHERE rs.operator.id = :operatorId and rs.id = :id")
    int updateStatusByOperator(
            @Param("id") Long id,
            @Param("operatorId") Long operatorId, @Param("status") boolean status);
}
