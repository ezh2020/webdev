package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.AssignmentSubmission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 作业提交相关业务接口。
 */
public interface AssignmentSubmissionService extends IService<AssignmentSubmission> {

    /**
     * 学生提交作业（首次提交）。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String submit(Long assignmentId,
                  Long studentId,
                  String submissionText,
                  MultipartFile file);

    /**
     * 学生更新已提交的作业（重新上传附件 / 修改文本）。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String updateSubmission(Long id,
                            String submissionText,
                            MultipartFile file);

    /**
     * 删除提交记录，同时尝试删除磁盘上的附件。
     */
    boolean deleteWithFile(Long id);

    /**
     * 按作业查询提交列表。
     */
    List<AssignmentSubmission> listByAssignment(Long assignmentId);

    /**
     * 按学生查询提交列表。
     */
    List<AssignmentSubmission> listByStudent(Long studentId);

    /**
     * 按课程查询提交列表（通过作业关联 courseId）。
     */
    List<AssignmentSubmission> listByCourse(Long courseId);

    /**
     * 同一作业的平均分（只统计已打分的提交）。
     */
    Double getAverageScore(Long assignmentId);

    /**
     * 按课程查询提交列表，并按成绩排序（只统计已打分记录）。
     *
     * @param asc 为 true 时升序，false 时降序
     */
    List<AssignmentSubmission> listByCourseOrderByScore(Long courseId, boolean asc);

    /**
     * 学生按条件筛选作业提交情况：
     * - submitted: true=只看已提交，false=只看未提交，null=忽略
     * - graded: true=只看已打分，false=只看未打分，null=忽略
     * - courseId: 限定课程（可选）
     * - deadlineBefore: 截止时间早于该时间的作业（可选）
     *
     * 返回结果中会包含作业信息和该学生的提交状态。
     */
    List<StudentAssignmentSubmissionView> filterForStudent(Long studentId,
                                                           Long courseId,
                                                           LocalDateTime deadlineBefore,
                                                           Boolean submitted,
                                                           Boolean graded);

    /**
     * 学生作业+提交视图。
     */
    class StudentAssignmentSubmissionView {
        private Long assignmentId;
        private String assignmentTitle;
        private Long courseId;
        private LocalDateTime deadline;

        private Long submissionId;
        private LocalDateTime submittedAt;
        private Double score;
        private String status;
        private Boolean submitted;
        private Boolean graded;

        public Long getAssignmentId() {
            return assignmentId;
        }

        public void setAssignmentId(Long assignmentId) {
            this.assignmentId = assignmentId;
        }

        public String getAssignmentTitle() {
            return assignmentTitle;
        }

        public void setAssignmentTitle(String assignmentTitle) {
            this.assignmentTitle = assignmentTitle;
        }

        public Long getCourseId() {
            return courseId;
        }

        public void setCourseId(Long courseId) {
            this.courseId = courseId;
        }

        public LocalDateTime getDeadline() {
            return deadline;
        }

        public void setDeadline(LocalDateTime deadline) {
            this.deadline = deadline;
        }

        public Long getSubmissionId() {
            return submissionId;
        }

        public void setSubmissionId(Long submissionId) {
            this.submissionId = submissionId;
        }

        public LocalDateTime getSubmittedAt() {
            return submittedAt;
        }

        public void setSubmittedAt(LocalDateTime submittedAt) {
            this.submittedAt = submittedAt;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Boolean getSubmitted() {
            return submitted;
        }

        public void setSubmitted(Boolean submitted) {
            this.submitted = submitted;
        }

        public Boolean getGraded() {
            return graded;
        }

        public void setGraded(Boolean graded) {
            this.graded = graded;
        }
    }
}
