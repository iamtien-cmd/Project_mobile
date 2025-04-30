package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
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
import vn.iostar.doan.model.User1;
import vn.iostar.doan.modelRequest.LoginRequest;

public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Gọi layout activity_main.xml
        TextInputLayout inputUser = findViewById(R.id.etUsername);
        TextInputLayout inputPass = findViewById(R.id.etPassword);

        EditText txtUser = inputUser.getEditText();
        EditText txtPass = inputPass.getEditText();

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Xử lý sự kiện khi nhấn nút Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = txtUser.getText().toString();
                String password = txtPass.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
                    return;
                }

                LoginRequest request = new LoginRequest(username, password);
                ApiService.apiService.loginUser(request).enqueue(new Callback<User1>() {
                    @Override
                    public void onResponse(Call<User1> call, Response<User1> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User1 user = response.body();
                            Toast.makeText(LoginActivity.this, "Xin chào " + user.getFullName(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("token", user.getToken());
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<User1> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


        TextView textView = findViewById(R.id.tvRegister);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang LoginActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);

                startActivity(intent);
            }
        });
        TextView txtForget = findViewById(R.id.txtForget);
        txtForget.setOnClickListener(v -> {
            // Start the ForgetPasswordActivity when the "Forgot password?" link is clicked
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });
    }


}
