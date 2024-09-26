package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.mapper.UserMapper;
import com.itheima.reggie.service.UserService;

import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Shinsam
 * @Date: 2024/09/23/14:20
 * @Description: 前台用户管理
 * @Notice:
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *发送手机验证码 -- 改造使用Redis缓存
     * @param user
     * @param httpSession
     */
    @Override
    public boolean sendMsg(User user, HttpSession httpSession) {

        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //测试时直接输出验证码，不用阿里云短信了
            log.info("生成的验证码为：{}", code);

            //调用阿里云提供的短信服务API完成发送短信
            //SMSUtils.sendMessage("", "", phone, string);

            //需要将生成的验证码保存到Session
            //httpSession.setAttribute(phone, code);
            //将生成的验证码缓存到Redis中，并且设置有效期为5分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);

            return true;
        }
        return false;
    }

    /**
     * 输入验证码后登录或者注册并登录 -- 改造使用Redis缓存
     * @param map
     * @param httpSession
     * @return
     */
    @Override
    public User loginUser(Map map, HttpSession httpSession) {
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        //Object codeInSession = httpSession.getAttribute(phone);
        //从Redis中获取缓存的验证码
        Object codeInSession = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对(页面提交的验证码和Session中保存的验证码比对)
        if (codeInSession != null && codeInSession.equals(code)) {
            //如果能够比对成功，说明登录成功
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = this.getOne(queryWrapper);

            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                this.save(user);
            }

            //设置Session的user值，使过滤器放行
            httpSession.setAttribute("user", user.getId());

            //如果用户登录成功，删除Redis中缓存的验证码
            redisTemplate.delete(phone);

            return user;
        }
        return null;
    }
}
