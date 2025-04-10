package vn.iostar.doan.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import vn.iostar.doan.R;

public class MainActivity extends AppCompatActivity {
    private static final String URL_GIF_IMAGE="https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZmRhN3Y5am1xbXc1bGN5YTU0emtzNmgyZ3N6eG9qMjJ6YTNjaG93dCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/26u4cqVR8dsmedTJ6/giphy.gif";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Gọi layout activity_main.xml



        // Lắng nghe sự kiện click trên màn hình
        View mainLayout = findViewById(R.id.mainLayout);
        mainLayout.setOnClickListener(v -> {
            // Chuyển sang màn hình khác
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng activity hiện tại (tùy chọn)
        });
    }


}
