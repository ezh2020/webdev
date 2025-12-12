package com.webdev.webdev;

import lombok.Data;

/**
 * 教师个人档案整体更新请求。
 * 包含 User 表中的基础信息 + Teacher 表中的任教信息。
 */
@Data
public class TeacherProfileUpdateRequest {

    /**
     * 关联的用户主键 ID，用于定位 User / Teacher 记录。
     */
    private Long userId;

    // User 基础信息
    private String realName;
    private String email;
    private String phone;

    // Teacher 任教信息
    private String teacherId;
    private String department;
    private String title;
    private String office;
}

