package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.CourseSearchRequest;
import com.webdev.webdev.model.Course;

import java.util.List;

public interface CourseService extends IService<Course> {
    boolean addCourse(Course course);

    boolean updateCourse(Course course);

    boolean deleteCourse(Course course);

    Course getCourseById(int courseId);

    /**
     * 根据教师 ID 查询该教师所授课程列表。
     */
    List<Course> listByTeacherId(Long teacherId);

    /**
     * 根据学期查询课程列表。
     * 示例：2024-FALL, 2024-SPRING 等（具体格式根据你的项目约定）。
     */
    List<Course> listBySemester(String semester);

    /**
     * 多条件组合搜索课程（时间 / 学分 / 余量）+ 排序。
     */
    List<Course> searchCourses(CourseSearchRequest request);
}
