package com.webdev.webdev.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assignment_submissions")
public class AssignmentSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("assignment_id")
    private Long assignmentId;

    @TableField("student_id")
    private Long studentId;

    @TableField("submission_text")
    private String submissionText;

    @TableField("attachment_path")
    private String attachmentPath;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedAt;

    private Double score;
    private String feedback;
    private String status;
}