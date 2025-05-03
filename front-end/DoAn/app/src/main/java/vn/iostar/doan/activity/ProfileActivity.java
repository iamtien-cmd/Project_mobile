package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
// import android.view.View; // Không dùng trực tiếp, bỏ import cũng được
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// --- THÊM CÁC IMPORT CHO ACTIVITY RESULT API ---
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
// --- HẾT PHẦN IMPORT MỚI ---

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

// Thêm các import còn thiếu nếu cần
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Address;
import vn.iostar.doan.model.User;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity"; // Thêm TAG để log

    private TextView emailTextView, fullNameTextView, phoneTextView, addressTextView;
    private ImageView avatarImageView;
    private Button editProfileButton;
    private Button editAddress;
    private String token;
    private User currentUser;

    // --- Khai báo ActivityResultLauncher ---
    private ActivityResultLauncher<Intent> editProfileLauncher,shippingAddressLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        token = getIntent().getStringExtra("token");
        Log.d(TAG, "Token nhận được: " + token);

        AnhXa();

        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "ActivityResultLauncher callback received. ResultCode: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Log.i(TAG, "Received RESULT_OK with data from EditProfileActivity.");
                        Intent data = result.getData();

                        String updatedFullName = data.getStringExtra("UPDATED_FULL_NAME");
                        String updatedPhone = data.getStringExtra("UPDATED_PHONE");
                        String updatedAvatarUrl = data.getStringExtra("UPDATED_AVATAR_URL");

                        Log.d(TAG, "Extracted Full Name: " + (updatedFullName != null ? updatedFullName : "NULL"));
                        Log.d(TAG, "Extracted Phone: " + (updatedPhone != null ? updatedPhone : "NULL"));
                        Log.d(TAG, "Extracted Avatar URL: " + (updatedAvatarUrl != null ? updatedAvatarUrl : "NULL"));

                        if (updatedFullName != null) {
                            fullNameTextView.setText(updatedFullName);
                            if (currentUser != null) currentUser.setFullName(updatedFullName);
                        }
                        if (updatedPhone != null) {
                            phoneTextView.setText(updatedPhone);
                            if (currentUser != null) currentUser.setPhone(updatedPhone);
                        }
                        if (updatedAvatarUrl != null && !updatedAvatarUrl.isEmpty()) {
                            if (currentUser != null) currentUser.setAvatar(updatedAvatarUrl);
                            Glide.with(ProfileActivity.this)
                                    .load(updatedAvatarUrl)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_launcher_foreground) // Ảnh chờ
                                    .error(R.drawable.ic_launcher_foreground) // Ảnh lỗi
                                    .into(avatarImageView);
                        } else if (updatedAvatarUrl != null && updatedAvatarUrl.isEmpty()) {
                            Log.d(TAG, "Received empty Avatar URL, setting default image.");
                            if (currentUser != null) currentUser.setAvatar(null); // Cập nhật state
                            avatarImageView.setImageResource(R.drawable.ic_launcher_foreground);
                        }

                    } else {
                        Log.i(TAG, "EditProfileActivity finished without RESULT_OK or data.");
                    }
                });

        shippingAddressLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "ActivityResultLauncher callback received from ShippingAddressActivity. ResultCode: " + result.getResultCode());
                    if (result.getResultCode() == RESULT_OK) {
                        Log.i(TAG, "ShippingAddressActivity finished with RESULT_OK. Refreshing user info.");
                        if (token != null && !token.isEmpty()) {
                            getUserInfo(token);
                        } else {
                            Log.e(TAG, "Token is null during address activity result. Cannot refresh user info.");
                        }

                    } else {
                        Log.i(TAG, "ShippingAddressActivity finished with non-OK result.");
                    }
                });

        if (token != null && !token.isEmpty()) {
            getUserInfo(token);
        } else {
            Log.e(TAG, "Token is null or empty. Cannot fetch initial user info.");
            Toast.makeText(this, "Lỗi xác thực.", Toast.LENGTH_SHORT).show();
        }

        setupButtonClickListeners();
    }

    public void AnhXa(){
        editProfileButton = findViewById(R.id.editProfileButton);
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        addressTextView = findViewById(R.id.addressTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
        editAddress = findViewById(R.id.shippingAddressButton);
    }

    private void getUserInfo(String token) {
        Log.d(TAG, "Fetching initial user info...");
        ApiService.apiService.getUserInfo("Bearer " + token)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Initial user info fetched successfully.");
                            currentUser = response.body();
                            updateUI(currentUser); // Cập nhật UI lần đầu
                        } else {
                            Log.e(TAG, "Failed to get initial user info. Code: " + response.code());
                            Toast.makeText(ProfileActivity.this, "Lỗi tải thông tin: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e(TAG, "API Error getting initial user info: " + t.getMessage(), t);
                        Toast.makeText(ProfileActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(User userInfo) {
        if (userInfo == null) {
            Log.w(TAG, "updateUI called with null userInfo.");
            return;
        }

        emailTextView.setText(userInfo.getEmail() != null ? userInfo.getEmail() : "");
        fullNameTextView.setText(userInfo.getFullName() != null ? userInfo.getFullName() : "");
        phoneTextView.setText(userInfo.getPhone() != null ? userInfo.getPhone() : "");

        StringBuilder addressBuilder = new StringBuilder();
        List<Address> addresses = userInfo.getAddresses();
        Log.d(TAG, "Addresses list received. Size: " + (addresses != null ? addresses.size() : "null"));
        Address defaultAddress = null; // Biến để lưu địa chỉ mặc định tìm thấy

        if (addresses != null && !addresses.isEmpty()) {
            // Lặp qua danh sách địa chỉ để tìm địa chỉ mặc định
            for (Address address : addresses) {
                // Kiểm tra null cho từng đối tượng Address trong list cũng là tốt
                if (address != null && address.isDefaultAddress()) {
                    defaultAddress = address; // Tìm thấy địa chỉ mặc định
                    break; // Vì chỉ có một địa chỉ mặc định (sau khi backend đã sửa), thoát vòng lặp
                }
            }

            // Sau khi tìm xong, kiểm tra xem có địa chỉ mặc định nào được tìm thấy không
            if (defaultAddress != null) {
                addressBuilder.append(defaultAddress.getHouseNumber());
                Log.d(TAG, "Default address found and displayed.");
            } else {
                // Không tìm thấy địa chỉ mặc định nào trong danh sách (dù danh sách không rỗng)
                // Điều này có thể xảy ra nếu chưa có địa chỉ nào được set default hoặc có lỗi logic
                addressBuilder.append("Chưa có địa chỉ mặc định được thiết lập.");
                Log.w(TAG, "No default address found among the list.");
            }

        } else {
            // Danh sách địa chỉ rỗng hoặc null
            addressBuilder.append("Chưa có địa chỉ.");
            Log.d(TAG, "Address list is null or empty.");
        }

        addressTextView.setText(addressBuilder.toString().trim());

        if (userInfo.getAvatar() != null && !userInfo.getAvatar().isEmpty()) {
            Glide.with(ProfileActivity.this)
                    .load(userInfo.getAvatar())
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(avatarImageView);
        } else {
            Log.d(TAG, "updateUI - Avatar URL is null or empty, setting default image.");
            avatarImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    private void setupButtonClickListeners() {
        editProfileButton.setOnClickListener(v -> navigateToEditProfile());
        editAddress.setOnClickListener(v -> navigateToShippingAddress());
    }
    private void navigateToShippingAddress() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Lỗi xác thực, không thể xem địa chỉ.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(ProfileActivity.this, ShippingAddressActivity.class);
        intent.putExtra("TOKEN", token); // Pass the token to the address activity

        Log.i(TAG, "Launching ShippingAddressActivity using launcher...");
        shippingAddressLauncher.launch(intent); // Use the new launcher
    }
    private void navigateToEditProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Đang tải thông tin, vui lòng chờ.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
        intent.putExtra("CURRENT_FULL_NAME", currentUser.getFullName());
        intent.putExtra("CURRENT_PHONE", currentUser.getPhone());
        intent.putExtra("CURRENT_AVATAR_URL", currentUser.getAvatar());
        intent.putExtra("TOKEN", token);

        Log.i(TAG, "Launching EditProfileActivity using launcher...");
        editProfileLauncher.launch(intent);
    }
}