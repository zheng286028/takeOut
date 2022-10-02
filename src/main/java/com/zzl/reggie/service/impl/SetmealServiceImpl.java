package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.common.Constants;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.dto.SetmealDto;
import com.zzl.reggie.pojo.Setmeal;
import com.zzl.reggie.mapper.SetmealMapper;
import com.zzl.reggie.pojo.SetmealDish;
import com.zzl.reggie.service.SetmealDishService;
import com.zzl.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper,Setmeal> implements SetmealService {
    @Resource
    private SetmealDishService setmealDishService;


    @Transactional
    @Override
    public void insertSetmealAndSermealDish(SetmealDto setmealDto) {
        //添加套餐
        this.save(setmealDto);
        //添加当前套餐的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for(SetmealDish dishAndSet:setmealDishes){
            dishAndSet.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除----批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteSermealAndSermealDish(List<Long> ids) {
        //判断当前套餐是否售卖
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus, Constants.DISH_STATUS_YES);
        //查询
        int count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("当前套餐正在售卖，不能删除");
        }
        //删除套餐
        this.removeByIds(ids);
        //删除套餐和菜品的关系
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        //删除
        setmealDishService.remove(queryWrapper1);
    }

    /**
     * 修改----批量修改状态
     * @param status
     * @param ids
     * @return
     */
    @Override
    public boolean saveEditSetMealOnStatusByIds(int status, Long[] ids) {
        //遍历数据
        Setmeal setmeal=null;
        for(Long id:ids){
            //封装数据
            setmeal = new Setmeal();
            setmeal.setStatus(status);
            setmeal.setId(id);
            //修改
            boolean update = this.updateById(setmeal);
        }
        return true;
    }
    /**
     * 编辑保存套餐
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public boolean saveEditSetMealAndSetMealDishBySetMealId(SetmealDto setmealDto) {
        //修改当前套餐
        this.updateById(setmealDto);

        //根据当前套餐id修改套餐和菜品
        //先删除当前套餐的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //将套餐和菜品的数据遍历出来
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish dish:setmealDishes){
            //添加
            dish.setSetmealId(setmealDto.getId());
            setmealDishService.save(dish);
        }
        return false;
    }
}
