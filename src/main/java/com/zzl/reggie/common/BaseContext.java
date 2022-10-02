package com.zzl.reggie.common;

/**
 * 基于ThreadLocal封装工具类，保存当前已经登录的用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){ //将当前id存储到ThreadLocal线程变量中
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){ //获取当前线程中的变量
        return threadLocal.get();
    }
}
