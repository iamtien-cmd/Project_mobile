package vn.iostar.doan.activity;

import android.os.Bundle;
import android.util.Log;
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
import vn.iostar.doan.model.User1;

public class ProfileActivity extends AppCompatActivity {
    private TextView emailTextView, fullNameTextView, phoneTextView, addressTextView;
    private ImageView avatarImageView;
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        token = getIntent().getStringExtra("token");
        // Giờ token đã có, bạn dùng token để gọi API lấy profile user
        Log.d("TokenDebug", "Token nhận được: " + token);

        AnhXa();
        // lấy thông tin người dùng
        getUserInfo(token);
        //chỉnh sửa thông tin người dùng
    }
    public void AnhXa(){
        emailTextView = findViewById(R.id.emailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        addressTextView = findViewById(R.id.addressTextView);
        avatarImageView = findViewById(R.id.avatarImageView);
    }
    private void getUserInfo(String token) {
        ApiService.apiService.getUserInfo("Bearer " + token)
                .enqueue(new Callback<User1>() {
            @Override
            public void onResponse(Call<User1> call, Response<User1> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User1 user = response.body();
                    updateUI(user);
                } else {
                    Toast.makeText(ProfileActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User1> call, Throwable t) {
                Log.e("API Error", "Yêu cầu thất bại: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Lỗi kết nối API", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(User1 userInfo) {
        emailTextView.setText(userInfo.getEmail());
        fullNameTextView.setText(userInfo.getFullName());
        phoneTextView.setText(userInfo.getPhone());

        StringBuilder addressBuilder = new StringBuilder();
        for (Address address : userInfo.getAddresses()) {
            addressBuilder.append(address.getHouseNumber())
                    .append(", ")
                    .append(address.getDistrict())
                    .append(", ")
                    .append(address.getCity())
                    .append(", ")
                    .append(address.getCountry())
                    .append("\n");
        }
        addressTextView.setText(addressBuilder.toString());

        if (userInfo.getAvatar() != null) {
            Glide.with(ProfileActivity.this)
                    .load(userInfo.getAvatar()) // URL của avatar
                    .circleCrop()
                    .into(avatarImageView);
        }
    }
}
