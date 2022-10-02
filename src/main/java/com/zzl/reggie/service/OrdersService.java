package com.zzl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzl.reggie.pojo.Orders;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/06/01  16:02
 */
public interface OrdersService extends IService<Orders> {

    void payShoppingCartByUserId(Orders orders);
}
