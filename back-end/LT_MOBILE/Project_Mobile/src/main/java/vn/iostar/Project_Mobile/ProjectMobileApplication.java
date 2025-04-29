package vn.iostar.Project_Mobile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.CategoryRepository;

import vn.iostar.Project_Mobile.repository.ProductRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "vn.iostar.Project_Mobile.repository")
public class ProjectMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectMobileApplication.class, args);
    }

    /*@Bean // Hàm kiểm tra mật khẩu mã hóa có khớp không
    public CommandLineRunner demo() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "123";
            String encodedPassword = "$2a$10$ZNdj1hMQ3mVnBtzgAxKygeO9hPLF9gF/7eP99gSnhAXWTy1pxf4Fe";
            String encoded = encoder.encode(rawPassword); // mã hoá "123"

            System.out.println("Mã hoá mật khẩu: " + encoded);
            System.out.println("So sánh lại: " + encoder.matches(rawPassword, encoded)); // nên trả về true
            boolean match = encoder.matches(rawPassword, encodedPassword);
            System.out.println("Mật khẩu có khớp không? " + match);  // Sẽ in ra sau khi app khởi động
        };
    }*/
}
