package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.common.Constants;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.dto.DishDto;
import com.zzl.reggie.pojo.Dish;
import com.zzl.reggie.mapper.DishMapper;
import com.zzl.reggie.pojo.DishFlavor;
import com.zzl.reggie.pojo.SetmealDish;
import com.zzl.reggie.service.DishFlavorService;
import com.zzl.reggie.service.DishService;
import com.zzl.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {
    @Autowired
    DishFlavorService dishFlavorService;
    @Resource
    private SetmealDishService setmealDishService;
    /**
     * 添加菜品和口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveDishWithFlavor(DishDto dishDto) {
        //添加菜品
        this.save(dishDto);
        //添加当前菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for(DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishDto selectDishById(Long id) {
        //查询菜品
        Dish dish = this.getById(id);
        //查询当前菜品口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        DishDto dishDto = new DishDto();
        dishDto.setFlavors(flavors);
        //拷贝
        BeanUtils.copyProperties(dish,dishDto);
        return dishDto;
    }

    /**
     * 根据菜品id修改修改和当前菜品口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateDishAndDishFlavorByDishId(DishDto dishDto) {
        //修改菜品
        updateById(dishDto);
        //删除当前菜品的口味
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor dishFlavor:flavors){
            dishFlavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);
    }
    /**
     * 根据id删除---批量删除
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void deleteDishById(List<Long> ids) {
        //删除菜品前先判断是否在售卖
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus, Constants.DISH_STATUS_YES);
        int count = this.count(queryWrapper);
        //判断是否在售卖
        if(count>0){
            throw new CustomException("当前菜品正在售卖");
        }
        //查询当前菜品是否被套餐关联
        LambdaQueryWrapper<SetmealDish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(SetmealDish::getDishId,ids);
        int count1 = setmealDishService.count(dishLambdaQueryWrapper);
        if(count1>0){
            throw new CustomException("当前菜品正在随着套餐售卖");
        }
        //根据当前菜品id删除菜品口味
        LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(DishFlavor::getDishId,ids);
        dishFlavorService.remove(lambdaQueryWrapper);
        //删除菜品
        this.removeByIds(ids);
    }
    /**
     * 修改菜品的状态---批量修改
     * @param ids
     * @param status
     * @return
     */
    @Override
    @Transactional
    public void updateDishStatusById(List<Long> ids, int status) {
        //修改前判断当前菜品是否在随着套餐售卖
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getDishId,ids);
        int count = setmealDishService.count(queryWrapper);
        if(count>0){
            throw new CustomException("当前菜品正在随着套餐售卖");
        }
        for(Long id:ids){
            //封装数据
            Dish dish = new Dish();
            dish.setStatus(status);
            dish.setId(id);
            //修改
            this.updateById(dish);
        }
    }
}
