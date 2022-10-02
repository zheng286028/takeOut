package com.zzl.reggie.controller;

import com.zzl.reggie.common.ReturnObject;
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
 * 功能描述
 *
 * @author 郑子浪
 * @date 2022/05/29  14:05
 */
@RequestMapping("/common")
@RestController
public class commonController {
    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public ReturnObject<String> upload(MultipartFile file){
        //获取源文件名称,并截取该文件的后缀
        String filename = file.getOriginalFilename();
        String indexName=filename.substring(filename.lastIndexOf("."));
        //判断当前目录是否存在,不存在则创建
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdir();
        }

        //创建文件名称
        String fileName=UUID.randomUUID().toString()+indexName;
        try {
            file.transferTo(new File(basePath+fileName)); //通过流的将该文件输出到指定位置
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ReturnObject.success(fileName);
    }

    /**
     * 下载文件
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void fileDownload(String name, HttpServletResponse response){
        try {
            //输入流
            FileInputStream inputStream = new FileInputStream(new File(basePath+name));
            //响应输出流
            ServletOutputStream outputStream = response.getOutputStream();
            //写文件
            int len=0;
            byte[] bytes = new byte[1024];
            while((len=inputStream.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            //关闭流
            outputStream.close();
            inputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
