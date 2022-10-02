package com.zzl.reggie.controller;

import com.zzl.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/06/01  16:08
 */
@Controller
@Slf4j
@RestController
@RequestMapping("/ordersDetail")
public class OrderDetailController {
    @Resource
    private OrderDetailService orderDetailService;


}
