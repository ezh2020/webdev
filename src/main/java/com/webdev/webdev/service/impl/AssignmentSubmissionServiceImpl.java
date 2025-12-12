package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.AssignmentSubmissionMapper;
import com.webdev.webdev.model.Assignment;
import com.webdev.webdev.model.AssignmentSubmission;
import com.webdev.webdev.service.AssignmentService;
import com.webdev.webdev.service.AssignmentSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AssignmentSubmissionServiceImpl extends ServiceImpl<AssignmentSubmissionMapper, AssignmentSubmission>
        implements AssignmentSubmissionService {

    /**
     * 作业提交附件基础目录。
     */
    private static final Path BASE_DIR = Paths.get("submission-files");

    // 附件大小上限：50MB
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    @Autowired
    private AssignmentService assignmentService;

    @Override
    public String submit(Long assignmentId,
                         Long studentId,
                         String submissionText,
                         MultipartFile file) {
        if (assignmentId == null) {
            return "assignmentId 不能为空";
        }
        if (studentId == null) {
            return "studentId 不能为空";
        }
        if (file == null || file.isEmpty()) {
            return "提交附件不能为空";
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return "附件大小不能超过 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB";
        }

        Assignment assignment = assignmentService.getById(assignmentId);
        if (assignment == null) {
            return "作业不存在";
        }

        String storedPath;
        try {
            storedPath = storeFile(assignmentId, studentId, file);
        } catch (RuntimeException e) {
            return e.getMessage();
        }

        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignmentId(assignmentId);
        submission.setStudentId(studentId);
        submission.setSubmissionText(submissionText);
        submission.setAttachmentPath(storedPath);
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus("SUBMITTED");

        boolean ok = this.save(submission);
        return ok ? null : "保存作业提交失败";
    }

    @Override
    public String updateSubmission(Long id,
                                   String submissionText,
                                   MultipartFile file) {
        if (id == null) {
            return "id 不能为空";
        }
        if (file == null || file.isEmpty()) {
            return "更新时必须提供新的附件";
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return "附件大小不能超过 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB";
        }

        AssignmentSubmission submission = this.getById(id);
        if (submission == null) {
            return "提交记录不存在";
        }

        // 删除旧文件
        if (StringUtils.hasText(submission.getAttachmentPath())) {
            Path oldPath = resolveToBaseDir(submission.getAttachmentPath());
            try {
                Files.deleteIfExists(oldPath);
            } catch (IOException ignored) {
            }
        }

        String storedPath;
        try {
            storedPath = storeFile(submission.getAssignmentId(), submission.getStudentId(), file);
        } catch (RuntimeException e) {
            return e.getMessage();
        }

        if (submissionText != null) {
            submission.setSubmissionText(submissionText);
        }
        submission.setAttachmentPath(storedPath);
        submission.setSubmittedAt(LocalDateTime.now());

        boolean ok = this.updateById(submission);
        return ok ? null : "更新作业提交失败";
    }

    @Override
    public boolean deleteWithFile(Long id) {
        AssignmentSubmission submission = this.getById(id);
        if (submission == null) {
            return false;
        }

        if (StringUtils.hasText(submission.getAttachmentPath())) {
            Path path = resolveToBaseDir(submission.getAttachmentPath());
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
            }
        }

        return this.removeById(id);
    }

    @Override
    public List<AssignmentSubmission> listByAssignment(Long assignmentId) {
        if (assignmentId == null) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                        .orderByDesc(AssignmentSubmission::getSubmittedAt)
        );
    }

    @Override
    public List<AssignmentSubmission> listByStudent(Long studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getStudentId, studentId)
                        .orderByDesc(AssignmentSubmission::getSubmittedAt)
        );
    }

    @Override
    public List<AssignmentSubmission> listByCourse(Long courseId) {
        if (courseId == null) {
            return Collections.emptyList();
        }

        List<Assignment> assignments = assignmentService.list(
                new LambdaQueryWrapper<Assignment>()
                        .eq(Assignment::getCourseId, courseId)
        );
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> ids = assignments.stream()
                .map(Assignment::getId)
                .collect(Collectors.toList());

        return this.list(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .in(AssignmentSubmission::getAssignmentId, ids)
                        .orderByDesc(AssignmentSubmission::getSubmittedAt)
        );
    }

    @Override
    public Double getAverageScore(Long assignmentId) {
        if (assignmentId == null) {
            return null;
        }
        List<AssignmentSubmission> list = this.list(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                        .isNotNull(AssignmentSubmission::getScore)
        );
        if (list == null || list.isEmpty()) {
            return null;
        }
        double sum = 0.0;
        int count = 0;
        for (AssignmentSubmission s : list) {
            if (s.getScore() != null) {
                sum += s.getScore();
                count++;
            }
        }
        return count == 0 ? null : sum / count;
    }

    @Override
    public List<AssignmentSubmission> listByCourseOrderByScore(Long courseId, boolean asc) {
        List<AssignmentSubmission> list = listByCourse(courseId);
        if (list.isEmpty()) {
            return list;
        }
        // 只对已打分记录排序，未打分的放在最后
        Comparator<AssignmentSubmission> comparator = Comparator.comparing(
                s -> s.getScore() == null ? Double.NEGATIVE_INFINITY : s.getScore()
        );
        if (!asc) {
            comparator = comparator.reversed();
        }
        return list.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentAssignmentSubmissionView> filterForStudent(Long studentId,
                                                                  Long courseId,
                                                                  LocalDateTime deadlineBefore,
                                                                  Boolean submitted,
                                                                  Boolean graded) {
        if (studentId == null) {
            return Collections.emptyList();
        }

        // 先筛选出符合条件的作业
        LambdaQueryWrapper<Assignment> assignmentWrapper = new LambdaQueryWrapper<>();
        assignmentWrapper.eq(courseId != null, Assignment::getCourseId, courseId);
        if (deadlineBefore != null) {
            assignmentWrapper.le(Assignment::getDeadline, deadlineBefore);
        }
        List<Assignment> assignments = assignmentService.list(assignmentWrapper);
        if (assignments == null || assignments.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> assignmentIds = assignments.stream()
                .map(Assignment::getId)
                .collect(Collectors.toList());

        // 查询该学生在这些作业下的所有提交
        List<AssignmentSubmission> submissions = this.list(
                new LambdaQueryWrapper<AssignmentSubmission>()
                        .eq(AssignmentSubmission::getStudentId, studentId)
                        .in(AssignmentSubmission::getAssignmentId, assignmentIds)
        );
        java.util.Map<Long, AssignmentSubmission> submissionMap = submissions.stream()
                .collect(Collectors.toMap(AssignmentSubmission::getAssignmentId, s -> s, (a, b) -> b));

        List<StudentAssignmentSubmissionView> result = new java.util.ArrayList<>();
        for (Assignment assignment : assignments) {
            AssignmentSubmission sub = submissionMap.get(assignment.getId());
            boolean hasSubmission = (sub != null);
            boolean isGraded = hasSubmission && sub.getScore() != null;

            // 提交状态筛选
            if (submitted != null) {
                if (submitted && !hasSubmission) {
                    continue;
                }
                if (!submitted && hasSubmission) {
                    continue;
                }
            }

            // 打分状态筛选（仅对已提交作业起作用）
            if (graded != null) {
                if (!hasSubmission) {
                    // 未提交时，如果要求 graded/ungraded，直接过滤掉
                    continue;
                }
                if (graded && !isGraded) {
                    continue;
                }
                if (!graded && isGraded) {
                    continue;
                }
            }

            StudentAssignmentSubmissionView view = new StudentAssignmentSubmissionView();
            view.setAssignmentId(assignment.getId());
            view.setAssignmentTitle(assignment.getTitle());
            view.setCourseId(assignment.getCourseId());
            view.setDeadline(assignment.getDeadline());

            if (sub != null) {
                view.setSubmissionId(sub.getId());
                view.setSubmittedAt(sub.getSubmittedAt());
                view.setScore(sub.getScore());
                view.setStatus(sub.getStatus());
                view.setSubmitted(true);
                view.setGraded(sub.getScore() != null);
            } else {
                view.setSubmitted(false);
                view.setGraded(false);
            }

            result.add(view);
        }

        // 可以按截止时间或课程排序，这里按截止时间升序
        result.sort(Comparator.comparing(StudentAssignmentSubmissionView::getDeadline,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return result;
    }

    private String storeFile(Long assignmentId, Long studentId, MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            throw new RuntimeException("文件名无效，必须包含后缀名");
        }
        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();

        Path targetDir = BASE_DIR
                .resolve(String.valueOf(assignmentId))
                .resolve(String.valueOf(studentId));
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("创建提交文件目录失败：" + e.getMessage(), e);
        }

        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path targetFile = targetDir.resolve(storedFileName);

        try {
            file.transferTo(targetFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException("保存提交附件失败：" + e.getMessage(), e);
        }

        // 相对路径存入数据库
        return BASE_DIR.getFileName()
                + "/" + assignmentId
                + "/" + studentId
                + "/" + storedFileName;
    }

    private Path resolveToBaseDir(String storedPath) {
        Path path = Paths.get(storedPath);
        if (!path.isAbsolute()) {
            path = BASE_DIR.getParent() != null
                    ? BASE_DIR.getParent().resolve(path)
                    : Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }
}
