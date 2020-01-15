package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class UploadService {
    @Autowired
    private FastFileStorageClient client;
    //指定上传的文件类型
    private static final List<String> CONTENT_TYPES = Arrays.asList("image/jpeg", "image/gif", "image/png");

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadService.class);

    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        if(!CONTENT_TYPES.contains(contentType)){
            // 文件类型不合法，直接返回null
            LOGGER.info("文件类型不合法：{}", originalFilename);
            return null;
        }
        try {
            // 校验文件的内容
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null){
                LOGGER.info("文件内容不合法：{}", originalFilename);
                return null;
            }

            // 保存到服务器
            //file.transferTo(new File("D:\\leyou\\images\\" + originalFilename));
            String ext = StringUtils.substringAfterLast(originalFilename, ".");
            StorePath path = client.uploadFile(file.getInputStream(), file.getSize(), ext, null);
            // 生成url地址，返回
            return "http://image.leyou.com/" + path.getFullPath();
        } catch (IOException e) {
            LOGGER.info("服务器内部错误：{}", originalFilename);
            e.printStackTrace();
        }
        return null;
    }
}
