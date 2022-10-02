package com.zzl.reggie.dto;

import com.zzl.reggie.pojo.Dish;
import com.zzl.reggie.pojo.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/29  17:06
 */
@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
