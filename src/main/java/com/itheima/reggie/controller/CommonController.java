package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * @Author: Shinsam
 * @Date: 2024/09/20/10:26
 * @Description: 通用控制类
 * @Notice:
 */
@Slf4j
@RequestMapping("/common")
@RestController
public class CommonController {

    @Value("${reggie.path}")
    private String path;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        log.info("上传文件：{}", file.toString());

        //获取文件原始名字
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        //创建目录对象
        File dir = new File(path);
        //通过目录对象判断目录是非为空
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //转存文件
        try {
            file.transferTo(new File(path + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {

        try {
            //文件输入流，读取文件内容
            FileInputStream fileInputStream = new FileInputStream(new File(path + name));
            //输出流，将文件数据写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();

            //设置响应文件的类型
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
