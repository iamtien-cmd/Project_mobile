package vn.iostar.doan.activity;

import android.annotation.SuppressLint;
// import android.content.Intent; // Không dùng Intent trong file này hiện tại
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.gson.Gson; // Gson vẫn cần nếu muốn tự parse lỗi chẳng hạn
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException; // Vẫn cần để bắt lỗi parse thủ công nếu muốn

import java.io.IOException;
import java.text.NumberFormat;
// import java.text.ParseException; // Không cần ParseException nữa
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
// import java.util.TimeZone; // Import nếu bạn dùng setTimeZone

// --- Import cần thiết cho Retrofit ---
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// ------------------------------------

import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService; // <<< Import ApiService
// import vn.iostar.doan.model.Address; // Import Address nếu dùng
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine;
import vn.iostar.doan.model.OrderStatus;
import vn.iostar.doan.model.Product2; // <<< Đảm bảo class này tồn tại và đúng

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";
    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private ScrollView scrollViewContent;

    // Views for Order Info
    private TextView tvDetailOrderId, tvDetailOrderStatus, tvDetailOrderDate, tvDetailPredictDate, tvDetailPaymentMethod;

    // Views for Shipping Address
    private TextView tvDetailShippingAddress;

    // Container for Product Items
    private LinearLayout llOrderDetailItems;

    // Views for Order Summary
    private TextView tvDetailItemsSubtotal, tvDetailShippingFee, tvDetailDiscountAmount, tvDetailTotalPrice;
    private RelativeLayout layoutDiscount;
    private TextView tvDetailDiscountLabel;

    // Không cần OkHttpClient client nữa nếu chỉ dùng Retrofit
    // private OkHttpClient client;
    private Gson gson; // Vẫn có thể hữu ích để debug hoặc parse lỗi thủ công
    private NumberFormat currencyFormatter;
    // private SimpleDateFormat apiDateFormat; // Không cần parse thủ công nữa
    private SimpleDateFormat displayDateFormat;

    private long orderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId == -1) {
            Log.e(TAG, "Order ID not passed correctly via Intent.");
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "Received Order ID: " + orderId);

        // --- Khởi tạo ---
        // client = new OkHttpClient(); // Bỏ đi
        gson = new GsonBuilder().create(); // Vẫn giữ nếu cần debug
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()); // Bỏ đi
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        // displayDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        // --- Ánh xạ Views ---
        setupViews(); // Gom ánh xạ vào hàm riêng cho gọn

        // --- Cấu hình Toolbar ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đơn hàng #" + orderId);
        }

        // --- Gọi API để lấy dữ liệu chi tiết ---
        fetchOrderDetailWithRetrofit(orderId); // <<< Gọi hàm mới dùng Retrofit
    }

    // Hàm ánh xạ view
    private void setupViews() {
        toolbar = findViewById(R.id.toolbar_order_detail);
        progressBar = findViewById(R.id.progress_bar_order_detail);
        tvErrorMessage = findViewById(R.id.tv_error_message_order_detail);
        scrollViewContent = findViewById(R.id.scroll_view_order_detail);

        tvDetailOrderId = findViewById(R.id.tv_detail_order_id);
        tvDetailOrderStatus = findViewById(R.id.tv_detail_order_status);
        tvDetailOrderDate = findViewById(R.id.tv_detail_order_date);
        tvDetailPredictDate = findViewById(R.id.tv_detail_predict_date);
        tvDetailPaymentMethod = findViewById(R.id.tv_detail_payment_method);

        tvDetailShippingAddress = findViewById(R.id.tv_detail_shipping_address);

        llOrderDetailItems = findViewById(R.id.ll_order_detail_items);

        tvDetailItemsSubtotal = findViewById(R.id.tv_detail_items_subtotal);
        tvDetailShippingFee = findViewById(R.id.tv_detail_shipping_fee);
        tvDetailDiscountAmount = findViewById(R.id.tv_detail_discount_amount);
        tvDetailTotalPrice = findViewById(R.id.tv_detail_total_price);
        layoutDiscount = findViewById(R.id.layout_discount);
        tvDetailDiscountLabel = findViewById(R.id.tv_detail_discount_label);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --- Hàm gọi API lấy chi tiết đơn hàng bằng Retrofit ---
    private void fetchOrderDetailWithRetrofit(long currentOrderId) {
        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);
        scrollViewContent.setVisibility(View.GONE);

        // === TODO: Lấy Authentication Token nếu API yêu cầu ===
        // String authToken = "Bearer " + your_token;
        // ======================================================

        // Sử dụng đối tượng apiService đã tạo trong interface ApiService
        Call<Order> call = ApiService.apiService.getOrderDetail(currentOrderId);
        // Nếu cần token:
        // Call<Order> call = ApiService.apiService.getOrderDetail(authToken, currentOrderId);

        Log.d(TAG, "Calling API: " + call.request().url()); // Log URL đang gọi

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                showLoading(false); // Ẩn loading khi có kết quả

                if (response.isSuccessful() && response.body() != null) {
                    // Thành công và có dữ liệu trả về
                    Order orderDetail = response.body();
                    Log.d(TAG, "API call successful. Order received: " + orderDetail.getOrderId());
                    displayOrderDetails(orderDetail); // Hiển thị dữ liệu
                    scrollViewContent.setVisibility(View.VISIBLE); // Hiện nội dung
                } else {
                    // Server trả về lỗi (4xx, 5xx)
                    String errorMsg = "Lỗi " + response.code() + ": Không thể tải chi tiết đơn hàng.";
                    String errorBodyString = null;
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                            // Cố gắng hiển thị lỗi cụ thể hơn nếu có
                            if (!errorBodyString.isEmpty() && !errorBodyString.startsWith("<")) { // Tránh log HTML lỗi
                                errorMsg = "Lỗi " + response.code() + ": " + errorBodyString.substring(0, Math.min(errorBodyString.length(), 100));
                            }
                            Log.e(TAG, "API call failed with code " + response.code() + ". Error Body: " + errorBodyString);
                        } else {
                            Log.e(TAG, "API call failed with code " + response.code() + ". No error body.");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    showError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                // Lỗi mạng hoặc lỗi trong quá trình thực hiện request/response
                showLoading(false);
                Log.e(TAG, "API call failed on network/exception: ", t);
                showError("Lỗi kết nối mạng. Vui lòng thử lại.");
            }
        });
    }


    // --- Hàm hiển thị dữ liệu lên UI (Giữ nguyên phần lớn) ---
    @SuppressLint("SetTextI18n")
    private void displayOrderDetails(Order order) {
        if (order == null) {
            showError("Không có dữ liệu chi tiết cho đơn hàng này.");
            return;
        }

        // --- Thông tin chung ---
        tvDetailOrderId.setText("Mã ĐH: #" + order.getOrderId());
        tvDetailPaymentMethod.setText("Thanh toán: " + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A"));
        tvDetailOrderDate.setText("Ngày đặt: " + formatDate(order.getOrderDate()));
        tvDetailPredictDate.setText("Dự kiến nhận: " + formatDate(order.getPredictReceiveDate()));
        tvDetailPredictDate.setVisibility(order.getPredictReceiveDate() != null ? View.VISIBLE : View.GONE);
        OrderStatus status = order.getStatus();
        String statusText = getStatusDisplayString(status);
        int statusColor = getStatusColor(status);
        tvDetailOrderStatus.setText(statusText);
        tvDetailOrderStatus.setTextColor(statusColor);

        // --- Địa chỉ ---
        tvDetailShippingAddress.setText(order.getShippingAddress() != null ? order.getShippingAddress() : "Không có thông tin địa chỉ.");

        // --- Danh sách sản phẩm ---
        llOrderDetailItems.removeAllViews();
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < order.getOrderLines().size(); i++) {
                OrderLine line = order.getOrderLines().get(i);
                // <<< SỬ DỤNG Product2 NHƯ TRONG CODE GỐC CỦA BẠN >>>
                // <<< Đảm bảo class Product2 tồn tại và có các getter cần thiết >>>
                Product2 product = line.getProduct();

                View itemView = inflater.inflate(R.layout.list_item_order_detail_product, llOrderDetailItems, false);
                ImageView ivProduct = itemView.findViewById(R.id.iv_product_image_detail);
                TextView tvName = itemView.findViewById(R.id.tv_product_name_detail);
                TextView tvQuantity = itemView.findViewById(R.id.tv_product_quantity_detail);
                TextView tvPrice = itemView.findViewById(R.id.tv_product_price_detail);

                if (product != null) {
                    tvName.setText(product.getName());
                    Glide.with(this)
                            .load(product.getImageUrl()) // <<< Cần getImageUrl() trong Product2
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(ivProduct);
                } else {
                    tvName.setText("Sản phẩm không xác định");
                    ivProduct.setImageResource(R.drawable.placeholder_image);
                }

                tvQuantity.setText("SL: x" + line.getQuantity());
                tvPrice.setText(currencyFormatter.format(line.getPrice())); // <<< Cần getPrice() trong OrderLine
                llOrderDetailItems.addView(itemView);

                // Thêm đường kẻ
                if (i < order.getOrderLines().size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) getResources().getDimension(R.dimen.divider_height)
                    );
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.divider_margin), 0, (int) getResources().getDimension(R.dimen.divider_margin));
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
                    llOrderDetailItems.addView(divider);
                }
            }
        } else {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("Không có sản phẩm trong đơn hàng này.");
            // ... set layout params ...
            llOrderDetailItems.addView(noItemsText);
        }

        // --- Tóm tắt đơn hàng ---
        tvDetailItemsSubtotal.setText(currencyFormatter.format(order.getItemsSubtotal())); // <<< Cần getItemsSubtotal()
        tvDetailShippingFee.setText(currencyFormatter.format(order.getShippingFee()));    // <<< Cần getShippingFee()

        if (order.getDiscountAmount() > 0) { // <<< Cần getDiscountAmount()
            layoutDiscount.setVisibility(View.VISIBLE);
            String discountLabel = "Giảm giá";
            if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) { // <<< Cần getVoucherCode()
                discountLabel += " (" + order.getVoucherCode() + ")";
            }
            tvDetailDiscountLabel.setText(discountLabel + ":");
            tvDetailDiscountAmount.setText("- " + currencyFormatter.format(order.getDiscountAmount()));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
        tvDetailTotalPrice.setText(currencyFormatter.format(order.getTotalPrice()));
    }

    // --- Hàm helper định dạng ngày ---
    private String formatDate(Date date) {
        if (date == null) return "N/A";
        try {
            return displayDateFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + date, e);
            return "Lỗi ngày";
        }
    }

    // --- Hàm helper lấy chuỗi trạng thái ---
    private String getStatusDisplayString(OrderStatus status) {
        if (status == null) return "N/A";
        switch (status) {
            case WAITING: return "Đang chờ xác nhận";
            case REVIEWED: return "Đã xác nhận";
            case SHIPPING: return "Đang vận chuyển";
            case RECEIVED: return "Đã giao thành công";
            case ERROR: return "Đã hủy";
            default: return status.name();
        }
    }

    // --- Hàm helper lấy màu trạng thái ---
    private int getStatusColor(OrderStatus status) {
        int colorResId = R.color.my_grey_neutral;
        if (status != null) {
            switch (status) {
                case RECEIVED: colorResId = R.color.my_green_success; break;
                case SHIPPING: colorResId = R.color.my_orange_processing; break;
                case ERROR:    colorResId = R.color.my_red_error; break;
                // WAITING, REVIEWED dùng màu mặc định
            }
        }
        return ContextCompat.getColor(this, colorResId);
    }

    // --- Các hàm tiện ích UI ---
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        if (scrollViewContent != null) {
            scrollViewContent.setVisibility(View.GONE);
        }
        if (tvErrorMessage != null) {
            if (message != null && !message.isEmpty()) {
                tvErrorMessage.setText(message);
                tvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                tvErrorMessage.setVisibility(View.GONE);
            }
        }
    }
}