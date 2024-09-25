package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.mapper.ShoppingCartMapper;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/14:10
 * @Description: 购物车管理
 * @Notice:
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    /**
     * 添加菜品和套餐到购物车
     * @param shoppingCart
     * @return
     */
    @Override
    public ShoppingCart add(ShoppingCart shoppingCart) {

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentUserId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentUserId);

        //查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //设置与用户关联的条件
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, currentUserId);
        //用Dish作为判断条件
        Long dishId = shoppingCart.getDishId();

        if (dishId != null) {
            //说明添加的是菜品，再进行菜品匹配操作
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            //说明添加的是套餐
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart cart = this.getOne(shoppingCartLambdaQueryWrapper);

        //具体查询当前菜品或者套餐是否在购物车中
        if (cart != null) {
            //如果已经存在，就在原来数量基础上加一
            cart.setNumber(cart.getNumber() + 1);
            this.updateById(cart);
        } else {
            //如果不存在，则添加到购物车，数量默认设置一
            shoppingCart.setNumber(1);
            // 后加 -- 购物车后创建时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            this.save(shoppingCart);
             cart = shoppingCart;
        }
        return cart;
    }

    /**
     * 根据用户Id展示购物车信息
     * 通过线程即可获取到当前用户Id
     * @return
     */
    @Override
    public List<ShoppingCart> listByUId() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> cartList = this.list(queryWrapper);
        return cartList;
    }

    /**
     * 根据用户Id清空购物车
     */
    @Override
    public void deleteByUId() {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        this.remove(queryWrapper);
    }
}
















