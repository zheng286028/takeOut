package com.zzl.reggie.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzl.reggie.common.ReturnObject;
import com.zzl.reggie.common.adminAccount;
import com.zzl.reggie.pojo.Employee;
import com.zzl.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public ReturnObject<Employee> login(HttpServletRequest request, @RequestBody Employee employee){

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return ReturnObject.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return ReturnObject.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return ReturnObject.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp);
        return ReturnObject.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ReturnObject<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return ReturnObject.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public ReturnObject<String> save(HttpSession session, @RequestBody Employee employee){
        //判断当前用户是否可以添加
        if(adminAccount.selectIsNotAdmin(session)){
            employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

            employeeService.save(employee);

            return ReturnObject.success("新增员工成功");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public ReturnObject<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return ReturnObject.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public ReturnObject<String> update(HttpSession session, @RequestBody Employee employee){
        if(adminAccount.selectIsNotAdmin(session)) {
            employeeService.updateById(employee);

            return ReturnObject.success("员工信息修改成功");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @param session
     * @return
     */
    @GetMapping("/{id}")
    public ReturnObject<Employee> getById(@PathVariable Long id, HttpSession session){
        if(adminAccount.selectIsNotAdmin(session)) {
            Employee employee = employeeService.getById(id);
            if (employee != null) {
                return ReturnObject.success(employee);
            }
            return ReturnObject.error("没有查询到对应员工信息");
        }
        return ReturnObject.error("权限不足");
    }

    /**
     * 判断当前用户是不是管理员
     * @param session
     * @return
     */
    @GetMapping("/selectIsNotAdmin")
    public ReturnObject<String> selectIsNotAdmin(HttpSession session){
        if(adminAccount.selectIsNotAdmin(session)) {
            return ReturnObject.success("管理员你好");
        }
        return ReturnObject.error("权限不足");
    }
}
