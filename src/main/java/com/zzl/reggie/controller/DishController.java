package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.Constants;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.dto.DishDto;
import com.zzl.reggie.pojo.Category;
import com.zzl.reggie.pojo.Dish;
import com.zzl.reggie.pojo.DishFlavor;
import com.zzl.reggie.service.CategoryService;
import com.zzl.reggie.service.DishFlavorService;
import com.zzl.reggie.service.DishService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/29  16:31
 */
@Controller
@RequestMapping("/dish")
@RestController
@Slf4j
@Api(tags = "菜品")
public class DishController {;
    @Resource
    private DishService dishService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private DishFlavorService dishFlavorService;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品和口味
     * @param dishDto
     */
    @PostMapping
    public ReturnObject<String> list(@RequestBody DishDto dishDto){
        dishService.saveDishWithFlavor(dishDto);
        return ReturnObject.success("新增菜品成功");
    }

    /**
     * 分页条件查询
     * @param name
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ReturnObject<Page> quintDishByPageAndCondition(String name, int page, int pageSize){

        //1、分页构造器
        Page<Dish> dishPage = new Page<>(page,pageSize);
        //6、通过拷贝获取dishPage的值和category的名称进行查询
        Page<DishDto> dishDtoPage = new Page<>();
        //2、条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //3、过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //4、排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //5、执行查询
        dishService.page(dishPage,queryWrapper);

        //6.1、对象拷贝,只是将总记录数和分页条件拷贝
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        //6.2、查询到的菜品数据
        List<Dish> records = dishPage.getRecords();

        List<DishDto> list = new ArrayList<>();
        DishDto dishDto=null;
        //6.3、遍历菜品数据
        for (Dish dish:records){
            dishDto = new DishDto();
            //6.9、将收集到的数据存储到list
            list.add(dishDto);
            //6.8、因为dishDto继承了Dish，不能只要一个名字，这样数据库没值会报错,所以需要将菜品拷贝到dishDto
            BeanUtils.copyProperties(dish,dishDto);
            //6.4、通过菜品对象获取分类id
            Long categoryId = dish.getCategoryId();
            //6.5、查询分类
            Category category = categoryService.getById(categoryId);
            //6.6、获取分类名称
            String categoryName = category.getName();
            //6.7、将分类赋值个体dishDto
            dishDto.setCategoryName(categoryName);
        }

        dishDtoPage.setRecords(list); //7、分页查询
        //8、响应
        return ReturnObject.success(dishDtoPage);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ReturnObject<DishDto> selectDishById(@PathVariable("id") Long id){

        DishDto dishDto = dishService.selectDishById(id);
        return ReturnObject.success(dishDto);
    }

    /**
     * 根据id修改
     * @param dishDto
     * @return
     */
    @PutMapping
    public ReturnObject<String> saveEditDishById(@RequestBody DishDto dishDto){
        //修改
        dishService.updateDishAndDishFlavorByDishId(dishDto);
        //清理缓存
        String key = "dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();
        redisTemplate.delete(key);
        return ReturnObject.success("修改成功");
    }

    /**
     * 根据id删除---批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional
    public ReturnObject<String> deleteDishById(@RequestParam List<Long> ids){
        try {
            dishService.deleteDishById(ids);
            //清理缓存,收集key,根据key删除
            Set keys = redisTemplate.keys("dish_*");
            redisTemplate.delete(keys);
            return ReturnObject.success("删除成功");
        }catch (CacheException e){
            throw new CustomException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 修改菜品的状态---批量修改
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public ReturnObject<String> updateDishStatusById(@RequestParam List<Long> ids, @PathVariable int status){
        try {
            dishService.updateDishStatusById(ids, status);
            //清空缓存,全部清除
            Set keys = redisTemplate.keys("dish_*");
            redisTemplate.delete(keys);
            return ReturnObject.success("修改成功");
        }catch (CacheException e){
            throw new CustomException("系统繁忙，请稍后重试");
        }

    }

    /**
     * 查询当前菜品的名称
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public ReturnObject<List<DishDto>> selectDishNameByCategoryId(Dish dish){
        List<DishDto> dishDtoList=null;
        //构造key
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //查询redis
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList!=null){
            //存在，直接return
            return ReturnObject.success(dishDtoList);
        }

        log.info(dish.getCategoryId().toString());
        //查询categoryId查询当前菜品的名称
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //查询状态为1的
        queryWrapper.eq(Dish::getStatus, Constants.DISH_STATUS_YES);
        //排序条件
        queryWrapper.orderByDesc(Dish::getSort).orderByAsc(Dish::getUpdateTime);
        //查询
        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoList = new ArrayList<>();
        //查询当前口味
        DishDto dishDto=null;
        for(Dish dishList:dishes){
            //查询当前菜品的分类名称
            Category category = categoryService.getById(dish.getCategoryId());
            //查询当前菜品的分类
            dishDto = new DishDto();
            //根据当前菜品id查询口味
            LambdaQueryWrapper<DishFlavor>lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishList.getId());
            List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
            //存储到DishDto,及集合
            dishDto.setCategoryName(category.getName());
            dishDto.setFlavors(list);
            //再将当前菜品也去存进去
            BeanUtils.copyProperties(dishList,dishDto);
            dishDtoList.add(dishDto);
        }
        //不存在，将查询到的数据存储到redis
        redisTemplate.opsForValue().set(key,dishDtoList,30, TimeUnit.MINUTES);
        return ReturnObject.success(dishDtoList);
    }
}
