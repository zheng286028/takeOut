package com.zzl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzl.reggie.dto.DishDto;
import com.zzl.reggie.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveDishWithFlavor(DishDto dishDto);

    DishDto selectDishById(Long id);

    void updateDishAndDishFlavorByDishId(DishDto dishDto);

    void deleteDishById(List<Long> ids);

    void updateDishStatusById(List<Long>  ids,int status);
}
