package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.TeacherProfileUpdateRequest;
import com.webdev.webdev.mapper.TeacherMapper;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    @Autowired
    private UserService userService;

    @Override
    public Teacher createForUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getId());
        // teacherId 在数据库中为 NOT NULL 且 UNIQUE，这里先用 userId 生成一个初始工号，
        // 后续可以通过档案维护接口更新为真实工号，避免插入时违反约束。
        teacher.setTeacherId("T" + user.getId());
        // 其它字段（department、title 等）由后续接口补充
        this.save(teacher);
        return teacher;
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    public boolean updateProfile(TeacherProfileUpdateRequest request) {
        if (request == null || request.getUserId() == null) {
            return false;
        }
        try {
            User user = userService.getById(request.getUserId());
            if (user == null) {
                return false;
            }
            if (!"TEACHER".equalsIgnoreCase(user.getRole())) {
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

            // 查找并更新 Teacher 任教信息
            Teacher teacher = this.getOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, request.getUserId())
            );
            if (teacher == null) {
                teacher = createForUser(user);
            }

            if (StringUtils.hasText(request.getTeacherId())) {
                teacher.setTeacherId(request.getTeacherId());
            }
            if (StringUtils.hasText(request.getDepartment())) {
                teacher.setDepartment(request.getDepartment());
            }
            if (StringUtils.hasText(request.getTitle())) {
                teacher.setTitle(request.getTitle());
            }
            if (StringUtils.hasText(request.getOffice())) {
                teacher.setOffice(request.getOffice());
            }

            return this.updateById(teacher);
        } catch (Exception e) {
            return false;
        }
    }
}
