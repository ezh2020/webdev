package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.CourseSelectionMapper;
import com.webdev.webdev.model.CourseSelection;
import com.webdev.webdev.service.CourseSelectionService;
import org.springframework.stereotype.Service;

@Service
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {
}