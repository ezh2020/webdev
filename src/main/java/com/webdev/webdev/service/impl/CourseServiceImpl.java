package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.CourseMapper;
import com.webdev.webdev.model.Course;
import com.webdev.webdev.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
}