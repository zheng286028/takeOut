package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.dto.SetmealDto;
import com.zzl.reggie.pojo.Category;
import com.zzl.reggie.pojo.Setmeal;
import com.zzl.reggie.pojo.SetmealDish;
import com.zzl.reggie.service.CategoryService;
import com.zzl.reggie.service.SetmealDishService;
import com.zzl.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/30  15:12
 */
@Controller
@RequestMapping("/setmeal")
@RestController
@Slf4j
public class SetmealContaoller {
    @Resource
    private SetmealService setmealService;

    @Resource
    private CategoryService categoryService;
    @Resource
    private SetmealDishService setmealDishService;

    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public ReturnObject<String> addToSetmealAndSetmealDish(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        try {
            //添加
            setmealService.insertSetmealAndSermealDish(setmealDto);
            return ReturnObject.success("添加成功");
        }catch (CacheException cacheException){
            throw new CustomException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 分页条件查询
     * @param name
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ReturnObject<Page> selectSetMealByPageAndCondition(String name, int page, int pageSize){

        Page<Setmeal> setmealPage = new Page<>(page,pageSize);

        Page<SetmealDto> setmealDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //条件
        queryWrapper.eq(name != null,Setmeal::getName,name);
        //排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //查询
        setmealService.page(setmealPage,queryWrapper);

        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");
        //查询当前套餐的分类
        List<Setmeal> records = setmealPage.getRecords();

        List<SetmealDto> list = new ArrayList<>();
        SetmealDto setmealDto=null;
        for(Setmeal set:records){
            setmealDto = new SetmealDto();
            //查询分类
            Category category = categoryService.getById(set.getCategoryId());
            //分类名称添加到setmealDto
            setmealDto.setCategoryName(category.getName());

            BeanUtils.copyProperties(set,setmealDto);
            list.add(setmealDto);
        }
        //将数据给setmealDtos
        setmealDtoPage.setRecords(list);

        return ReturnObject.success(setmealDtoPage);
    }

    /**
     * 删除----批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public ReturnObject<String> deleteSetMealByIds(@RequestParam List<Long> ids){
        try {
            setmealService.deleteSermealAndSermealDish(ids);
            return ReturnObject.success("删除成功");
        }catch (CacheException e){
            throw new CustomException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 修改----批量修改状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public ReturnObject<String> saveEditSetMealOnStatusByIds(@PathVariable int status, Long[] ids){
        boolean b = setmealService.saveEditSetMealOnStatusByIds(status, ids);
        if(b){
            return ReturnObject.success("修改成功");
        }
        return ReturnObject.success("系统繁忙，请稍后重试");
    }

    /**
     * 根据id查询当前套餐
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ReturnObject<SetmealDto> selectSetMealById(@PathVariable Long id){
        log.info(id.toString());
        //查询当前套餐
        Setmeal setmeal = setmealService.getById(id);
        //查询当前套餐和菜品,根据当前套餐id查询
        LambdaQueryWrapper<SetmealDish>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        SetmealDto dto = new SetmealDto();
        //将查询到的数据存储到SetmealDto
        dto.setSetmealDishes(list);
        BeanUtils.copyProperties(setmeal,dto);
        //响应数据
        return ReturnObject.success(dto);
    }

    /**
     * 编辑保存套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "SetmealCache",key = "#setmealDto.categoryId + '_' + 1")
    public ReturnObject<String> saveEditSetMealAndSetMealDishBySetMealId(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        try {
            setmealService.saveEditSetMealAndSetMealDishBySetMealId(setmealDto);
            return ReturnObject.success("修改成功");
        }catch (Exception e){
           throw new CustomException("系统繁忙，qingshaohcs");
        }
    }

    /**
     * 根据分类id查询当前套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "SetmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status",unless = "#result==null")
    public ReturnObject<List<Setmeal>> selectSerMealByCategoryId(Setmeal setmeal){
        //查询当前套餐
        LambdaQueryWrapper<Setmeal>queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        //根据当前
        return ReturnObject.success(setmealList);
    }
}
