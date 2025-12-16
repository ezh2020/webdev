package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.model.Notification;
import com.webdev.webdev.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知相关接口：
 * - 管理员/教师编辑并发布通知
 * - 按课程或全部查询通知
 * - 删除单条通知
 */
@RestController
@RequestMapping("/api/notification")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 新建并发布一条通知。
     *
     * 请求体示例：
     * {
     *   "courseId": 1,
     *   "title": "期末考试说明",
     *   "content": "请同学们注意期末考试安排……",
     *   "publisherId": 1001,
     *   "isImportant": true
     * }
     */
    @PostMapping("/create")
    public Result<Notification> create(@RequestBody Notification notification) {
        if (notification == null) {
            return Result.fail("通知内容不能为空");
        }
        if (notification.getCourseId() == null) {
            return Result.fail("courseId 不能为空（通知必须归属到一门课程）");
        }
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            return Result.fail("通知标题不能为空");
        }
        if (notification.getContent() == null || notification.getContent().trim().isEmpty()) {
            return Result.fail("通知内容不能为空");
        }

        boolean ok = notificationService.publishNotification(
                notification.getCourseId(),
                notification.getPublisherId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getIsImportant()
        );
        if (!ok) {
            return Result.fail("保存通知失败");
        }
        // 再查一次，返回完整实体（包含自增主键等）
        // 这里简单地使用与刚插入条件最接近的一条记录；对于典型使用场景已足够。
        List<Notification> list = notificationService.listByCourse(notification.getCourseId());
        Notification latest = list.isEmpty() ? null : list.get(0);
        return Result.ok(latest != null ? latest : notification);
    }

    /**
     * 修改已有通知（例如修正文案）。
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Notification notification) {
        if (notification == null || notification.getId() == null) {
            return Result.fail("通知 id 不能为空");
        }
        if (notification.getTitle() != null && notification.getTitle().trim().isEmpty()) {
            return Result.fail("通知标题不能为空");
        }
        if (notification.getContent() != null && notification.getContent().trim().isEmpty()) {
            return Result.fail("通知内容不能为空");
        }

        boolean ok = notificationService.updateById(notification);
        if (!ok) {
            return Result.fail("更新通知失败");
        }
        return Result.ok(null);
    }

    /**
     * 按课程查询通知列表（按发布时间倒序）。
     * 接口：GET /api/notification/listByCourse?courseId=1
     */
    @GetMapping("/listByCourse")
    public Result<List<Notification>> listByCourse(@RequestParam("courseId") Long courseId) {
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }
        List<Notification> list = notificationService.listByCourse(courseId);
        return Result.ok(list);
    }

    /**
     * 查询全部通知（按发布时间倒序）。
     */
    @GetMapping("/listAll")
    public Result<List<Notification>> listAll() {
        List<Notification> list = notificationService.listOrderByTimeDesc();
        return Result.ok(list);
    }

    /**
     * 根据 id 获取单条通知详情。
     */
    @GetMapping("/{id}")
    public Result<Notification> getById(@PathVariable("id") Long id) {
        Notification notification = notificationService.getById(id);
        if (notification == null) {
            return Result.fail("通知不存在");
        }
        return Result.ok(notification);
    }

    /**
     * 删除单条通知。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean ok = notificationService.removeById(id);
        if (!ok) {
            return Result.fail("删除通知失败");
        }
        return Result.ok(null);
    }
}
