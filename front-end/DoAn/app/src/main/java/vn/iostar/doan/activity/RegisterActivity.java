package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import vn.iostar.doan.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ImageView googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ ID từ layout
        etFullName = findViewById(R.id.etFullname);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        googleSignIn = findViewById(R.id.googleSignIn);

        // Xử lý sự kiện khi nhấn nút Đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etUsername.getText().toString().trim();  // Email hoặc username
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            } else {
                // Đăng ký thành công (lưu vào database ở đây nếu có)
                Toast.makeText(RegisterActivity.this, "Đăng ký thành công! Vui lòng xác thực OTP.", Toast.LENGTH_SHORT).show();

                // Gửi username qua VerifyOtpActivity
                Intent intent = new Intent(RegisterActivity.this, VerifyOtpRegisterActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish(); // Tùy chọn: đóng RegisterActivity
            }
        });



        // Xử lý sự kiện đăng nhập bằng Google
        googleSignIn.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "Tính năng đăng ký với Google chưa được triển khai!", Toast.LENGTH_SHORT).show();
        });
    }
}
