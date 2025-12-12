package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.CourseResource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * 课程资源相关业务接口。
 */
public interface CourseResourceService extends IService<CourseResource> {

    /**
     * 上传课程资源文件并保存数据库记录。
     */
    CourseResource uploadResource(Long courseId,
                                  Long uploaderId,
                                  String title,
                                  String description,
                                  MultipartFile file);

    /**
     * 按课程查询资源列表。
     */
    List<CourseResource> listByCourse(Long courseId);

    /**
     * 删除资源记录并尝试删除磁盘文件。
     */
    boolean deleteResource(Long id);

    /**
     * 下载前准备：根据资源 ID 返回文件路径和资源信息。
     */
    Optional<ResourceDownload> prepareDownload(Long id);

    /**
     * 课程资源下载信息，用于控制层构建响应。
     */
    class ResourceDownload {
        private final CourseResource resource;
        private final Path absolutePath;

        public ResourceDownload(CourseResource resource, Path absolutePath) {
            this.resource = resource;
            this.absolutePath = absolutePath;
        }

        public CourseResource getResource() {
            return resource;
        }

        public Path getAbsolutePath() {
            return absolutePath;
        }
    }
}
