package com.trading.platform.notification.service;

import com.trading.platform.notification.entity.Notification;
import com.trading.platform.notification.repository.NotificationRepository;
import com.trading.platform.user.entity.User;
import com.trading.platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user).message(message).read(false)
                .createdAt(LocalDateTime.now()).build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public void markAllRead(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}
