package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/21:35
 * @Description: 订单管理业务层
 * @Notice:
 */
@Service
public class OrderServiceImlp extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 提交购物车订单
     * 接受的请求体的数据要保存在两张表中，并清空购物车
     * 订单、订单详细|购物车表
     * @param orders
     */
    @Transactional
    @Override
    public void submit(Orders orders) {

        //获得当前用户id
        Long currentId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> cartList = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        if (cartList == null || cartList.size() == 0) {
            throw new CustomException("购物车数据为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(currentId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }
        
        //设置一个随机订单号
        long orderId = IdWorker.getId();
        //在购物车集合中计算总金额数据，并将其他数据放入订单详细表
        //原子整数类，保证线程安全
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = cartList.stream().map(new Function<ShoppingCart, OrderDetail>() {
            @Override
            public OrderDetail apply(ShoppingCart shoppingCart) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
                orderDetail.setNumber(shoppingCart.getNumber());
                orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
                orderDetail.setDishId(shoppingCart.getDishId());
                orderDetail.setSetmealId(shoppingCart.getSetmealId());
                orderDetail.setName(shoppingCart.getName());
                orderDetail.setImage(shoppingCart.getImage());
                orderDetail.setAmount(shoppingCart.getAmount());
                amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
                return orderDetail;
            }
        }).collect(Collectors.toList());


        //向订单表插入数据，一条数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);


        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }
}











