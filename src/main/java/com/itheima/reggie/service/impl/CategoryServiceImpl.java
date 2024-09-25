package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/17:30
 * @Description: 菜品分类Service层接口实现类
 * @Notice:
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据ids删除菜品分类，删除前需要判断器是否关联有菜品和套餐
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        //判断与菜品的存在关系，若关联则抛出一个自定义的业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, ids);
        int dishCount = dishService.count(dishLambdaQueryWrapper);

        if (dishCount > 0) {
            throw new CustomException("该菜品分类下关联了菜品，不能删除");
        }

        //判断与套餐的存在关系，若关联则抛出一个自定义的业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, ids);
        int setMealCount = setmealService.count(setmealLambdaQueryWrapper);

        if (setMealCount > 0) {
            throw new CustomException("该菜品分类下关联了套餐，不能删除");
        }

        //正常删除分类
        super.removeById(ids);

    }
}


































