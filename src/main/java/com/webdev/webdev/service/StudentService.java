package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.StudentProfileUpdateRequest;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.User;

/**
 * 学生领域相关业务。
 */
public interface StudentService extends IService<Student> {

    /**
     * 用户注册成功后，为其创建学生档案（仅绑定 userId，其他信息后续补充）。
     */
    Student createForUser(User user);

    /**
     * 用户删除时，根据 userId 删除学生档案。
     */
    boolean deleteByUserId(Long userId);

    /**
     * 更新学生整体档案（包含 User 基本信息 + Student 学籍信息）。
     */
    boolean updateProfile(StudentProfileUpdateRequest request);
}
