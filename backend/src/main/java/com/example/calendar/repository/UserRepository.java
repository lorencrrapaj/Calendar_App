// src/main/java/com/example/calendar/repository/UserRepository.java
package com.example.calendar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.calendar.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
