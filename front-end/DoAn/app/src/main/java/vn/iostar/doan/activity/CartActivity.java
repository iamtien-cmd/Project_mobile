package vn.iostar.doan.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView; // Import NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent; // Keep necessary imports
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

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
// Remove duplicate imports if any

public class CartActivity extends AppCompatActivity implements CartAdapter.CartItemListener {

    private static final String TAG = "CartActivity";

    private String authToken;
    private Toolbar toolbar;
    // Views
    private RecyclerView recyclerViewCart;
    private ProgressBar progressBarCart;
    private TextView textViewEmptyCart;

    private TextView textViewSubtotalValue, textViewTaxesValue, textViewTotalValue;
    private CheckBox checkboxSelectAll;
    private AppCompatButton buttonCheckout;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Get token
        authToken = getIntent().getStringExtra("token");
        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "Auth token is missing!");
            Toast.makeText(this, "Lỗi xác thực. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setupViews();
        setupToolbar();

        setupRecyclerView();
        setupListeners();
        fetchCartData();
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
    private void setupViews() {
        recyclerViewCart = findViewById(R.id.recyclerViewCartItems);
        progressBarCart = findViewById(R.id.progressBarCart);
        textViewEmptyCart = findViewById(R.id.textViewEmptyCart);
        toolbar = findViewById(R.id.toolbar);

        textViewSubtotalValue = findViewById(R.id.textViewSubtotalValue);
        textViewTaxesValue = findViewById(R.id.textViewTaxesValue);
        textViewTotalValue = findViewById(R.id.textViewTotalValue);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);
        buttonCheckout = findViewById(R.id.buttonCheckout);

        progressBarCart.setVisibility(View.GONE);
        textViewEmptyCart.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItemList, this);
        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCart.setAdapter(cartAdapter);
        recyclerViewCart.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean stateNeedsChange = false;
            for (CartItem item : cartItemList) {
                if (item.isSelected() != isChecked) {
                    stateNeedsChange = true;
                    break;
                }
            }

            if (buttonView.isPressed() || stateNeedsChange) {
                Log.d(TAG, "SelectAll triggered. IsPressed: " + buttonView.isPressed() + ", NeedsChange: " + stateNeedsChange + ", NewState: " + isChecked);
                selectAllItems(isChecked);
                cartAdapter.notifyDataSetChanged();
                updateSummary();
            } else {
                Log.d(TAG, "SelectAll event skipped. IsPressed: " + buttonView.isPressed() + ", NeedsChange: " + stateNeedsChange + ", CurrentState: " + isChecked);
            }
        });

        buttonCheckout.setOnClickListener(v -> {
            List<CartItem> selectedItems = new ArrayList<>();
            for (CartItem item : cartItemList) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
            if (!selectedItems.isEmpty()) {
                Toast.makeText(this, "Chuyển đến thanh toán với " + selectedItems.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                // Intent checkoutIntent = new Intent(this, CheckoutActivity.class);
                // // Pass data (make CartItem Parcelable or pass IDs)
                // checkoutIntent.putExtra("authToken", authToken);
                // startActivity(checkoutIntent);
            } else {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCartData() {
        showLoading(true); // Show loading indicator

        String header = "Bearer " + authToken;
        ApiService.apiService.getCartItems(header).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                showLoading(false); // Hide loading indicator FIRST
                if (response.isSuccessful() && response.body() != null) {
                    cartItemList.clear();
                    cartItemList.addAll(response.body());

                    if (cartItemList.isEmpty()) {
                        showDataView(false, "Giỏ hàng của bạn đang trống"); // Show empty message
                    } else {
                        showDataView(true, null); // Show the nested scroll view with data
                        cartAdapter.updateCartItems(cartItemList);
                        cartAdapter.notifyDataSetChanged();
                        // Select all by default on first load (optional)
                        selectAllItems(true);
                        cartAdapter.notifyDataSetChanged(); // Update checkboxes in adapter
                        updateSummary();
                        updateSelectAllCheckboxState(); // Ensure "Select All" reflects state
                    }
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                showLoading(false); // Hide loading indicator FIRST
                Log.e(TAG, "Network failure fetching cart", t);
                showDataView(false, "Lỗi kết nối mạng. Vui lòng thử lại."); // Show error
                updateSummary(); // Reset summary values
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
                    Cart updatedCart = response.body(); // The API returns the updated cart
                    Log.i(TAG, "Quantity updated successfully via API.");

                    // *** Strategy 1: Re-fetch all cart data (Simplest & Safest) ***
                    Toast.makeText(CartActivity.this, "Đã cập nhật số lượng.", Toast.LENGTH_SHORT).show();
                    fetchCartData();
                } else {
                    // Handle API error (similar to onItemRemoved)
                    String errorMsg = "Cập nhật số lượng thất bại.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyStr = response.errorBody().string();
                            errorMsg += " Lỗi: " + response.code() + " - " + errorBodyStr;
                            Log.e(TAG, "API Error (Update Quantity): " + response.code() + " Body: " + errorBodyStr);
                        } else {
                            errorMsg += " Lỗi: " + response.code() + " " + response.message();
                            Log.e(TAG, "API Error (Update Quantity): " + response.code() + " Message: " + response.message());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body (Update Quantity)", e);
                        errorMsg += " Lỗi server không xác định.";
                    }

                    if (response.code() == 401) {
                        errorMsg = "Phiên đăng nhập hết hạn.";
                        // TODO: Redirect to login
                    } else if (response.code() == 400) {
                        // Could be invalid quantity, product not found, etc.
                        errorMsg = "Yêu cầu không hợp lệ (số lượng hoặc sản phẩm?).";
                    }
                    Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Log.e(TAG, "API Call Failed (Update Quantity): " + t.getMessage(), t);
                Toast.makeText(CartActivity.this, "Lỗi kết nối. Không thể cập nhật số lượng.", Toast.LENGTH_LONG).show();
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
                    Log.d(TAG, "authToken = " + authToken);
                    Log.d(TAG, "Product ID to remove: " + productIdToRemove);
                    ApiService.apiService.removeCartItem(header,productIdToRemove).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                int position = cartItemList.indexOf(item);
                                if (position != -1) {
                                    cartItemList.remove(position);
                                    cartAdapter.notifyItemRemoved(position);
                                    cartAdapter.notifyItemRangeChanged(position, cartItemList.size());
                                    updateSummary();
                                    updateSelectAllCheckboxState();
                                    if (cartItemList.isEmpty()) {
                                        showDataView(false, "Giỏ hàng của bạn đã trống");
                                    }
                                }else {
                                    Log.w(TAG, "Item removed successfully via API, but not found in local list for UI update.");
                                    fetchCartData();
                                }

                            } else {
                                String errorMsg = "Xóa sản phẩm thất bại.";
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBodyStr = response.errorBody().string();
                                        errorMsg += " Lỗi: " + response.code() + " - " + errorBodyStr;
                                        Log.e(TAG, "API Error: " + response.code() + " Body: " + errorBodyStr);
                                    } else {
                                        errorMsg += " Lỗi: " + response.code() + " " + response.message();
                                        Log.e(TAG, "API Error: " + response.code() + " Message: " + response.message());
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error reading error body", e);
                                    errorMsg += " Lỗi server không xác định.";
                                }
                                // Xử lý cụ thể cho từng mã lỗi nếu cần
                                if (response.code() == 401) {
                                    errorMsg = "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
                                    // TODO: Điều hướng đến màn hình đăng nhập
                                } else if (response.code() == 400) {
                                    errorMsg = "Yêu cầu không hợp lệ (có thể sản phẩm không tồn tại?).";
                                }
                                Toast.makeText(CartActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "API Call Failed: " + t.getMessage(), t);
                            Toast.makeText(CartActivity.this, "Lỗi kết nối hoặc hệ thống. Không thể xóa sản phẩm.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public void onItemSelectionChanged() {
        updateSummary();
        updateSelectAllCheckboxState(); // Sync "Select All" checkbox
    }

    // --- Utility Methods ---

    private void updateSummary() {
        double subtotal = 0;
        int selectedItemCount = 0;
        boolean hasItemSelected = false; // Use this to enable/disable checkout button

        for (CartItem item : cartItemList) {
            if (item.isSelected()) {
                subtotal += item.getItemTotalPrice();
                selectedItemCount++;
                hasItemSelected = true;
            }
        }

        double taxes = subtotal * 0.05; // Example 5% tax
        double total = subtotal + taxes;

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        textViewSubtotalValue.setText(currencyFormat.format(subtotal));
        textViewTaxesValue.setText("+ " + currencyFormat.format(taxes));
        textViewTotalValue.setText(currencyFormat.format(total));

        // Update Checkout Button State
        buttonCheckout.setText("Thanh toán (" + selectedItemCount + ")");
        buttonCheckout.setEnabled(hasItemSelected); // Enable only if at least one item is selected
        // Consider setting different backgrounds based on enabled state using a selector drawable
        // Example: buttonCheckout.setBackgroundResource(R.drawable.checkout_button_selector);
        // Or set color directly (less ideal than selector)
        if (hasItemSelected) {
            // Use your enabled background (ensure bottom_nav_background is suitable or create a new one)
            buttonCheckout.setBackgroundResource(R.drawable.bottom_nav_background);
        } else {
            // Use your disabled background (ensure bg is suitable or create a specific disabled one)
            buttonCheckout.setBackgroundResource(R.drawable.bg); // Create drawable/bg_disabled.xml (e.g., gray color)
        }
    }

    // Make sure you have a drawable like res/drawable/bg_disabled.xml:
    /*
    <shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
        <solid android:color="#CCCCCC"/> // Gray color
        <corners android:radius="8dp"/> // Optional: match your enabled button corners
    </shape>
    */


    private void updateSelectAllCheckboxState() {
        if (cartItemList.isEmpty()) {
            // To prevent triggering the listener unnecessarily, only set if needed
            if (checkboxSelectAll.isChecked()) {
                checkboxSelectAll.setChecked(false);
            }
            return;
        }

        boolean allSelected = true;
        for (CartItem item : cartItemList) {
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }

        // Only update the checkbox state if it's different from the calculated state
        // This prevents infinite loops with the setOnCheckedChangeListener
        if (checkboxSelectAll.isChecked() != allSelected) {
            checkboxSelectAll.setChecked(allSelected);
        }
    }


    private void selectAllItems(boolean select) {
        for (CartItem item : cartItemList) {
            item.setSelected(select);
        }
    }

    /**
     * Shows/Hides the loading progress bar.
     *
     * @param isLoading True to show loading, false to hide.
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarCart.setVisibility(View.VISIBLE);
//            nestedScrollView.setVisibility(View.GONE); // Hide content area
            textViewEmptyCart.setVisibility(View.GONE); // Hide empty text
        } else {
            progressBarCart.setVisibility(View.GONE);
            // Don't touch nestedScrollView or textViewEmptyCart here,
            // their visibility is determined by showDataView() after loading finishes.
        }
    }

    /**
     * Shows either the main content view (NestedScrollView) or the empty/error message.
     *
     * @param showData True to show NestedScrollView, False to show TextViewEmptyCart.
     * @param message  The message to display in TextViewEmptyCart if showData is false.
     */
    private void showDataView(boolean showData, String message) {
        if (showData) {
//            nestedScrollView.setVisibility(View.VISIBLE); // Show the scrollable content
            textViewEmptyCart.setVisibility(View.GONE);   // Hide the empty message
        } else {
//            nestedScrollView.setVisibility(View.GONE);     // Hide the scrollable content
            textViewEmptyCart.setText(message);           // Set the message
            textViewEmptyCart.setVisibility(View.VISIBLE); // Show the empty message
        }
    }


    private void handleApiError(Response<?> response) {
        // showLoading(false) should have been called before this
        Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
        String errorMessage = "Đã xảy ra lỗi khi tải giỏ hàng."; // Default
        if (response.errorBody() != null) {
            try {
                // Try reading the error body for a more specific message
                errorMessage = response.errorBody().string();
                // Clean up potential quotes from JSON string response
                if (errorMessage.length() > 2 && errorMessage.startsWith("\"") && errorMessage.endsWith("\"")) {
                    errorMessage = errorMessage.substring(1, errorMessage.length() - 1);
                }
                Log.e(TAG, "Error body string: " + errorMessage);
            } catch (IOException e) {
                Log.e(TAG, "Error reading error body", e);
                errorMessage = response.message(); // Fallback to HTTP message
            }
        } else {
            if (response.message() != null && !response.message().isEmpty()) {
                errorMessage = response.message();
            }
        }
        showDataView(false, "Lỗi " + response.code() + ": " + errorMessage); // Show error in the empty view
        updateSummary(); // Reset summary values
    }

    // TODO: Implement API calls for update and delete
    // private void callUpdateApi(long cartItemId, int newQuantity) { ... }
    // private void callDeleteApi(long cartItemId) { ... }
    // In their onResponse success callbacks, call fetchCartData() to refresh.
}