package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.common.BaseContext;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.mapper.OrdersMapper;
import com.zzl.reggie.pojo.*;
import com.zzl.reggie.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/06/01  16:05
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Resource
    private UserService userService;

    @Resource
    private AddressBookService addressBookService;

    @Resource
    private ShoppingCartService shoppingCartService;

    @Resource
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void payShoppingCartByUserId(Orders orders) {
        //* 获取当前用户,和当前用户地址
        User user = userService.getById(BaseContext.getCurrentId());
        //购物车
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,user.getId());
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if(shoppingCarts==null || shoppingCarts.size()==0){
            throw new CustomException("购物车为空，下单失败");
        }
        //当前用户地址
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if(addressBook==null){
            throw new CustomException("地址为空，下单失败");
        }
        //订单号
        long orderId = IdWorker.getId();
        //总金额
        AtomicInteger amount = new AtomicInteger(0);

        //遍历购物车
        OrderDetail orderDetail=null;
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for(ShoppingCart sho:shoppingCarts){
            // 每遍历一次往OrderDetail中添加数据，并保存
            orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(sho.getNumber());
            orderDetail.setDishFlavor(sho.getDishFlavor());
            orderDetail.setDishId(sho.getDishId());
            orderDetail.setSetmealId(sho.getSetmealId());
            orderDetail.setName(sho.getName());
            orderDetail.setImage(sho.getImage());
            orderDetail.setAmount(sho.getAmount());
            amount.addAndGet(sho.getAmount().multiply(new BigDecimal(sho.getNumber())).intValue());//计算总金额
            //保存到集合中
            orderDetailList.add(orderDetail);
        }
        //将该菜品信息存储到order_detail中
        orderDetailService.saveBatch(orderDetailList);

        //封装数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(user.getId());
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(addressBook.getConsignee());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);
        //清空购物车
        shoppingCartService.remove(queryWrapper);
    }
}
