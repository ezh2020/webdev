package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.NotificationMapper;
import com.webdev.webdev.model.Notification;
import com.webdev.webdev.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public boolean publishNotification(Long courseId,
                                       Long publisherId,
                                       String title,
                                       String content,
                                       Boolean isImportant) {
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

        return this.save(notification);
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
