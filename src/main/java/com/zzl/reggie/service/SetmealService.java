package com.zzl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzl.reggie.dto.SetmealDto;
import com.zzl.reggie.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void insertSetmealAndSermealDish(SetmealDto setmealDto);

    void deleteSermealAndSermealDish(List<Long> ids);

    boolean saveEditSetMealOnStatusByIds(int status, Long[] ids);

    boolean saveEditSetMealAndSetMealDishBySetMealId(SetmealDto setmealDto);
}
