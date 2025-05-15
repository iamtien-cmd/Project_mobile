package vn.iostar.doan.activity; // <<<< THAY ĐỔI CHO ĐÚNG PACKAGE CỦA BẠN

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import vn.iostar.doan.R;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine;
import vn.iostar.doan.model.OrderStatus;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.User; // Đảm bảo import User model


public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    public static final String EXTRA_ORDER_ID = "ORDER_ID"; // << SỬA THÀNH KEY NÀY
    public static final String EXTRA_ACTION_MODE = "ACTION_MODE_KEY";
    public static final String EXTRA_USER_ID_FOR_REVIEW = "USER_ID_FOR_REVIEW_KEY";
    public static final String ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW = "SELECT_PRODUCT_FOR_REVIEW_ACTION";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private ScrollView scrollViewContent;
    private TextView tvDetailOrderId, tvDetailOrderStatus, tvDetailOrderDate, tvDetailPredictDate, tvDetailPaymentMethod;
    private TextView tvDetailRecipientInfoAndAddress; // THAY THẾ cho tvDetailShippingAddress
    private LinearLayout llOrderDetailItems;
    private TextView tvDetailItemsSubtotal, tvDetailShippingFee, tvDetailDiscountAmount, tvDetailTotalPrice;
    private RelativeLayout layoutDiscount;
    private TextView tvDetailDiscountLabel;

    // THÊM CÁC BIẾN CHO THÔNG TIN NGƯỜI ĐẶT
    private TextView tvOrderPlacerName, tvOrderPlacerPhone, tvOrderPlacerEmail;


    private NumberFormat currencyFormatter;
    private SimpleDateFormat displayDateFormat;

    private long orderId = -1;
    private String currentActionMode = null;
    private Long userIdForReview = -1L;
    private Order currentOrderData;

    private ActivityResultLauncher<Intent> productRatingLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Intent intent = getIntent();
        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L);
        if (intent.hasExtra(EXTRA_ACTION_MODE)) {
            currentActionMode = intent.getStringExtra(EXTRA_ACTION_MODE);
        }
        if (intent.hasExtra(EXTRA_USER_ID_FOR_REVIEW)) {
            userIdForReview = intent.getLongExtra(EXTRA_USER_ID_FOR_REVIEW, -1L);
        }

        if (orderId == -1) {
            Log.e(TAG, "Order ID not passed correctly via Intent.");
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d(TAG, "Received Order ID: " + orderId + ", ActionMode: " + currentActionMode + ", UserID for Review: " + userIdForReview);

        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        initProductRatingLauncher();
        setupViews(); // Gọi sau khi khởi tạo launcher

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode)) {
                getSupportActionBar().setTitle("Chọn sản phẩm để đánh giá");
            } else {
                getSupportActionBar().setTitle("Chi tiết Đơn hàng"); // Tiêu đề chung hơn
            }
        }

        if (savedInstanceState == null || currentOrderData == null) {
            fetchOrderDetailWithRetrofit(orderId);
        } else {
            // Nếu có dữ liệu cũ, có thể hiển thị nó trước rồi mới fetch lại, hoặc chỉ fetch lại.
            // Ở đây ta fetch lại để đảm bảo dữ liệu mới nhất.
            fetchOrderDetailWithRetrofit(orderId);
        }
    }

    private void initProductRatingLauncher() {
        productRatingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "ProductRatingActivity returned RESULT_OK.");
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Log.d(TAG, "ProductRatingActivity returned code: " + result.getResultCode() + ". Setting RESULT_CANCELED for OrderHistoryActivity.");
                        setResult(Activity.RESULT_CANCELED);
                        // Không cần finish() ở đây nếu bạn muốn người dùng ở lại màn hình chi tiết
                        // Nhưng nếu luồng là "chọn SP -> đánh giá -> quay lại lịch sử", thì finish() là đúng
                        // Dựa vào logic trước đó, finish() khi productRatingLauncher trả về kết quả
                        finish();
                    }
                }
        );
    }


    private void setupViews() {
        toolbar = findViewById(R.id.toolbar_order_detail);
        progressBar = findViewById(R.id.progress_bar_order_detail);
        tvErrorMessage = findViewById(R.id.tv_error_message_order_detail);
        scrollViewContent = findViewById(R.id.scroll_view_order_detail);

        // Thông tin chung
        tvDetailOrderId = findViewById(R.id.tv_detail_order_id);
        tvDetailOrderStatus = findViewById(R.id.tv_detail_order_status);
        tvDetailOrderDate = findViewById(R.id.tv_detail_order_date);
        tvDetailPredictDate = findViewById(R.id.tv_detail_predict_date);
        tvDetailPaymentMethod = findViewById(R.id.tv_detail_payment_method);

        // THÊM ÁNH XẠ CHO THÔNG TIN NGƯỜI ĐẶT
        tvOrderPlacerName = findViewById(R.id.tv_order_placer_name);
        tvOrderPlacerPhone = findViewById(R.id.tv_order_placer_phone);
        tvOrderPlacerEmail = findViewById(R.id.tv_order_placer_email);

        // SỬA ÁNH XẠ CHO THÔNG TIN NGƯỜI NHẬN
        tvDetailRecipientInfoAndAddress = findViewById(R.id.tv_detail_recipient_info_and_address);

        // Danh sách sản phẩm và Tóm tắt đơn hàng
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
            if (ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode)) {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchOrderDetailWithRetrofit(long currentOrderIdToFetch) {
        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);
        scrollViewContent.setVisibility(View.GONE);

        Call<Order> call = ApiService.apiService.getOrderDetail(currentOrderIdToFetch);
        Log.d(TAG, "Calling API for order detail: " + call.request().url());

        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    currentOrderData = response.body();
                    Log.d(TAG, "API call successful. Order received: " + currentOrderData.getOrderId());
                    // Cập nhật lại tiêu đề Toolbar với mã đơn hàng thực tế
                    if (getSupportActionBar() != null && !ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode)) {
                        getSupportActionBar().setTitle("Đơn hàng #" + currentOrderData.getOrderId());
                    }
                    displayOrderDetails(currentOrderData);
                    scrollViewContent.setVisibility(View.VISIBLE);
                } else {
                    String errorMsg = "Lỗi " + response.code() + ": Không thể tải chi tiết đơn hàng.";
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            if (!errorBodyString.isEmpty() && !errorBodyString.startsWith("<")) { // Tránh hiển thị HTML
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
                showLoading(false);
                Log.e(TAG, "API call failed on network/exception: ", t);
                showError("Lỗi kết nối mạng. Vui lòng thử lại.");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayOrderDetails(Order order) {
        if (order == null) {
            showError("Không có dữ liệu chi tiết cho đơn hàng này.");
            return;
        }

        // Thông tin chung
        tvDetailOrderId.setText("Mã ĐH: #" + order.getOrderId());
        tvDetailPaymentMethod.setText("Thanh toán: " + (order.getPaymentMethod() != null ? order.getPaymentMethod().toString() : "N/A"));
        tvDetailOrderDate.setText("Ngày đặt: " + formatDate(order.getOrderDate()));
        tvDetailPredictDate.setText("Dự kiến nhận: " + formatDate(order.getPredictReceiveDate()));
        tvDetailPredictDate.setVisibility(order.getPredictReceiveDate() != null ? View.VISIBLE : View.GONE);

        OrderStatus status = order.getStatus();
        String statusText = getStatusDisplayString(status);
        int statusColor = getStatusColor(status);
        tvDetailOrderStatus.setText(statusText);
        tvDetailOrderStatus.setTextColor(statusColor);

        // THÊM HIỂN THỊ THÔNG TIN NGƯỜI ĐẶT HÀNG
        User placer = order.getUser();
        if (placer != null) {
            tvOrderPlacerName.setText("Tên: " + (placer.getFullName() != null && !placer.getFullName().isEmpty() ? placer.getFullName() : "N/A"));
            tvOrderPlacerPhone.setText("SĐT: " + (placer.getPhone() != null && !placer.getPhone().isEmpty() ? placer.getPhone() : "N/A"));
            if (placer.getEmail() != null && !placer.getEmail().isEmpty()) {
                tvOrderPlacerEmail.setText("Email: " + placer.getEmail());
                tvOrderPlacerEmail.setVisibility(View.VISIBLE);
            } else {
                tvOrderPlacerEmail.setVisibility(View.GONE);
            }
        } else {
            tvOrderPlacerName.setText("Tên: N/A");
            tvOrderPlacerPhone.setText("SĐT: N/A");
            tvOrderPlacerEmail.setVisibility(View.GONE);
        }

        Log.d(TAG, "Raw shippingInfo from order object: '" + order.getShippingAddress() + "'");

        String shippingInfo = order.getShippingAddress();

        if (shippingInfo != null && !shippingInfo.trim().isEmpty()) {
            // Trường hợp backend trả về placeholder cho 3 giá trị rỗng (khi address object là null)
            if (shippingInfo.equals("||")) {
                tvDetailRecipientInfoAndAddress.setText("Thông tin người nhận và địa chỉ không có.");
            } else {
                // Tách chuỗi thành các phần, -1 để giữ lại các phần tử rỗng ở cuối (nếu có)
                String[] parts = shippingInfo.split("\\|", -1);
                Log.d(TAG, "Split shippingInfo into parts: " + java.util.Arrays.toString(parts) + " (length: " + parts.length + ")");


                // Mong đợi 3 phần tử: Tên, SĐT, Đường
                String name = (parts.length > 0) ? parts[0].trim() : "";
                String phone = (parts.length > 1) ? parts[1].trim() : "";
                String street = (parts.length > 2) ? parts[2].trim() : "";
                // KHÔNG lấy parts[3], parts[4], parts[5] nữa

                Log.d(TAG, "Parsed Name: '" + name + "', Phone: '" + phone + "', Street: '" + street + "'");


                StringBuilder displayBuilder = new StringBuilder();
                boolean firstLineAdded = false;

                // 1. Thêm tên người nhận
                if (!name.isEmpty()) {
                    displayBuilder.append("Người nhận: ").append(name);
                    firstLineAdded = true;
                }

                // 2. Thêm số điện thoại
                if (!phone.isEmpty()) {
                    if (firstLineAdded) {
                        displayBuilder.append("\n"); // Xuống dòng nếu đã có dòng trước
                    }
                    displayBuilder.append("SĐT: ").append(phone);
                    firstLineAdded = true;
                }

                // 3. Thêm địa chỉ đường (StreetAddress)
                // Không cần List<String> addressDetails nữa nếu chỉ có street
                if (!street.isEmpty()) {
                    if (firstLineAdded) {
                        displayBuilder.append("\n");
                    }
                    displayBuilder.append("Địa chỉ: ").append(street);
                    // firstLineAdded = true; // Không cần thiết nếu đây là dòng cuối
                }

                // Kiểm tra xem có nội dung nào được thêm vào không
                if (displayBuilder.length() > 0) {
                    tvDetailRecipientInfoAndAddress.setText(displayBuilder.toString());
                    Log.d(TAG, "Final display string: '" + displayBuilder.toString() + "'");
                } else {
                    // Trường hợp này xảy ra nếu shippingInfo ban đầu không phải "||"
                    // nhưng sau khi trim, tất cả 3 phần tử đều rỗng (ví dụ: " | | ")
                    tvDetailRecipientInfoAndAddress.setText("Thông tin người nhận và địa chỉ không đầy đủ.");
                    Log.d(TAG, "DisplayBuilder was empty, showing 'không đầy đủ'. Original parts: " + java.util.Arrays.toString(parts));
                }
            }
        } else {
            tvDetailRecipientInfoAndAddress.setText("Không có thông tin người nhận hoặc địa chỉ.");
            Log.d(TAG, "shippingInfo was null or empty.");
        }

        // Danh sách sản phẩm
        llOrderDetailItems.removeAllViews();
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < order.getOrderLines().size(); i++) {
                OrderLine line = order.getOrderLines().get(i);
                Product product = line.getProduct();

                View itemView = inflater.inflate(R.layout.list_item_order_detail_product, llOrderDetailItems, false);
                ImageView ivProduct = itemView.findViewById(R.id.iv_product_image_detail);
                TextView tvName = itemView.findViewById(R.id.tv_product_name_detail);
                TextView tvQuantity = itemView.findViewById(R.id.tv_product_quantity_detail);
                TextView tvPrice = itemView.findViewById(R.id.tv_product_price_detail);
                Button btnEvaluateThisProduct = itemView.findViewById(R.id.btn_evaluate_this_product);

                if (product != null) {
                    tvName.setText(product.getName());
                    Glide.with(this)
                            .load(product.getImage())
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(ivProduct);

                    if (ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode) &&
                            order.getStatus() == OrderStatus.RECEIVED && // Chỉ cho đánh giá khi đã nhận
                            userIdForReview != null && userIdForReview > 0L) {

                        btnEvaluateThisProduct.setVisibility(View.VISIBLE);
                        final long currentSelectedProductId = product.getProductId();

                        btnEvaluateThisProduct.setOnClickListener(v -> {
                            Log.d(TAG, "Evaluate this product clicked. ProductId: " + currentSelectedProductId +
                                    ", UserId: " + userIdForReview + ", OrderId: " + order.getOrderId());

                            Intent intentToProductRating = new Intent(OrderDetailActivity.this, ProductRatingActivity.class);
                            intentToProductRating.putExtra("product_id", currentSelectedProductId);
                            intentToProductRating.putExtra("user_id", userIdForReview);
                            intentToProductRating.putExtra("order_id", order.getOrderId());

                            if (productRatingLauncher != null) {
                                productRatingLauncher.launch(intentToProductRating);
                            } else {
                                Log.e(TAG, "productRatingLauncher is null! Cannot launch ProductRatingActivity for result.");
                                startActivity(intentToProductRating); // Fallback
                                setResult(Activity.RESULT_CANCELED); // Nếu fallback, nên báo là cancel
                                finish();
                            }
                        });
                    } else {
                        btnEvaluateThisProduct.setVisibility(View.GONE);
                    }
                } else {
                    tvName.setText("Sản phẩm không xác định");
                    ivProduct.setImageResource(R.drawable.placeholder_image); // Hoặc một ảnh mặc định
                    btnEvaluateThisProduct.setVisibility(View.GONE);
                }

                tvQuantity.setText("SL: x" + line.getQuantity());
                tvPrice.setText(currencyFormatter.format(line.getPrice()));
                llOrderDetailItems.addView(itemView);

                // Thêm divider nếu không phải item cuối
                if (i < order.getOrderLines().size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (int) getResources().getDimension(R.dimen.divider_height) // Define in dimens.xml e.g. 1dp
                    );
                    params.setMargins(0, (int) getResources().getDimension(R.dimen.divider_margin_vertical), // e.g. 8dp
                            0, (int) getResources().getDimension(R.dimen.divider_margin_vertical));
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color)); // Define in colors.xml e.g. #E0E0E0
                    llOrderDetailItems.addView(divider);
                }
            }
        } else {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("Không có sản phẩm trong đơn hàng này.");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16,0,16); // Thêm margin cho đẹp
            noItemsText.setLayoutParams(params);
            llOrderDetailItems.addView(noItemsText);
        }

        // Tóm tắt đơn hàng
        double itemsSubtotal = order.getItemsSubtotal(); // Lấy từ Order object
        double totalPrice = order.getTotalPrice();     // Lấy từ Order object

        // Tính toán shippingFee ở client từ totalPrice và itemsSubtotal
        double calculatedShippingFee = totalPrice - itemsSubtotal;

        tvDetailItemsSubtotal.setText(currencyFormatter.format(itemsSubtotal));
        tvDetailShippingFee.setText(currencyFormatter.format(calculatedShippingFee)); // Sử dụng giá trị vừa tính

        if (order.getDiscountAmount() > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            String discountLabel = "Giảm giá";
            if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
                discountLabel += " (" + order.getVoucherCode() + ")";
            }
            tvDetailDiscountLabel.setText(discountLabel + ":");
            tvDetailDiscountAmount.setText("- " + currencyFormatter.format(order.getDiscountAmount()));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
        tvDetailTotalPrice.setText(currencyFormatter.format(totalPrice)); // Hiển thị totalPrice từ Order object
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        try {
            return displayDateFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + date, e);
            return "Lỗi ngày";
        }
    }

    private String getStatusDisplayString(OrderStatus status) {
        if (status == null) return "Không xác định";
        switch (status) {
            case WAITING: return "Đang chờ xử lý";
            case REVIEWED: return "Đã đánh giá";
            case SHIPPING: return "Đang vận chuyển";
            case RECEIVED: return "Đã giao thành công";
            case CANCELLED: return "Đã hủy";
            case ERROR: return "Lỗi đơn hàng";
            default: return status.name(); // Hoặc một chuỗi mặc định khác
        }
    }

    private int getStatusColor(OrderStatus status) {
        int colorResId = R.color.my_grey_neutral; // Màu mặc định
        if (status != null) {
            switch (status) {
                case RECEIVED: colorResId = R.color.my_green_success; break;
                case SHIPPING: colorResId = R.color.my_orange_processing; break;
                case CANCELLED:
                case ERROR:    colorResId = R.color.my_red_error; break;
                case WAITING:
                case REVIEWED: colorResId = R.color.my_blue_info; break;
                // Không cần default vì đã có màu mặc định ở trên
            }
        }
        return ContextCompat.getColor(this, colorResId);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (isLoading) {
            if (scrollViewContent != null) scrollViewContent.setVisibility(View.GONE);
            if (tvErrorMessage != null) tvErrorMessage.setVisibility(View.GONE);
        }
        // Không tự động ẩn scrollViewContent và tvErrorMessage khi !isLoading
        // việc này sẽ được xử lý bởi fetchOrderDetailWithRetrofit
    }

    private void showError(String message) {
        if (scrollViewContent != null) {
            scrollViewContent.setVisibility(View.GONE); // Ẩn nội dung chính khi có lỗi
        }
        if (tvErrorMessage != null) {
            if (message != null && !message.isEmpty()) {
                tvErrorMessage.setText(message);
                tvErrorMessage.setVisibility(View.VISIBLE);
            } else {
                tvErrorMessage.setText("Đã xảy ra lỗi không xác định."); // Thông báo lỗi mặc định
                tvErrorMessage.setVisibility(View.VISIBLE);
            }
        }
    }
}