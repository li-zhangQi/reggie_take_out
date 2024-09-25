package com.itheima;

import org.junit.jupiter.api.Test;

/**
 * @Author: Shinsam
 * @Date: 2024/09/20/11:05
 * @Description:
 * @Notice:
 */
public class test {
    @Test
    public void uploadTest() {
        String name = "lskdfjdslf.png";
        String suffix = name.substring(name.lastIndexOf("."));
        System.out.println(suffix);
    }
}
