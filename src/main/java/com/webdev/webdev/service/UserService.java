package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.User;

public interface UserService extends IService<User> {
    boolean register(User user);
    User login(String username, String password);
    User getByUserInfo(Long userId);
}