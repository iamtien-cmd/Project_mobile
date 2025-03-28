package vn.iostar.doan.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.converter.gson.GsonConverterFactory;

import java.time.LocalDateTime;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import vn.iostar.doan.entity.Address;
import vn.iostar.doan.entity.Product;
import vn.iostar.doan.entity.User;

public interface ApiService {
    // Link API:
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://192.168.2.128:8082/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @GET("api/users/getListUsers")
    Call<User> getListUsers(
            @Query("fullname") String fullname,
            @Query("birthday") LocalDateTime birthday,
            @Query("phone") String phone,
            @Query("username") String username,
            @Query("password") String password,
            @Query("email") String email);

    @GET("api/products")
    Call<List<Product>> getListProducts();


}
