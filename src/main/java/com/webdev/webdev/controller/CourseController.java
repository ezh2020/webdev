package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.CourseSearchRequest;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.servlet.http.HttpSession;

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

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    private User getLoginUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(AuthConstants.SESSION_USER_ID);
        if (userId == null) {
            return null;
        }
        return userService.getById(userId);
    }

    private Teacher getCurrentTeacher(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        return teacherService.getOne(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, user.getId())
        );
    }

    /**
     * 创建课程
     * 前端传入一个 Course 对象（不需要 id，id 数据库会自增）。
     * 仅允许已登录教师或管理员创建课程：
     * - 教师：课程的 teacherId 强制绑定为当前教师档案的主键；
     * - 管理员：可创建任意课程（保留前端传入的 teacherId）。
     */
    @PostMapping("/add")
    public Result<Void> addCourse(@RequestBody Course course, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限创建课程");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法创建课程");
            }
            // 统一从教师档案主键绑定课程，避免前端伪造 teacherId
            course.setTeacherId(teacher.getId());
        }

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
    public Result<Void> updateCourse(@RequestBody Course course, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        if (course == null || course.getId() == null) {
            return Result.fail("课程 id 不能为空");
        }

        Course dbCourse = courseService.getById(course.getId());
        if (dbCourse == null) {
            return Result.fail("课程不存在");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限修改课程");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法修改课程");
            }
            if (!teacher.getId().equals(dbCourse.getTeacherId())) {
                return Result.fail("无权修改其他教师的课程");
            }
            // 防止前端修改任课教师
            course.setTeacherId(dbCourse.getTeacherId());
        }

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
    public Result<Void> deleteCourse(@PathVariable Integer id, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        Course course = courseService.getById(id);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限删除课程");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法删除课程");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权删除其他教师的课程");
            }
        }

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
     * 接口：GET /api/course/listByTeacher?teacherId=1
     */
    @GetMapping("/listByTeacher")
    public Result<List<Course>> listCoursesByTeacher(@RequestParam(value = "teacherId", required = false) Long teacherId,
                                                     HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看教师课程");
        }

        Long actualTeacherId;
        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案");
            }
            // 教师只能查看自己的课程，忽略前端传入的 teacherId
            actualTeacherId = teacher.getId();
        } else {
            // 管理员可以按传入的 teacherId 查询
            if (teacherId == null) {
                return Result.fail("teacherId 不能为空");
            }
            actualTeacherId = teacherId;
        }

        List<Course> courses = courseService.listByTeacherId(actualTeacherId);
        return Result.ok(courses);
    }

    /**
     * 根据学期查询课程列表。
     * 接口：GET /api/course/listBySemester?semester=2024-FALL
     */
    @GetMapping("/listBySemester")
    public Result<List<Course>> listBySemester(@RequestParam String semester) {
        List<Course> courses = courseService.listBySemester(semester);
        return Result.ok(courses);
    }

    /**
     * 课程多条件组合搜索（时间/学分/余量）+ 排序。
     * 接口：
     * POST /api/course/search
     * {
     *   "semester": "2023-2024-1",
     *   "minCredit": 2,
     *   "maxCredit": 4,
     *   "minRemain": 5,
     *   "sortBy": "remain",
     *   "sortOrder": "desc"
     * }
     */
    @PostMapping("/search")
    public Result<List<Course>> search(@RequestBody CourseSearchRequest request) {
        List<Course> courses = courseService.searchCourses(request);
        return Result.ok(courses);
    }

    /**
     * 获取课程对应教师的公开信息（用于前端展示教师姓名/邮箱/办公室等）。
     * 接口：GET /api/course/teacherInfo?courseId=1
     */
    @GetMapping("/teacherInfo")
    public Result<CourseTeacherInfoView> teacherInfo(@RequestParam("courseId") Long courseId,
                                                     HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        CourseTeacherInfoView view = new CourseTeacherInfoView();
        view.courseId = course.getId();
        view.courseName = course.getCourseName();
        view.teacherId = course.getTeacherId();

        if (course.getTeacherId() != null) {
            Teacher teacher = teacherService.getById(course.getTeacherId());
            if (teacher != null) {
                view.teacherCode = teacher.getTeacherId();
                view.department = teacher.getDepartment();
                view.title = teacher.getTitle();
                view.office = teacher.getOffice();

                if (teacher.getUserId() != null) {
                    User teacherUser = userService.getById(teacher.getUserId());
                    if (teacherUser != null) {
                        view.userId = teacherUser.getId();
                        view.realName = teacherUser.getRealName();
                        view.email = teacherUser.getEmail();
                        view.phone = teacherUser.getPhone();
                    }
                }
            }
        }

        return Result.ok(view);
    }

    public static class CourseTeacherInfoView {
        public Long courseId;
        public String courseName;
        public Long teacherId;

        public Long userId;
        public String realName;
        public String email;
        public String phone;

        public String teacherCode;
        public String department;
        public String title;
        public String office;
    }
}
