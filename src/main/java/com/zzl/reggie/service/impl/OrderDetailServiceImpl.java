package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.mapper.OrderDetailMapper;
import com.zzl.reggie.pojo.OrderDetail;
import com.zzl.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/06/01  16:04
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper,OrderDetail> implements OrderDetailService {
}
