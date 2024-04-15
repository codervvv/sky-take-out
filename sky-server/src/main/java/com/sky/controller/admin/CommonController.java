package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("上传文件：{}",file);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString()+extension;
        String filePath = null;
        try {
            filePath = "D:\\86181\\Documents\\project\\sky_delivery\\image\\"+objectName;
            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(file.getBytes());
            fileOutputStream.close();
            String imageUrl = "http://127.0.0.1:8080/images/" + objectName;
            return Result.success(imageUrl);
        } catch (IOException e) {
            log.error("文件上传失败");
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
