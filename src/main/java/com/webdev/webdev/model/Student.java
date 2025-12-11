package com.webdev.webdev.model;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

    @Data
    @TableName("students")
    public class Student {
        @TableId(type = IdType.AUTO)
        private Long id;

        @TableField("user_id")
        private Long userId;

        @TableField("student_id")
        private String studentId;

        @TableField("class_name")
        private String className;

        private String major;

        @TableField("enrollment_year")
        private Integer enrollmentYear;
    }

