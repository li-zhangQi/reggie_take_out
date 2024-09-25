package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.ShoppingCart;

import java.util.List;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/14:09
 * @Description:
 * @Notice:
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    //添加购物车
    ShoppingCart add(ShoppingCart shoppingCart);

    List<ShoppingCart> listByUId();

    void deleteByUId();
}
