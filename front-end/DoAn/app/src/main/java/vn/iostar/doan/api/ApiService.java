package vn.iostar.doan.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.http.Header;
import retrofit2.http.Path;

import okhttp3.MultipartBody;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import vn.iostar.doan.model.Category;
import vn.iostar.doan.model.Comment;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User;
import vn.iostar.doan.model.User1;
import vn.iostar.doan.modelRequest.ChatRequest;
import vn.iostar.doan.modelRequest.CommentRequest;
import vn.iostar.doan.modelRequest.ForgotPasswordRequest;
import vn.iostar.doan.modelRequest.LoginRequest;
import vn.iostar.doan.modelRequest.RegisterRequest;
import vn.iostar.doan.modelResponse.ChatResponse;
import vn.iostar.doan.modelResponse.ImageUploadResponse;

public interface ApiService {
    // Link API:
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create();
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    @GET("/api/auth/info")
    Call<User> getUserInfo(@Header("Authorization") String token);

    @POST("/api/auth/login")
    Call<User> loginUser(@Body LoginRequest userRequest);

    @POST("/api/auth/register")
    Call<User1> registerUser(@Body User1 user);

    @POST("/api/auth/verifyOtpRegister")
    Call<User1> verifyOtpRegister(@Body RegisterRequest registerRequest);
    @GET("api/products")
    Call<List<Product>> getListProducts();
    @GET("/api/orders/status/{userId}")
    Call<ArrayList<Order>> getOrdersByUserId(@Path("userId") long userId);
    @POST("/api/auth/forgot-password")
    Call<ForgotPasswordRequest> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @POST("/api/auth/verifyOtpForgotPassword")
    Call<User1> verifyOtpForgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @POST("/api/auth/reset-password")
    Call<User1> resetPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @POST("/api/comments")
    Call<Comment> createComment(@Body CommentRequest commentRequest);

    @GET("/api/comments/product/{productId}")
    Call<List<Comment>> getCommentsByProduct(@Path("productId") long productId);

    @Multipart
    // ---- SỬA ĐƯỜNG DẪN Ở ĐÂY ----
    @POST("api/v1/upload/image") // <<< ĐƯỜNG DẪN ĐẦY ĐỦ KHỚP VỚI BACKEND
        // ---------------------------
    Call<ImageUploadResponse> uploadImage(
            @Part MultipartBody.Part file // <<< Tên part "file" cũng phải khớp backend
    );
    @POST("api/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);
    @PUT("api/orders/{orderId}/cancel") // Đường dẫn tương đối từ Base URL
    Call<Order> cancelOrder(@Path("orderId") Long orderId);
    @GET("api/product/category/{id}")
    Call<List<Product>> getProductsByCategory(@Path("id") Long id);

    @GET("/api/product/categories")
    Call<List<Category>> getAllCategories();

    @GET("/api/product/getListProducts")
    Call<List<Product>> getAllProducts();

    @GET("api/product/{id}")
    Call<Product> getProductDetails(@Path("id") long productId);

    @GET("api/orders/{orderId}") // <<< THÊM ENDPOINT LẤY CHI TIẾT NẾU CHƯA CÓ
    Call<Order> getOrderDetail(@Path("orderId") Long orderId);
}
