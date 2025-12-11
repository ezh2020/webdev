package com.webdev.webdev;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class MyDemoTest {

    @Autowired
    private UserService userService;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private CourseService courseService;

    @Test
    public void test1() {
        boolean ok = teacherService.update(
                new UpdateWrapper<Teacher>()
                        .eq("id", 11L)
                        .set("title", "教授"));
        System.out.println("更新结果: " + ok);
    }
}

