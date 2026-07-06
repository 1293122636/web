package com.library.controller;
import com.library.exception.AppException;        // 自定义异常
import jakarta.servlet.http.HttpServletRequest;   // HTTP 请求对象
import org.slf4j.Logger;                          // 日志（记录运行信息）
import org.slf4j.LoggerFactory;                   // 日志工厂
import org.springframework.beans.factory.annotation.Value;  // 读取配置
import org.springframework.http.ResponseEntity;   // HTTP 响应
import org.springframework.web.bind.annotation.*; // Web 注解
import org.springframework.web.multipart.MultipartFile; // 文件上传
import java.io.IOException;                       // IO 异常
import java.nio.file.Files;                       // 文件操作
import java.nio.file.Path;                        // 文件路径
import java.nio.file.Paths;                       // 路径工具
import java.util.Map;                             // Map 集合
import java.util.Set;                             // Set 集合
import java.util.UUID;                            // 生成唯一 ID
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg","image/png","image/webp");
    private static final long MAX_SIZE = 5*1024*1024;
    private final Path uploadDir =Paths.get("src/main/resources/static/covers");
    @PostMapping("/cover")
    public ResponseEntity<?> uploadCover(@RequestParam("file") MultipartFile file,HttpServletRequest request){
        String role=(String) request.getAttribute("userRole");
        if(!"admin".equals(role)){
            throw AppException.forbidden("仅管理员可用");
        }
        if(file.isEmpty()){
            throw AppException.badRequest("文件不能为空");
        }
        String contentType =file.getContentType();
        if(contentType==null||!ALLOWED_TYPES.contains(contentType)){
            throw AppException.badRequest("只接受jpg/png/webp 格式图片");
        }
        if(file.getSize()> MAX_SIZE){
            throw AppException.badRequest("文件大小不能超过5MB");
        }
        String ext=switch(contentType){
            case "image/jpeg"->".jpg";
            case "image/png"->".png";
            case "image/webp"->".webp";
            default -> ".jpg";
        };
        String filename =System.currentTimeMillis() +"-"+UUID.randomUUID().toString().substring(0,8)+ext;
        try{
            Files.createDirectories(uploadDir);
            Path targetPath = uploadDir.resolve(filename);
            file.transferTo(targetPath.toFile());
            log.info("Cover upload:{}",filename);
        } catch(IOException e){
            log.error("Cover upload failed",e);
            throw AppException.serverError("文件保存失败");
        }
        return ResponseEntity.ok(Map.of("path","/covers/"+filename));
        

    }

}
