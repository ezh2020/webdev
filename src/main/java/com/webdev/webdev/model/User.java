package com.webdev.webdev.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
//MyBatis-Plus数据库实体类
@Data  // 这个注解会自动生成getter、setter、toString等方法
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role;
    private String email;
    private String phone;

    @TableField("real_name")
    private String realName;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
