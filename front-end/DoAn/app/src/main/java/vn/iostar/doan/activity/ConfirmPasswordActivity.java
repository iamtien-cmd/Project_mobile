package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import vn.iostar.doan.model.User1;
import vn.iostar.doan.modelRequest.ForgotPasswordRequest;

public class ConfirmPasswordActivity extends AppCompatActivity {

    private TextInputLayout txtNewPassLayout, txtRepassLayout;
    private TextInputEditText txtNewPass, txtRepass;
    private Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        String email = getIntent().getStringExtra("email");
        String otp = getIntent().getStringExtra("otp");

        // Initialize UI elements
        txtNewPassLayout = findViewById(R.id.txt_newpass);
        txtRepassLayout = findViewById(R.id.txt_repass);

        txtNewPass = (TextInputEditText) txtNewPassLayout.getEditText();
        txtRepass = (TextInputEditText) txtRepassLayout.getEditText();

        btnChange = findViewById(R.id.btnLogin);

        // Button click listener to call reset password API
        btnChange.setOnClickListener(v -> {
            String newPass = txtNewPass.getText().toString().trim();
            String repass = txtRepass.getText().toString().trim();

            // Validate password fields
            if (TextUtils.isEmpty(newPass) || TextUtils.isEmpty(repass)) {
                Toast.makeText(ConfirmPasswordActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else if (!newPass.equals(repass)) {
                // If passwords don't match
                Toast.makeText(ConfirmPasswordActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Create ForgotPasswordRequest with email, OTP, and new passwords
                ForgotPasswordRequest request = new ForgotPasswordRequest();
                request.setEmail(email);
                request.setOtp(otp);
                request.setNewPassword(newPass);
                request.setConfirmPassword(repass);

                // Call API to reset password
                resetPassword(request);
            }
        });
    }

    private void resetPassword(ForgotPasswordRequest request) {
        // Make the API call to reset the password
        ApiService.apiService.resetPassword(request).enqueue(new Callback<User1>() {
            @Override
            public void onResponse(Call<User1> call, Response<User1> response) {
                if (response.isSuccessful()) {
                    // If password reset is successful, show message and redirect to LoginActivity
                    Toast.makeText(ConfirmPasswordActivity.this, "Password reset successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ConfirmPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();  // Finish the current activity to prevent going back to it
                } else {
                    // If response is not successful, show error message
                    Toast.makeText(ConfirmPasswordActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User1> call, Throwable t) {
                // Handle failure (network error, etc.)
                Toast.makeText(ConfirmPasswordActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
