package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.NotificationMapper;
import com.webdev.webdev.model.Notification;
import com.webdev.webdev.service.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
}