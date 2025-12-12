package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.model.AssignmentSubmission;
import com.webdev.webdev.service.AssignmentSubmissionService;
import com.webdev.webdev.service.AssignmentSubmissionService.StudentAssignmentSubmissionView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 作业提交相关接口：
 * - 学生提交作业（带附件）
 * - 更新已提交作业（重新上传附件 / 修改文本）
 * - 删除提交
 * - 按 id / 作业 / 学生 / 课程 查询提交记录
 */
@RestController
@RequestMapping("/api/assignmentSubmission")
@CrossOrigin(origins = "*")
public class AssignmentSubmissionController {

    @Autowired
    private AssignmentSubmissionService assignmentSubmissionService;

    /**
     * 学生提交作业。
     *
     * 示例：POST /api/assignmentSubmission/submit  (multipart/form-data)
     * form-data:
     * - assignmentId: 1
     * - studentId: 1001
     * - submissionText: "这是作业答案文本"
     * - file: <附件>
     */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam("assignmentId") Long assignmentId,
                               @RequestParam("studentId") Long studentId,
                               @RequestParam(value = "submissionText", required = false) String submissionText,
                               @RequestParam("file") MultipartFile file) {
        String error = assignmentSubmissionService.submit(assignmentId, studentId, submissionText, file);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 学生更新已提交作业。
     * 必须提供新的附件。
     *
     * 示例：POST /api/assignmentSubmission/update  (multipart/form-data)
     * form-data:
     * - id: 1
     * - submissionText: "更新后的答案"
     * - file: <新附件>
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestParam("id") Long id,
                               @RequestParam(value = "submissionText", required = false) String submissionText,
                               @RequestParam("file") MultipartFile file) {
        String error = assignmentSubmissionService.updateSubmission(id, submissionText, file);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 删除提交记录（同时删除附件）。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean ok = assignmentSubmissionService.deleteWithFile(id);
        if (!ok) {
            return Result.fail("提交记录不存在或删除失败");
        }
        return Result.ok(null);
    }

    /**
     * 根据 ID 查询单条提交记录。
     */
    @GetMapping("/{id}")
    public Result<AssignmentSubmission> getById(@PathVariable("id") Long id) {
        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return Result.fail("提交记录不存在");
        }
        return Result.ok(submission);
    }

    /**
     * 按作业查询提交列表。
     * 示例：GET /api/assignmentSubmission/listByAssignment?assignmentId=1
     */
    @GetMapping("/listByAssignment")
    public Result<List<AssignmentSubmission>> listByAssignment(@RequestParam("assignmentId") Long assignmentId) {
        List<AssignmentSubmission> list = assignmentSubmissionService.listByAssignment(assignmentId);
        return Result.ok(list);
    }

    /**
     * 按学生查询提交列表。
     * 示例：GET /api/assignmentSubmission/listByStudent?studentId=1001
     */
    @GetMapping("/listByStudent")
    public Result<List<AssignmentSubmission>> listByStudent(@RequestParam("studentId") Long studentId) {
        List<AssignmentSubmission> list = assignmentSubmissionService.listByStudent(studentId);
        return Result.ok(list);
    }

    /**
     * 按课程查询提交列表（通过作业的 courseId 关联）。
     * 示例：GET /api/assignmentSubmission/listByCourse?courseId=1
     */
    @GetMapping("/listByCourse")
    public Result<List<AssignmentSubmission>> listByCourse(@RequestParam("courseId") Long courseId) {
        List<AssignmentSubmission> list = assignmentSubmissionService.listByCourse(courseId);
        return Result.ok(list);
    }

    /**
     * 同一作业的平均分（只统计已打分提交）。
     * 示例：GET /api/assignmentSubmission/averageScore?assignmentId=1
     */
    @GetMapping("/averageScore")
    public Result<Double> averageScore(@RequestParam("assignmentId") Long assignmentId) {
        Double avg = assignmentSubmissionService.getAverageScore(assignmentId);
        // 如果暂时没有打分记录，avg 可能为 null，这里直接返回 ok(null)
        return Result.ok(avg);
    }

    /**
     * 按课程查询提交列表，并按成绩排序。
     * 示例：GET /api/assignmentSubmission/listByCourseOrderByScore?courseId=1&order=desc
     * - order 可选：asc / desc，默认 desc（高分在前）
     */
    @GetMapping("/listByCourseOrderByScore")
    public Result<List<AssignmentSubmission>> listByCourseOrderByScore(
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "order", required = false, defaultValue = "desc") String order) {
        boolean asc = "asc".equalsIgnoreCase(order);
        List<AssignmentSubmission> list = assignmentSubmissionService.listByCourseOrderByScore(courseId, asc);
        return Result.ok(list);
    }

    /**
     * 学生按条件筛选作业提交情况：
     * - submitted: true=只看已提交，false=只看未提交
     * - graded: true=只看已打分，false=只看未打分
     * - courseId: 限定课程（可选）
     * - deadlineBefore: 截止时间早于该时间的作业（可选）
     *
     * 示例：
     * GET /api/assignmentSubmission/studentFilter?studentId=1001&submitted=true&graded=false&courseId=1&deadlineBefore=2025-01-01T23:59:59
     */
    @GetMapping("/studentFilter")
    public Result<List<StudentAssignmentSubmissionView>> studentFilter(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "deadlineBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineBefore,
            @RequestParam(value = "submitted", required = false) Boolean submitted,
            @RequestParam(value = "graded", required = false) Boolean graded) {

        List<StudentAssignmentSubmissionView> list =
                assignmentSubmissionService.filterForStudent(studentId, courseId, deadlineBefore, submitted, graded);
        return Result.ok(list);
    }
}
