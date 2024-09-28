package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @Author: Shinsam
 * @Date: 2024/09/23/14:21
 * @Description: 用户管理类
 * @Notice:
 */
@Api(tags = "后台用户管理接口")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * HttpSession：用于获取和操作用户的会话，由SpringMVC自动注入
     * @param user
     * @return
     */
    @ApiOperation(value = "发送手机验证码接口")
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession) {

        return userService.sendMsg(user, httpSession) ? R.success("短信发送成功") : R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     * Json类型请求参数与实体类字段对不上时，可以考虑一：用DTO对象拓展，二：用Map集合存储
     * 但二者都需要用@RequestBody解析
     * @param map
     * @param httpSession
     * @return
     */
    @ApiOperation(value = "移动端用户登录接口")
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession httpSession) {
        User user = userService.loginUser(map, httpSession);
        return user != null ? R.success(user) : R.error("登录失败");
    }

}


























