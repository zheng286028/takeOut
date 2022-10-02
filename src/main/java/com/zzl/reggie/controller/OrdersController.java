package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.pojo.Orders;
import com.zzl.reggie.service.OrderDetailService;
import com.zzl.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/06/01  16:06
 */
@Controller
@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Resource
    private OrdersService ordersService;

    /**
     * 支付订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public ReturnObject<String> payShoppingCartByUserId(@RequestBody Orders orders){
        log.info("订单信息为：{}",orders);
        ordersService.payShoppingCartByUserId(orders);
        return ReturnObject.success("支付成功");
    }

    @GetMapping("/page")
    public ReturnObject<Page> selectOrdersByPageAndCondition(int page, int pageSize, Long number, String beginTime, String endTime){
        log.info("分页查询");
        //分页
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number!=null,Orders::getNumber,number);
        queryWrapper.gt(beginTime!=null,Orders::getCheckoutTime,beginTime);
        queryWrapper.lt(beginTime!=null,Orders::getCheckoutTime,endTime);
        //分页条件查询
        ordersService.page(ordersPage,queryWrapper);
        return ReturnObject.success(ordersPage);
    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public ReturnObject<String> saveEditOrdersOfStatusById(@RequestBody Orders orders){
        log.info("订单信息为：{}",orders);
        //修改
        try{
            ordersService.updateById(orders);
            return ReturnObject.success("订单状态已修改");
        }catch (Exception e){
            e.printStackTrace();
            return ReturnObject.error("系统繁忙，请稍后重试");
        }
    }

}
