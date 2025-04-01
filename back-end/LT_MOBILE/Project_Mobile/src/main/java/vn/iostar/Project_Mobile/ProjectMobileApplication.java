package vn.iostar.Project_Mobile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "vn.iostar.Project_Mobile.repository")
public class ProjectMobileApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectMobileApplication.class, args);
	}

}
