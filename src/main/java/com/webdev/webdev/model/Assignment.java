package com.webdev.webdev.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assignments")
public class Assignment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    private String title;
    private String description;

    @TableField("max_score")
    private Double maxScore;

    private LocalDateTime deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
