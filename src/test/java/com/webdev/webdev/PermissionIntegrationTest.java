package com.webdev.webdev;

import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.model.AssignmentSubmission;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.AssignmentService;
import com.webdev.webdev.service.AssignmentSubmissionService;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.service.StudentService;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 针对课程 / 作业 / 作业提交相关权限做一组端到端集成测试，
 * 用来验证我们在 Controller 层加的权限逻辑是否与角色、session 一致。
 *
 * 注意：
 * - 测试依赖真实数据库连接（application.yml 的配置）；
 * - 建议在本地搭建一套独立测试库或开启事务回滚。
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PermissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AssignmentSubmissionService assignmentSubmissionService;

    /**
     * 创建一个简单用户实体，不通过 /api/user/register，避免干扰注册逻辑。
     */
    private User createUser(String role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUsername(role.toLowerCase() + "_" + suffix);
        // 测试中不依赖密码校验，这里直接存明文即可
        user.setPassword("test123456");
        user.setRole(role);
        user.setEmail(role.toLowerCase() + "_" + suffix + "@test.local");
        user.setPhone("000-0000");
        user.setRealName(role + "_" + suffix);
        user.setStatus("ACTIVE");
        userService.save(user);
        return user;
    }

    private Teacher createTeacherUser() {
        User user = createUser("TEACHER");
        return teacherService.createForUser(user);
    }

    private Student createStudentUser() {
        User user = createUser("STUDENT");
        return studentService.createForUser(user);
    }

    private Course createCourseForTeacher(Teacher teacher) {
        Course course = new Course();
        course.setCourseCode("CODE_" + UUID.randomUUID().toString().substring(0, 6));
        course.setCourseName("Test Course");
        course.setCredit(2.0);
        course.setDescription("Permission test course");
        course.setTeacherId(teacher.getId());
        course.setMaxStudents(50);
        course.setCurrentStudents(0);
        // 使用一个将来的学期编码，满足 CourseService 的校验
        course.setSemester("2099-2100-1");
        boolean ok = courseService.addCourse(course);
        if (!ok) {
            throw new IllegalStateException("创建测试课程失败");
        }
        return course;
    }

    private Assignment createAssignmentForCourse(Course course) {
        Assignment a = new Assignment();
        a.setCourseId(course.getId());
        a.setTitle("Test Assignment");
        a.setDescription("Permission test assignment");
        a.setMaxScore(100.0);
        a.setDeadline(LocalDateTime.now().plusDays(7));
        // 这里直接使用底层 save，避免额外业务校验干扰权限测试
        assignmentService.save(a);
        return a;
    }

    private AssignmentSubmission createSubmission(Assignment assignment, Student student) {
        AssignmentSubmission s = new AssignmentSubmission();
        s.setAssignmentId(assignment.getId());
        s.setStudentId(student.getId());
        s.setSubmissionText("TEST");
        s.setStatus("SUBMITTED");
        assignmentSubmissionService.save(s);
        return s;
    }

    @Test
    @Transactional
    @DisplayName("老师不能修改其他老师的课程信息")
    public void teacherCannotUpdateOtherTeacherCourse() throws Exception {
        Teacher t1 = createTeacherUser();
        Teacher t2 = createTeacherUser();
        Course courseOfT1 = createCourseForTeacher(t1);

        String body = "{\"id\":" + courseOfT1.getId() + ",\"courseName\":\"Hacked Name\"}";

        mockMvc.perform(
                        post("/api/course/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                                .sessionAttr(AuthConstants.SESSION_USER_ID, t2.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("无权修改")));
    }

    @Test
    @Transactional
    @DisplayName("老师只能看到自己课程列表，忽略前端传入的 teacherId")
    public void teacherListByTeacherIgnoresParam() throws Exception {
        Teacher t1 = createTeacherUser();
        Teacher t2 = createTeacherUser();
        Course c1 = createCourseForTeacher(t1);
        Course c2 = createCourseForTeacher(t2);

        // 以 t1 身份调用，但恶意传入 t2 的 id，应该仍然只看到 t1 的课程
        mockMvc.perform(
                        get("/api/course/listByTeacher")
                                .param("teacherId", String.valueOf(t2.getId()))
                                .sessionAttr(AuthConstants.SESSION_USER_ID, t1.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // 返回的数据中至少包含 t1 的课程
                .andExpect(jsonPath("$.data[0].teacherId").value(t1.getId()));
    }

    @Test
    @Transactional
    @DisplayName("老师只能为自己课程下的作业提交打分")
    public void teacherCanOnlyGradeOwnCourseSubmissions() throws Exception {
        Teacher t1 = createTeacherUser();
        Teacher t2 = createTeacherUser();
        Student s = createStudentUser();

        Course cOfT1 = createCourseForTeacher(t1);
        Course cOfT2 = createCourseForTeacher(t2);
        Assignment a1 = createAssignmentForCourse(cOfT1);
        Assignment a2 = createAssignmentForCourse(cOfT2);

        AssignmentSubmission sub1 = createSubmission(a1, s);
        AssignmentSubmission sub2 = createSubmission(a2, s);

        // t1 为自己课程的提交打分：应成功
        mockMvc.perform(
                        post("/api/assignmentSubmission/grade")
                                .param("id", String.valueOf(sub1.getId()))
                                .param("score", "90")
                                .sessionAttr(AuthConstants.SESSION_USER_ID, t1.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // t1 尝试给 t2 课程的提交打分：应被拒绝
        mockMvc.perform(
                        post("/api/assignmentSubmission/grade")
                                .param("id", String.valueOf(sub2.getId()))
                                .param("score", "80")
                                .sessionAttr(AuthConstants.SESSION_USER_ID, t1.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("无权为其他教师课程下的作业提交打分")));
    }

    @Test
    @Transactional
    @DisplayName("学生只能更新 / 删除自己的作业提交")
    public void studentCanOnlyModifyOwnSubmission() throws Exception {
        Student s1 = createStudentUser();
        Student s2 = createStudentUser();
        Teacher teacher = createTeacherUser();
        Course course = createCourseForTeacher(teacher);
        Assignment assignment = createAssignmentForCourse(course);

        AssignmentSubmission subOfS1 = createSubmission(assignment, s1);

        // s2 尝试更新 s1 的提交，应被拒绝（即使提供合法文件）
        byte[] dummy = "DUMMY".getBytes(StandardCharsets.UTF_8);
        mockMvc.perform(
                        multipart("/api/assignmentSubmission/update")
                                .file("file", dummy)
                                .param("id", String.valueOf(subOfS1.getId()))
                                .param("submissionText", "HACKED")
                                .sessionAttr(AuthConstants.SESSION_USER_ID, s2.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("无权更新其他学生的提交记录")));

        // s2 尝试删除 s1 的提交，也应被拒绝
        mockMvc.perform(
                        delete("/api/assignmentSubmission/" + subOfS1.getId())
                                .sessionAttr(AuthConstants.SESSION_USER_ID, s2.getUserId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("无权删除其他学生的提交记录")));
    }
}

