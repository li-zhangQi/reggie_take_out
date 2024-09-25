package com.itheima.reggie.dto;

/**
 * @Author: Shinsam
 * @Date: 2024/09/20/16:38
 * @Description: DTO，全称为数据传输对象，一般用于展示层(前端)与服务层（后端）之间的数据传输
 * @Notice: 当前端数据与后端实体类字段不是意义对应时后使用
 */

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
