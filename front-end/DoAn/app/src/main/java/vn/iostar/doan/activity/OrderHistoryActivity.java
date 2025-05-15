package vn.iostar.doan.activity; // <<<< Đảm bảo đúng package

import static android.content.ContentValues.TAG;

import android.app.Activity; // <<< THÊM IMPORT
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // <<< THÊM IMPORT
import androidx.activity.result.contract.ActivityResultContracts; // <<< THÊM IMPORT
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects; // Tùy chọn

import vn.iostar.doan.R; // <<<< Đảm bảo đúng package
import vn.iostar.doan.adapter.OrdersPagerAdapter; // <<<< Đảm bảo đúng package
// Import SharedPreferences helper nếu bạn dùng để lấy userId
// import vn.iostar.doan.util.SharedPreferencesUtils;

public class OrderHistoryActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private OrdersPagerAdapter pagerAdapter;
    private Toolbar toolbar;
    private String authToken;
    private Long currentUserId = -1L; // Khởi tạo giá trị không hợp lệ
    private ImageView ivCartMenuBottom, ivCartLocation, ivCartAboutUs, ivCartIconSelf;

    private final List<String> orderStatuses = Arrays.asList(
            "WAITING",   // Chờ xử lý/xác nhận
            "SHIPPING",  // Đang giao
            "RECEIVED",  // Đã nhận (Có thể là trạng thái cho phép đánh giá)
            "REVIEWED",  // Đã đánh giá
            "CANCELLED", // Đã hủy (Thêm nếu backend có trạng thái này)
            "ERROR"      // Lỗi (Ít dùng)
    );

    private ActivityResultLauncher<Intent> reviewActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        currentUserId = getIntent().getLongExtra("userId", -1L);
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE); // Thay "AppPrefs" bằng tên SharedPreferences của bạn
        authToken = prefs.getString("token", "");
        if (currentUserId <= 0) {
            Log.e(TAG, "Invalid User ID received: " + currentUserId + ". Cannot load order history.");
            Toast.makeText(this, "Lỗi: Không thể tải lịch sử đơn hàng.", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity nếu không có ID hợp lệ
            return;
        }
        Log.d(TAG, "OrderHistoryActivity started for UserID: " + currentUserId);

        toolbar = findViewById(R.id.toolbar); // Đảm bảo ID toolbar đúng trong layout
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Theo dõi đơn hàng"); // Đặt tiêu đề
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Ánh xạ TabLayout và ViewPager2
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        View cartBottomNavLayoutContainer = findViewById(R.id.bottomNavContainer);

        if (cartBottomNavLayoutContainer != null) {
            // 2. Tìm các ImageView con BÊN TRONG cartBottomNavLayoutContainer
            // Sử dụng các ID đã được định nghĩa trong activity_cart.xml cho thanh điều hướng
            ivCartMenuBottom = cartBottomNavLayoutContainer.findViewById(R.id.ivMenuBottom);
            ivCartLocation = cartBottomNavLayoutContainer.findViewById(R.id.ivLocation);
            ivCartAboutUs = cartBottomNavLayoutContainer.findViewById(R.id.ivaboutus);
            ivCartIconSelf = cartBottomNavLayoutContainer.findViewById(R.id.ivcart);

            // Log để kiểm tra
            if (ivCartMenuBottom == null) Log.w(TAG, "setupViews: ivCartMenuBottom not found.");
            if (ivCartLocation == null) Log.w(TAG, "setupViews: ivCartLocation not found.");
            if (ivCartAboutUs == null) Log.w(TAG, "setupViews: ivCartAboutUs not found.");
            if (ivCartIconSelf == null) Log.w(TAG, "setupViews: ivCartIconSelf not found.");

        } else {
            Log.e(TAG, "setupViews: Bottom navigation layout (R.id.cartBottomNavContainer) not found!");
        }
        // Đăng ký ActivityResultLauncher (Nên gọi trước khi setup ViewPager)
        setupActivityResultLaunchers();

        // Setup ViewPager và Adapter
        setupViewPager();

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        // Xử lý click cho icon Home (ivCartMenuBottom)
        if (ivCartMenuBottom != null) {
            ivCartMenuBottom.setOnClickListener(v -> {
                Log.d(TAG, "Home icon clicked from CartActivity");
                Intent intent = new Intent(OrderHistoryActivity.this, HomeActivity.class);
                intent.putExtra("token", authToken);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Đóng CartActivity để không quay lại bằng nút back
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartMenuBottom is null!");
        }

        // Xử lý click cho icon Location (ivCartLocation)
        if (ivCartLocation != null) {
            ivCartLocation.setOnClickListener(v -> {
                Log.d(TAG, "Location icon clicked from CartActivity");
                Intent intent = new Intent(OrderHistoryActivity.this, AboutAppActivity.class); // Hoặc Activity khác
                // intent.putExtra("token", authToken); // Nếu cần
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartLocation is null!");
        }

        // Xử lý click cho icon About Us (ivCartAboutUs)
        if (ivCartAboutUs != null) {
            ivCartAboutUs.setOnClickListener(v -> {
                Log.d(TAG, "About Us icon clicked from CartActivity");
                Intent intent = new Intent(OrderHistoryActivity.this, AboutUsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartAboutUs is null!");
        }

        // Xử lý click cho icon User/Profile (ivCartIconSelf)
        if (ivCartIconSelf != null) {
            ivCartIconSelf.setOnClickListener(v -> {
                Log.d(TAG, "User/Profile icon clicked from CartActivity");
                Intent intent = new Intent(OrderHistoryActivity.this, CartActivity.class);
                intent.putExtra("token", authToken);
                startActivity(intent);
                // Không nên finish() ở đây nếu người dùng có thể muốn quay lại giỏ hàng
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartIconSelf is null!");
        }
    }
    private void setupActivityResultLaunchers() {
        reviewActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Callback này được gọi khi Activity được khởi chạy bằng launcher này kết thúc
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Nếu kết quả là OK (nghĩa là người dùng đã thực hiện thành công hành động, vd: đánh giá)
                        Log.d(TAG, "Received RESULT_OK from review activity. Refreshing order fragments...");
                        // Gọi hàm để làm mới dữ liệu trong các Fragment con
                        refreshOrderFragments();
                        Toast.makeText(this, "Đã cập nhật danh sách đơn hàng.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Kết quả không phải OK (người dùng có thể đã nhấn back, hoặc có lỗi)
                        Log.d(TAG, "Review activity returned without RESULT_OK, result code: " + result.getResultCode());
                        // Không cần làm gì thêm trong trường hợp này
                    }
                });
    }

    /**
     * Cấu hình ViewPager2 và TabLayout.
     */
    private void setupViewPager() {
        // Tạo Adapter cho ViewPager
        pagerAdapter = new OrdersPagerAdapter(this, currentUserId, orderStatuses);
        viewPager.setAdapter(pagerAdapter);

        // Kết nối TabLayout với ViewPager2 để hiển thị các Tab tương ứng
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Lấy tiêu đề cho từng tab từ Adapter
            tab.setText(pagerAdapter.getPageTitle(position));
        }).attach();
    }


    /**
     * Làm mới dữ liệu trong các Fragment con của ViewPager.
     * Cách đơn giản nhất là tạo lại Adapter và gán lại cho ViewPager.
     */
    private void refreshOrderFragments() {
        if (pagerAdapter != null && viewPager != null) {
            Log.d(TAG, "Refreshing fragments by recreating PagerAdapter.");
            // Lưu lại vị trí tab đang được chọn (nếu muốn quay lại đúng tab đó)
            // int currentItem = viewPager.getCurrentItem();

            // Tạo một instance mới của Adapter với dữ liệu (userId) hiện tại
            pagerAdapter = new OrdersPagerAdapter(this, currentUserId, orderStatuses);
            // Gán Adapter mới cho ViewPager. Thao tác này sẽ khiến ViewPager
            // yêu cầu tạo lại các Fragment cần thiết (bao gồm cả Fragment đang hiển thị)
            viewPager.setAdapter(pagerAdapter);

            // Cần phải gắn lại TabLayoutMediator sau khi set Adapter mới
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                tab.setText(pagerAdapter.getPageTitle(position));
            }).attach();

            // (Tùy chọn) Khôi phục lại tab đang được chọn trước đó
            // viewPager.setCurrentItem(currentItem, false); // false để không có hiệu ứng chuyển tab

        } else {
            Log.w(TAG, "PagerAdapter or ViewPager is null during refresh attempt. Cannot refresh.");
        }
    }

    /**
     * Phương thức này được gọi bởi các Fragment con (OrderListFragment)
     * khi người dùng nhấn nút để chuyển sang màn hình đánh giá.
     * Nó sử dụng ActivityResultLauncher đã đăng ký để khởi chạy Activity
     * và chờ kết quả trả về.
     * @param intent Intent đã được chuẩn bị bởi Fragment để mở Activity đánh giá.
     */
    public void launchReviewActivity(Intent intent) {
        if (reviewActivityResultLauncher != null) {
            Log.d(TAG, "Launching review activity for result using launcher...");
            // Khởi chạy Activity (vd: ProductRatingActivity) và chờ kết quả
            reviewActivityResultLauncher.launch(intent);
        } else {
            // Trường hợp hiếm gặp khi launcher chưa được khởi tạo
            Log.e(TAG, "reviewActivityResultLauncher is not initialized! Cannot launch for result.");
            Toast.makeText(this, "Lỗi: Không thể đăng ký làm mới tự động.", Toast.LENGTH_SHORT).show();
            // Fallback: Vẫn mở Activity nhưng sẽ không tự động làm mới khi quay lại
            startActivity(intent);
        }
    }

    // Xử lý sự kiện nhấn nút back trên Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        // Điều hướng quay lại HomeActivity (hoặc màn hình trước đó)
        Intent intent = new Intent(OrderHistoryActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Đóng Activity hiện tại
        return true; // Đã xử lý
    }

    // (Tùy chọn) Xử lý nút Back vật lý tương tự nếu bạn muốn đảm bảo nó cũng về Home
    // @Override
    // public void onBackPressed() {
    //     Intent intent = new Intent(OrderHistoryActivity.this, HomeActivity.class);
    //     intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //     startActivity(intent);
    //     finish();
    //     // Hoặc gọi super.onBackPressed(); nếu chỉ muốn hành vi mặc định (quay lại màn hình trước trong stack)
    // }
}