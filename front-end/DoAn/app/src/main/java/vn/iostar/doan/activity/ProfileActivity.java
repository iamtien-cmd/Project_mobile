package vn.iostar.doan.activity;

import android.content.Intent; // *** Thêm import Intent ***
import android.os.Bundle;
import android.util.Log;
import android.view.View;      // *** Thêm import View ***
import android.widget.Button;   // *** Thêm import Button ***
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Address;
import vn.iostar.doan.model.User;
// Bỏ import User1 nếu không dùng
// import vn.iostar.doan.model.User1;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity"; // *** Thêm TAG để log ***

    private TextView emailTextView, fullNameTextView, phoneTextView, addressTextView;
    private ImageView avatarImageView;
    private Button orderHistoryButton; // *** Khai báo Button ***
    // Các button khác nếu cần
    private Button editProfileButton, shippingAddressButton, creditCardsButton;

    private String token;
    private long currentUserId = 0L; // *** Khai báo và khởi tạo userId ***

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // *** Đảm bảo tên layout đúng ***

        token = getIntent().getStringExtra("token");
        Log.d(TAG, "Token received: " + token); // *** Sử dụng TAG ***

        // Kiểm tra token trước khi làm gì khác
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty. Cannot proceed.");
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_LONG).show();
            // Có thể chuyển về màn hình Login ở đây
            // Intent loginIntent = new Intent(this, LoginActivity.class);
            // startActivity(loginIntent);
            finish(); // Đóng activity profile nếu không có token
            return;
        }

        AnhXa(); // Ánh xạ các View

        // Lấy thông tin người dùng bằng token
        getUserInfo(token);

        // --- Thiết lập Listener cho nút Order History ---
        // Đặt listener *sau khi* đã ánh xạ nút trong AnhXa()
        orderHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Kiểm tra xem userId có hợp lệ không (đã lấy được từ API chưa)
                if (currentUserId <= 0) {
                    // Người dùng chưa đăng nhập hoặc có lỗi khi lấy ID từ API
                    Toast.makeText(ProfileActivity.this, "User information not loaded yet or invalid.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Order History button clicked but User ID is not valid: " + currentUserId);
                    // Có thể thử gọi lại API getUserInfo nếu muốn
                    // getUserInfo(token);
                    return; // Dừng xử lý tại đây
                }

                // 2. Tạo Intent để chuyển sang OrderHistoryActivity
                Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);

                // 3. Đính kèm userId vào Intent dưới dạng Extra
                // *** QUAN TRỌNG: Sử dụng một key nhất quán (ví dụ: "user_id") ***
                intent.putExtra("userId", currentUserId);
                Log.d(TAG, "Starting OrderHistoryActivity with User ID: " + currentUserId);

                // 4. Bắt đầu OrderHistoryActivity
                startActivity(intent);
            }
        });

        // --- (Tùy chọn) Thiết lập Listener cho các nút khác ---
        editProfileButton.setOnClickListener(v -> {
            if (currentUserId <= 0) {
                Toast.makeText(ProfileActivity.this, "User information not available.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            // Intent editIntent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            // editIntent.putExtra("user_id", currentUserId);
            // startActivity(editIntent);
        });

        shippingAddressButton.setOnClickListener(v -> {
            Toast.makeText(this, "Shipping Address Clicked", Toast.LENGTH_SHORT).show();
            // Intent addressIntent = new Intent(ProfileActivity.this, ShippingAddressActivity.class);
            // addressIntent.putExtra("user_id", currentUserId);
            // startActivity(addressIntent);
        });

        creditCardsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Credit Cards Clicked", Toast.LENGTH_SHORT).show();
            // Intent cardIntent = new Intent(ProfileActivity.this, CreditCardActivity.class);
            // cardIntent.putExtra("user_id", currentUserId);
            // startActivity(cardIntent);
        });


    }

    public void AnhXa(){
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        addressTextView = findViewById(R.id.addressTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
        orderHistoryButton = findViewById(R.id.orderHistoryButton); // *** Ánh xạ nút Order History ***
        // Ánh xạ các nút khác
        editProfileButton = findViewById(R.id.editProfileButton);
        shippingAddressButton = findViewById(R.id.shippingAddressButton);
        creditCardsButton = findViewById(R.id.creditCardsButton);
    }

    private void getUserInfo(String token) {
        Log.d(TAG, "Attempting to get user info with token...");
        ApiService.apiService.getUserInfo("Bearer " + token)
                .enqueue(new Callback<User>() { // *** Đảm bảo model User có trường ID ***
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            Log.d(TAG, "User info received successfully.");

                            // Cập nhật giao diện người dùng
                            updateUI(user);

                        } else {
                            // Xử lý lỗi rõ ràng hơn
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) errorBody = response.errorBody().string();
                            } catch (Exception e) { Log.e(TAG, "Error reading error body", e); }
                            Log.e(TAG, "Failed to get user info. Code: " + response.code() + ", Message: " + response.message() + ", Body: " + errorBody);
                            Toast.makeText(ProfileActivity.this, "Error loading profile: " + response.code(), Toast.LENGTH_SHORT).show();
                            // Nếu lỗi 401 hoặc 403, có thể token hết hạn -> yêu cầu đăng nhập lại
                            if (response.code() == 401 || response.code() == 403) {
                                // Chuyển về màn hình Login
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e(TAG, "API call failed: " + t.getMessage(), t); // Log cả stacktrace
                        Toast.makeText(ProfileActivity.this, "Network error. Please check connection.", Toast.LENGTH_SHORT).show();
                    }
                });
        // *** Bỏ phần setOnClickListener ở đây đi, đã chuyển ra onCreate ***
    }

    private void updateUI(User userInfo) {
        // Kiểm tra null trước khi setText để tránh crash
        currentUserId = userInfo.getUserId();
        if (userInfo.getEmail() != null) {
            emailTextView.setText(userInfo.getEmail());
        } else {
            emailTextView.setText("N/A"); // Hoặc để trống
        }

        if (userInfo.getFullName() != null) {
            fullNameTextView.setText(userInfo.getFullName());
        } else {
            fullNameTextView.setText("N/A");
        }

        if (userInfo.getPhone() != null) {
            phoneTextView.setText(userInfo.getPhone());
        } else {
            phoneTextView.setText("N/A");
        }

        // Xử lý địa chỉ (kiểm tra list null hoặc empty)
        if (userInfo.getAddresses() != null && !userInfo.getAddresses().isEmpty()) {
            StringBuilder addressBuilder = new StringBuilder();
            // Chỉ hiển thị địa chỉ đầu tiên hoặc lặp qua nếu muốn
            Address firstAddress = userInfo.getAddresses().get(0);
            if (firstAddress != null) {
                addressBuilder.append(firstAddress.getHouseNumber() != null ? firstAddress.getHouseNumber() + ", " : "")
                        .append(firstAddress.getDistrict() != null ? firstAddress.getDistrict() + ", " : "")
                        .append(firstAddress.getCity() != null ? firstAddress.getCity() + ", " : "")
                        .append(firstAddress.getCountry() != null ? firstAddress.getCountry() : "");
                // Xóa dấu phẩy và khoảng trắng thừa ở cuối nếu có
                if (addressBuilder.length() > 2 && addressBuilder.substring(addressBuilder.length() - 2).equals(", ")) {
                    addressBuilder.setLength(addressBuilder.length() - 2);
                }
            }
            addressTextView.setText(addressBuilder.length() > 0 ? addressBuilder.toString() : "No address available");
        } else {
            addressTextView.setText("No address available");
        }

        // Load Avatar bằng Glide (kiểm tra URL null)
        if (userInfo.getAvatar() != null && !userInfo.getAvatar().isEmpty()) {
            Glide.with(ProfileActivity.this)
                    .load(userInfo.getAvatar()) // URL của avatar
                    .placeholder(R.drawable.icon_avatar) // Ảnh mặc định khi đang load
                    .error(R.drawable.icon_avatar) // Ảnh mặc định nếu load lỗi
                    .circleCrop() // Bo tròn ảnh
                    .into(avatarImageView);
        } else {
            // Đặt ảnh mặc định nếu không có URL avatar
            Glide.with(ProfileActivity.this)
                    .load(R.drawable.icon_avatar)
                    .circleCrop()
                    .into(avatarImageView);
        }
    }
}