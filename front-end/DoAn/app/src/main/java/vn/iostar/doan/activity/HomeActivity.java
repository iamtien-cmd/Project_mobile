package vn.iostar.doan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import vn.iostar.doan.model.Address;
import vn.iostar.doan.model.Category;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rcCate, rclProduct;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private List<Category> categoryList;
    private List<Product> productList;
    private List<Product> filteredProductList;
    private SearchView searchView;
    private ViewFlipper viewFlipperMain;
    private ImageView imgUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //init data
        AnhXa();
        loadUserInfo();
        setupViewFlipper();
        getAllProducts();
        GetCategory();
        setupSearchView();
        setupUserMenu();
    }
    private void AnhXa() {
        // Ánh xạ
        rcCate = findViewById(R.id.rc_category);
        rclProduct = findViewById(R.id.rclcon);
        searchView = findViewById(R.id.searchView);
        viewFlipperMain = findViewById(R.id.viewFlipperMain);
        View headerLayout = findViewById(R.id.headerLayout); // Bắt đầu từ header
        imgUser = headerLayout.findViewById(R.id.imgUser);
    }
    private void loadUserInfo() {
        String token = getIntent().getStringExtra("token");

        if (token == null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            token = prefs.getString("token", "");
        }

        ApiService.apiService.getUserInfo("Bearer " + token)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User user = response.body();
                            if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
                                Address address = user.getAddresses().get(0);
                                String fullAddress = address.getHouseNumber() + ", " +
                                        address.getDistrict() + ", " +
                                        address.getCity() + ", " +
                                        address.getCountry();

                                TextView tvLocationAddress = findViewById(R.id.tvLocationAddress);
                                tvLocationAddress.setText(fullAddress);
                            }
                        } else {
                            Log.e("API", "Error fetching user info: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("API", "Failed to connect to server", t);
                    }
                });
    }
    private void getAllProducts() {
        ApiService.apiService.getAllProducts()
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            productList = response.body();
                            filteredProductList = new ArrayList<>(productList);
                            setupProductRecyclerView(filteredProductList);
                        } else {
                            Log.e("GetProducts", "Response error: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        Log.e("GetProducts", "Request failed", t);
                    }
                });
    }
    private void setupViewFlipper() {
        ViewFlipperManager.setupViewFlipper(viewFlipperMain, this);
    }

    private void setupProductRecyclerView(List<Product> products) {
        productAdapter = new ProductAdapter(this, products);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rclProduct.setLayoutManager(gridLayoutManager);
        rclProduct.setAdapter(productAdapter);
    }

    private void GetCategory() {
        // Gọi trực tiếp ApiService đã được khai báo sẵn
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
                    Log.e("GetCategory", "Response Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                Log.e("GetCategory", "Failure: " + t.getMessage(), t);
            }
        });
    }

    private void getProductsByCategory(Long categoryId) {
        ApiService.apiService.getProductsByCategory(categoryId).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body();
                    if (viewFlipperMain.getVisibility() == View.VISIBLE) {
                        viewFlipperMain.setVisibility(View.GONE); // Ẩn flipper nếu đang hiện
                    }
                    // TODO: Hiển thị danh sách sản phẩm ra RecyclerView
                    // Ví dụ bạn có 1 productAdapter và rcProduct
                    productAdapter.updateList(products);
                    rclProduct.setAdapter(productAdapter);
                    productAdapter.notifyDataSetChanged();
                } else {
                    Log.e("getProductsByCategory", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("getProductsByCategory", "Failure: " + t.getMessage(), t);
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
        if (viewFlipperMain.getVisibility() == View.VISIBLE) {
            viewFlipperMain.setVisibility(View.GONE); // Ẩn flipper nếu đang hiện
        }
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);  // Viết thêm 1 hàm updateList trong adapter
    }
    private void setupUserMenu() {
        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(HomeActivity.this, imgUser);
                popupMenu.getMenuInflater().inflate(R.menu.menu_user, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.menu_profile) {
                            String token = getIntent().getStringExtra("token");
                            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                            intent.putExtra("token", token);
                            startActivity(intent);
                            return true;
                        } else if (id == R.id.menu_logout) {
                            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }
                });

                popupMenu.show();
            }
        });
    }


}

