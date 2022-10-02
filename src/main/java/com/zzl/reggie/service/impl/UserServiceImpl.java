package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.mapper.UserMapper;
import com.zzl.reggie.pojo.User;
import com.zzl.reggie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  10:52
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>implements UserService {
    @Autowired
    private JavaMailSender javaMailSender;
    @Value("spring.mail.username")
    private String from;
    /**
     * 发送邮箱
     * @param to
     * @param subject
     * @param text
     */
    @Override
    public void sendMsg(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        //2、发送邮件
        javaMailSender.send(message);
    }
}
