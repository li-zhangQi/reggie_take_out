package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/20:44
 * @Description: 菜品管理业务层实现类
 * @Notice:
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Lazy
    @Autowired
    private CategoryService categoryService;

    @Value("${reggie.path}")
    private String path;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 菜品和口味数据分别插入两张表 -- 改造加入redis缓存
     * @param dishDto
     */
    @Transactional
    @Override
    public void addWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //获取用来设置口味对应的自动生成的菜品Id值
        Long dishId = dishDto.getId();

        //使用Stream流处理每一个口味关联到指定菜品
        List<DishFlavor> flavorList = dishDto.getFlavors();
        List<DishFlavor> flavors = flavorList.stream()
                .map(dishFlavor -> {
                    dishFlavor.setDishId(dishId);
                    return dishFlavor;
                }).collect(Collectors.toList());

        //全部删除redis中的菜品信息
        //Set keys = redisTemplate.keys("Dish_*");
        //redisTemplate.delete(keys);

        //精准删除redis中的数据
        String key = "Dish_" +dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        //批量保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 菜品分页查询
     * 同时查询两张表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @Override
    public Page<DishDto> page(int page, int pageSize, String name) {

        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByAsc(Dish::getCreateTime);
        //dishPage的Page对象已查询出来
        super.page(dishPage, queryWrapper);

        //进行Page对象的属性拷贝，把dishPage除records属性的所有信息拷贝到dishDtoPage
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        //再手动对records信息进行加工
        List<Dish> dishPageRecords = dishPage.getRecords();
        List<DishDto> dishDtoList = dishPageRecords.stream().map(new Function<Dish, DishDto>() {
            @Override
            public DishDto apply(Dish dish) {
                //创建一个实体对象用以拷贝属性
                DishDto dishDto = new DishDto();
                //属性拷贝
                BeanUtils.copyProperties(dish, dishDto);
                //根据ID查询菜品分类对象
                Long categoryId = dish.getCategoryId();
                Category category = categoryService.getById(categoryId);
                //避免出现空指针异常
                if (category != null) {
                    dishDto.setCategoryName(category.getName());
                }
                return dishDto;
            }
        }).collect(Collectors.toList());
        //设置加工好的records信息
        dishDtoPage.setRecords(dishDtoList);

        return dishDtoPage;
    }

    /**
     * 菜品信息回显
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询Dish表的信息
        Dish dish = this.getById(id);

        //查询Dish_Flavor表信息
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorLambdaQueryWrapper);


        //使用DishDto对象传输。把相同信息多的直接对象拷贝，把另一张表的少部分数据直接赋值给新的目标对象
        DishDto dishDto = new DishDto();
        //拷贝
        BeanUtils.copyProperties(dish, dishDto);
        //赋值即可
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    /**
     * 更改菜品信息 -- 改造加入redis缓存
     * @param dishDto
     */
    @Transactional
    @Override
    public void updateByIdWithFlavor(DishDto dishDto) {
        //更新菜品基本信息
        this.updateById(dishDto);

        //依据菜品ID先全部清除对应的旧口味信息
        LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(flavorLambdaQueryWrapper);

        //重新新增带有菜品ID的新口味信息
        List<DishFlavor> flavors = dishDto.getFlavors().stream().map(dishFlavor -> {
            dishFlavor.setDishId(dishDto.getId());
            return dishFlavor;
        }).collect(Collectors.toList());

        //精准删除redis中的数据
        String key = "Dish_" +dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        //批量保存设置好的口味信息
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 设置菜品的售卖状态
     * @param status
     * @param ids
     */
    @Override
    public void updateDishStatus(int status, List<Long> ids) {

        //依据ids查询多个Dish对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null, Dish::getId, ids);
        List<Dish> list = this.list(queryWrapper);
        //！！！！！！！！！！！！！！！！！！！！！老是忘记MP的list方法！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        List<Dish> dishList = list.stream().map(dish -> {
            dish.setStatus(status);
            return dish;
        }).collect(Collectors.toList());
        this.updateBatchById(dishList);
    }

    /**
     * 菜品删除
     * @param ids
     */
    @Transactional
    @Override
    public void remove(List<Long> ids) {
        //根据Id获取菜品
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        //启售状态不能删除，异常抛出提示
        queryWrapper.eq(Dish::getStatus, "1");
        int count = this.count(queryWrapper);
        if (count >0) {
            throw new CustomException("删除列表中有菜品为启售状态，删除失败！");
        }

        //能删除的菜品集合
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(Dish::getId, ids);

        //连带通过文件对象把删除图片
        List<Dish> dishList = this.list(queryWrapper1);
        dishList.forEach(new Consumer<Dish>() {
            @Override
            public void accept(Dish dish) {
                File file = new File(path + dish.getImage());
                file.delete();
            }
        });

        //连带删除口味
        LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        flavorLambdaQueryWrapper.in(DishFlavor::getDishId, ids);
        dishFlavorService.remove(flavorLambdaQueryWrapper);

        //最终删除本菜品
        this.removeByIds(ids);
    }

    /**
     * 根据分类Id查询菜品 - 改进 - 再改进加入Redis缓存
     * @param dish
     */
    /*@Override
    public List<Dish> getByCategoryId(Dish dish) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());

        //只列出状态为启售的菜品即1，此时体现了形参用实体对象替代接受单个属性的好处
        queryWrapper.eq(Dish::getStatus, 1);

        queryWrapper.like(dish.getName() != null, Dish::getName, dish.getName());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = this.list(queryWrapper);
        return dishList;
    }*/
    @Override
    public List<DishDto> getByCategoryId(Dish dish) {

        List<DishDto> dishDtosWithFlavor = null;

        //尝试先从redis中查询数据
        //动态构造一个key值
        String key = "Dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        dishDtosWithFlavor = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtosWithFlavor != null) {
            //如果存在，直接返回，无需查询数据库
            return dishDtosWithFlavor;
        }

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        dishLambdaQueryWrapper.eq(dish.getStatus() != null, Dish::getStatus, 1);
        List<Dish> dishList = this.list(dishLambdaQueryWrapper);

        List<DishDto> dishDtoList = dishList.stream().map(new Function<Dish, DishDto>() {
            @Override
            public DishDto apply(Dish dish) {
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(dish, dishDto);
                return dishDto;
            }
        }).collect(Collectors.toList());

        dishDtosWithFlavor = dishDtoList.stream().map(new Function<DishDto, DishDto>() {
            @Override
            public DishDto apply(DishDto dishDto) {
                LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
                flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
                List<DishFlavor> flavors = dishFlavorService.list(flavorLambdaQueryWrapper);
                dishDto.setFlavors(flavors);
                return dishDto;
            }
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtosWithFlavor, 60, TimeUnit.MINUTES);

        return dishDtosWithFlavor;
    }
}














