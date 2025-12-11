package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程相关接口控制器：
 * - 创建课程
 * - 修改课程
 * - 删除课程
 * - 按 ID 查询课程 / 查询全部课程
 *
 * 提示：这里主要是把前端传来的 JSON 转成 Course 对象，
 * 然后调用 CourseService 中已经实现的业务方法。
 */
@RestController
@RequestMapping("/api/course")
@CrossOrigin(origins = "*")
public class CourseController {

    @Autowired
    private CourseService courseService;

    /**
     * 创建课程
     * 前端传入一个 Course 对象（不需要 id，id 数据库会自增）。
     * 目前不在后端做权限判断，由前端根据登录角色控制可见按钮。
     */
    @PostMapping("/add")
    public Result<Void> addCourse(@RequestBody Course course) {
        boolean ok = courseService.addCourse(course);
        if (!ok) {
            // 可能原因：必填项不完整、课程代码重复、学分/人数不合法等
            return Result.fail("创建课程失败，请检查课程代码是否重复、必填信息是否完整");
        }
        return Result.ok(null);
    }

    /**
     * 更新课程信息
     * 需要传入课程 id，以及想要修改的字段。
     */
    @PostMapping("/update")
    public Result<Void> updateCourse(@RequestBody Course course) {
        boolean ok = courseService.updateCourse(course);
        if (!ok) {
            // 可能原因：课程不存在、课程代码重复、人数设置不合理等
            return Result.fail("更新课程失败，请检查课程是否存在以及填入信息是否合法");
        }
        return Result.ok(null);
    }

    /**
     * 删除课程
     * 这里只做一个简单的接口：通过路径参数传入课程 id。
     * Service 内部会判断：如果 currentStudents > 0 则不允许删除。
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Integer id) {
        Course course = new Course();
        course.setId(id.longValue());

        boolean ok = courseService.deleteCourse(course);
        if (!ok) {
            return Result.fail("删除课程失败，可能课程不存在或已有学生选课");
        }
        return Result.ok(null);
    }

    /**
     * 根据课程 ID 查询课程详情。
     * 注意：这里使用你在 CourseService 中自定义的 getCourseById 方法。
     */
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Integer id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return Result.fail("课程不存在");
        }
        return Result.ok(course);
    }

    /**
     * 查询所有课程列表。
     */
    @GetMapping("/listAll")
    public Result<List<Course>> listAllCourses() {
        List<Course> courses = courseService.list();
        return Result.ok(courses);
    }

    /**
     * 根据教师 ID 查询该教师所授课程列表。
     * 示例：GET /api/course/listByTeacher?teacherId=1
     */
    @GetMapping("/listByTeacher")
    public Result<List<Course>> listCoursesByTeacher(@RequestParam Long teacherId) {
        List<Course> courses = courseService.listByTeacherId(teacherId);
        return Result.ok(courses);
    }

    /**
     * 根据学期查询课程列表。
     * 示例：GET /api/course/listBySemester?semester=2024-FALL
     */
    @GetMapping("/listBySemester")
    public Result<List<Course>> listBySemester(@RequestParam String semester) {
        List<Course> courses = courseService.listBySemester(semester);
        return Result.ok(courses);
    }
}
