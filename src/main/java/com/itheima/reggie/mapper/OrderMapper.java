package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: Shinsam
 * @Date: 2024/09/24/21:32
 * @Description:
 * @Notice:
 */
@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}
