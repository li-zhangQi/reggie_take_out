package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/20:43
 * @Description:
 * @Notice:
 */
public interface SetmealService extends IService<Setmeal> {

    void saveSetmealWithDish(SetmealDto setmealDto);

    Page<SetmealDto> pageWithDish(int page, int pageSize, String name);

    void updateStatus(int status, List<Long> ids);

    void deleteByIds(List<Long> ids);

    SetmealDto getWithDish(Long id);

    void updateWithDish(SetmealDto setmealDto);

    List<Setmeal> list(Long categoryId, Integer status);
}
