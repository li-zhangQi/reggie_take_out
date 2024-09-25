package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;


/**
 * @Author: Shinsam
 * @Date: 2024/09/19/20:42
 * @Description: 菜品实现类接口
 * @Notice:
 */
public interface DishService extends IService<Dish> {

    Page<DishDto> page(int page, int pageSize, String name);

    void addWithFlavor(DishDto dishDto);

    //根据ID菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    //更新数据，原普通数据更新即可，口味数据可能会被删除，不能简单更新
    void updateByIdWithFlavor(DishDto dishDto);

    void updateDishStatus(int status, List<Long> ids);

    void remove(List<Long> ids);

    //在套餐中查询茶品新信息 - 改进
    //List<Dish> getByCategoryId(Dish dish);
    List<DishDto> getByCategoryId(Dish dish);
}










