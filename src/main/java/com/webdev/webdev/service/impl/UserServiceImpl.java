package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.UserMapper;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.UserService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * 用户相关业务逻辑实现：
 * - 注册：检查重名 + 密码加密 + 保存
 * - 登录：根据用户名查用户并校验密码
 * - 根据 userId 查询用户信息
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean register(User user) {
        try {
            System.out.println("start to register user " + user.getUsername());

            // 1. 检查用户名是否已存在
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, user.getUsername());
            User existingUser = this.getOne(queryWrapper);
            if (existingUser != null) {
                System.out.println("user already exist " + user.getUsername());
                return false;
            }

            // 2. 密码加密存储
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            user.setPassword(hashedPassword);

            // 3. 保存到数据库
            boolean saveResult = this.save(user);
            System.out.println("register user result: " + saveResult);
            return saveResult;
        } catch (Exception e) {
            System.out.println("register user exception " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User login(String username, String password) {
        try {
            // 1. 根据用户名查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            User user = this.getOne(queryWrapper);

            if (user == null) {
                // 用户名不存在
                return null;
            }

            // 2. 对比密码（BCrypt：明文 + 加盐后的密文）
            boolean match = BCrypt.checkpw(password, user.getPassword());
            if (!match) {
                // 密码不正确
                return null;
            }

            // 3. 登录成功，返回用户对象
            return user;
        } catch (Exception e) {
            System.out.println("login exception " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User getByUserInfo(Long userId) {
        try {
            return this.getById(userId);
        } catch (Exception e) {
            System.out.println("getByUserInfo exception " + e.getMessage());
            return null;
        }
    }
}
