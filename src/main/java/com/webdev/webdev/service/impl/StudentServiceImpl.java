package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.StudentMapper;
import com.webdev.webdev.model.Student;
import com.webdev.webdev.service.StudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<com.webdev.webdev.mapper.StudentMapper, com.webdev.webdev.model.Student> implements com.webdev.webdev.service.StudentService {
}