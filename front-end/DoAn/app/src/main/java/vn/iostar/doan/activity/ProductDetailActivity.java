package vn.iostar.doan.activity; // Đảm bảo package này đúng

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import me.relex.circleindicator.CircleIndicator3;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.adapter.ImagesViewPager2Adapter;
import vn.iostar.doan.adapter.ProductAdapter;
import vn.iostar.doan.model.Cart;
import vn.iostar.doan.model.Comment;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.modelRequest.CartActionRequest;


public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private ViewPager2 viewPagerProductImages;
    private CircleIndicator3 indicator;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView textViewError;
    private NestedScrollView nestedScrollViewContent;
    private TextView textViewProductName;
    private TextView textViewProductPrice;
    private TextView textViewProductQuantity;
    private TextView textViewProductCategory;
    private TextView textViewProductDescription;
    private TextView textViewRelatedTitle;
    private RecyclerView recyclerViewRelatedProducts;
    private TextView textViewCommentsTitle;
    private RecyclerView recyclerViewComments;
    private FloatingActionButton fabAddToCart;

    private ImagesViewPager2Adapter imagesAdapter;
    private ProductAdapter relatedProductAdapter;
    private ApiService apiService;
    private long currentProductId = -1;
    private String authToken;
    private ImageView ivcart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        AnhXa();
        setupToolbar();

        apiService = ApiService.apiService;

        currentProductId = getIntent().getLongExtra("productId", -1);
        authToken = getIntent().getStringExtra("token");


        if (currentProductId != -1) {
            fetchProductDetails(currentProductId);
        } else {
            showError("Không tìm thấy mã sản phẩm!");
        }
        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Authentication token is missing! Cannot proceed.");
            Toast.makeText(this, "Lỗi xác thực người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fabAddToCart.setOnClickListener(v -> {
            if (currentProductId != -1) {
                int quantityToAdd = 1;
                addProductToCart(currentProductId, quantityToAdd);
            } else {
                Toast.makeText(this, "Lỗi: Không thể thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Cannot add to cart. ProductId: " + currentProductId + ", Token valid: " + (authToken != null && !authToken.isEmpty()));
            }
        });
        if (ivcart != null) { // Kiểm tra null phòng trường hợp layout có vấn đề
            ivcart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Tạo Intent để chuyển sang CartActivity
                    Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                    // Nếu CartActivity cần token, bạn có thể truyền nó qua intent
                    if (authToken != null && !authToken.isEmpty()) {
                        intent.putExtra("token", authToken);
                    } else {
                        Log.w(TAG, "Starting CartActivity without auth token.");
                        // Có thể hiển thị thông báo yêu cầu đăng nhập nếu cần
                    }
                    startActivity(intent);
                }
            });
        } else {
            Log.e(TAG, "ivcart ImageView not found in the layout!");
        }
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void AnhXa() {
        viewPagerProductImages = findViewById(R.id.viewPagerProductImages);
        indicator = findViewById(R.id.indicator);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        textViewError = findViewById(R.id.textViewError);
        nestedScrollViewContent = findViewById(R.id.nestedScrollView);

        textViewProductName = findViewById(R.id.textViewProductName);
        textViewProductPrice = findViewById(R.id.textViewProductPrice);
        textViewProductQuantity = findViewById(R.id.textViewProductQuantity);
        textViewProductCategory = findViewById(R.id.textViewProductCategory);
        textViewProductDescription = findViewById(R.id.textViewProductDescription);
        textViewRelatedTitle = findViewById(R.id.textViewRelatedTitle);
        recyclerViewRelatedProducts = findViewById(R.id.recyclerViewRelatedProducts);
        textViewCommentsTitle = findViewById(R.id.textViewCommentsTitle);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        fabAddToCart = findViewById(R.id.fabAddToCart);
        ivcart = findViewById(R.id.ivcart);

        recyclerViewRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedProductAdapter = new ProductAdapter(this, new java.util.ArrayList<>(),null);
        recyclerViewRelatedProducts.setAdapter(relatedProductAdapter);
        recyclerViewRelatedProducts.setHasFixedSize(true);

    }

    private void fetchProductDetails(long productId) {
        showLoading(true);
        showError(null);

        // Gọi hàm getProductDetails từ instance apiService đã lấy
        Call<Product> call = apiService.getProductDetails(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    displayProductDetails(product);
                    showContent(true);
                } else {
                    String errorMsg = "Không thể tải dữ liệu sản phẩm.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += "\nLỗi: " + response.code() + " - " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    } else {
                        errorMsg += "\nMã lỗi: " + response.code() + " - " + response.message();
                    }
                    Log.e(TAG, "API call failed: " + errorMsg);
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed: ", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void displayProductDetails(Product product) {
        textViewProductName.setText(product.getName());

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewProductPrice.setText(currencyFormatter.format(product.getPrice()));

        textViewProductQuantity.setText("Còn lại: " + product.getQuantity());
        if (product.getCategory() != null) {
            textViewProductCategory.setText("Danh mục: " + product.getCategory().getName());
            textViewProductCategory.setVisibility(View.VISIBLE);
        } else {
            textViewProductCategory.setVisibility(View.GONE);
        }
        textViewProductDescription.setText(product.getDescription());

        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
            imagesAdapter = new ImagesViewPager2Adapter(product.getImageUrls());
            viewPagerProductImages.setAdapter(imagesAdapter);

            indicator.setViewPager(viewPagerProductImages);
            indicator.setVisibility(product.getImageUrls().size() > 1 ? View.VISIBLE : View.GONE);
            viewPagerProductImages.setVisibility(View.VISIBLE);
        } else {
            viewPagerProductImages.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);
            Log.w(TAG, "Không có hình ảnh nào cho sản phẩm " + product.getProductId());
        }

        List<Product> related = product.getRelatedProducts();
        if (related != null && !related.isEmpty()) {
            textViewRelatedTitle.setVisibility(View.VISIBLE);
            recyclerViewRelatedProducts.setVisibility(View.VISIBLE);
            relatedProductAdapter.updateList(related);
        } else {
            textViewRelatedTitle.setVisibility(View.GONE);
            recyclerViewRelatedProducts.setVisibility(View.GONE);
        }

        List<Comment> comments = product.getComments();
        if (comments != null && !comments.isEmpty()) {
            textViewCommentsTitle.setVisibility(View.VISIBLE);
            recyclerViewComments.setVisibility(View.VISIBLE);
        } else {
            textViewCommentsTitle.setVisibility(View.GONE);
            recyclerViewComments.setVisibility(View.GONE);
        }
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            showContent(false);
            showError(null);
        }
    }

    private void showError(String message) {
        if (message != null) {
            textViewError.setText(message);
            textViewError.setVisibility(View.VISIBLE);
            showContent(false);
        } else {
            textViewError.setVisibility(View.GONE);
        }
    }

    private void showContent(boolean show) {
        if (nestedScrollViewContent != null) {
            nestedScrollViewContent.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            textViewProductName.setVisibility(show ? View.VISIBLE : View.GONE);
            textViewProductPrice.setVisibility(show ? View.VISIBLE : View.GONE);
            textViewProductQuantity.setVisibility(show ? View.VISIBLE : View.GONE);
            textViewProductCategory.setVisibility(show ? View.VISIBLE : View.GONE);
            textViewProductDescription.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        fabAddToCart.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void addProductToCart(long productId, int quantity) {
        if (apiService == null) {
            Log.e(TAG, "ApiService is not initialized!");
            Toast.makeText(this, "Lỗi dịch vụ, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true); // Hiện loading nếu muốn

        CartActionRequest request = new CartActionRequest(productId, quantity);
        String headerToken = "Bearer " + authToken;

        apiService.addToCart(headerToken, request)
                .enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(@NonNull Call<Cart> call, @NonNull Response<Cart> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Product added to cart successfully.");
                } else {
                    String errorMsg = "Lỗi khi thêm vào giỏ hàng.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                            if (response.code() == 400) {
                                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
                            } else if (response.code() == 401 || response.code() == 403) {
                                Toast.makeText(ProductDetailActivity.this, "Phiên đăng nhập hết hạn.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Lỗi máy chủ: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProductDetailActivity.this, errorMsg + " (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                        }
                        Log.e(TAG, "Failed to add to cart: " + response.code() + " - " + errorMsg);

                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                        Toast.makeText(ProductDetailActivity.this, "Lỗi không xác định khi thêm vào giỏ.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Cart> call, @NonNull Throwable t) {
                showLoading(false); // Ẩn loading
                Log.e(TAG, "API call failed (addToCart): ", t);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}