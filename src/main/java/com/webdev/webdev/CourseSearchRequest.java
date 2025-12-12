package com.webdev.webdev;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程多条件查询请求：
 * - 按时间（创建时间区间）筛选
 * - 按学分筛选
 * - 按课程余量（maxStudents - currentStudents）筛选
 * - 支持排序
 */
@Data
public class CourseSearchRequest {

    /**
     * 学期编码（可选），例如：2023-2024-1
     */
    private String semester;

    /**
     * 创建时间起始（包含）
     */
    private LocalDateTime startCreatedAt;

    /**
     * 创建时间结束（包含）
     */
    private LocalDateTime endCreatedAt;

    /**
     * 学分下限 / 上限
     */
    private Double minCredit;
    private Double maxCredit;

    /**
     * 最小余量（剩余可选人数），例如：2 表示只查还至少剩 2 个名额的课程。
     */
    private Integer minRemain;

    /**
     * 排序字段：
     * - time    : 按创建时间
     * - credit  : 按学分
     * - remain  : 按课程余量
     */
    private String sortBy;

    /**
     * 排序方向：asc / desc，默认 desc。
     */
    private String sortOrder;
}

