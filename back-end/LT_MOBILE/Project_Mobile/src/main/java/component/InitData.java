package component;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import com.google.gson.JsonParser;  
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import jakarta.annotation.PostConstruct;
import vn.iostar.Project_Mobile.entity.Product;
import vn.iostar.Project_Mobile.repository.ProductRepository;

public class InitData {
	  @Autowired
	    private ProductRepository productRepository;

	    @PostConstruct
	    public void init() {
	        try {
	            loadJson("product.json");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private void loadJson(String fileName) throws Exception {
	        JsonElement root = JsonParser.parseReader(
	                new InputStreamReader(new ClassPathResource(fileName).getInputStream(), StandardCharsets.UTF_8));
	        parseJson(root);
	    }

	    private void parseJson(JsonElement element) {
	        Gson gson = new Gson();
	        Product[] products = gson.fromJson(element, Product[].class);
	        productRepository.saveAll(List.of(products));
	    }

}
