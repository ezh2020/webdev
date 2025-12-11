package com.webdev.webdev;

import lombok.Data;

/**
 * 修改密码请求体，要求用户已登录。
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码，用于校验。
     */
    private String oldPassword;

    /**
     * 新密码，后端会加密保存。
     */
    private String newPassword;
}

