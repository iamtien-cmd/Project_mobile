package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.modelRequest.ForgotPasswordRequest;

public class ForgetPasswordActivity extends AppCompatActivity {
    private EditText edtEmail; // Chỉnh sửa ở đây thành EditText
    private Button btnSendOtp;
    private TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_otp);  // Đặt đúng tên file XML của bạn

        TextInputLayout textInputLayout = findViewById(R.id.txt_email);
        edtEmail = textInputLayout.getEditText(); // Không cần thay đổi dòng này nữa, vì edtEmail là EditText

        btnSendOtp = findViewById(R.id.btn_sendOtp);
        txtLogin = findViewById(R.id.txt_login);

        // Xử lý nút Send OTP
        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();

                if (isValidEmail(email)) {
                    ForgotPasswordRequest request = new ForgotPasswordRequest(email);

                    // Gọi API forgot-password
                    ApiService.apiService.forgotPassword(request).enqueue(new Callback<ForgotPasswordRequest>() {
                        @Override
                        public void onResponse(Call<ForgotPasswordRequest> call, Response<ForgotPasswordRequest> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ForgetPasswordActivity.this, "Mã OTP đã được gửi tới " + email, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ForgetPasswordActivity.this, VerifyOtpForgetPasswordActivity.class);
                                intent.putExtra("email", email);
                                startActivity(intent);
                                finish(); // Tùy bạn muốn quay lại hay không
                            } else {
                                Toast.makeText(ForgetPasswordActivity.this, "Không thể gửi OTP, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ForgotPasswordRequest> call, Throwable t) {
                            Toast.makeText(ForgetPasswordActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Hàm kiểm tra email hợp lệ
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
