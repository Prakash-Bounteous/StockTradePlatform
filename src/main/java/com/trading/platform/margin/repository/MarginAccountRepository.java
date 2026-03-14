package com.trading.platform.margin.repository;

import com.trading.platform.margin.entity.MarginAccount;
import com.trading.platform.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarginAccountRepository extends JpaRepository<MarginAccount, Long> {

    Optional<MarginAccount> findByUser(User user);

}