package com.webdev.webdev.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_resources")
public class CourseResource {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    private String title;
    private String description;

    @TableField("file_path")
    private String filePath;

    @TableField("file_size")
    private Long fileSize;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadedAt;

    @TableField("download_count")
    private Integer downloadCount;
}