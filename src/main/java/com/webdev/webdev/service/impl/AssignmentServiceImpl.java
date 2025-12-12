package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.AssignmentMapper;
import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.service.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AssignmentServiceImpl extends ServiceImpl<AssignmentMapper, Assignment> implements AssignmentService {

    @Override
    public String createAssignment(Assignment assignment) {
        if (assignment == null) {
            return "作业信息不能为空";
        }
        if (assignment.getCourseId() == null) {
            return "courseId 不能为空";
        }
        if (!StringUtils.hasText(assignment.getTitle())) {
            return "作业标题不能为空";
        }
        if (assignment.getMaxScore() != null && assignment.getMaxScore() <= 0) {
            return "maxScore 必须为正数";
        }
        if (assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(LocalDateTime.now().minusYears(1))) {
            return "截止时间不合法";
        }

        boolean ok = this.save(assignment);
        return ok ? null : "保存作业失败";
    }

    @Override
    public String updateAssignment(Assignment assignment) {
        if (assignment == null || assignment.getId() == null) {
            return "id 不能为空";
        }

        Assignment db = this.getById(assignment.getId());
        if (db == null) {
            return "作业不存在";
        }

        if (assignment.getMaxScore() != null && assignment.getMaxScore() <= 0) {
            return "maxScore 必须为正数";
        }
        if (assignment.getDeadline() != null
                && assignment.getDeadline().isBefore(LocalDateTime.now().minusYears(1))) {
            return "截止时间不合法";
        }

        boolean ok = this.updateById(assignment);
        return ok ? null : "更新作业失败";
    }
}
