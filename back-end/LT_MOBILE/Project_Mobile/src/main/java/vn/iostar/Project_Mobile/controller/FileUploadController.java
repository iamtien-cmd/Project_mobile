package vn.iostar.Project_Mobile.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/upload") // Hoặc đường dẫn của bạn
public class FileUploadController {

    // Lấy đường dẫn thư mục lưu ảnh từ application.properties
    @Value("${upload.path:/path/to/your/static/images}") // Thay đổi đường dẫn thực tế
    private String uploadPath;

    // Lấy Base URL của server từ application.properties (để tạo URL công khai)
     @Value("${server.base.url:http://localhost:8080}") // Thay đổi URL thực tế
     private String serverBaseUrl;

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) { // "file" phải khớp @Part bên Android

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Tạo tên file duy nhất
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Tạo đường dẫn đầy đủ đến file sẽ lưu
            Path filePath = uploadDir.resolve(uniqueFileName);

            // Lưu file ảnh
            Files.copy(file.getInputStream(), filePath);

            // Tạo URL công khai (ví dụ đơn giản, cần cấu hình static resource serving)
            // Giả sử /images/ là đường dẫn web map tới uploadPath
            String publicUrl = serverBaseUrl + "/api/images/" + uniqueFileName;

            // Tạo đối tượng response
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", publicUrl); // "imageUrl" phải khớp ImageUploadResponse bên Android

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            // Ghi log lỗi chi tiết ở đây
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Could not upload the file: " + e.getMessage());
        } catch (Exception e) {
             e.printStackTrace();
            return ResponseEntity.internalServerError().body("An unexpected error occurred during upload.");
        }
    }
}
