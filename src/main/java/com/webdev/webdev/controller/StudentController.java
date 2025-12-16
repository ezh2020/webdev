package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.Result;
import com.webdev.webdev.StudentProfileUpdateRequest;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.StudentService;
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
 * 学生档案相关接口。
 * 提供：
 * - /api/student/me             获取当前登录学生档案
 * - /api/student/updateProfile  更新学生个人资料（隐式联动 User / Student）
 * - /api/student/listAll        管理员查看所有学生档案
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

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
     * 获取当前登录学生的档案信息。
     * 通过 Session 中的 userId 查找对应的 Student 记录。
     */
    @GetMapping("/me")
    public Result<Student> me(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return Result.fail("未登录");
        }

        User user = userService.getById(userId);
        if (user == null || !"STUDENT".equalsIgnoreCase(user.getRole())) {
            return Result.fail("当前用户不是学生");
        }

        Student student = studentService.getOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId)
        );
        if (student == null) {
            return Result.fail("学生档案不存在");
        }
        return Result.ok(student);
    }

    /**
     * 管理员查询所有学生档案。
     */
    @GetMapping("/listAll")
    public Result<List<Student>> listAll(HttpSession session) {
        User current = getLoginUser(session);
        if (current == null) {
            return Result.fail("未登录");
        }
        if (current.getRole() == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            return Result.fail("当前用户无权限查看学生档案");
        }
        List<Student> list = studentService.list();
        return Result.ok(list);
    }

    /**
     * 更新学生个人整体档案（基础信息 + 学籍信息）。
     * 只需要一个接口，内部隐式更新 User / Student。
     */
    @PostMapping("/updateProfile")
    public Result<Void> updateProfile(@RequestBody StudentProfileUpdateRequest request) {
        if (request == null || request.getUserId() == null) {
            return Result.fail("userId 不能为空");
        }
        boolean ok = studentService.updateProfile(request);
        if (!ok) {
            return Result.fail("更新学生档案失败");
        }
        return Result.ok(null);
    }
}
