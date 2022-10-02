package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzl.reggie.common.BaseContext;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.pojo.ShoppingCart;
import com.zzl.reggie.pojo.User;
import com.zzl.reggie.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  21:19
 */
@Controller
@RequestMapping("/shoppingCart")
@Slf4j
@RestController
@Api(tags = "购物车接口")
public class ShoppingCartController {
    @Resource
    private ShoppingCartService shoppingCartService;

    /**
     * 添加菜品/套餐到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加菜品/套餐到购物车接口")
    public ReturnObject<ShoppingCart> addToShoppingCart(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        //封装当前用户id
        Long UserId = BaseContext.getCurrentId();
        shoppingCart.setUserId(UserId);
        //判断当前添加的是菜品还是套餐
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if(shoppingCart.getDishId()!=null){
            //菜品
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            queryWrapper.eq(ShoppingCart::getUserId, UserId);
            queryWrapper.eq(shoppingCart.getDishFlavor()!=null,ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        }else{
            //套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            queryWrapper.eq(ShoppingCart::getUserId, UserId);
        }
        //查询当前菜品/套餐是否存在
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        if(one!=null){
            //在原来的菜品上加一
            Integer number = one.getNumber();
            one.setNumber(number+1);
            one.setCreateTime(LocalDateTime.now());
            shoppingCartService.updateById(one);
        }else{
            //保存菜品/套餐
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            //因为状态没有值，重新赋值，响应
            one = shoppingCart;
        }
        return ReturnObject.success(one);
    }

    /**
     * 查询当前用户的购物车
     * @return
     */
    @ApiOperation(value = "查询当前用户购物车接口")
    @GetMapping("/list")
    public ReturnObject<List<ShoppingCart>> quintShoppingCartByUserId(){
        log.info("当前用户购物车信息");
        //根据当前用户查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //排序
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //查询
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return ReturnObject.success(list);
    }

    /**
     * 删除购物车中的数据
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public ReturnObject<ShoppingCart> deleteShoppingCartMiddleDishByDishIdOrSetMealId(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        if(shoppingCart.getDishId()!=null){
            //菜品，查询当前套餐是否存在
            queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            queryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
            queryWrapper.eq(shoppingCart.getDishFlavor()!=null,ShoppingCart::getDishFlavor,shoppingCart.getDishFlavor());
        }else{
            //套餐
            queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //查询
        ShoppingCart one = shoppingCartService.getOne(queryWrapper);
        Integer number = one.getNumber();
        //判断是否存在
        if(number>1){
            //存在，将number减一
            one.setNumber(number-1);
            //修改
            shoppingCartService.updateById(one);
        }else if(number==1){
            //直接删除
            shoppingCartService.removeById(one);
        }
        return ReturnObject.success(one);
    }

    /**
     * 清空当前用户购物车
     * @return
     */
    @DeleteMapping("/clean")
    public ReturnObject<String> emptyShoppingCartByUserId(){
        //添加构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //条件
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        try {
            //删除
            shoppingCartService.remove(queryWrapper);
            return ReturnObject.success("删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return ReturnObject.error("系统繁忙，请稍后重试");
        }
    }
}
