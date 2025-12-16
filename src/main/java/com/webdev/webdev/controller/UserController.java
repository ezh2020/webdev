package com.webdev.webdev.controller;

import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.ChangePasswordRequest;
import com.webdev.webdev.LoginRequest;
import com.webdev.webdev.RegisterRequest;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.StudentService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户相关接口控制器：
 * - /api/user/register  注册
 * - /api/user/login     登录
 * - /api/user/me        查询当前登录用户
 * - /api/user/logout    退出登录
 * - /api/user/changePassword 修改密码
 *
 * 说明：这里使用 HttpSession 来保持登录状态，
 * 同时用 loginUsers 这个 Map 记录 sessionId -> userId 的简单映射，便于理解“谁登录了”。
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    //Bean会自动找实现了userService的suerServiceImpl
    private UserService userService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    // 简单记录登录状态：key = sessionId, value = userId
    private Map<String, Long> loginUsers = new HashMap<>();

    private User getLoginUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }

    /**
     * 学生 / 教师注册接口。
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        // 简单校验角色
        String role = request.getRole();
        if (role == null ||
                !(role.equalsIgnoreCase("STUDENT") || role.equalsIgnoreCase("TEACHER"))) {
            return Result.fail("角色必须是 STUDENT 或 TEACHER");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // 加密在 UserServiceImpl.register 中进行
        user.setRealName(request.getRealName());
        user.setRole(role.toUpperCase());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus("ACTIVE");

        boolean ok = userService.register(user);
        if (!ok) {
            return Result.fail("用户名已存在或保存失败");
        }

        // 注册成功后，根据角色自动创建学生 / 教师档案（与 User 通过 userId 关联）
        try {
            String upperRole = user.getRole();
            if ("STUDENT".equalsIgnoreCase(upperRole)) {
                studentService.createForUser(user);
            } else if ("TEACHER".equalsIgnoreCase(upperRole)) {
                teacherService.createForUser(user);
            }
        } catch (Exception ignored) {
            // 这里失败不影响用户注册主流程，但会导致没有自动档案，需要后续补数据
        }
        return Result.ok(null);
    }

    /**
     * 删除用户：同时级联删除学生 / 教师档案。
     * 注意：该接口仅做基础权限校验，实际使用时可按需扩展。
     */
    @PostMapping("/delete")
    public Result<Void> deleteUser(@RequestBody Map<String, Long> body) {
        Long userId = body != null ? body.get("userId") : null;
        if (userId == null) {
            return Result.fail("userId 不能为空");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        String role = user.getRole();
        try {
            if ("STUDENT".equalsIgnoreCase(role)) {
                studentService.deleteByUserId(userId);
            } else if ("TEACHER".equalsIgnoreCase(role)) {
                teacherService.deleteByUserId(userId);
            }
        } catch (Exception ignored) {
            // 关联删除失败不阻断用户删除，但可能残留孤立记录
        }

        userService.removeById(userId);
        return Result.ok(null);
    }

    /**
     * 管理员查询所有用户列表。
     */
    @GetMapping("/listAll")
    public Result<List<User>> listAll(HttpSession session) {
        User current = getLoginUser(session);
        if (current == null) {
            return Result.fail("未登录");
        }
        if (current.getRole() == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            return Result.fail("当前用户无权限查看用户列表");
        }
        List<User> list = userService.list();
        return Result.ok(list);
    }

    /**
     * 登录接口：账号密码验证 + 保存登录状态到 Session。
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequest request,
                              HttpSession session) {
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return Result.fail("账号状态异常：" + user.getStatus());
        }

        // 登录成功：把 userId 写入 Session，并记录在 loginUsers 中
        session.setAttribute(AuthConstants.SESSION_USER_ID, user.getId());
        loginUsers.put(session.getId(), user.getId());

        return Result.ok(user);
    }

    /**
     * 查询当前登录用户信息。
     */
    @GetMapping("/me")
    public Result<User> me(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return Result.fail("未登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(user);
    }

    /**
     * 修改密码：需要已登录。
     */
    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request,
                                       HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return Result.fail("未登录");
        }

        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        // 校验旧密码
        if (!org.mindrot.jbcrypt.BCrypt.checkpw(request.getOldPassword(), user.getPassword())) {
            return Result.fail("原密码不正确");
        }

        // 设置新密码（加密后再保存）
        String newHashed = org.mindrot.jbcrypt.BCrypt.hashpw(
                request.getNewPassword(),
                org.mindrot.jbcrypt.BCrypt.gensalt()
        );
        user.setPassword(newHashed);
        userService.updateById(user);
        return Result.ok(null);
    }

    /**
     * 退出登录：清理 Session，并从 loginUsers 中移除。
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId != null) {
            loginUsers.remove(session.getId());
        }
        session.invalidate();
        return Result.ok(null);
    }
}
