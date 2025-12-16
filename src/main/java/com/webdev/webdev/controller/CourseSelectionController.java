package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.CourseSelection;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.service.CourseSelectionService;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.StudentService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

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

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

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
     * 新增选课记录（学生选课）。
     * 前端传入 JSON：
     * {
     *   "studentId": 1,
     *   "courseId": 2,
     *   "status": "SELECTED"
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
     * 接口：POST /api/courseSelection/publishGrade
     * 请求体示例：
     * {
     *   "id": 1,
     *   "finalScore": 90.5,
     *   "status": "GRADED"
     * }
     */
    @PostMapping("/publishGrade")
    public Result<Void> publishGrade(@RequestBody CourseSelection selection, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }
        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限发布成绩");
        }
        if (selection == null || selection.getId() == null) {
            return Result.fail("id 不能为空");
        }
        if (isTeacher) {
            CourseSelection db = courseSelectionService.getById(selection.getId());
            if (db == null) {
                return Result.fail("选课记录不存在");
            }
            Course course = courseService.getById(db.getCourseId());
            if (course == null) {
                return Result.fail("课程不存在");
            }
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权发布其他教师课程的成绩");
            }
        }
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
     * 接口：GET /api/courseSelection/listByCourse?courseId=1
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
     * 按课程查询选课记录（带学生档案 + 用户信息，用于教师端管理展示）。
     * 接口：GET /api/courseSelection/listByCourseDetail?courseId=1
     */
    @GetMapping("/listByCourseDetail")
    public Result<List<CourseSelectionDetailView>> listByCourseDetail(@RequestParam("courseId") Long courseId,
                                                                      HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看选课名单");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }
        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程的选课名单");
            }
        }

        List<CourseSelection> selections = courseSelectionService.list(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, courseId)
        );
        if (selections == null || selections.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> studentPkIds = selections.stream()
                .map(CourseSelection::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<Student> students = studentService.listByIds(studentPkIds);
        Map<Long, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            studentMap.put(s.getId(), s);
        }

        List<Long> userIds = students.stream()
                .map(Student::getUserId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        Map<Long, User> userMap = new HashMap<>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }

        List<CourseSelectionDetailView> result = selections.stream().map(sel -> {
            CourseSelectionDetailView view = new CourseSelectionDetailView();
            view.id = sel.getId();
            view.courseId = sel.getCourseId();
            view.studentPkId = sel.getStudentId();
            view.status = sel.getStatus();
            view.finalScore = sel.getFinalScore();
            view.selectedAt = sel.getSelectedAt();

            Student stu = sel.getStudentId() != null ? studentMap.get(sel.getStudentId()) : null;
            if (stu != null) {
                view.studentNo = stu.getStudentId();
                view.className = stu.getClassName();
                view.major = stu.getMajor();
                view.enrollmentYear = stu.getEnrollmentYear();
                if (stu.getUserId() != null) {
                    User u = userMap.get(stu.getUserId());
                    if (u != null) {
                        view.userId = u.getId();
                        view.realName = u.getRealName();
                        view.email = u.getEmail();
                        view.phone = u.getPhone();
                    }
                }
            }
            return view;
        }).collect(Collectors.toList());

        return Result.ok(result);
    }

    public static class CourseSelectionDetailView {
        public Long id;
        public Long courseId;
        public Long studentPkId;
        public String studentNo;
        public String className;
        public String major;
        public Integer enrollmentYear;

        public Long userId;
        public String realName;
        public String email;
        public String phone;

        public String status;
        public Double finalScore;
        public java.time.LocalDateTime selectedAt;
    }

    /**
     * 按学生查询选课记录。
     * 接口：GET /api/courseSelection/listByStudent?studentId=1
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
     * 接口：GET /api/courseSelection/listByTeacher?teacherId=1
     */
    @GetMapping("/listByTeacher")
    public Result<List<CourseSelection>> listByTeacher(@RequestParam(value = "teacherId", required = false) Long teacherId,
                                                       HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看选课记录");
        }

        Long actualTeacherId;
        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案");
            }
            // 教师只能查看自己课程的选课记录，忽略前端传入的 teacherId
            actualTeacherId = teacher.getId();
        } else {
            if (teacherId == null) {
                return Result.fail("teacherId 不能为空");
            }
            actualTeacherId = teacherId;
        }

        List<Course> courses = courseService.listByTeacherId(actualTeacherId);
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
