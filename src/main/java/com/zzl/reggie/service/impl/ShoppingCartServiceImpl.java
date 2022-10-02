package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.mapper.ShoppingCartMapper;
import com.zzl.reggie.pojo.ShoppingCart;
import com.zzl.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  21:19
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper,ShoppingCart> implements ShoppingCartService {
}
