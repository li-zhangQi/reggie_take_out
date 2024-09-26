package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/20:45
 * @Description: 套餐管理业务层实现类
 * @Notice:
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Lazy
    @Autowired
    private CategoryService categoryService;

    @Value("${reggie.path}")
    private String path;

    /**
     * 添加套餐
     * @param setmealDto
     */
    @CacheEvict(value = "setmealCache", allEntries = true)
    @Transactional
    @Override
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //添加套餐基本信息到Setmeal表
        this.save(setmealDto);

        //DTO的id就是父类的id
        Long dtoId = setmealDto.getId();

        //添加关联信息到套餐菜单关系表（通过请求体的主键获取两部分相关的数据）
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes().stream()
                .map(new Function<SetmealDish, SetmealDish>() {
                    @Override
                    public SetmealDish apply(SetmealDish setmealDish) {
                        setmealDish.setSetmealId(dtoId);
                        return setmealDish;
                    }
                }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     * 重点：Page分多少页由原实体对象决定，即被拷贝对象，但又不需要它的不完整记录；
     * 原对象的DTO增强对象为拷贝的目标对象，获取拷贝信息后，做自己的记录增强
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<SetmealDto> pageWithDish(int page, int pageSize, String name) {

        //设置俩个Page
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>(page, pageSize);

        //先用基本的Page查询得出信息
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //已获取分页信息
        super.page(setmealPage, setmealLambdaQueryWrapper);

        //分页信息拷贝，但排除分页数据记录
        BeanUtils.copyProperties(setmealPage, dtoPage, "records");

        //将setmealPage里面的记录信息增强为DTO类型
        List<SetmealDto> setmealDtoList = setmealPage.getRecords().stream().map(
                new Function<Setmeal, SetmealDto>() {
                    @Override
                    public SetmealDto apply(Setmeal setmeal) {
                        //创建一个新DTO对象
                        SetmealDto dto = new SetmealDto();
                        //表一基本信息拷贝
                        BeanUtils.copyProperties(setmeal, dto);
                        //通过分类ID设置分类信息
                        Long categoryId = setmeal.getCategoryId();
                        //此时涉及到了表二，设置前一个表不足的信息
                        Category category = categoryService.getById(categoryId);
                        if (category != null) {
                            dto.setCategoryName(category.getName());
                        }
                        return dto;
                    }
                }
        ).collect(Collectors.toList());
        //将设置好的记录信息重新封装给DTO
        dtoPage.setRecords(setmealDtoList);
        return dtoPage;
    }

    /**
     * 套餐启停售状态
     * @param status
     * @param ids
     */
    @Override
    public void updateStatus(int status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = this.list(queryWrapper).stream().map(
                new Function<Setmeal, Setmeal>() {
                    @Override
                    public Setmeal apply(Setmeal setmeal) {
                        setmeal.setStatus(status);
                        return setmeal;
                    }
                }
        ).collect(Collectors.toList());
        this.updateBatchById(setmealList);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @CacheEvict(value = "setmealCache", allEntries = true) //allEntries匹配value下的全部缓存
    @Transactional
    @Override
    public void deleteByIds(List<Long> ids) {
        //查询需要被删除的套餐信息
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(ids != null, Setmeal::getId, ids);
        //包含启售状态的套餐不能删除
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);
        if (count > 0) {
            throw new CustomException("删除的套餐列表中含有启售中的套餐，删除失败！");
        }


        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        this.list(queryWrapper).stream().forEach(
                new Consumer<Setmeal>() {
                    @Override
                    public void accept(Setmeal setmeal) {
                        //删除套餐图片
                        File file = new File(path + setmeal.getImage());
                        file.delete();
                    }
                }
        );

        //删除套餐菜品关系表信息
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

        //最终删除套餐
        this.removeByIds(ids);
    }

    /**
     * 通过套餐Id查询寻套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getWithDish(Long id) {
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(setmealDishLambdaQueryWrapper);

        setmealDto.setSetmealDishes(setmealDishList);

        return setmealDto;
    }

    /**
     * 更新套餐信息和关联的菜品关系
     * 两表更新信息
     * @param setmealDto
     */
    @CacheEvict(value = "setmealCache", allEntries = true)
    @Transactional
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //通过DTO对象获取基本数据并做保存
        this.updateById(setmealDto);

        //清空旧关联的菜品信息
        //此Id即为Setmeal的Id
        Long setmealDtoId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId, setmealDtoId);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        //重新设置菜品与套餐的关系
        List<SetmealDish> setmealDishList = setmealDishes.stream().map(
                new Function<SetmealDish, SetmealDish>() {
                    @Override
                    public SetmealDish apply(SetmealDish setmealDish) {
                        setmealDish.setSetmealId(setmealDtoId);
                        return setmealDish;
                    }
                }
        ).collect(Collectors.toList());

        //重新插入关联菜品信息
        setmealDishService.saveBatch(setmealDishList);
    }

    /**
     * 展示可售套餐信息 -- 改造加入SpringCache封装的redis缓存
     * @param categoryId
     * @param status
     * @return
     */
    @Cacheable(value = "setmealCache", key = "#categoryId + '_' + #status")
    @Override
    public List<Setmeal> list(Long categoryId, Integer status) {
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, categoryId);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = this.list(setmealLambdaQueryWrapper);
        return setmealList;
    }
}

























