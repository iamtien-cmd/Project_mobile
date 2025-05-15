package vn.iostar.doan.activity;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.List;
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
import vn.iostar.doan.model.User;


public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailActivity";

    public static final String EXTRA_ORDER_ID = "ORDER_ID";
    public static final String EXTRA_ACTION_MODE = "ACTION_MODE_KEY";
    public static final String EXTRA_USER_ID_FOR_REVIEW = "USER_ID_FOR_REVIEW_KEY";
    public static final String ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW = "SELECT_PRODUCT_FOR_REVIEW_ACTION";

    // ** Thêm hằng số cho key truyền Product ID (đã có) **
    public static final String EXTRA_PRODUCT_ID = "productId"; // <-- Sử dụng key mà ProductDetailActivity đang dùng

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private ScrollView scrollViewContent;
    private TextView tvDetailOrderId, tvDetailOrderStatus, tvDetailOrderDate, tvDetailPredictDate, tvDetailPaymentMethod;
    private TextView tvDetailRecipientInfoAndAddress;
    private LinearLayout llOrderDetailItems;
    private TextView tvDetailItemsSubtotal, tvDetailShippingFee, tvDetailDiscountAmount, tvDetailTotalPrice;
    private RelativeLayout layoutDiscount;
    private TextView tvDetailDiscountLabel;

    // Biến cho thông tin người đặt
    private TextView tvOrderPlacerName, tvOrderPlacerPhone, tvOrderPlacerEmail;

    private NumberFormat currencyFormatter;
    private SimpleDateFormat displayDateFormat;

    private long orderId = -1;
    private String currentActionMode = null;
    private Long userIdForReview = -1L; // ID người dùng cho mục đích review (nếu có)
    private long currentLoggedInUserId = -1L; // << THÊM BIẾN NÀY để lưu ID người dùng hiện tại
    private Order currentOrderData;
    private ImageView ivCartMenuBottom, ivCartLocation, ivCartAboutUs, ivCartIconSelf; // Biến cho bottom nav
    private String authToken;
    private ActivityResultLauncher<Intent> productRatingLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        authToken = prefs.getString("token", "");
        currentLoggedInUserId = prefs.getLong("userId", -1L); // << LẤY USER ID TỪ SHARED PREFS

        Intent intent = getIntent();
        orderId = intent.getLongExtra(EXTRA_ORDER_ID, -1L);
        if (intent.hasExtra(EXTRA_ACTION_MODE)) {
            currentActionMode = intent.getStringExtra(EXTRA_ACTION_MODE);
        }
        // userIdForReview chỉ được dùng trong chế độ đánh giá, khác với currentLoggedInUserId
        if (intent.hasExtra(EXTRA_USER_ID_FOR_REVIEW)) {
            userIdForReview = intent.getLongExtra(EXTRA_USER_ID_FOR_REVIEW, -1L);
        }

        if (orderId == -1) {
            Log.e(TAG, "Order ID not passed correctly via Intent.");

            finish();
            return;
        }
        Log.d(TAG, "Received Order ID: " + orderId + ", ActionMode: " + currentActionMode + ", UserID for Review: " + userIdForReview + ", Logged-in UserID: " + currentLoggedInUserId);


        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        displayDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        initProductRatingLauncher();
        setupViews();
        setupBottomNavigation(); // Gọi setup bottom navigation sau khi AnhXa
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode)) {
                getSupportActionBar().setTitle("Chọn sản phẩm để đánh giá");
            } else {
                // Tiêu đề sẽ được cập nhật sau khi fetch data
                getSupportActionBar().setTitle("Chi tiết Đơn hàng");
            }
        }

        // Luôn fetch data để đảm bảo dữ liệu mới nhất
        fetchOrderDetailWithRetrofit(orderId);
    }

    // ... (Các phương thức setupBottomNavigation, initProductRatingLauncher, setupViews, onOptionsItemSelected giữ nguyên)
    private void setupBottomNavigation() {
        // Xử lý click cho icon Home (ivCartMenuBottom)
        if (ivCartMenuBottom != null) {
            ivCartMenuBottom.setOnClickListener(v -> {
                Log.d(TAG, "Home icon clicked from OrderDetailActivity");
                Intent intent = new Intent(OrderDetailActivity.this, HomeActivity.class);
                intent.putExtra("token", authToken); // Truyền token về HomeActivity
                // Dùng cờ để clear stack và đưa HomeActivity lên đầu
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish(); // Đóng OrderDetailActivity
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartMenuBottom is null!");
        }

        // Xử lý click cho icon Location (ivCartLocation)
        if (ivCartLocation != null) {
            ivCartLocation.setOnClickListener(v -> {
                Log.d(TAG, "Location icon clicked from OrderDetailActivity");
                Intent intent = new Intent(OrderDetailActivity.this, AboutAppActivity.class); // Hoặc Activity khác bạn muốn
                // intent.putExtra("token", authToken); // Nếu cần
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartLocation is null!");
        }

        // Xử lý click cho icon About Us (ivCartAboutUs)
        if (ivCartAboutUs != null) {
            ivCartAboutUs.setOnClickListener(v -> {
                Log.d(TAG, "About Us icon clicked from OrderDetailActivity");
                Intent intent = new Intent(OrderDetailActivity.this, AboutUsActivity.class);
                startActivity(intent);
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartAboutUs is null!");
        }

        // Xử lý click cho icon User/Profile (ivCartIconSelf)
        if (ivCartIconSelf != null) {
            ivCartIconSelf.setOnClickListener(v -> {
                Log.d(TAG, "Cart icon clicked from OrderDetailActivity");
                Intent intent = new Intent(OrderDetailActivity.this, CartActivity.class); // Chuyển đến CartActivity
                intent.putExtra("token", authToken); // Truyền token
                startActivity(intent);
                // KHÔNG finish() ở đây trừ khi bạn muốn thoát hoàn toàn khỏi luồng Order Detail -> Cart
            });
        } else {
            Log.e(TAG, "setupBottomNavigation: ivCartIconSelf is null!");
        }
    }
    private void initProductRatingLauncher() {
        productRatingLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Nhận kết quả từ ProductRatingActivity
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "ProductRatingActivity returned RESULT_OK.");
                        // Nếu review thành công, có thể muốn refresh chi tiết đơn hàng
                        // hoặc báo lại cho OrderHistoryActivity để refresh danh sách
                        // Dựa vào logic hiện tại, bạn set RESULT_OK và finish() để quay lại OrderHistoryActivity
                        setResult(Activity.RESULT_OK);
                        finish(); // Quay lại OrderHistoryActivity
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Log.d(TAG, "ProductRatingActivity returned RESULT_CANCELED.");
                        // Nếu người dùng hủy đánh giá, có thể không làm gì hoặc refresh
                        // Dựa vào logic hiện tại, bạn set RESULT_CANCELED và finish()
                        setResult(Activity.RESULT_CANCELED);
                        finish(); // Quay lại OrderHistoryActivity
                    } else {
                        Log.w(TAG, "ProductRatingActivity returned unexpected result code: " + result.getResultCode());
                        // Xử lý các mã lỗi khác nếu có
                        setResult(Activity.RESULT_CANCELED); // Coi như bị hủy
                        finish(); // Quay lại OrderHistoryActivity
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

        // THÔNG TIN NGƯỜI ĐẶT
        tvOrderPlacerName = findViewById(R.id.tv_order_placer_name);
        tvOrderPlacerPhone = findViewById(R.id.tv_order_placer_phone);
        tvOrderPlacerEmail = findViewById(R.id.tv_order_placer_email);

        // THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ
        tvDetailRecipientInfoAndAddress = findViewById(R.id.tv_detail_recipient_info_and_address);

        // Danh sách sản phẩm và Tóm tắt đơn hàng
        llOrderDetailItems = findViewById(R.id.ll_order_detail_items);
        tvDetailItemsSubtotal = findViewById(R.id.tv_detail_items_subtotal);
        tvDetailShippingFee = findViewById(R.id.tv_detail_shipping_fee);
        tvDetailDiscountAmount = findViewById(R.id.tv_detail_discount_amount);
        tvDetailTotalPrice = findViewById(R.id.tv_detail_total_price);
        layoutDiscount = findViewById(R.id.layout_discount);
        tvDetailDiscountLabel = findViewById(R.id.tv_detail_discount_label);

        // Ánh xạ các icon Bottom Navigation (đã nằm trong layout gốc activity_order_detail)
        // LinearLayout cha của các icon trong layout đã combine
        LinearLayout bottomNavLayout = findViewById(R.id.bottomNav);

        if (bottomNavLayout != null) {
            ivCartMenuBottom = bottomNavLayout.findViewById(R.id.ivMenuBottom);
            ivCartLocation = bottomNavLayout.findViewById(R.id.ivLocation);
            ivCartAboutUs = bottomNavLayout.findViewById(R.id.ivaboutus);
            ivCartIconSelf = bottomNavLayout.findViewById(R.id.ivcart);

            if (ivCartMenuBottom == null) Log.w(TAG, "setupViews: ivMenuBottom not found in bottomNav.");
            if (ivCartLocation == null) Log.w(TAG, "setupViews: ivLocation not found in bottomNav.");
            if (ivCartAboutUs == null) Log.w(TAG, "setupViews: ivaboutus not found in bottomNav.");
            if (ivCartIconSelf == null) Log.w(TAG, "setupViews: ivcart not found in bottomNav.");
        } else {
            Log.e(TAG, "setupViews: bottomNav LinearLayout not found!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Nếu đang ở chế độ chọn sản phẩm đánh giá, báo cancel
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

        // HIỂN THỊ THÔNG TIN NGƯỜI ĐẶT HÀNG
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

        // HIỂN THỊ THÔNG TIN NGƯỜI NHẬN & ĐỊA CHỈ
        String shippingInfo = order.getShippingAddress();

        if (shippingInfo != null && !shippingInfo.trim().isEmpty()) {
            if (shippingInfo.equals("||")) {
                tvDetailRecipientInfoAndAddress.setText("Thông tin người nhận và địa chỉ không có.");
            } else {
                String[] parts = shippingInfo.split("\\|", -1);
                String name = (parts.length > 0) ? parts[0].trim() : "";
                String phone = (parts.length > 1) ? parts[1].trim() : "";
                String street = (parts.length > 2) ? parts[2].trim() : "";

                StringBuilder displayBuilder = new StringBuilder();
                boolean firstLineAdded = false;

                if (!name.isEmpty()) {
                    displayBuilder.append("Người nhận: ").append(name);
                    firstLineAdded = true;
                }
                if (!phone.isEmpty()) {
                    if (firstLineAdded) displayBuilder.append("\n");
                    displayBuilder.append("SĐT: ").append(phone);
                    firstLineAdded = true;
                }
                if (!street.isEmpty()) {
                    if (firstLineAdded) displayBuilder.append("\n");
                    displayBuilder.append("Địa chỉ: ").append(street);
                }

                if (displayBuilder.length() > 0) {
                    tvDetailRecipientInfoAndAddress.setText(displayBuilder.toString());
                } else {
                    tvDetailRecipientInfoAndAddress.setText("Thông tin người nhận và địa chỉ không đầy đủ.");
                }
            }
        } else {
            tvDetailRecipientInfoAndAddress.setText("Không có thông tin người nhận hoặc địa chỉ.");
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
                            .placeholder(R.drawable.placeholder_image) // Thay bằng placeholder của bạn
                            .error(R.drawable.error_image) // Thay bằng error image của bạn
                            .into(ivProduct);

                    // ** THÊM ONCLICKLISTENER CHO TOÀN BỘ ITEM SẢN PHẨM **
                    final long clickedProductId = product.getProductId(); // Lấy ID sản phẩm
                    itemView.setOnClickListener(v -> {
                        Log.d(TAG, "Product item clicked. Launching ProductDetailActivity for product ID: " + clickedProductId);
                        // Gọi hàm chuyển Activity, truyền ID sản phẩm, User ID và Token
                        navigateToProductDetail(clickedProductId, currentLoggedInUserId, authToken);
                    });
                    // ** KẾT THÚC THÊM ONCLICKLISTENER **


                    if (ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW.equals(currentActionMode) &&
                            order.getStatus() == OrderStatus.RECEIVED && // Chỉ cho đánh giá khi đã nhận
                            userIdForReview != null && userIdForReview > 0L) {

                        btnEvaluateThisProduct.setVisibility(View.VISIBLE);
                        final long currentSelectedProductId = product.getProductId();

                        btnEvaluateThisProduct.setOnClickListener(v -> {
                            Log.d(TAG, "Evaluate this product button clicked. ProductId: " + currentSelectedProductId +
                                    ", UserId: " + userIdForReview + ", OrderId: " + order.getOrderId());

                            Intent intentToProductRating = new Intent(OrderDetailActivity.this, ProductRatingActivity.class);
                            intentToProductRating.putExtra("product_id", currentSelectedProductId);
                            intentToProductRating.putExtra("user_id", userIdForReview);
                            intentToProductRating.putExtra("order_id", order.getOrderId());
                            // Có thể truyền thêm token nếu ProductRatingActivity cần
                            intentToProductRating.putExtra("token", authToken);


                            if (productRatingLauncher != null) {
                                productRatingLauncher.launch(intentToProductRating);
                            } else {
                                Log.e(TAG, "productRatingLauncher is null! Cannot launch ProductRatingActivity for result.");
                                // Fallback: Start activity without result
                                startActivity(intentToProductRating);
                                // Nếu không dùng launcher, không thể báo kết quả về OrderHistoryActivity
                                // Có thể thông báo cho người dùng và không finish()
                                Toast.makeText(OrderDetailActivity.this, "Không thể theo dõi kết quả đánh giá.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        btnEvaluateThisProduct.setVisibility(View.GONE);
                    }
                } else {
                    tvName.setText("Sản phẩm không xác định");
                    ivProduct.setImageResource(R.drawable.placeholder_image); // Hoặc một ảnh mặc định
                    btnEvaluateThisProduct.setVisibility(View.GONE);
                    // Nếu sản phẩm null, không cho click item
                    itemView.setClickable(false);
                }

                tvQuantity.setText("SL: x" + line.getQuantity());
                tvPrice.setText(currencyFormatter.format(line.getPrice()));
                llOrderDetailItems.addView(itemView);

                // Thêm divider nếu không phải item cuối
                if (i < order.getOrderLines().size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelSize(R.dimen.divider_height)
                    );
                    params.setMargins(0, getResources().getDimensionPixelSize(R.dimen.divider_margin_vertical),
                            0, getResources().getDimensionPixelSize(R.dimen.divider_margin_vertical));
                    divider.setLayoutParams(params);
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
                    llOrderDetailItems.addView(divider);
                }
            }
        } else {
            TextView noItemsText = new TextView(this);
            noItemsText.setText("Không có sản phẩm trong đơn hàng này.");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 16,0,16);
            noItemsText.setLayoutParams(params);
            llOrderDetailItems.addView(noItemsText);
        }

        // Tóm tắt đơn hàng
        double itemsSubtotal = order.getItemsSubtotal();
        double totalPrice = order.getTotalPrice();
        double discountAmount = order.getDiscountAmount(); // Lấy discount amount

        // Tính toán shippingFee: Total = Subtotal + Shipping - Discount
        // => Shipping = Total - Subtotal + Discount
        double calculatedShippingFee = totalPrice - itemsSubtotal + discountAmount;

        tvDetailItemsSubtotal.setText(currencyFormatter.format(itemsSubtotal));
        tvDetailShippingFee.setText(currencyFormatter.format(calculatedShippingFee)); // Sử dụng giá trị vừa tính

        if (discountAmount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            String discountLabel = "Giảm giá";
            if (order.getVoucherCode() != null && !order.getVoucherCode().isEmpty()) {
                discountLabel += " (" + order.getVoucherCode() + ")";
            }
            tvDetailDiscountLabel.setText(discountLabel + ":");
            tvDetailDiscountAmount.setText("- " + currencyFormatter.format(discountAmount));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }
        tvDetailTotalPrice.setText(currencyFormatter.format(totalPrice));
    }

    // ** THÊM PHƯƠNG THỨC CHUYỂN ĐẾN CHI TIẾT SẢN PHẨM **
    private void navigateToProductDetail(long productId, long userId, String token) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, productId); // Key phải khớp với ProductDetailActivity
        intent.putExtra("userId", userId);           // Truyền User ID (currentLoggedInUserId)
        intent.putExtra("token", token);             // Truyền Token

        startActivity(intent);
    }
    // ** KẾT THÚC PHƯƠNG THỨC CHUYỂN ĐẾN CHI TIẾT SẢN PHẨM **


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
            default: return status.name();
        }
    }

    private int getStatusColor(OrderStatus status) {
        int colorResId = R.color.my_grey_neutral;
        if (status != null) {
            switch (status) {
                case RECEIVED: colorResId = R.color.my_green_success; break;
                case SHIPPING: colorResId = R.color.my_orange_processing; break;
                case CANCELLED:
                case ERROR:    colorResId = R.color.my_red_error; break;
                case WAITING:
                case REVIEWED: colorResId = R.color.my_blue_info; break;
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
                tvErrorMessage.setText("Đã xảy ra lỗi không xác định.");
                tvErrorMessage.setVisibility(View.VISIBLE);
            }
        }
    }

    // Cần thêm định nghĩa cho R.dimen.divider_height, R.dimen.divider_margin_vertical
    // và R.color.divider_color trong các file values/dimens.xml và values/colors.xml
}