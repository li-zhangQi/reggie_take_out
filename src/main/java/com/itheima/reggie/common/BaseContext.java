package com.itheima.reggie.common;


/**
 * @Author: Shinsam
 * @Date: 2024/09/19/16:32
 * @Description: 基于Threadlocal封装工具类，用于保存和获取当前用户id
 * @Notice: ThreadLocal作用域是一个线程，而一次HTTP请求就是一个线程
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 存储指定值
     * @param id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 取出存储的值
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }


}
