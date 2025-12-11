package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.AssignmentMapper;
import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.service.AssignmentService;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {
}