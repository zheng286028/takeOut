package com.zzl.reggie.common;

import com.zzl.reggie.pojo.Employee;

import javax.servlet.http.HttpSession;

public class adminAccount {
    /**
     * 判断当前用户是否是管理员
     * @param session
     * @return
     */
    public static Boolean selectIsNotAdmin(HttpSession session){
        Employee admin =(Employee) session.getAttribute("employee");
        if(Constants.EMPLOYEE_NAME.equals(admin.getName()) &&
                Constants.EMPLOYEE_ID==admin.getId() &&
                Constants.EMPLOYEE_USERNAME.equals(admin.getUsername())) {
            return true;
        }
        return false;
    }
}
