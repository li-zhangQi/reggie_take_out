package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/21:34
 * @Description:
 * @Notice:
 */
public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
