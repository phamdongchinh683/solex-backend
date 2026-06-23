package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Address;
import com.example.solex_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    Optional<Address> findByIdAndUser(Long id, User user);
    boolean existsByIdAndUser(Long id, User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = true WHERE a.id = :id AND a.user.id = :userId")
    void setDefaultByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
