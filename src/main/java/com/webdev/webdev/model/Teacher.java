package com.webdev.webdev.model;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

    @Data
    @TableName("teachers")
    public class Teacher {
        @TableId(type = IdType.AUTO)
        private Long id;

        @TableField("user_id")
        private Long userId;

        @TableField("teacher_id")
        private String teacherId;

        private String department;
        private String title;
        private String office;
    }

