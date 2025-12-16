package com.webdev.webdev.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webdev.webdev.mapper.CourseResourceMapper;
import com.webdev.webdev.model.CourseResource;
import com.webdev.webdev.service.CourseResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseResourceServiceImpl extends ServiceImpl<CourseResourceMapper, CourseResource> implements CourseResourceService {

    /**
     * 课程文件在服务器上的基础目录（相对项目启动目录）。
     * 可以根据需要改成绝对路径或从配置文件读取。
     */
    private static final Path BASE_DIR = Paths.get("course-files");

    // 统一的文件大小上限：50MB
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private static Path baseDirAbsolute() {
        return Paths.get("").toAbsolutePath().resolve(BASE_DIR).normalize();
    }

    @Override
    public CourseResource uploadResource(Long courseId,
                                         Long uploaderId,
                                         String title,
                                         String description,
                                         MultipartFile file) {
        if (courseId == null) {
            throw new IllegalArgumentException("courseId 不能为空");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }

        String originalName = file.getOriginalFilename();
        if (!StringUtils.hasText(originalName) || !originalName.contains(".")) {
            throw new IllegalArgumentException("文件名无效，必须包含后缀名");
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();

        // 构造存储路径：{abs}/course-files/{courseId}/{uuid}.{ext}
        // 注意：不要使用 MultipartFile#transferTo 写入相对路径，部分容器会把相对路径解析到临时目录中，导致找不到目录。
        Path targetDir = baseDirAbsolute().resolve(String.valueOf(courseId));
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new RuntimeException("创建课程文件目录失败：" + e.getMessage(), e);
        }

        String storedFileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path targetFile = targetDir.resolve(storedFileName);

        try {
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败：" + e.getMessage(), e);
        }

        String relativePath = BASE_DIR.resolve(String.valueOf(courseId)).resolve(storedFileName).toString()
                .replace("\\", "/");

        CourseResource resource = new CourseResource();
        resource.setCourseId(courseId);
        resource.setTitle(StringUtils.hasText(title) ? title : originalName);
        resource.setDescription(description);
        resource.setFilePath(relativePath);
        resource.setFileSize(file.getSize());
        resource.setUploaderId(uploaderId);
        resource.setUploadedAt(LocalDateTime.now());
        resource.setDownloadCount(0);

        this.save(resource);
        return resource;
    }

    @Override
    public List<CourseResource> listByCourse(Long courseId) {
        if (courseId == null) {
            return java.util.Collections.emptyList();
        }
        return this.list(
                new LambdaQueryWrapper<CourseResource>()
                        .eq(CourseResource::getCourseId, courseId)
                        .orderByDesc(CourseResource::getUploadedAt)
        );
    }

    @Override
    public boolean deleteResource(Long id) {
        CourseResource resource = this.getById(id);
        if (resource == null) {
            return false;
        }

        if (StringUtils.hasText(resource.getFilePath())) {
            Path filePath = resolveToBaseDir(resource.getFilePath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
                // 文件删除失败不阻断数据库删除
            }
        }

        return this.removeById(id);
    }

    @Override
    public Optional<ResourceDownload> prepareDownload(Long id) {
        CourseResource resource = this.getById(id);
        if (resource == null || !StringUtils.hasText(resource.getFilePath())) {
            return Optional.empty();
        }

        Path absolutePath = resolveToBaseDir(resource.getFilePath());
        if (!Files.exists(absolutePath)) {
            return Optional.empty();
        }

        // 更新下载次数（失败也不影响下载本身）
        try {
            resource.setDownloadCount(
                    java.util.Optional.ofNullable(resource.getDownloadCount()).orElse(0) + 1
            );
            this.updateById(resource);
        } catch (Exception ignored) {
        }

        return Optional.of(new ResourceDownload(resource, absolutePath));
    }

    private Path resolveToBaseDir(String storedPath) {
        Path baseAbs = baseDirAbsolute();
        Path stored = Paths.get(storedPath).normalize();
        Path abs = stored.isAbsolute()
                ? stored.toAbsolutePath().normalize()
                : Paths.get("").toAbsolutePath().resolve(stored).normalize();

        // 防止路径穿越：必须落在 course-files 目录下
        if (!abs.startsWith(baseAbs)) {
            return baseAbs.resolve(stored.getFileName()).normalize();
        }
        return abs;
    }
}
