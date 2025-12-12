package com.webdev.webdev.controller;

import com.webdev.webdev.Result;
import com.webdev.webdev.model.CourseResource;
import com.webdev.webdev.service.CourseResourceService;
import com.webdev.webdev.service.CourseResourceService.ResourceDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 课程资源接口：
 * - 上传课程资源（封面图 / 作业文件 / 文档 / 视频）
 * - 按课程查询资源列表
 * - 删除资源（包含磁盘文件）
 * - 下载资源文件
 *
 * 说明：
 * - 对上传文件做后缀白名单校验与大小限制；
 * - 按课程 + 资源类别分子目录存储，并使用随机文件名避免冲突；
 * - 数据表中 filePath 存储的是相对路径，便于迁移与部署。
 */
@RestController
@RequestMapping("/api/courseResource")
@CrossOrigin(origins = "*")
public class CourseResourceController {

    @Autowired
    private CourseResourceService courseResourceService;

    /**
     * 上传课程资源文件。
     *
     * 示例：POST /api/courseResource/upload  (multipart/form-data)
     * form-data:
     * - courseId: 1
     * - uploaderId: 1001
     * - title: "第1章课件"
     * - description: "PDF 版"
     * - file: <二进制文件>
     */
    @PostMapping("/upload")
    public Result<CourseResource> upload(@RequestParam("courseId") Long courseId,
                                         @RequestParam(value = "uploaderId", required = false) Long uploaderId,
                                         @RequestParam(value = "title", required = false) String title,
                                         @RequestParam(value = "description", required = false) String description,
                                         @RequestParam("file") MultipartFile file) {
        try {
            CourseResource resource = courseResourceService.uploadResource(
                    courseId,
                    uploaderId,
                    title,
                    description,
                    file
            );
            return Result.ok(resource);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (RuntimeException e) {
            return Result.fail("上传课程资源失败：" + e.getMessage());
        }
    }

    /**
     * 按课程查询课程资源列表。
     * 示例：GET /api/courseResource/listByCourse?courseId=1
     */
    @GetMapping("/listByCourse")
    public Result<List<CourseResource>> listByCourse(@RequestParam("courseId") Long courseId) {
        if (courseId == null) {
            return Result.fail("courseId 不能为空");
        }
        List<CourseResource> list = courseResourceService.listByCourse(courseId);
        return Result.ok(list);
    }

    /**
     * 删除课程资源（同时尝试删除磁盘上的文件）。
     * 示例：DELETE /api/courseResource/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean ok = courseResourceService.deleteResource(id);
        if (!ok) {
            return Result.fail("资源不存在或删除失败");
        }
        return Result.ok(null);
    }

    /**
     * 下载课程资源文件。
     * 示例：GET /api/courseResource/download/{id}
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> download(@PathVariable("id") Long id) {
        Optional<ResourceDownload> opt = courseResourceService.prepareDownload(id);
        if (!opt.isPresent()) {
            return ResponseEntity.badRequest().body("文件不存在");
        }

        ResourceDownload download = opt.get();
        Path filePath = download.getAbsolutePath();

        try {
            String fileName = filePath.getFileName().toString();
            InputStreamResource body = new InputStreamResource(Files.newInputStream(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(body);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("读取文件失败：" + e.getMessage());
        }
    }

}
