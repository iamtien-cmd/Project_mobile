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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.CategoryRepository;

import vn.iostar.Project_Mobile.repository.FavoriteRepository;
import vn.iostar.Project_Mobile.repository.ProductRepository;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "vn.iostar.Project_Mobile.repository")
public class ProjectMobileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectMobileApplication.class, args);
    }
  

  
    
}