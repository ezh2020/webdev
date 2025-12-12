package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.Notification;

public interface NotificationService extends IService<Notification> {
    /**
     * 统一封装通知发布逻辑，便于课程选课、退选、成绩发布等场景复用。
     */
    boolean publishNotification(Long courseId,
                                Long publisherId,
                                String title,
                                String content,
                                Boolean isImportant);

    /**
     * 按课程查询通知，按发布时间倒序。
     */
    java.util.List<Notification> listByCourse(Long courseId);

    /**
     * 查询全部通知，按发布时间倒序。
     */
    java.util.List<Notification> listOrderByTimeDesc();
}
