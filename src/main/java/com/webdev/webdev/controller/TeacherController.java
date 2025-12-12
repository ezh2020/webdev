package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.TeacherProfileUpdateRequest;
import com.webdev.webdev.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师档案相关接口。
 * 只提供一个整体更新接口，内部自动联动 User 与 Teacher 表。
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

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
