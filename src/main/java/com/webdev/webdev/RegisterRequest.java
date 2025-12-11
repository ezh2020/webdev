package com.webdev.webdev;

import lombok.Data;

/**
 * 注册请求体，用于学生 / 教师注册账号。
 */
@Data
public class RegisterRequest {

    /**
     * 登录用用户名，需全局唯一。
     */
    private String username;

    /**
     * 登录密码（明文），后端会进行加密存储。
     */
    private String password;

    /**
     * 角色：建议固定为 STUDENT / TEACHER。
     */
    private String role;

    private String email;
    private String phone;
}

