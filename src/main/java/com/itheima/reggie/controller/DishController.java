package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Author: Shinsam
 * @Date: 2024/09/20/13:07
 * @Description: 菜品管理类
 * @Notice:
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 添加菜品方法。
     * 以引入DTO对象；
     * 不需要R<DishDto>类型返回值的原因是，添加完毕数据到数据库即可，前端不需要拿着对象进行进一步的操作
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.addWithFlavor(dishDto);
        return R.success("添加菜品成功");
    }

    /**
     * 菜品分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        Page<DishDto> pageInfo = dishService.page(page, pageSize, name);
        return R.success(pageInfo);
    }

    /**
     * 根据ID菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto withFlavor = dishService.getByIdWithFlavor(id);
        return R.success(withFlavor);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> updata(@RequestBody DishDto dishDto) {
        dishService.updateByIdWithFlavor(dishDto);
        return R.success("修改菜品信息成功！");
    }

    /**
     * 设置菜品启停售状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable int status, @RequestParam List<Long> ids) {
        dishService.updateDishStatus(status, ids);
        return R.success("操作成功！");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        dishService.remove(ids);
        return R.success("删除菜品成功");
    }

    /**
     * 查询菜品信息
     * getByCId(Long categoryId) 改 getByCId(Dish dish)，
     * categoryId属性包含于Dish对象中，可以使用Dish对象直接接收更加通用
     * @param dish
     * @return
     */
    /*@GetMapping("/list")
    public R<List<Dish>> getByCId(Dish dish) {
        List<Dish> byCategoryId = dishService.getByCategoryId(dish);
        return R.success(byCategoryId);
    }*/

    /**
     * 查询菜品信息 - 改进
     * 使用Dto对象获取更多菜品相关数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getByCId(Dish dish) {
        List<DishDto> byCategoryId = dishService.getByCategoryId(dish);
        return R.success(byCategoryId);
    }
}







































