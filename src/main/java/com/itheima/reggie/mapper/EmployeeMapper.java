package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Shinsam
 * @Date: 2024/09/16/16:13
 * @Description: dao层接口，继承MP的BaseMapper<>接口
 * @Notice: @Mapper注释自动生成接口的代理对象，并且自动将其加载到Bean容器内
 */

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
