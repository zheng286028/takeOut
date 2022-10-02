package com.zzl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzl.reggie.mapper.AddressBookMapper;
import com.zzl.reggie.pojo.AddressBook;
import com.zzl.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/31  13:52
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper,AddressBook> implements AddressBookService {
}
