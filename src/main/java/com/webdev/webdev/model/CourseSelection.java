package com.webdev.webdev.model;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

    @Data
    @TableName("course_selections")
    public class CourseSelection {
        @TableId(type = IdType.AUTO)
        private Long id;

        @TableField("student_id")
        private Long studentId;

        @TableField("course_id")
        private Long courseId;

        @TableField(fill = FieldFill.INSERT)
        private LocalDateTime selectedAt;

        private String status;

        @TableField("final_score")
        private Double finalScore;
    }

