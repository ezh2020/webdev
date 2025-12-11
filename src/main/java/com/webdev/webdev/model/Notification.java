package com.webdev.webdev.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    private String title;
    private String content;

    @TableField("publisher_id")
    private Long publisherId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime publishTime;

    @TableField("is_important")
    private Boolean isImportant;
}
