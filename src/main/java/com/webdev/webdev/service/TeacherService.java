package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.TeacherProfileUpdateRequest;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;

/**
 * 教师领域相关业务。
 */
public interface TeacherService extends IService<Teacher> {

    /**
     * 用户注册成功后，为其创建教师档案（仅绑定 userId，其他信息后续补充）。
     */
    Teacher createForUser(User user);

    /**
     * 用户删除时，根据 userId 删除教师档案。
     */
    boolean deleteByUserId(Long userId);

    /**
     * 更新教师整体档案（包含 User 基本信息 + Teacher 任教信息）。
     */
    boolean updateProfile(TeacherProfileUpdateRequest request);
}
