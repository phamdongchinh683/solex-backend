package com.example.solex_backend.repository;

import com.example.solex_backend.domain.Address;
import com.example.solex_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}