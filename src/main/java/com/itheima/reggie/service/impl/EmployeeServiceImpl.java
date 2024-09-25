package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import com.itheima.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @Author: Shinsam
 * @Date: 2024/09/16/16:28
 * @Description: Service层实现类，继承MP的ServiceImpl父类，泛型指定Mapper实体和实体类，最后实现自制Service接口
 * @Notice: @Service注解将实现类对象交给Bean管理
 */

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService{

}
