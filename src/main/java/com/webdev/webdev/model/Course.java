package com.webdev.webdev.model;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

    @Data
    @TableName("courses")
    public class Course {
        @TableId(type = IdType.AUTO)
        private Long id;

        @TableField("course_code")
        private String courseCode;

        @TableField("course_name")
        private String courseName;

        private Double credit;
        private String description;

        @TableField("teacher_id")
        private Long teacherId;

        @TableField("max_students")
        private Integer maxStudents;

        @TableField("current_students")
        private Integer currentStudents;

        private String semester;
        private String status;

        @TableField(fill = FieldFill.INSERT)
        private LocalDateTime createdAt;
    }

