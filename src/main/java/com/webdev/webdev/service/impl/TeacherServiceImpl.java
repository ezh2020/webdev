package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.TeacherMapper;
import com.webdev.webdev.model.Teacher;
import com.webdev.webdev.service.TeacherService;
import org.springframework.stereotype.Service;

@Service
public class TeacherServiceImpl extends ServiceImpl<com.webdev.webdev.mapper.TeacherMapper, com.webdev.webdev.model.Teacher> implements com.webdev.webdev.service.TeacherService {
}