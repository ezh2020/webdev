package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.NotificationMapper;
import com.webdev.webdev.model.Notification;
import com.webdev.webdev.service.NotificationService;
import com.webdev.webdev.websocket.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Autowired(required = false)
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public boolean publishNotification(Long courseId,
                                       Long publisherId,
                                       String title,
                                       String content,
                                       Boolean isImportant) {
        // 默认作为 SYSTEM 类型的通知，用于兼容旧调用方
        return publishNotification(courseId, publisherId, title, content, isImportant, null);
    }

    @Override
    public boolean publishNotification(Long courseId,
                                       Long publisherId,
                                       String title,
                                       String content,
                                       Boolean isImportant,
                                       String type) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        Notification notification = new Notification();
        notification.setCourseId(courseId);
        notification.setPublisherId(publisherId);
        notification.setTitle(title.trim());
        notification.setContent(content.trim());
        notification.setIsImportant(isImportant != null ? isImportant : Boolean.FALSE);

        boolean saved = this.save(notification);
        // 保存成功后，通过 WebSocket 推送实时消息（若 WebSocket 可用）
        if (saved && notificationWebSocketHandler != null) {
            String fullMessage = notification.getTitle() + "：" + notification.getContent();
            notificationWebSocketHandler.broadcast(type != null ? type : "SYSTEM", fullMessage);
        }
        return saved;
    }

    @Override
    public java.util.List<Notification> listByCourse(Long courseId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Notification::getCourseId, courseId);
        }
        wrapper.orderByDesc(Notification::getPublishTime);
        return this.list(wrapper);
    }

    @Override
    public java.util.List<Notification> listOrderByTimeDesc() {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Notification::getPublishTime);
        return this.list(wrapper);
    }
}
