package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.CourseSelection;

/**
 * 选课关系相关业务接口。
 */
public interface CourseSelectionService extends IService<CourseSelection> {

    /**
     * 学生选课。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String addSelection(CourseSelection selection);

    /**
     * 学生退课。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String deleteSelection(Long id);

    /**
     * 教师发布成绩。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String publishGrade(CourseSelection selection);
}
