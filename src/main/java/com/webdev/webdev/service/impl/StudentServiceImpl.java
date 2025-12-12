package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.StudentProfileUpdateRequest;
import com.webdev.webdev.mapper.StudentMapper;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.StudentService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Autowired
    private UserService userService;

    @Override
    public Student createForUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        Student student = new Student();
        student.setUserId(user.getId());
        // 其它字段（studentId、className、major 等）由后续接口补充
        this.save(student);
        return student;
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    public boolean updateProfile(StudentProfileUpdateRequest request) {
        if (request == null || request.getUserId() == null) {
            return false;
        }
        try {
            User user = userService.getById(request.getUserId());
            if (user == null) {
                return false;
            }
            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                return false;
            }

            // 更新 User 基础信息（只更新非空字段）
            if (StringUtils.hasText(request.getRealName())) {
                user.setRealName(request.getRealName());
            }
            if (StringUtils.hasText(request.getEmail())) {
                user.setEmail(request.getEmail());
            }
            if (StringUtils.hasText(request.getPhone())) {
                user.setPhone(request.getPhone());
            }
            userService.updateById(user);

            // 查找并更新 Student 学籍信息
            Student student = this.getOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, request.getUserId())
            );
            if (student == null) {
                student = createForUser(user);
            }

            if (StringUtils.hasText(request.getStudentId())) {
                student.setStudentId(request.getStudentId());
            }
            if (StringUtils.hasText(request.getClassName())) {
                student.setClassName(request.getClassName());
            }
            if (StringUtils.hasText(request.getMajor())) {
                student.setMajor(request.getMajor());
            }
            if (request.getEnrollmentYear() != null) {
                student.setEnrollmentYear(request.getEnrollmentYear());
            }

            return this.updateById(student);
        } catch (Exception e) {
            return false;
        }
    }
}
