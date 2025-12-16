package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.CourseSelectionMapper;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.model.CourseSelection;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.model.User;
import com.webdev.webdev.service.CourseSelectionService;
import com.webdev.webdev.service.CourseService;
import com.webdev.webdev.service.NotificationService;
import com.webdev.webdev.service.StudentService;
import com.webdev.webdev.service.TeacherService;
import com.webdev.webdev.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {

    @Autowired
    private CourseService courseService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private UserService userService;

    @Override
    public String addSelection(CourseSelection selection) {
        if (selection == null
                || selection.getStudentId() == null
                || selection.getCourseId() == null) {
            return "studentId 和 courseId 不能为空";
        }

        long count = this.count(
                new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getStudentId, selection.getStudentId())
                        .eq(CourseSelection::getCourseId, selection.getCourseId())
        );
        if (count > 0) {
            return "已经选过该课程，不能重复选课";
        }

        Course course = courseService.getById(selection.getCourseId());
        if (course == null) {
            return "课程不存在";
        }

        Integer max = course.getMaxStudents();
        Integer current = course.getCurrentStudents() == null ? 0 : course.getCurrentStudents();
        if (max != null && current >= max) {
            return "课程人数已满，无法选课";
        }

        if (selection.getSelectedAt() == null) {
            selection.setSelectedAt(LocalDateTime.now());
        }
        selection.setStatus(normalizeStatus(selection.getStatus()));

        boolean ok = this.save(selection);
        if (!ok) {
            return "保存选课记录失败";
        }

        // 更新课程当前人数（失败不影响选课本身）
        course.setCurrentStudents(current + 1);
        courseService.updateById(course);

        // 选课成功后通知相关学生和老师（失败不影响主流程）
        try {
            Student student = studentService.getById(selection.getStudentId());
            Teacher teacher = course.getTeacherId() != null
                    ? teacherService.getById(course.getTeacherId())
                    : null;

            String studentName = resolveUserRealName(
                    student != null ? student.getUserId() : null,
                    "学生"
            );
            String teacherName = resolveUserRealName(
                    teacher != null ? teacher.getUserId() : null,
                    "老师"
            );

            String title = "选课成功通知";
            String content = "学生" + studentName + "已成功选修课程《"
                    + (course.getCourseName() != null ? course.getCourseName() : "未知课程")
                    + "》，授课教师：" + teacherName + "。";

            notificationService.publishNotification(
                    course.getId(),
                    teacher != null ? teacher.getUserId() : null,
                    title,
                    content,
                    Boolean.FALSE,
                    "ENROLL"
            );
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public String deleteSelection(Long id) {
        CourseSelection selection = this.getById(id);
        if (selection == null) {
            return "选课记录不存在";
        }

        Course course = courseService.getById(selection.getCourseId());
        if (course != null) {
            Integer current = course.getCurrentStudents() == null ? 0 : course.getCurrentStudents();
            if (current > 0) {
                course.setCurrentStudents(current - 1);
                courseService.updateById(course);
            }
        }

        this.removeById(id);

        // 退课成功后通知相关学生和老师
        try {
            if (course != null) {
                Student student = studentService.getById(selection.getStudentId());
                Teacher teacher = course.getTeacherId() != null
                        ? teacherService.getById(course.getTeacherId())
                        : null;

                String studentName = resolveUserRealName(
                        student != null ? student.getUserId() : null,
                        "学生"
                );
                String teacherName = resolveUserRealName(
                        teacher != null ? teacher.getUserId() : null,
                        "老师"
                );

                String title = "退课成功通知";
                String content = "学生" + studentName + "已退选课程《"
                        + (course.getCourseName() != null ? course.getCourseName() : "未知课程")
                        + "》，授课教师：" + teacherName + "。";

                notificationService.publishNotification(
                        course.getId(),
                        teacher != null ? teacher.getUserId() : null,
                        title,
                        content,
                        Boolean.FALSE,
                        "DROP"
                );
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public String publishGrade(CourseSelection selection) {
        if (selection == null || selection.getId() == null) {
            return "id 不能为空";
        }
        if (selection.getFinalScore() == null) {
            return "finalScore 不能为空";
        }

        CourseSelection dbSelection = this.getById(selection.getId());
        if (dbSelection == null) {
            return "选课记录不存在";
        }

        dbSelection.setFinalScore(selection.getFinalScore());
        if (selection.getStatus() != null) {
            dbSelection.setStatus(normalizeStatus(selection.getStatus()));
        }

        boolean ok = this.updateById(dbSelection);
        if (!ok) {
            return "更新成绩失败";
        }

        // 成绩发布后发送通知
        try {
            Course course = courseService.getById(dbSelection.getCourseId());
            Student student = studentService.getById(dbSelection.getStudentId());
            Teacher teacher = (course != null && course.getTeacherId() != null)
                    ? teacherService.getById(course.getTeacherId())
                    : null;

            String studentName = resolveUserRealName(
                    student != null ? student.getUserId() : null,
                    "学生"
            );
            String teacherName = resolveUserRealName(
                    teacher != null ? teacher.getUserId() : null,
                    "老师"
            );

            String courseName = (course != null && course.getCourseName() != null)
                    ? course.getCourseName()
                    : "未知课程";

            String title = "成绩发布通知";
            String content = "学生" + studentName + "的课程《"
                    + courseName + "》最终成绩已发布："
                    + dbSelection.getFinalScore()
                    + "，授课教师：" + teacherName + "。";

            notificationService.publishNotification(
                    dbSelection.getCourseId(),
                    teacher != null ? teacher.getUserId() : null,
                    title,
                    content,
                    Boolean.TRUE,
                    "GRADE"
            );
        } catch (Exception ignored) {
        }

        return null;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return "SELECTED";
        }
        String s = rawStatus.trim().toUpperCase(Locale.ROOT);
        if ("SELECTED".equals(s) || "WITHDRAWN".equals(s) || "COMPLETED".equals(s)) {
            return s;
        }
        if ("ENROLLED".equals(s) || "ENROLL".equals(s)) {
            return "SELECTED";
        }
        if ("DROPPED".equals(s) || "DROP".equals(s) || "WITHDRAW".equals(s)) {
            return "WITHDRAWN";
        }
        if ("FINISHED".equals(s) || "DONE".equals(s)) {
            return "COMPLETED";
        }
        return "SELECTED";
    }

    /**
     * 从 User 中解析真实姓名，找不到时返回兜底前缀。
     */
    private String resolveUserRealName(Long userId, String fallbackPrefix) {
        if (userId == null) {
            return fallbackPrefix;
        }
        User user = userService.getById(userId);
        if (user == null) {
            return fallbackPrefix;
        }
        if (user.getRealName() != null && !user.getRealName().trim().isEmpty()) {
            return user.getRealName().trim();
        }
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername().trim();
        }
        return fallbackPrefix;
    }
}
