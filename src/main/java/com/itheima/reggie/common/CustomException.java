package com.itheima.reggie.common;

/**
 * @Author: Shinsam
 * @Date: 2024/09/19/22:16
 * @Description: 自定义异常处理器类
 * @Notice: 作用是在一些方法内抛出异常，让全局异常处理器捕获和处理
 */
public class CustomException extends RuntimeException{
    /**
     * 有参构造方法，传递信息进行构造
     * @param message
     */
    public CustomException(String message) {
        super(message);
    }
}
