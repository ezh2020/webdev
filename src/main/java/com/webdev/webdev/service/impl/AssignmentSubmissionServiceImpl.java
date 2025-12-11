package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.AssignmentSubmissionMapper;
import com.webdev.webdev.model.AssignmentSubmission;
import com.webdev.webdev.service.AssignmentSubmissionService;
import org.springframework.stereotype.Service;

@Service
public class AssignmentSubmissionServiceImpl extends ServiceImpl<AssignmentSubmissionMapper, AssignmentSubmission> implements AssignmentSubmissionService {
}