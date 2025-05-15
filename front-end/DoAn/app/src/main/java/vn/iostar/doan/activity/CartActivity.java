package vn.iostar.doan.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView; // Thêm import ImageView
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.CartAdapter;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Cart;
import vn.iostar.doan.model.CartItem;
import vn.iostar.doan.modelRequest.CartActionRequest;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private static final String TAG = "CartActivity";

    private String authToken;
    private Toolbar toolbar;
    private RecyclerView recyclerViewCart;
    private ProgressBar progressBarCart;
    private TextView textViewEmptyCart;

    private TextView textViewTotalValue;
    private CheckBox checkboxSelectAll;
    private AppCompatButton buttonCheckout;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();

    // --- Khai báo các ImageView cho Bottom Navigation của CartActivity ---
    private ImageView ivCartMenuBottom, ivCartLocation, ivCartAboutUs, ivCartIconSelf;
    // --- Hết khai báo ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo bạn đang sử dụng layout có RelativeLayout làm gốc và thanh điều hướng
        setContentView(R.layout.activity_cart); // Layout này phải là cái đã được chỉnh sửa

        authToken = getIntent().getStringExtra("token");
        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Auth token is missing!");
            Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setupViews();       // Ánh xạ tất cả các view, bao gồm cả bottom navigation
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        fetchCartData();
        setupBottomNavigation(); // Gọi hàm thiết lập bottom navigation
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
            finish(); // Hoặc onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews() {
        // Views của nội dung Cart
        recyclerViewCart = findViewById(R.id.recyclerViewCartItems);
        progressBarCart = findViewById(R.id.progressBarCart);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);
        toolbar = findViewById(R.id.toolbar);

        // Views của summary layout
        textViewTotalValue = findViewById(R.id.textViewTotalValue);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
        buttonCheckout = findViewById(R.id.buttonCheckout);

        // --- Ánh xạ các View cho Bottom Navigation ---
        // 1. Tìm LinearLayout cha chứa các icon (theo ID trong XML `activity_cart.xml`)
        View cartBottomNavLayoutContainer = findViewById(R.id.cartBottomNavContainer);

        if (cartBottomNavLayoutContainer != null) {
            // 2. Tìm các ImageView con BÊN TRONG cartBottomNavLayoutContainer
            // Sử dụng các ID đã được định nghĩa trong activity_cart.xml cho thanh điều hướng
            ivCartMenuBottom = cartBottomNavLayoutContainer.findViewById(R.id.ivCartMenuBottom);
            ivCartLocation = cartBottomNavLayoutContainer.findViewById(R.id.ivCartLocation);
            ivCartAboutUs = cartBottomNavLayoutContainer.findViewById(R.id.ivCartAboutUs);
            ivCartIconSelf = cartBottomNavLayoutContainer.findViewById(R.id.ivCartIconSelf);

            // Log để kiểm tra
            if (ivCartMenuBottom == null) Log.w(TAG, "setupViews: ivCartMenuBottom not found.");
            if (ivCartLocation == null) Log.w(TAG, "setupViews: ivCartLocation not found.");
            if (ivCartAboutUs == null) Log.w(TAG, "setupViews: ivCartAboutUs not found.");
            if (ivCartIconSelf == null) Log.w(TAG, "setupViews: ivCartIconSelf not found.");

        } else {
            Log.e(TAG, "setupViews: Bottom navigation layout (R.id.cartBottomNavContainer) not found!");
        }
        // --- Hết phần ánh xạ bottom navigation ---

        if(progressBarCart != null) progressBarCart.setVisibility(View.GONE);
        if(textViewEmptyCart != null) textViewEmptyCart.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItemList, this);
        if (recyclerViewCart != null) {
            recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewCart.setAdapter(cartAdapter);
        } else {
            Log.e(TAG, "recyclerViewCart is null in setupRecyclerView");
        }
    }

    private void setupListeners() {
        if (checkboxSelectAll != null) {
            setupSelectAllListener(); // Gọi hàm đã tách riêng
        } else {
            Log.e(TAG, "checkboxSelectAll is null, cannot set listener.");
        }

        if (buttonCheckout != null) {
            buttonCheckout.setOnClickListener(v -> {
                List<CartItem> selectedItems = new ArrayList<>();
                for (CartItem item : cartItemList) {
                    if (item.isSelected()) {
                        selectedItems.add(item);
                    }
                }
                if (!selectedItems.isEmpty()) {
                    ArrayList<Long> selectedItemIds = new ArrayList<>();
                    for (CartItem selectedItem : selectedItems) {
                        selectedItemIds.add(selectedItem.getCartItemId());
                    }
                    Toast.makeText(this, "Chuyển đến thanh toán với " + selectedItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                    Intent checkoutIntent = new Intent(this, OrderActivity.class);
                    checkoutIntent.putExtra("authToken", authToken);
                    checkoutIntent.putExtra("SELECTED_CART_ITEM_IDS", selectedItemIds);
                    startActivity(checkoutIntent);
                } else {
                    Toast.makeText(this, "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "buttonCheckout is null, cannot set listener.");
        }
    }

    private void fetchCartData() {
        showLoading(true);
        String header = "Bearer " + authToken;
        ApiService.apiService.getCartItems(header).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    cartItemList.clear();
                    cartItemList.addAll(response.body());
                    if (cartItemList.isEmpty()) {
                        showDataView(false, "Giỏ hàng của bạn đang trống");
                    } else {
                        showDataView(true, null);
                        if (cartAdapter != null) {
                            cartAdapter.updateCartItems(cartItemList);
                            selectAllItems(true);
                            cartAdapter.notifyDataSetChanged();
                        }
                    }
                    updateSummary();
                    updateSelectAllCheckboxState();
                } else {
                    handleApiError(response);
                    updateSummary();
                }
            }
            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network failure fetching cart", t);
                showDataView(false, "Lỗi kết nối mạng. Vui lòng thử lại.");
                updateSummary();
            }
        });
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            onItemRemoved(item);
            return;
        }
        Long productId = item.getProduct().getProductId();
        String header = "Bearer " + authToken;
        CartActionRequest updateRequest = new CartActionRequest(productId, newQuantity);

        ApiService.apiService.updateCartItem(header, updateRequest).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Quantity updated successfully via API.");
                    Toast.makeText(CartActivity.this, "Đã cập nhật số lượng.", Toast.LENGTH_SHORT).show();
                    fetchCartData();
                } else {
                    String errorMsg = "Cập nhật số lượng thất bại.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " Lỗi: " + response.code() + " - " + response.errorBody().string();
                        } else {
                            errorMsg += " Lỗi: " + response.code() + " " + response.message();
                        }
                    } catch (IOException e) { Log.e(TAG, "Error reading error body (Update Quantity)", e); }
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    fetchCartData();
                }
            }
            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e(TAG, "API Call Failed (Update Quantity): " + t.getMessage(), t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối. Không thể cập nhật số lượng.", Toast.LENGTH_LONG).show();
                fetchCartData();
            }
        });
    }

    @Override
    public void onItemRemoved(CartItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm '" + item.getProduct().getName() + "' khỏi giỏ hàng?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Long productIdToRemove = item.getProduct().getProductId();
                    String header = "Bearer " + authToken;
                    ApiService.apiService.removeCartItem(header,productIdToRemove).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                int position = cartItemList.indexOf(item);
                                if (position != -1) {
                                    cartItemList.remove(position);
                                    if (cartAdapter != null) {
                                        cartAdapter.notifyItemRemoved(position);
                                        cartAdapter.notifyItemRangeChanged(position, cartItemList.size() - position);
                                    }
                                    updateSummary();
                                    updateSelectAllCheckboxState();
                                    if (cartItemList.isEmpty()) {
                                        showDataView(false, "Giỏ hàng của bạn đã trống");
                                    }
                                    Toast.makeText(CartActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                }else {
                                    fetchCartData();
                                }
                            } else {
                                String errorMsg = "Xóa sản phẩm thất bại.";
                                try {
                                    if (response.errorBody() != null) {
                                        errorMsg += " Lỗi: " + response.code() + " - " + response.errorBody().string();
                                    }
                                } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                                Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "API Call Failed (Remove Item): " + t.getMessage(), t);
                            Toast.makeText(CartActivity.this, "Lỗi kết nối. Không thể xóa sản phẩm.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public void onItemSelectionChanged() {
        updateSummary();
        updateSelectAllCheckboxState();
    }

    private void updateSummary() {
        double subtotal = 0;
        int selectedItemCount = 0;
        boolean hasItemSelected = false;

        for (CartItem item : cartItemList) {
            if (item.isSelected()) {
                subtotal += item.getItemTotalPrice();
                selectedItemCount++;
                hasItemSelected = true;
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        if(textViewTotalValue != null) textViewTotalValue.setText(currencyFormat.format(subtotal));

        if (buttonCheckout != null) {
            buttonCheckout.setText("Thanh toán (" + selectedItemCount + ")");
            buttonCheckout.setEnabled(hasItemSelected);
            if (hasItemSelected) {
                buttonCheckout.setBackgroundResource(R.drawable.bg);
            } else {
                buttonCheckout.setBackgroundResource(R.drawable.bottom_nav_background);
            }
        }
    }

    private void updateSelectAllCheckboxState() {
        if (checkboxSelectAll == null) return;

        if (cartItemList.isEmpty()) {
            if (checkboxSelectAll.isChecked()) {
                checkboxSelectAll.setOnCheckedChangeListener(null);
                checkboxSelectAll.setChecked(false);
                setupSelectAllListener();
            }
            checkboxSelectAll.setEnabled(false);
            return;
        } else {
            checkboxSelectAll.setEnabled(true);
        }

        boolean allSelected = true;
        for (CartItem item : cartItemList) {
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }

        if (checkboxSelectAll.isChecked() != allSelected) {
            checkboxSelectAll.setOnCheckedChangeListener(null);
            checkboxSelectAll.setChecked(allSelected);
            setupSelectAllListener();
        }
    }

    private void setupSelectAllListener() {
        if (checkboxSelectAll == null) return;
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean stateNeedsChange = false;
            for (CartItem item : cartItemList) {
                if (item.isSelected() != isChecked) {
                    stateNeedsChange = true;
                    break;
                }
            }
            if (buttonView.isPressed() || stateNeedsChange) {
                selectAllItems(isChecked);
                if (cartAdapter != null) cartAdapter.notifyDataSetChanged();
                updateSummary();
            }
        });
    }

    private void selectAllItems(boolean select) {
        for (CartItem item : cartItemList) {
            item.setSelected(select);
        }
    }

    private void showLoading(boolean isLoading) {
        if(progressBarCart != null) progressBarCart.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading && textViewEmptyCart != null) {
            textViewEmptyCart.setVisibility(View.GONE);
        }
    }

    private void showDataView(boolean showData, String message) {
        if (recyclerViewCart == null || textViewEmptyCart == null) return;
        if (showData) {
            recyclerViewCart.setVisibility(View.VISIBLE);
            textViewEmptyCart.setVisibility(View.GONE);
        } else {
            recyclerViewCart.setVisibility(View.GONE);
            textViewEmptyCart.setText(message);
            textViewEmptyCart.setVisibility(View.VISIBLE);
        }
    }

    private void handleApiError(Response<?> response) {
        Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
        String errorMessage = "Đã xảy ra lỗi khi tải giỏ hàng.";
        // ... (phần xử lý lỗi giữ nguyên) ...
        showDataView(false, "Lỗi " + response.code() + ": " + errorMessage);
    }

    // --- Phương thức setupBottomNavigation cho CartActivity ---
    private void setupBottomNavigation() {
        // Xử lý click cho icon Home (ivCartMenuBottom)
        if (ivCartMenuBottom != null) {
            ivCartMenuBottom.setOnClickListener(v -> {
                Log.d(TAG, "Home icon clicked from CartActivity");
                Intent intent = new Intent(CartActivity.this, HomeActivity.class);
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
                Intent intent = new Intent(CartActivity.this, AboutAppActivity.class); // Hoặc Activity khác
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
                Intent intent = new Intent(CartActivity.this, AboutUsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartAboutUs is null!");
        }

        // Xử lý click cho icon User/Profile (ivCartIconSelf)
        if (ivCartIconSelf != null) {
            ivCartIconSelf.setOnClickListener(v -> {
                Log.d(TAG, "User/Profile icon clicked from CartActivity");
                Intent intent = new Intent(CartActivity.this, CartActivity.class);
                intent.putExtra("token", authToken);
                startActivity(intent);
                // Không nên finish() ở đây nếu người dùng có thể muốn quay lại giỏ hàng
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartIconSelf is null!");
        }
    }
    // --- Hết phương thức setupBottomNavigation ---
}