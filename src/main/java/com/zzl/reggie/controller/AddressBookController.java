package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.BaseContext;
import com.zzl.reggie.common.Constants;
import com.zzl.reggie.common.CustomException;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.pojo.AddressBook;
import com.zzl.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     */
    @PostMapping
    public ReturnObject<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        addressBookService.save(addressBook);
        return ReturnObject.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public ReturnObject<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        //SQL:update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        addressBook.setIsDefault(1);
        //SQL:update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return ReturnObject.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public ReturnObject get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return ReturnObject.success(addressBook);
        } else {
            return ReturnObject.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public ReturnObject<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return ReturnObject.error("没有找到该对象");
        } else {
            return ReturnObject.success(addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("/list")
    public ReturnObject<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);

        //条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != addressBook.getUserId(), AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        //SQL:select * from address_book where user_id = ? order by update_time desc
        return ReturnObject.success(addressBookService.list(queryWrapper));
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public ReturnObject<String> updateAddressBookById(@RequestBody AddressBook addressBook){
        log.info(addressBook.toString());
        try {
            //修改地址
            addressBookService.updateById(addressBook);
            return ReturnObject.success("修改成功");
        }catch (CacheException e){
            throw new CustomException("系统繁忙，请稍后重试");
        }
    }

    /**
     * 根据id删除地址
     * @param ids
     * @return
     */
    @DeleteMapping
    public ReturnObject<String> deleteAddressBookById(Long ids){
        log.info(ids.toString());
            //判断当前地址是不是默认地址
            AddressBook addressBook = addressBookService.getById(ids);
            if(addressBook.getIsDefault()== Constants.ADDRESS_BOOK_ISDEFAULT_YES){
                throw new CustomException("当前地址为默认地址，不能删除");
            }
        try {
            //根据id删除地址
            addressBookService.removeById(ids);
            return ReturnObject.success("删除成功");
        }catch (Exception e){
            e.printStackTrace();
            return ReturnObject.error("系统繁忙，请稍后重试");
        }
    }
}
