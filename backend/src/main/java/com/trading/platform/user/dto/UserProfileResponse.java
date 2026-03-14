package com.trading.platform.user.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private BigDecimal balance;
    private String role;
}
