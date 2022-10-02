package com.zzl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.common.adminAccount;
import com.zzl.reggie.pojo.Category;
import com.zzl.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @param session
     * @return
     */
    @PostMapping
    public ReturnObject<String> save(@RequestBody Category category, HttpSession session){
        //判断是否有权限
        if(adminAccount.selectIsNotAdmin(session)) {
            //添加
            log.info("category:{}", category);
            categoryService.save(category);
            return ReturnObject.success("新增分类成功");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public ReturnObject<Page> page(int page, int pageSize){
        //分页构造器
        Page<Category> pageInfo = new Page<>(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(pageInfo,queryWrapper);
        return ReturnObject.success(pageInfo);
    }

    /**
     * 根据id删除分类
     * @param id
     * @param session
     * @return
     */
    @DeleteMapping
    public ReturnObject<String> delete(Long id, HttpSession session){
        //判断是否有权限
        if(adminAccount.selectIsNotAdmin(session)) {
            //删除
            categoryService.remove(id);
            return ReturnObject.success("分类信息删除成功");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @param session
     * @return
     */
    @PutMapping
    public ReturnObject<String> update(@RequestBody Category category, HttpSession session){
        //判断是否有权限
        if(adminAccount.selectIsNotAdmin(session)) {
            //修改
            categoryService.updateById(category);
            return ReturnObject.success("修改分类信息成功");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 查询菜品分类
     * @param category
     * @return
     */
    @GetMapping("/list")
    public ReturnObject<List<Category>> selectCategoryByType(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询
        List<Category> categories = categoryService.list(queryWrapper);

        return ReturnObject.success(categories);
    }
}
