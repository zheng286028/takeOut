package com.zzl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzl.reggie.pojo.User;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  10:52
 */
public interface UserService extends IService<User> {
    void sendMsg(String to,String subject,String text);
}
