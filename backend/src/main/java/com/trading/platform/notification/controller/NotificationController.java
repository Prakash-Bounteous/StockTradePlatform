package com.trading.platform.notification.controller;

import com.trading.platform.notification.entity.Notification;
import com.trading.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {
        return notificationService.getNotifications(authentication.getName());
    }

    @PostMapping("/read-all")
    public String markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return "All notifications marked as read";
    }
}
