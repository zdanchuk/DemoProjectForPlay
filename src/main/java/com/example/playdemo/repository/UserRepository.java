package com.example.playdemo.repository;

import com.example.playdemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Pobiera użytkownika na podstawie numeru telefonu.
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
}