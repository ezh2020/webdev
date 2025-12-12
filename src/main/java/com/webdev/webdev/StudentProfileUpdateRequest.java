package com.webdev.webdev;

import lombok.Data;

/**
 * 学生个人档案整体更新请求。
 * 包含 User 表中的基础信息 + Student 表中的学籍信息。
 */
@Data
public class StudentProfileUpdateRequest {

    /**
     * 关联的用户主键 ID，用于定位 User / Student 记录。
     */
    private Long userId;

    // User 基础信息
    private String realName;
    private String email;
    private String phone;

    // Student 学籍信息
    private String studentId;
    private String className;
    private String major;
    private Integer enrollmentYear;
}

