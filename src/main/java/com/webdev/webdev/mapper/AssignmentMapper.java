package com.webdev.webdev.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webdev.webdev.model.Assignment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssignmentMapper extends BaseMapper<Assignment> {
}