package com.webdev.webdev;

/**
 * 认证 / 会话相关常量。
 *
 * 当前项目使用 HttpSession 保存已登录用户的 ID，
 * 这里集中维护 key，避免在各个 Controller 中硬编码字符串。
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    /**
     * Session 中保存当前登录用户 ID 的属性名。
     * 由 UserController 在登录成功后写入。
     */
    public static final String SESSION_USER_ID = "LOGIN_USER_ID";
}

