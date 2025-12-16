package com.webdev.webdev.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.webdev.webdev.AuthConstants;
import com.webdev.webdev.Result;
import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.model.AssignmentSubmission;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.AssignmentService;
import com.webdev.webdev.service.AssignmentSubmissionService;
import com.webdev.webdev.service.AssignmentSubmissionService.StudentAssignmentSubmissionView;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.service.StudentService;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

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

    private static final Path ATTACHMENT_BASE_DIR = Paths.get("submission-files");

    @Autowired
    private AssignmentSubmissionService assignmentSubmissionService;

    @Autowired
    private AssignmentService assignmentService;

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

    private Student getCurrentStudent(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        return studentService.getOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getId())
        );
    }

    /**
     * 学生提交作业。
     *
     * 接口：POST /api/assignmentSubmission/submit  (multipart/form-data)
     * form-data:
     * - assignmentId: 1
     * - studentId: 1001
     * - submissionText: "这是作业答案文本"
     * - file: <附件>
     */
    @PostMapping("/submit")
    public Result<Void> submit(@RequestParam("assignmentId") Long assignmentId,
                               @RequestParam(value = "studentId", required = false) Long studentId,
                               @RequestParam(value = "submissionText", required = false) String submissionText,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return Result.fail("只有学生可以提交作业");
        }
        Student student = getCurrentStudent(user);
        if (student == null) {
            return Result.fail("未找到当前用户对应的学生档案，无法提交作业");
        }

        Long realStudentId = student.getId();
        if (studentId != null && !studentId.equals(realStudentId)) {
            return Result.fail("不允许代替其他学生提交作业");
        }

        String error = assignmentSubmissionService.submit(assignmentId, realStudentId, submissionText, file);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 学生更新已提交作业。
     * 必须提供新的附件。
     *
     * 接口：POST /api/assignmentSubmission/update  (multipart/form-data)
     * form-data:
     * - id: 1
     * - submissionText: "更新后的答案"
     * - file: <新附件>
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestParam("id") Long id,
                               @RequestParam(value = "submissionText", required = false) String submissionText,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
            return Result.fail("只有学生可以更新自己的作业提交");
        }
        Student student = getCurrentStudent(user);
        if (student == null) {
            return Result.fail("未找到当前用户对应的学生档案，无法更新提交");
        }

        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return Result.fail("提交记录不存在");
        }
        if (!student.getId().equals(submission.getStudentId())) {
            return Result.fail("无权更新其他学生的提交记录");
        }

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
    public Result<Void> delete(@PathVariable("id") Long id, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return Result.fail("提交记录不存在");
        }

        boolean isStudent = "STUDENT".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (isStudent) {
            Student student = getCurrentStudent(user);
            if (student == null) {
                return Result.fail("未找到当前用户对应的学生档案，无法删除提交");
            }
            if (!student.getId().equals(submission.getStudentId())) {
                return Result.fail("无权删除其他学生的提交记录");
            }
        } else if (!isAdmin) {
            return Result.fail("当前用户无权限删除提交记录");
        }

        boolean ok = assignmentSubmissionService.deleteWithFile(id);
        if (!ok) {
            return Result.fail("提交记录不存在或删除失败");
        }
        return Result.ok(null);
    }

    /**
     * 教师为作业提交打分 / 修改分数。
     *
     * 接口：POST /api/assignmentSubmission/grade
     * form-data 或 x-www-form-urlencoded:
     * - id: 提交记录主键
     * - score: 分数（0 ~ 对应作业 maxScore）
     * - feedback: 评语（可选）
     * - status: 状态（可选，默认为 GRADED）
     */
    @PostMapping("/grade")
    public Result<Void> grade(@RequestParam("id") Long id,
                              @RequestParam("score") Double score,
                              @RequestParam(value = "feedback", required = false) String feedback,
                              @RequestParam(value = "status", required = false) String status,
                              HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限为作业提交打分");
        }

        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return Result.fail("提交记录不存在");
        }

        Assignment assignment = assignmentService.getById(submission.getAssignmentId());
        if (assignment == null) {
            return Result.fail("关联的作业不存在");
        }

        Course course = courseService.getById(assignment.getCourseId());
        if (course == null) {
            return Result.fail("关联的课程不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法打分");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权为其他教师课程下的作业提交打分");
            }
        }

        String error = assignmentSubmissionService.gradeSubmission(id, score, feedback, status);
        if (error != null) {
            return Result.fail(error);
        }
        return Result.ok(null);
    }

    /**
     * 根据 ID 查询单条提交记录。
     */
    @GetMapping("/{id}")
    public Result<AssignmentSubmission> getById(@PathVariable("id") Long id, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return Result.fail("提交记录不存在");
        }

        boolean isStudent = "STUDENT".equalsIgnoreCase(user.getRole());
        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (isStudent) {
            Student student = getCurrentStudent(user);
            if (student == null || !student.getId().equals(submission.getStudentId())) {
                return Result.fail("无权查看其他学生的作业提交");
            }
        } else if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看提交记录");
            }

            Assignment assignment = assignmentService.getById(submission.getAssignmentId());
            if (assignment == null) {
                return Result.fail("关联的作业不存在");
            }
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null || !teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下的提交记录");
            }
        } else if (!isAdmin) {
            return Result.fail("当前用户无权限查看作业提交");
        }
        return Result.ok(submission);
    }

    /**
     * 按作业查询提交列表。
     * 接口：GET /api/assignmentSubmission/listByAssignment?assignmentId=1
     */
    @GetMapping("/listByAssignment")
    public Result<List<AssignmentSubmission>> listByAssignment(@RequestParam("assignmentId") Long assignmentId,
                                                               HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看作业提交列表");
        }

        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            return Result.fail("作业不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看提交列表");
            }
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null || !teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下的提交列表");
            }
        }

        List<AssignmentSubmission> list = assignmentSubmissionService.listByAssignment(assignmentId);
        return Result.ok(list);
    }

    /**
     * 按作业查询提交列表（带学生学号/姓名/班级信息，用于教师端展示）。
     * 接口：GET /api/assignmentSubmission/listByAssignmentDetail?assignmentId=1
     */
    @GetMapping("/listByAssignmentDetail")
    public Result<List<AssignmentSubmissionDetailView>> listByAssignmentDetail(@RequestParam("assignmentId") Long assignmentId,
                                                                              HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看作业提交列表");
        }

        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            return Result.fail("作业不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看提交列表");
            }
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null || !teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下的提交列表");
            }
        }

        List<AssignmentSubmission> list = assignmentSubmissionService.listByAssignment(assignmentId);
        if (list == null || list.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> studentPkIds = list.stream()
                .map(AssignmentSubmission::getStudentId)
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

        List<AssignmentSubmissionDetailView> result = list.stream().map(sub -> {
            AssignmentSubmissionDetailView view = new AssignmentSubmissionDetailView();
            view.id = sub.getId();
            view.assignmentId = sub.getAssignmentId();
            view.studentId = sub.getStudentId();
            view.submissionText = sub.getSubmissionText();
            view.attachmentPath = sub.getAttachmentPath();
            view.submittedAt = sub.getSubmittedAt();
            view.score = sub.getScore();
            view.feedback = sub.getFeedback();
            view.status = sub.getStatus();

            Student stu = sub.getStudentId() != null ? studentMap.get(sub.getStudentId()) : null;
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

    public static class AssignmentSubmissionDetailView {
        public Long id;
        public Long assignmentId;
        public Long studentId; // 学生表主键（用于接口调用）
        public String studentNo; // 学号
        public String className;
        public String major;
        public Integer enrollmentYear;

        public Long userId;
        public String realName;
        public String email;
        public String phone;

        public String submissionText;
        public String attachmentPath;
        public LocalDateTime submittedAt;
        public Double score;
        public String feedback;
        public String status;
    }

    /**
     * 下载作业提交的附件。
     * 接口：GET /api/assignmentSubmission/downloadAttachment/{id}
     */
    @GetMapping("/downloadAttachment/{id}")
    public ResponseEntity<?> downloadAttachment(@PathVariable("id") Long id, HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("未登录");
        }

        AssignmentSubmission submission = assignmentSubmissionService.getById(id);
        if (submission == null) {
            return ResponseEntity.badRequest().body("提交记录不存在");
        }

        boolean isStudent = "STUDENT".equalsIgnoreCase(user.getRole());
        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());

        if (isStudent) {
            Student student = getCurrentStudent(user);
            if (student == null || !student.getId().equals(submission.getStudentId())) {
                return ResponseEntity.status(403).body("无权下载其他学生的附件");
            }
        } else if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return ResponseEntity.status(403).body("未找到教师档案");
            }
            Assignment assignment = assignmentService.getById(submission.getAssignmentId());
            if (assignment == null) {
                return ResponseEntity.badRequest().body("关联的作业不存在");
            }
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null || !teacher.getId().equals(course.getTeacherId())) {
                return ResponseEntity.status(403).body("无权下载其他教师课程下的附件");
            }
        } else if (!isAdmin) {
            return ResponseEntity.status(403).body("无权限下载附件");
        }

        if (!StringUtils.hasText(submission.getAttachmentPath())) {
            return ResponseEntity.badRequest().body("附件不存在");
        }

        Path stored = Paths.get(submission.getAttachmentPath()).normalize();
        Path baseAbs = Paths.get("").toAbsolutePath().resolve(ATTACHMENT_BASE_DIR).normalize();
        Path abs = stored.isAbsolute()
                ? stored.toAbsolutePath().normalize()
                : Paths.get("").toAbsolutePath().resolve(stored).normalize();

        // 防止路径穿越：必须在 submission-files 目录下
        if (!abs.startsWith(baseAbs)) {
            return ResponseEntity.badRequest().body("附件路径不合法");
        }
        if (!Files.exists(abs)) {
            return ResponseEntity.badRequest().body("文件不存在");
        }

        try {
            String fileName = abs.getFileName().toString();
            InputStreamResource body = new InputStreamResource(Files.newInputStream(abs));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            String contentType = Files.probeContentType(abs);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(body);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("读取文件失败：" + e.getMessage());
        }
    }

    /**
     * 按学生查询提交列表。
     * 接口：GET /api/assignmentSubmission/listByStudent?studentId=1001
     */
    @GetMapping("/listByStudent")
    public Result<List<AssignmentSubmission>> listByStudent(@RequestParam("studentId") Long studentId,
                                                            HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isStudent = "STUDENT".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (isStudent) {
            Student student = getCurrentStudent(user);
            if (student == null) {
                return Result.fail("未找到当前用户对应的学生档案，无法查询提交记录");
            }
            if (!student.getId().equals(studentId)) {
                return Result.fail("无权查看其他学生的提交记录");
            }
        } else if (!isAdmin) {
            return Result.fail("当前用户无权限按学生查询提交记录");
        }

        List<AssignmentSubmission> list = assignmentSubmissionService.listByStudent(studentId);
        return Result.ok(list);
    }

    /**
     * 按课程查询提交列表（通过作业的 courseId 关联）。
     * 接口：GET /api/assignmentSubmission/listByCourse?courseId=1
     */
    @GetMapping("/listByCourse")
    public Result<List<AssignmentSubmission>> listByCourse(@RequestParam("courseId") Long courseId,
                                                           HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限按课程查询提交记录");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查询提交记录");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下的提交记录");
            }
        }

        List<AssignmentSubmission> list = assignmentSubmissionService.listByCourse(courseId);
        return Result.ok(list);
    }

    /**
     * 同一作业的平均分（只统计已打分提交）。
     * 接口：GET /api/assignmentSubmission/averageScore?assignmentId=1
     */
    @GetMapping("/averageScore")
    public Result<Double> averageScore(@RequestParam("assignmentId") Long assignmentId,
                                       HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看作业平均分");
        }

        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            return Result.fail("作业不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看平均分");
            }
            Course course = courseService.getById(assignment.getCourseId());
            if (course == null || !teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下作业的平均分");
            }
        }

        Double avg = assignmentSubmissionService.getAverageScore(assignmentId);
        // 如果暂时没有打分记录，avg 可能为 null，这里直接返回 ok(null)
        return Result.ok(avg);
    }

    /**
     * 按课程查询提交列表，并按成绩排序。
     * 接口：GET /api/assignmentSubmission/listByCourseOrderByScore?courseId=1&order=desc
     * - order 可选：asc / desc，默认 desc（高分在前）
     */
    @GetMapping("/listByCourseOrderByScore")
    public Result<List<AssignmentSubmission>> listByCourseOrderByScore(
            @RequestParam("courseId") Long courseId,
            @RequestParam(value = "order", required = false, defaultValue = "desc") String order,
            HttpSession session) {
        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限按课程查询成绩");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看成绩");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下的成绩");
            }
        }

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
     * 接口：
     * GET /api/assignmentSubmission/studentFilter?studentId=1001&submitted=true&graded=false&courseId=1&deadlineBefore=2025-01-01T23:59:59
     */
    @GetMapping("/studentFilter")
    public Result<List<StudentAssignmentSubmissionView>> studentFilter(
            @RequestParam("studentId") Long studentId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "deadlineBefore", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineBefore,
            @RequestParam(value = "submitted", required = false) Boolean submitted,
            @RequestParam(value = "graded", required = false) Boolean graded,
            HttpSession session) {

        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isStudent = "STUDENT".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (isStudent) {
            Student student = getCurrentStudent(user);
            if (student == null) {
                return Result.fail("未找到当前用户对应的学生档案，无法筛选提交记录");
            }
            if (!student.getId().equals(studentId)) {
                return Result.fail("无权查看其他学生的作业情况");
            }
        } else if (!isAdmin) {
            return Result.fail("当前用户无权限按学生筛选作业情况");
        }

        List<StudentAssignmentSubmissionView> list =
                assignmentSubmissionService.filterForStudent(studentId, courseId, deadlineBefore, submitted, graded);
        return Result.ok(list);
    }

    /**
     * 教师 / 管理员查看某个学生在指定课程下的成绩变化趋势。
     * 接口：
     * GET /api/assignmentSubmission/studentTrend?studentId=1001&courseId=1
     */
    @GetMapping("/studentTrend")
    public Result<List<StudentAssignmentSubmissionView>> studentTrend(
            @RequestParam("studentId") Long studentId,
            @RequestParam("courseId") Long courseId,
            HttpSession session) {

        User user = getLoginUser(session);
        if (user == null) {
            return Result.fail("未登录");
        }

        boolean isTeacher = "TEACHER".equalsIgnoreCase(user.getRole());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
        if (!isTeacher && !isAdmin) {
            return Result.fail("当前用户无权限查看学生成绩趋势");
        }

        Course course = courseService.getById(courseId);
        if (course == null) {
            return Result.fail("课程不存在");
        }

        if (isTeacher) {
            Teacher teacher = getCurrentTeacher(user);
            if (teacher == null) {
                return Result.fail("未找到当前用户对应的教师档案，无法查看成绩趋势");
            }
            if (!teacher.getId().equals(course.getTeacherId())) {
                return Result.fail("无权查看其他教师课程下学生的成绩趋势");
            }
        }

        List<StudentAssignmentSubmissionView> list =
                assignmentSubmissionService.filterForStudent(studentId, courseId, null, null, null);
        return Result.ok(list);
    }
}
