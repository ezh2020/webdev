package com.webdev.webdev;

import com.webdev.webdev.model.Course;
import com.webdev.webdev.service.CourseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * 针对 CourseService 的基础业务规则做一组单元测试，
 * 用来验证课程新增 / 更新时的一些关键校验逻辑是否与实现一致。
 *
 * 注意：测试依赖真实数据库连接（沿用现有集成测试的配置），
 * 并通过 @Transactional 在用例结束后回滚数据。
 */
@SpringBootTest
@Transactional
public class CourseServiceTest {

    @Autowired
    private CourseService courseService;

    /**
     * 构造一个字段完整、学期在未来的课程对象，便于在不同测试场景中复用。
     */
    private Course buildValidFutureCourse(String code) {
        Course course = new Course();
        course.setCourseCode(code);
        course.setCourseName("Test " + code);
        course.setCredit(2.0);
        course.setDescription("Course service test");
        course.setTeacherId(1L);
        course.setMaxStudents(50);
        course.setCurrentStudents(0);
        // 使用一个远未来学期，确保通过 CourseServiceImpl 中的时间校验
        course.setSemester("9999-10000-1");
        course.setStatus("OPEN");
        return course;
    }

    @Test
    public void addCourseRejectsPastSemester() {
        Course course = buildValidFutureCourse("PAST_SEMESTER_CODE");
        // 设置为明显已经过去的学期，预期被拒绝
        course.setSemester("2000-2001-1");

        boolean ok = courseService.addCourse(course);
        Assertions.assertFalse(ok, "过去学期的课程不应该被允许创建");
    }

    @Test
    public void addCourseEnforcesUniqueCourseCode() {
        String code = "UNIQUE_CODE_TEST";

        Course first = buildValidFutureCourse(code);
        Course second = buildValidFutureCourse(code);

        boolean firstResult = courseService.addCourse(first);
        boolean secondResult = courseService.addCourse(second);

        Assertions.assertTrue(firstResult, "第一次新增课程应当成功");
        Assertions.assertFalse(secondResult, "同一 courseCode 不应允许重复创建课程");
    }

    @Test
    public void updateCourseRejectsMaxStudentsLessThanCurrent() {
        Course course = buildValidFutureCourse("CAPACITY_TEST_CODE");
        course.setMaxStudents(10);
        course.setCurrentStudents(5);

        boolean created = courseService.addCourse(course);
        Assertions.assertTrue(created, "前置课程创建应成功，以便验证更新逻辑");
        Assertions.assertNotNull(course.getId(), "新增课程后应写回主键 ID");

        Course toUpdate = new Course();
        toUpdate.setId(course.getId());
        // 将最大人数修改为小于已选人数，应该被业务规则拦截
        toUpdate.setMaxStudents(4);

        boolean updated = courseService.updateCourse(toUpdate);
        Assertions.assertFalse(updated, "最大人数小于当前选课人数时不应允许更新课程");
    }
}

