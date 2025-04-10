package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.modelRequest.RegisterRequest;

public class VerifyOtpRegisterActivity extends AppCompatActivity {

    private TextInputEditText etOtp;
    private Button btnConfirmOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        etOtp = findViewById(R.id.txt_otp); // Gắn ID trực tiếp cho TextInputEditText trong XML
        btnConfirmOtp = findViewById(R.id.btn_confirmOtp);

        btnConfirmOtp.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString().trim();
            String email = getIntent().getStringExtra("email");

            if (enteredOtp.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(email, enteredOtp);

            ApiService.apiService.verifyOtpRegister(request).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(VerifyOtpRegisterActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerifyOtpRegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(VerifyOtpRegisterActivity.this, "OTP không chính xác hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(VerifyOtpRegisterActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
