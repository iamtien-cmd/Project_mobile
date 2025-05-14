// File: ApiService.java
package vn.iostar.doan.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit; // Cho ví dụ timeout

import okhttp3.OkHttpClient; // << THÊM IMPORT
import okhttp3.logging.HttpLoggingInterceptor; // << THÊM IMPORT
import okhttp3.MultipartBody;
import okhttp3.RequestBody; // Mặc dù không dùng trực tiếp ở đây, nhưng thường đi kèm
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
 // << THÊM IMPORT NÀY (Thay package nếu cần)
import vn.iostar.doan.model.Address;
import vn.iostar.doan.model.Cart;
import vn.iostar.doan.model.CartItem;
import vn.iostar.doan.model.Category;
import vn.iostar.doan.model.Comment;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.SelectedItemDetail;
import vn.iostar.doan.model.User;
import vn.iostar.doan.model.User1;
import vn.iostar.doan.modelRequest.CartActionRequest;
import vn.iostar.doan.modelRequest.CartItemDetailsRequest;
import vn.iostar.doan.modelRequest.ChatRequest;
import vn.iostar.doan.modelRequest.CommentRequest;
import vn.iostar.doan.modelRequest.CreateOrderRequest;
import vn.iostar.doan.modelRequest.ForgotPasswordRequest;
import vn.iostar.doan.modelRequest.LoginRequest;
import vn.iostar.doan.modelRequest.RegisterRequest;
import vn.iostar.doan.modelRequest.UpdateProfileRequest;
import vn.iostar.doan.modelResponse.AddressInputDTO;
import vn.iostar.doan.modelResponse.ChatResponse;
import vn.iostar.doan.modelResponse.CreateOrderResponseDTO;
import vn.iostar.doan.modelResponse.ImageUploadResponse;

public interface ApiService {

    // --- PHẦN KHỞI TẠO MỚI CHO apiService VỚI LOGGING INTERCEPTOR ---

    // 1. Tạo HttpLoggingInterceptor
    // Các trường trong interface mặc định là public static final,
    // nên các đối tượng này sẽ được khởi tạo khi interface được load.
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);


    // 2. Tạo OkHttpClient
    OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Thêm logging interceptor
            .connectTimeout(60, TimeUnit.SECONDS) // Tăng thời gian chờ kết nối
            .readTimeout(60, TimeUnit.SECONDS)    // Tăng thời gian chờ đọc dữ liệu
            .writeTimeout(60, TimeUnit.SECONDS)   // Tăng thời gian chờ ghi dữ liệu
            .build();

    // 3. Tạo Gson
    Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Giữ nguyên định dạng ngày tháng của bạn
            // .setLenient() // CHỈ DÙNG KHI THỰC SỰ CẦN VÀ HIỂU RÕ VẤN ĐỀ
            .create();

    // 4. Khởi tạo Retrofit và ApiService instance
    // Biến apiService này sẽ là instance mà toàn bộ ứng dụng sử dụng
    ApiService apiService = new Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/") // IP của bạn cho máy ảo Android Studio
            .client(okHttpClient)             // << QUAN TRỌNG: Sử dụng OkHttpClient đã cấu hình
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService.class);

    // --- KẾT THÚC PHẦN KHỞI TẠO MỚI ---


    // --- CÁC ĐỊNH NGHĨA ENDPOINT API CỦA BẠN GIỮ NGUYÊN ---
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

    // Đảm bảo đường dẫn này là đúng cho việc TẠO comment
    // Ví dụ: /api/comments/create hoặc chỉ /api/comments nếu backend xử lý POST là tạo mới
    @POST("/api/comments")
    Call<Comment> createComment(@Header("Authorization") String token, @Body CommentRequest commentRequest);

    @GET("/api/comments/product/{productId}")
    Call<List<Comment>> getCommentsByProduct(@Path("productId") long productId);

    @Multipart
    @POST("api/v1/upload/image") // Endpoint upload ảnh chung (nếu có)
    Call<ImageUploadResponse> uploadImage(@Part MultipartBody.Part file);

    @POST("api/chat")
    Call<ChatResponse> sendMessage(@Body ChatRequest request);

    @PUT("api/orders/{orderId}/cancel")
    Call<Order> cancelOrder(@Path("orderId") Long orderId);

    @GET("api/product/category/{id}")
    Call<List<Product>> getProductsByCategory(@Path("id") Long id);

    @GET("/api/product/categories")
    Call<List<Category>> getAllCategories();

    // Xem xét nếu endpoint này trùng với "api/products"
    @GET("/api/product/getListProducts")
    Call<List<Product>> getAllProducts();

    @GET("api/product/{id}")
    Call<Product> getProductDetails(@Path("id") long productId);

    @POST("api/cart/add")
    Call<Cart> addToCart(@Header("Authorization") String token, @Body CartActionRequest request);

    @POST("/api/cart/items/details")
    Call<List<SelectedItemDetail>> getSelectedCartItemDetails(
            @Header("Authorization") String authToken,
            @Body CartItemDetailsRequest request
    );

    @GET("api/cart/items")
    Call<List<CartItem>> getCartItems(@Header("Authorization") String token);

    @DELETE("api/cart/remove")
    Call<ResponseBody> removeCartItem(
            @Header("Authorization") String header,
            @Query("productId") Long productIdToRemove
    );

    @PUT("api/cart/update")
    Call<Cart> updateCartItem(
            @Header("Authorization") String token,
            @Body CartActionRequest request
    );

    @PUT("api/auth/update")
    Call<User> updateProfile(
            @Header("Authorization") String authorization,
            @Body UpdateProfileRequest updateRequest
    );

    @Multipart
    @POST("api/upload/image") // Endpoint upload ảnh cho avatar (nếu khác với uploadImage ở trên)
    Call<ImageUploadResponse> uploadAvatarImage(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part imageFile
    );

    @GET("/api/addresses")
    Call<List<Address>> getUserAddresses(@Header("Authorization") String authHeader);

    @GET("/api/addresses/{addressId}")
    Call<Address> getAddressById(
            @Path("addressId") Long addressId,
            @Header("Authorization") String authHeader
    );

    @POST("/api/addresses")
    Call<Address> addAddress(
            @Header("Authorization") String authHeader,
            @Body AddressInputDTO addressInput
    );

    @PUT("/api/addresses/{addressId}")
    Call<Address> updateAddress(
            @Path("addressId") Long addressId,
            @Header("Authorization") String authHeader,
            @Body AddressInputDTO addressInput
    );

    @DELETE("/api/addresses/{addressId}")
    Call<Void> deleteAddress(
            @Path("addressId") Long addressId,
            @Header("Authorization") String authHeader
    );

    @PATCH("/api/addresses/{addressId}/default")
    Call<Void> setDefaultAddress(
            @Path("addressId") Long addressId,
            @Header("Authorization") String authHeader
    );

    @POST("api/order/createOrder")
    Call<CreateOrderResponseDTO> createOrder(
            @Header("Authorization") String authorization,
            @Body CreateOrderRequest orderRequest
    );

    // Endpoint này có vẻ cần token
    @GET("/api/order/{orderId}")
    Call<Order> getOrderDetails(
            @Header("Authorization") String authToken,
            @Path("orderId") Long orderId
    );

    // Endpoint này có thể là public hoặc cũng cần token, tùy backend
    @GET("api/orders/{orderId}")
    Call<Order> getOrderDetail(@Path("orderId") Long orderId);
}