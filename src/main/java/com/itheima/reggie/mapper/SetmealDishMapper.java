package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Shinsam
 * @Date: 2024/09/22/14:47
 * @Description: 套餐和菜品关系数据层
 * @Notice:
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
