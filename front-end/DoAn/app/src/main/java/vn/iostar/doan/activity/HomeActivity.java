package vn.iostar.doan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.CategoryAdapter;
import vn.iostar.doan.adapter.ProductAdapter;
import vn.iostar.doan.adapter.ViewFlipperManager;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Address; // Import Address model
import vn.iostar.doan.model.Category;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User; // Import User model

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity"; // Thêm TAG
    private String authToken;
    private RecyclerView rcCate, rclProduct;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categoryList;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private SearchView searchView;
    private ViewFlipper viewFlipperMain;
    private ImageView imgUser, ivCart;
    private TextView tvLocationAddress; // Khai báo TextView hiển thị địa chỉ


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //init data
        AnhXa();
        loadUserInfoAndInitData();
        setupSearchView();
        setupUserMenu();
        setupBottomNavigation();
    }
    private void AnhXa() {
        // Ánh xạ
        rcCate = findViewById(R.id.rc_category);
        rclProduct = findViewById(R.id.rclcon);
        searchView = findViewById(R.id.searchView);
        viewFlipperMain = findViewById(R.id.viewFlipperMain);
        tvLocationAddress = findViewById(R.id.tvLocationAddress); // Ánh xạ TextView địa chỉ

        // Bắt đầu từ header
        View headerLayout = findViewById(R.id.headerLayout); // Nếu tvLocationAddress nằm trong headerLayout
        // tvLocationAddress = headerLayout.findViewById(R.id.tvLocationAddress); // Ánh xạ nếu cần thiết
        imgUser = headerLayout.findViewById(R.id.imgUser);


        // Bottom navigation
        View bottomNavLayout = findViewById(R.id.bottom_navigation);
        ivCart = bottomNavLayout.findViewById(R.id.ivcart);

        // Khởi tạo Adapter sản phẩm sớm với list rỗng
        productAdapter = new ProductAdapter(this, new ArrayList<>(), null);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rclProduct.setLayoutManager(gridLayoutManager);
        rclProduct.setAdapter(productAdapter);

        // Đảm bảo các TextView khác trong header cũng được ánh xạ nếu cần
        // TextView tvAppName = headerLayout.findViewById(R.id.tvAppName);
        // ImageView ivLocation = headerLayout.findViewById(R.id.ivLocation);

    }
    private void loadUserInfoAndInitData() {
        String tokenFromIntent = getIntent().getStringExtra("token");
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        if (tokenFromIntent != null && !tokenFromIntent.isEmpty()) {
            this.authToken = tokenFromIntent;
            // Lưu lại token vào SharedPreferences nếu muốn
            prefs.edit().putString("token", authToken).apply();
        } else {
            // Lấy token từ SharedPreferences nếu không có trong Intent
            this.authToken = prefs.getString("token", "");
        }

        if (this.authToken == null || this.authToken.isEmpty()) {
            Log.e(TAG, "Token is missing! Redirecting to Login.");
            Toast.makeText(this, "Lỗi xác thực, vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Đóng HomeActivity
            return;
        }

        // Gán lại adapter với token sau khi xác định được token
        productAdapter = new ProductAdapter(this, new ArrayList<>(), this.authToken);
        rclProduct.setAdapter(productAdapter);

        // <<< SỬA LOGIC LẤY THÔNG TIN USER VÀ HIỂN THỊ ĐỊA CHỈ MẶC ĐỊNH >>>
        ApiService.apiService.getUserInfo("Bearer " + this.authToken)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            List<Address> addresses = user.getAddresses(); // Lấy danh sách địa chỉ

                            Log.d(TAG, "Addresses list received for Home. Size: " + (addresses != null ? addresses.size() : "null"));

                            Address defaultAddress = null; // Biến để lưu địa chỉ mặc định

                            if (addresses != null && !addresses.isEmpty()) {
                                for (Address address : addresses) {
                                    if (address != null && address.isDefaultAddress()) {
                                        defaultAddress = address; // Tìm thấy địa chỉ mặc định
                                        Log.d(TAG, "Found default address in the list for Home.");
                                        break;
                                    }
                                }

                                if (defaultAddress != null) {
                                    String fullAddress = defaultAddress.getHouseNumber();
                                    tvLocationAddress.setText(fullAddress);
                                    Log.d(TAG, "Default address displayed on Home: " + fullAddress);
                                } else {
                                    // Danh sách không rỗng, nhưng không có địa chỉ nào là mặc định
                                    tvLocationAddress.setText("Chưa có địa chỉ mặc định.");
                                    Log.w(TAG, "Address list is not empty, but no default address found for Home.");
                                }

                            } else {
                                // Nếu danh sách địa chỉ rỗng hoặc null
                                tvLocationAddress.setText("Chưa có địa chỉ.");
                                Log.d(TAG, "Address list is null or empty for Home.");
                            }
                        } else {
                            // Xử lý lỗi khi gọi API thành công nhưng response không thành công
                            Log.e(TAG, "Error fetching user info for Home: " + response.code());
                            tvLocationAddress.setText("Lỗi tải địa chỉ."); // Hiển thị thông báo lỗi
                            if (response.code() == 401 || response.code() == 403) {
                                Log.e(TAG, "Authentication error fetching user info for Home.");
                                // Xử lý lỗi xác thực nếu cần (ví dụ: chuyển về màn hình đăng nhập)
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        // Xử lý lỗi kết nối API
                        Log.e(TAG, "Failed to connect to server for Home", t);
                        tvLocationAddress.setText("Lỗi kết nối."); // Hiển thị thông báo lỗi kết nối
                    }
                });
        // <<< KẾT THÚC SỬA LOGIC LẤY THÔNG TIN USER VÀ HIỂN THỊ ĐỊA CHỈ MẶC ĐỊNH >>>


        getAllProducts();
        GetCategory();
        setupViewFlipper();
    }
    private void getAllProducts() {
        if (productAdapter == null || this.authToken == null) {
            Log.e(TAG, "Adapter or token is null. Cannot fetch products.");
            return;
        }
        ApiService.apiService.getAllProducts()
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            productList = response.body();
                            productAdapter.updateList(productList);
                        } else {
                            Log.e(TAG, "Response error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Log.e(TAG, "Request failed", t);
                    }
                });
    }
    private void setupViewFlipper() {
        ViewFlipperManager.setupViewFlipper(viewFlipperMain, this);
    }

    private void GetCategory() {
        ApiService.apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList = response.body(); // Lấy danh sách categories

                    // Khởi tạo Adapter
                    categoryAdapter = new CategoryAdapter(HomeActivity.this, categoryList, new CategoryAdapter.OnCategoryClickListener() {
                        @Override
                        public void onCategoryClick(Category category) {
                            // Khi click vào category sẽ chạy vào đây
                            getProductsByCategory(category.getCategoryId());
                        }
                    });
                    rcCate.setHasFixedSize(true);

                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(
                            HomeActivity.this, LinearLayoutManager.HORIZONTAL, false
                    );
                    rcCate.setLayoutManager(layoutManager);
                    rcCate.setAdapter(categoryAdapter);
                    categoryAdapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Response Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage(), t);
            }
        });
    }

    private void getProductsByCategory(Long categoryId) {
        if (productAdapter == null || this.authToken == null) {
            Log.e(TAG, "Adapter or token is null. Cannot fetch products.");
            return;
        }
        ApiService.apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    if (viewFlipperMain.getVisibility() == View.VISIBLE) {
                        viewFlipperMain.setVisibility(View.GONE); // Ẩn flipper nếu đang hiện
                    }
                    productAdapter.updateList(products);
                } else {
                    Log.e(TAG, "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage(), t);
            }
        });
    }
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });
    }

    private void filterProducts(String keyword) {
        if (productList == null || productAdapter == null) return; // Kiểm tra productList null
        if (viewFlipperMain.getVisibility() == View.VISIBLE) {
            viewFlipperMain.setVisibility(View.GONE); // Ẩn flipper nếu đang hiện
        }
        filteredProductList = new ArrayList<>(); // Đảm bảo filteredProductList được khởi tạo
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredProductList.add(product);
            }
        }
        productAdapter.updateList(filteredProductList);  // Viết thêm 1 hàm updateList trong adapter
    }
    private void setupUserMenu() {
        imgUser.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(HomeActivity.this, imgUser);
            popupMenu.getMenuInflater().inflate(R.menu.menu_user, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    intent.putExtra("token", authToken); // <<< Dùng authToken đã lưu
                    startActivity(intent);
                    return true;
                } else if (id == R.id.menu_logout) {
                    // Xóa token đã lưu (quan trọng)
                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    prefs.edit().remove("token").apply();

                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Đóng HomeActivity
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }
    private void setupBottomNavigation() {
        if (ivCart != null) {
            ivCart.setOnClickListener(v -> {
                Log.d(TAG, "Cart icon clicked"); // Thêm log để kiểm tra
                Intent intent = new Intent(HomeActivity.this, CartActivity.class);
                intent.putExtra("token", authToken);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "Cannot set listener, ivCart is null!");
        }

        // Thêm listener cho các icon khác nếu cần
        // ImageView ivMenuBottom = findViewById(R.id.ivMenuBottom);
        // if (ivMenuBottom != null) {
        //     ivMenuBottom.setOnClickListener(v -> {
        //         // Không làm gì hoặc về home (vì đang ở home rồi)
        //         Toast.makeText(this, "Bạn đang ở Trang chủ", Toast.LENGTH_SHORT).show();
        //     });
        // }

        // ImageView ivLocation = findViewById(R.id.ivLocation); // ID trong XML là ivLocation
        // if (ivLocation != null) {
        //    ivLocation.setOnClickListener(v -> {
        //        // Mở màn hình liên quan đến vị trí/bản đồ
        //        // Intent locationIntent = new Intent(HomeActivity.this, LocationActivity.class);
        //        // locationIntent.putExtra("token", authToken);
        //        // startActivity(locationIntent);
        //         Toast.makeText(this, "Chức năng Vị trí sắp ra mắt", Toast.LENGTH_SHORT).show();
        //    });
        // }
        ImageView ivAboutUs = findViewById(R.id.ivaboutus); // ID trong XML là ivaboutus
        if (ivAboutUs != null) {
            ivAboutUs.setOnClickListener(v -> {
                 Intent aboutIntent = new Intent(HomeActivity.this, AboutUsActivity.class);
                 aboutIntent.putExtra("token", authToken);
                 startActivity(aboutIntent);
                Toast.makeText(this, "Chức năng Giới thiệu sắp ra mắt", Toast.LENGTH_SHORT).show();
            });
        }
    }
}