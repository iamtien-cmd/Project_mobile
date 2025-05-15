package vn.iostar.Project_Mobile.config; // Or your preferred configuration package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Configure your RestTemplate here, just like you did in OrderServiceImpl
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        // Optionally configure timeouts, etc. on simpleClientHttpRequestFactory
        // simpleClientHttpRequestFactory.setConnectTimeout(5000); // 5 seconds
        // simpleClientHttpRequestFactory.setReadTimeout(5000);    // 5 seconds
        return new RestTemplate(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory));
    }
}