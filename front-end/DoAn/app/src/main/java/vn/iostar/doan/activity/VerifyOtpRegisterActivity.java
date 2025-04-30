package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.User;
import vn.iostar.doan.modelRequest.RegisterRequest;

public class VerifyOtpRegisterActivity extends AppCompatActivity {
    private TextInputEditText etOtp;
    private TextInputLayout otpLayout;
    private Button btnConfirmOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp_register);

        otpLayout = findViewById(R.id.otp_layout);
        etOtp = findViewById(R.id.txt_otp);
        btnConfirmOtp = findViewById(R.id.btn_confirmOtp);

        if (etOtp == null || btnConfirmOtp == null || otpLayout == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy view", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmOtp.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString(); // sửa Otp -> etOtp
            String email = getIntent().getStringExtra("email");
            if (enteredOtp.isEmpty()) {
                otpLayout.setError("Vui lòng nhập mã OTP");
                return;
            } else {
                otpLayout.setError(null);
            }
            RegisterRequest request = new RegisterRequest(email, enteredOtp);

            ApiService.apiService.verifyOtpRegister(request).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        User user = response.body();

                        Toast.makeText(VerifyOtpRegisterActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VerifyOtpRegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        otpLayout.setError("OTP không chính xác hoặc đã hết hạn");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    otpLayout.setError("Lỗi kết nối: " + t.getMessage());
                }
            });
        });
    }
}
