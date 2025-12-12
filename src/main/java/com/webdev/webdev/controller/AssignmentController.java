package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业相关接口：
 * - 新建作业
 * - 修改作业
 * - 删除作业
 * - 查询单个 / 列出全部
 * - 按课程 + 截止时间筛选作业
 */
@RestController
@RequestMapping("/api/assignment")
@CrossOrigin(origins = "*")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    /**
     * 新建作业。
     * 需要至少提供 courseId 和 title。
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Assignment assignment) {
        String error = assignmentService.createAssignment(assignment);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 更新作业。
     * 需要传入 id，其它字段按需更新。
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Assignment assignment) {
        if (assignment == null || assignment.getId() == null) {
            return Result.fail("id 不能为空");
        }
        String error = assignmentService.updateAssignment(assignment);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 删除作业。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean ok = assignmentService.removeById(id);
        if (!ok) {
            return Result.fail("作业不存在或删除失败");
        }
        return Result.ok(null);
    }

    /**
     * 根据 ID 获取作业详情。
     */
    @GetMapping("/{id}")
    public Result<Assignment> getById(@PathVariable("id") Long id) {
        Assignment assignment = assignmentService.getById(id);
        if (assignment == null) {
            return Result.fail("作业不存在");
        }
        return Result.ok(assignment);
    }

    /**
     * 列出所有作业。
     */
    @GetMapping("/listAll")
    public Result<List<Assignment>> listAll() {
        List<Assignment> list = assignmentService.list();
        return Result.ok(list);
    }

    /**
     * 按课程列出指定截止日期之前的作业。
     * 示例：
     * GET /api/assignment/listByCourseAndDeadline?courseId=1&before=2024-12-31T23:59:59
     * - courseId 必填
     * - before 可选：不传则返回该课程下全部作业
     */
    @GetMapping("/listByCourseAndDeadline")
    public Result<List<Assignment>> listByCourseAndDeadline(
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "before", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before
    ) {
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }

        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Assignment::getCourseId, courseId);
        if (before != null) {
            wrapper.le(Assignment::getDeadline, before);
        }
        wrapper.orderByAsc(Assignment::getDeadline);

        List<Assignment> list = assignmentService.list(wrapper);
        return Result.ok(list);
    }
}

