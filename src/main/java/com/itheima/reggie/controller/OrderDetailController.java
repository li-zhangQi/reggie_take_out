package com.itheima.reggie.controller;

import com.itheima.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/21:39
 * @Description: 订单管理类
 * @Notice:
 */
@Slf4j
@RestController
@RequestMapping("/")
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;
}
