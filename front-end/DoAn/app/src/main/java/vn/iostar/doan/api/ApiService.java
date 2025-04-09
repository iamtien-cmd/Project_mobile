package vn.iostar.doan.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User;
import vn.iostar.doan.modelRequest.LoginRequest;

public interface ApiService {
    // Link API:
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://192.168.2.137:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @POST("/login")
    Call<User> loginUser(@Body LoginRequest userRequest);

    @GET("api/products")
    Call<List<Product>> getListProducts();


}
