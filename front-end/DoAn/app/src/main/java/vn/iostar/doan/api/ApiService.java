package vn.iostar.doan.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.http.Path;

import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import vn.iostar.doan.model.Category;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User;
import vn.iostar.doan.modelRequest.LoginRequest;
import vn.iostar.doan.modelRequest.RegisterRequest;

public interface ApiService {
    // Link API:
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://192.168.100.232:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @GET("/api/auth/info")
    Call<User> getUserInfo(@Header("Authorization") String token);

    @POST("/api/auth/login")
    Call<User> loginUser(@Body LoginRequest userRequest);

    @POST("/api/auth/register")
    Call<User> registerUser(@Body User user);

    @POST("/api/auth/verifyOtpRegister")
    Call<Void> verifyOtpRegister(@Body RegisterRequest registerRequest);

    @GET("api/product/category/{id}")
    Call<List<Product>> getProductsByCategory(@Path("id") Long id);

    @GET("/api/product/categories")
    Call<List<Category>> getAllCategories();

    @GET("/api/product/getListProducts")
    Call<List<Product>> getAllProducts();

}
