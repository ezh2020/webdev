package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.StudentProfileUpdateRequest;
import com.webdev.webdev.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生档案相关接口。
 * 只提供一个整体更新接口，内部自动联动 User 与 Student 表。
 */
@RestController
@RequestMapping("/api/student")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

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
