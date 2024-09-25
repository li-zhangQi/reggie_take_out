package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.User;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Author: Shinsam
 * @Date: 2024/09/23/14:19
 * @Description:
 * @Notice:
 */
public interface UserService extends IService<User> {


    boolean sendMsg(User user, HttpSession httpSession);

    User loginUser(Map map, HttpSession httpSession);
}
