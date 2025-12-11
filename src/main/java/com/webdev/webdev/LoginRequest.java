package com.webdev.webdev;

import lombok.Data;

/**
 * 登录请求体，对应前端传过来的用户名和密码。
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}

