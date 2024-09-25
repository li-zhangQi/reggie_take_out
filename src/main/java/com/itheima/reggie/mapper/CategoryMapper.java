package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/17:27
 * @Description: 菜单实体类
 * @Notice:
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

}
