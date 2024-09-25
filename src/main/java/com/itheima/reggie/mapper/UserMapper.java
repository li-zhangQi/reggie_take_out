package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Shinsam
 * @Date: 2024/09/23/14:19
 * @Description:
 * @Notice:
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
