package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Restaurant;
import com.example.solex_backend.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByOperator(User operator);

    List<Restaurant> findByIsOpen(Boolean isOpen);

    @Query(value = "SELECT * FROM restaurants WHERE is_open = true " +
            "AND unaccent(lower(name)) LIKE '%' || unaccent(lower(:name)) || '%'", nativeQuery = true)
    List<Restaurant> searchOpenByName(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Restaurant r where r.id = :id")
    Optional<Restaurant> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Restaurant rs SET rs.isOpen = :status WHERE rs.operator.id = :operatorId and rs.id = :id")
    int updateStatusByOperator(
            @Param("id") Long id,
            @Param("operatorId") Long operatorId, @Param("status") boolean status);
}
