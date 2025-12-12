package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.CourseSelection;
import com.webdev.webdev.service.CourseSelectionService;
import com.webdev.webdev.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 选课表（学生-课程关系）接口：
 * - 新增选课记录
 * - 修改选课记录（例如状态、期末成绩）
 * - 删除选课记录
 * - 查询：按 id / 全部 / 按课程 / 按学生 / 按老师
 */
@RestController
@RequestMapping("/api/courseSelection")
@CrossOrigin(origins = "*")
public class CourseSelectionController {

    @Autowired
    private CourseSelectionService courseSelectionService;

    @Autowired
    private CourseService courseService;

    /**
     * 新增选课记录（学生选课）。
     * 前端传入 JSON：
     * {
     *   "studentId": 1,
     *   "courseId": 2,
     *   "status": "ENROLLED"
     * }
     */
    @PostMapping("/add")
    public Result<Void> add(@RequestBody CourseSelection selection) {
        String error = courseSelectionService.addSelection(selection);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 更新选课记录（例如修改状态、期末成绩）。
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody CourseSelection selection) {
        if (selection == null || selection.getId() == null) {
            return Result.fail("id 不能为空");
        }
        boolean ok = courseSelectionService.updateById(selection);
        if (!ok) {
            return Result.fail("更新选课记录失败");
        }
        return Result.ok(null);
    }

    /**
     * 删除选课记录（退课）。
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        String error = courseSelectionService.deleteSelection(id);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 教师发布最终成绩并通知学生和老师。
     * 示例：POST /api/courseSelection/publishGrade
     * {
     *   "id": 1,
     *   "finalScore": 90.5,
     *   "status": "GRADED"
     * }
     */
    @PostMapping("/publishGrade")
    public Result<Void> publishGrade(@RequestBody CourseSelection selection) {
        String error = courseSelectionService.publishGrade(selection);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 根据选课记录 ID 查询。
     */
    @GetMapping("/{id}")
    public Result<CourseSelection> getById(@PathVariable("id") Long id) {
        CourseSelection selection = courseSelectionService.getById(id);
        if (selection == null) {
            return Result.fail("选课记录不存在");
        }
        return Result.ok(selection);
    }

    /**
     * 查询全部选课记录。
     */
    @GetMapping("/listAll")
    public Result<List<CourseSelection>> listAll() {
        List<CourseSelection> list = courseSelectionService.list();
        return Result.ok(list);
    }

    /**
     * 按课程查询选课记录。
     * 示例：GET /api/courseSelection/listByCourse?courseId=1
     */
    @GetMapping("/listByCourse")
    public Result<List<CourseSelection>> listByCourse(@RequestParam("courseId") Long courseId) {
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }
        List<CourseSelection> list = courseSelectionService.list(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, courseId)
        );
        return Result.ok(list);
    }

    /**
     * 按学生查询选课记录。
     * 示例：GET /api/courseSelection/listByStudent?studentId=1
     */
    @GetMapping("/listByStudent")
    public Result<List<CourseSelection>> listByStudent(@RequestParam("studentId") Long studentId) {
        if (studentId == null) {
            return Result.fail("studentId 不能为空");
        }
        List<CourseSelection> list = courseSelectionService.list(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, studentId)
        );
        return Result.ok(list);
    }

    /**
     * 按老师查询其所有课程下的选课记录。
     * 示例：GET /api/courseSelection/listByTeacher?teacherId=1
     */
    @GetMapping("/listByTeacher")
    public Result<List<CourseSelection>> listByTeacher(@RequestParam("teacherId") Long teacherId) {
        if (teacherId == null) {
            return Result.fail("teacherId 不能为空");
        }

        List<Course> courses = courseService.listByTeacherId(teacherId);
        if (courses == null || courses.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> courseIds = courses.stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        List<CourseSelection> list = courseSelectionService.list(
                new LambdaQueryWrapper<CourseSelection>()
                        .in(CourseSelection::getCourseId, courseIds)
        );
        return Result.ok(list);
    }
}
