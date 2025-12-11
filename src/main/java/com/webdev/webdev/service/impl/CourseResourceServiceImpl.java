package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.CourseResourceMapper;
import com.webdev.webdev.model.CourseResource;
import com.webdev.webdev.service.CourseResourceService;
import org.springframework.stereotype.Service;

@Service
public class CourseResourceServiceImpl extends ServiceImpl<CourseResourceMapper, CourseResource> implements CourseResourceService {
}