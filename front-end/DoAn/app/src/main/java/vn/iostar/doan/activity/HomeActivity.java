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
import vn.iostar.doan.model.User1;

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
                .enqueue(new Callback<User1>() {
                    @Override
                    public void onResponse(Call<User1> call, Response<User1> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            User1 user = response.body();
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
                    public void onFailure(Call<User1> call, Throwable t) {
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


package vn.iostar.doan.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View; // Import View để dùng GONE/VISIBLE
import android.widget.ProgressBar; // Import ProgressBar nếu có
import android.widget.TextView; // Import TextView nếu có thông báo lỗi
import android.widget.Toast;

import androidx.annotation.NonNull; // Import NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson; // Import Gson
import com.google.gson.GsonBuilder; // Import GsonBuilder
import com.google.gson.JsonSyntaxException; // Import JsonSyntaxException
import com.google.gson.reflect.TypeToken; // Import TypeToken

import java.io.IOException; // Import IOException
import java.lang.reflect.Type; // Import Type
import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import okhttp3.Call; // Import okhttp3.Call
import okhttp3.Callback; // Import okhttp3.Callback
import okhttp3.OkHttpClient; // Import OkHttpClient
import okhttp3.Request; // Import okhttp3.Request
import okhttp3.Response; // Import okhttp3.Response
import okhttp3.ResponseBody; // Import ResponseBody
// Bỏ các import của Retrofit nếu không dùng nữa
// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.ImagesViewPageAdapter;
import vn.iostar.doan.adapter.ProductAdapter;
// Bỏ import ApiService nếu chỉ dùng OkHttp trực tiếp
// import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.image.Images;

public class HomeActivity extends AppCompatActivity {

    // --- Views ---
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private RecyclerView recyclerViewProducts;
    private ProgressBar progressBarProducts; // Tùy chọn: Thêm ProgressBar
    private TextView tvProductError; // Tùy chọn: Thêm TextView báo lỗi

    // --- Adapters ---
    private ImagesViewPageAdapter imagesAdapter;
    private ProductAdapter productAdapter;

    // --- Data ---
    private List<Images> imagesList;
    private List<Product> productList;

    // --- OkHttp & Gson ---
    private final OkHttpClient client = new OkHttpClient(); // Tạo instance OkHttpClient
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Hoặc định dạng khác nếu cần
            .create(); // Tạo instance Gson
    private static final String BASE_URL = "http://192.168.1.7:8080/"; // <<< Đảm bảo URL đúng

    // --- Handler cho Slideshow ---
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private static final long SLIDER_DELAY = 3000;
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // <<< Sử dụng layout mới

        // Ánh xạ Views
        viewPager = findViewById(R.id.viewpage);
        circleIndicator = findViewById(R.id.circle_indicator);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        // (Tùy chọn) Ánh xạ ProgressBar và TextView lỗi nếu bạn thêm vào layout
        // progressBarProducts = findViewById(R.id.progressBarProducts);
        // tvProductError = findViewById(R.id.tvProductError);

        // --- Thiết lập Slideshow Ảnh ---
        setupImageSlider();

        // --- Thiết lập Danh sách Sản phẩm ---
        setupProductRecyclerView();

        // --- Tải dữ liệu Sản phẩm từ API bằng OkHttp ---
        fetchProductsWithOkHttp();
    }

    // Thiết lập ViewPager (Giữ nguyên)
    private void setupImageSlider() {
        imagesList = getListImages();
        imagesAdapter = new ImagesViewPageAdapter(this, imagesList);
        viewPager.setAdapter(imagesAdapter);
        circleIndicator.setViewPager(viewPager);
        // ... (Phần auto-run và listener giữ nguyên) ...
        runnable = new Runnable() {
            @Override
            public void run() {
                if (imagesList == null || imagesList.isEmpty()) return;
                int currentItem = viewPager.getCurrentItem();
                int nextItem = (currentItem == imagesList.size() - 1) ? 0 : currentItem + 1;
                viewPager.setCurrentItem(nextItem);
            }
        };
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, SLIDER_DELAY);
            }
            @Override public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, SLIDER_DELAY);
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    handler.removeCallbacks(runnable);
                }
            }
        });
    }

    // Thiết lập RecyclerView (Giữ nguyên việc sửa constructor Adapter)
    private void setupProductRecyclerView() {
        productList = new ArrayList<>();
        // *** SỬA LỖI CONSTRUCTOR: TRUYỀN CONTEXT (this) ***
        productAdapter = new ProductAdapter(this, productList); // Khởi tạo Adapter
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productAdapter);
    }

    // --- Gọi API bằng OkHttp ---
    private void fetchProductsWithOkHttp() {
        Log.d(TAG, "Fetching products using OkHttp...");
        showProductLoading(true); // Hiển thị loading (nếu có)

        String url = BASE_URL + "api/products"; // <<< Endpoint lấy sản phẩm
        Request request = new Request.Builder()
                .url(url)
                // .addHeader("Authorization", "Bearer YOUR_TOKEN") // <<< THÊM TOKEN NẾU API YÊU CẦU
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Fetch Products onFailure: ", e);
                // Cập nhật UI trên Main Thread
                runOnUiThread(() -> {
                    showProductLoading(false);
                    showProductError("Lỗi mạng khi tải sản phẩm.");
                    // Toast.makeText(HomeActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                String responseData = null;
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        final String errorMsg = "Lỗi Server: " + response.code() + " " + response.message();
                        String errorBodyStr = (responseBody != null) ? responseBody.string() : "null";
                        Log.e(TAG, "OkHttp Fetch Products Error: " + errorMsg + ", Body: " + errorBodyStr);
                        // Cập nhật UI trên Main Thread
                        runOnUiThread(() -> {
                            showProductLoading(false);
                            showProductError(errorMsg);
                            // Toast.makeText(HomeActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        });
                        return; // Kết thúc sớm
                    }

                    if (responseBody != null) {
                        responseData = responseBody.string();
                        Log.d(TAG, "OkHttp Fetch Products Success JSON: " + responseData);
                    } else {
                        Log.e(TAG, "OkHttp Fetch Products Error: Response body is null");
                        runOnUiThread(() -> {
                            showProductLoading(false);
                            showProductError("Không nhận được dữ liệu sản phẩm.");
                            // Toast.makeText(HomeActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                        });
                        return; // Kết thúc sớm
                    }

                    // Parse JSON bằng Gson
                    Type listType = new TypeToken<ArrayList<Product>>() {}.getType();
                    final List<Product> fetchedProducts = gson.fromJson(responseData, listType);

                    if (fetchedProducts == null) {
                        Log.e(TAG, "Parsed product list is null.");
                        runOnUiThread(() -> {
                            showProductLoading(false);
                            showProductError("Lỗi định dạng dữ liệu sản phẩm.");
                            // Toast.makeText(HomeActivity.this, "Lỗi dữ liệu", Toast.LENGTH_SHORT).show();
                        });
                        return; // Kết thúc sớm
                    }

                    // Cập nhật UI trên Main Thread
                    runOnUiThread(() -> {
                        showProductLoading(false);
                        if (fetchedProducts.isEmpty()) {
                            showProductError("Không có sản phẩm nào."); // Hoặc thông báo khác
                        } else {
                            recyclerViewProducts.setVisibility(View.VISIBLE); // Hiện RecyclerView
                            if(tvProductError != null) tvProductError.setVisibility(View.GONE); // Ẩn lỗi
                            productAdapter.updateProducts(fetchedProducts); // Cập nhật adapter
                        }
                    });

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON Parsing Error: ", e);
                    Log.e(TAG, "Failed to parse JSON: " + responseData);
                    runOnUiThread(() -> {
                        showProductLoading(false);
                        showProductError("Lỗi xử lý dữ liệu sản phẩm (JSON).");
                        // Toast.makeText(HomeActivity.this, "Lỗi dữ liệu JSON", Toast.LENGTH_SHORT).show();
                    });
                } catch (IOException e) {
                    Log.e(TAG, "IOException reading product response body: ", e);
                    runOnUiThread(() -> {
                        showProductLoading(false);
                        showProductError("Lỗi đọc dữ liệu sản phẩm.");
                        // Toast.makeText(HomeActivity.this, "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected error in product onResponse: ", e);
                    runOnUiThread(() -> {
                        showProductLoading(false);
                        showProductError("Đã xảy ra lỗi không mong muốn.");
                        // Toast.makeText(HomeActivity.this, "Lỗi không xác định", Toast.LENGTH_SHORT).show();
                    });
                }
            } // Kết thúc onResponse
        }); // Kết thúc enqueue
    }
    // --- Kết thúc gọi API bằng OkHttp ---

    // --- Hàm helper quản lý UI cho phần Product ---
    private void showProductLoading(boolean isLoading) {
        if (progressBarProducts != null) {
            progressBarProducts.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        // Có thể ẩn/hiện RecyclerView khi loading nếu muốn
        // recyclerViewProducts.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showProductError(String message) {
        // Ẩn RecyclerView
        recyclerViewProducts.setVisibility(View.GONE);
        // Hiện TextView báo lỗi
        if (tvProductError != null) {
            tvProductError.setText(message);
            tvProductError.setVisibility(View.VISIBLE);
        } else { // Nếu không có TextView lỗi riêng, dùng Toast
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
    // --- Kết thúc hàm helper ---

    // Hàm tạo danh sách ảnh cứng (Giữ nguyên)
    private List<Images> getListImages() {
        List<Images> list = new ArrayList<>();
        list.add(new Images(R.drawable.qc1));
        list.add(new Images(R.drawable.qc2));
        list.add(new Images(R.drawable.qc3));
        list.add(new Images(R.drawable.qc4));
        return list;
    }

    // Xử lý lifecycle cho slideshow (Giữ nguyên)
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Removing slider callback");
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Posting slider callback");
        handler.postDelayed(runnable, SLIDER_DELAY);
    }

    // Bỏ lớp Adapter lồng nhau
}