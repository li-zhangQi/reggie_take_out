package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/17:29
 * @Description: 菜品分类Service层接口
 * @Notice:
 */
public interface CategoryService extends IService<Category> {

    /**
     * 自定义的一个业务处理方法
     * @param ids
     */
    public void remove(Long ids);

}
