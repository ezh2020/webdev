package com.webdev.webdev.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webdev.webdev.model.Assignment;

/**
 * 作业相关业务接口。
 */
public interface AssignmentService extends IService<Assignment> {

    /**
     * 新建作业。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String createAssignment(Assignment assignment);

    /**
     * 更新作业。
     *
     * @return null 表示成功，否则返回错误信息。
     */
    String updateAssignment(Assignment assignment);
}
