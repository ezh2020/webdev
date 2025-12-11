package com.webdev.webdev.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webdev.webdev.model.Student;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper extends BaseMapper<com.webdev.webdev.model.Student> {
}