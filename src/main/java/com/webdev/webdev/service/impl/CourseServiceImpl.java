package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.CourseSearchRequest;
import com.webdev.webdev.mapper.CourseMapper;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 课程相关业务实现：
 * - 基于 MyBatis-Plus 的通用 ServiceImpl
 * - 在此基础上增加基本的业务校验
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    /**
     * 新增课程
     * 1. 校验必填字段
     * 2. 校验课程代码唯一
     * 3. 设置合理的默认值
     */
    @Override
    public boolean addCourse(Course course) {
        if (course == null) {
            return false;
        }

        // 简单必填校验
        if (!StringUtils.hasText(course.getCourseCode())
                || !StringUtils.hasText(course.getCourseName())) {
            return false;
        }
        if (course.getCredit() == null || course.getCredit() <= 0) {
            return false;
        }
        if (course.getMaxStudents() != null && course.getMaxStudents() < 0) {
            return false;
        }

        // 学期必填校验：不允许为空
        if (!StringUtils.hasText(course.getSemester())) {
            return false;
        }

        // 不允许创建“已经过去”的学期的课程。
        // 当前学期格式约定为："2023-2024-1" 这种形式：
        //  - 第一段：学年起始年份，如 2023
        //  - 第二段：学年结束年份，如 2024
        //  - 第三段：学期序号，如 1 / 2
        try {
            String[] parts = course.getSemester().split("-");
            if (parts.length != 3) {
                return false;
            }
            int startYear = Integer.parseInt(parts[0]);
            int endYear = Integer.parseInt(parts[1]);
            int termIndex = Integer.parseInt(parts[2]); // 1 或 2

            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            int currentYear = now.getYear();
            int currentMonth = now.getMonthValue();

            // 根据当前时间推一个“当前学期”的编码：
            // 简单约定：
            //  - 每年 9 月及以后：学期 1，学年为 当前年-下一年，比如 2023-2024-1
            //  - 每年 2-7 月：学期 2，学年为 前一年-当前年，比如 2023-2024-2
            int curStartYear;
            int curEndYear;
            int curTermIndex;
            if (currentMonth >= 9) {
                curStartYear = currentYear;
                curEndYear = currentYear + 1;
                curTermIndex = 1;
            } else {
                curStartYear = currentYear - 1;
                curEndYear = currentYear;
                curTermIndex = 2;
            }

            // 把学期转换为一个可比较的数字：学年结束年份 * 10 + 学期序号
            int targetKey = endYear * 10 + termIndex;
            int currentKey = curEndYear * 10 + curTermIndex;

            if (targetKey < currentKey) {
                // 目标学期早于当前学期，视为“过去学期”，不允许创建
                return false;
            }
        } catch (Exception e) {
            // 解析失败也认为不合法
            return false;
        }

        // 课程代码唯一性校验
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getCourseCode, course.getCourseCode());
        if (this.count(wrapper) > 0) {
            // 已经存在同样的课程代码，认为新增失败
            return false;
        }

        // 默认值处理
        if (course.getCurrentStudents() == null) {
            course.setCurrentStudents(0);
        }
        if (!StringUtils.hasText(course.getStatus())) {
            course.setStatus("CLOSE"); // 默认状态：关闭选课，后续由教师/管理员手动开启
        }

        return this.save(course);
    }

    /**
     * 更新课程
     * 1. 检查课程是否存在
     * 2. 如果修改课程代码，校验唯一性
     * 3. 人数相关校验：最大人数不能小于已选人数
     */
    @Override
    public boolean updateCourse(Course course) {
        if (course == null || course.getId() == null) {
            return false;
        }

        Course dbCourse = this.getById(course.getId());
        if (dbCourse == null) {
            return false;
        }

        // 如果课程代码被修改，校验是否唯一
        if (StringUtils.hasText(course.getCourseCode())
                && !course.getCourseCode().equals(dbCourse.getCourseCode())) {
            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Course::getCourseCode, course.getCourseCode());
            if (this.count(wrapper) > 0) {
                return false;
            }
        }

        // 学分校验
        if (course.getCredit() != null && course.getCredit() <= 0) {
            return false;
        }

        // 人数校验
        if (course.getMaxStudents() != null && course.getMaxStudents() < 0) {
            return false;
        }
        Integer newMax = course.getMaxStudents() != null
                ? course.getMaxStudents()
                : dbCourse.getMaxStudents();
        Integer newCurrent = course.getCurrentStudents() != null
                ? course.getCurrentStudents()
                : dbCourse.getCurrentStudents();
        if (newMax != null && newCurrent != null && newMax < newCurrent) {
            // 不允许把最大人数设定得比当前人数还小
            return false;
        }

        return this.updateById(course);
    }

    /**
     * 删除课程（物理删除）
     * 这里增加一个简单保护：
     * - 如果课程已有人选（currentStudents > 0），则不允许删除
     *   后续你也可以改成逻辑删除：把 status 改为 CLOSED 等。
     */
    @Override
    public boolean deleteCourse(Course course) {
        if (course == null || course.getId() == null) {
            return false;
        }

        Course dbCourse = this.getById(course.getId());
        if (dbCourse == null) {
            return false;
        }

        if (dbCourse.getCurrentStudents() != null && dbCourse.getCurrentStudents() > 0) {
            // 已经有学生选了这门课，不允许直接删除
            return false;
        }

        return this.removeById(course.getId());
    }

    @Override
    public Course getCourseById(int courseId) {
        if (courseId < 0) {
            return null;
        }

        // 这里也可以直接使用父类的 getById 方法
        return this.getById(courseId);
    }

    @Override
    public java.util.List<Course> listByTeacherId(Long teacherId) {
        if (teacherId == null || teacherId <= 0) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getTeacherId, teacherId);
        return this.list(wrapper);
    }

    @Override
    public java.util.List<Course> listBySemester(String semester) {
        if (!StringUtils.hasText(semester)) {
            return java.util.Collections.emptyList();
        }
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getSemester, semester);
        return this.list(wrapper);
    }

    /**
     * 多条件组合搜索课程（时间 / 学分 / 余量）+ 排序。
     * 为了兼容不同数据库，这里“余量”和按余量排序在内存中处理。
     */
    @Override
    public List<Course> searchCourses(CourseSearchRequest request) {
        if (request == null) {
            // 没有条件时直接返回全部
            return this.list();
        }

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();

        // 学期筛选
        if (StringUtils.hasText(request.getSemester())) {
            wrapper.eq(Course::getSemester, request.getSemester());
        }

        // 创建时间区间筛选
        LocalDateTime start = request.getStartCreatedAt();
        LocalDateTime end = request.getEndCreatedAt();
        if (start != null) {
            wrapper.ge(Course::getCreatedAt, start);
        }
        if (end != null) {
            wrapper.le(Course::getCreatedAt, end);
        }

        // 学分区间筛选
        if (request.getMinCredit() != null) {
            wrapper.ge(Course::getCredit, request.getMinCredit());
        }
        if (request.getMaxCredit() != null) {
            wrapper.le(Course::getCredit, request.getMaxCredit());
        }

        // 先按时间 / 学分在数据库中做基础排序（如果请求指定）
        boolean asc = "asc".equalsIgnoreCase(request.getSortOrder());
        String sortBy = request.getSortBy();
        if ("time".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, Course::getCreatedAt);
        } else if ("credit".equalsIgnoreCase(sortBy)) {
            wrapper.orderBy(true, asc, Course::getCredit);
        }

        List<Course> list = this.list(wrapper);

        // 按余量筛选：在内存中处理，避免复杂 SQL
        if (request.getMinRemain() != null) {
            int minRemain = request.getMinRemain();
            list = list.stream()
                    .filter(c -> {
                        Integer max = c.getMaxStudents();
                        Integer cur = c.getCurrentStudents();
                        if (max == null || cur == null) {
                            return false;
                        }
                        return max - cur >= minRemain;
                    })
                    .collect(Collectors.toList());
        }

        // 如需按余量排序，或者未在数据库排序，则在内存中排序
        Comparator<Course> comparator = null;
        if ("remain".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingInt(c -> {
                Integer max = c.getMaxStudents();
                Integer cur = c.getCurrentStudents();
                if (max == null || cur == null) {
                    // 没有人数信息的课程排在最后
                    return Integer.MIN_VALUE;
                }
                return max - cur;
            });
        } else if ("time".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Course::getCreatedAt, (d1, d2) -> {
                if (Objects.equals(d1, d2)) {
                    return 0;
                }
                if (d1 == null) {
                    return -1;
                }
                if (d2 == null) {
                    return 1;
                }
                return d1.compareTo(d2);
            });
        } else if ("credit".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(c -> {
                Double credit = c.getCredit();
                return credit != null ? credit : 0.0;
            });
        }

        if (comparator != null) {
            if (!asc) {
                comparator = comparator.reversed();
            }
            list = list.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return list;
    }
}
