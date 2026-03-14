package com.trading.platform.user.repository;

import com.trading.platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Excludes internal system accounts from user-facing queries
    @Query("SELECT u FROM User u WHERE u.username NOT IN ('system', 'admin') AND u.role = 'USER'")
    List<User> findAllRealUsers();
}