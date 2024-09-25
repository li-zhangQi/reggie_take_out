package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/17:33
 * @Description: 菜品分类管理类
 * @Notice:
 */

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryContreller {

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分类分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        Page<Category> pageInfo = new Page(page, pageSize);

        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);

        categoryService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 更改菜品信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("更改菜品分类成功");
    }

    /**
     * 新增菜品分类
     * @param category
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("添加菜品分类成功");
    }

    /**
     * 菜品分类删除方法。
     * 接传递ID参数给 removeById 方法，则可以跳过解析对象的步骤，直接使用ID进行删除操作；
     * 开始把业务处理逻辑代码放入到Service层中，使用自定义的remove业务处理方法
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {

//        categoryService.removeById(ids);

        categoryService.remove(ids);

        return R.success("删除菜品成功");
    }

    /**
     * 依据条件查询分类数据
     * 参数为实体对象的简单参数请求传递
     * (原版全写在Controller，可改造为Service执行业务逻辑)
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}

















