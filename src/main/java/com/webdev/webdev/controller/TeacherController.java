package com.webdev.webdev.controller;

import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.Result;
import com.webdev.webdev.TeacherProfileUpdateRequest;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 教师档案相关接口。
 * 提供：
 * - /api/teacher/updateProfile  更新教师档案（隐式联动 User / Teacher）
 * - /api/teacher/listAll        管理员查看所有教师档案
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    private User getLoginUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }

    /**
     * 管理员查询所有教师档案。
     */
    @GetMapping("/listAll")
    public Result<List<Teacher>> listAll(HttpSession session) {
        User current = getLoginUser(session);
        if (current == null) {
            return Result.fail("未登录");
        }
        if (current.getRole() == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            return Result.fail("当前用户无权限查看教师档案");
        }
        List<Teacher> list = teacherService.list();
        return Result.ok(list);
    }

    /**
     * 更新教师个人整体档案（基础信息 + 任教信息）。
     * 只需要一个接口，内部隐式更新 User / Teacher。
     */
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(@RequestBody TeacherProfileUpdateRequest request) {
        if (request == null || request.getUserId() == null) {
            return Result.fail("userId 不能为空");
        }
        boolean ok = teacherService.updateProfile(request);
        if (!ok) {
            return Result.fail("更新教师档案失败");
        }
        return Result.ok(null);
    }
}
