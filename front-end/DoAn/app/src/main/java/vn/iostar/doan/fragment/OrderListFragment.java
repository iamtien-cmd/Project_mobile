package vn.iostar.doan.fragment;

import android.app.AlertDialog;
import android.content.Intent; // <<< Đảm bảo đã import Intent
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
// import okhttp3.MediaType; // MediaType không được dùng trong code này
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import vn.iostar.doan.R;
import vn.iostar.doan.activity.OrderDetailActivity; // <<< THÊM IMPORT (Giả sử tên Activity là thế này)
import vn.iostar.doan.activity.ProductRatingActivity; // <<< Đảm bảo Import đúng Activity
import vn.iostar.doan.adapter.OrderAdapter;
//import vn.iostar.doan.adapter.OrderInteractionListener; // Interface định nghĩa trong Adapter rồi
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine;
import vn.iostar.doan.model.OrderStatus;
import vn.iostar.doan.model.Product;

// <<< Đã implement OrderAdapter.OrderInteractionListener >>>
public class OrderListFragment extends Fragment implements OrderAdapter.OrderInteractionListener {

    private static final String TAG = "OrderListFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_ORDER_STATUS = "order_status";

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvNoOrders;

    // Khởi tạo OkHttpClient một lần
    private final OkHttpClient client = new OkHttpClient();
    // Khởi tạo Gson một lần, cấu hình DateFormat nếu cần
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Hoặc định dạng phù hợp với API của bạn
            .create();

    // <<< Đảm bảo BASE_URL chính xác và kết thúc bằng / >>>
    // <<< 10.0.2.2 là IP đặc biệt để kết nối từ Emulator đến localhost của máy host >>>
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    private Long userId;
    private String targetStatus; // Trạng thái muốn lọc (vd: "WAITING", "RECEIVED")

    // --- Factory Method để tạo Fragment với arguments ---
    public static OrderListFragment newInstance(Long userId, String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        args.putString(ARG_ORDER_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
            targetStatus = getArguments().getString(ARG_ORDER_STATUS);
            Log.d(TAG, "Fragment created for UserID: " + userId + ", Status: " + targetStatus);
        } else {
            Log.e(TAG, "Fragment created without required arguments! Using defaults.");
            userId = 1L; // Giá trị mặc định phòng trường hợp lỗi
            targetStatus = "WAITING"; // Hoặc null nếu muốn hiển thị lỗi
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho fragment này
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ Views từ layout
        recyclerViewOrders = view.findViewById(R.id.recycler_view_orders_fragment);
        progressBar = view.findViewById(R.id.progress_bar_fragment);
        tvErrorMessage = view.findViewById(R.id.tv_error_message_fragment);
        tvNoOrders = view.findViewById(R.id.tv_no_orders_fragment);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Lấy dữ liệu đơn hàng từ API
        // Kiểm tra lại userId và targetStatus trước khi gọi API
        if (userId != null && userId > 0 && targetStatus != null && !targetStatus.isEmpty()) {
            fetchOrdersWithOkHttp(userId, targetStatus);
        } else {
            Log.e(TAG, "Invalid arguments for fetching orders. UserID: " + userId + ", Status: " + targetStatus);
            showError("Lỗi cấu hình: Không thể tải danh sách đơn hàng.");
        }
    }

    // --- Thiết lập RecyclerView và Adapter ---
    private void setupRecyclerView() {
        // Đảm bảo context không null
        if (getContext() == null) {
            Log.e(TAG, "Context is null during setupRecyclerView");
            return;
        }
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        // Truyền 'this' (Fragment hiện tại) làm listener cho adapter
        // Vì Fragment này đã implements OrderAdapter.OrderInteractionListener
        orderAdapter = new OrderAdapter(getContext(), this);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    // --- Gọi API lấy danh sách đơn hàng theo User ID ---
    private void fetchOrdersWithOkHttp(Long currentUserId, final String statusToFilter) {
        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE); // Hiển thị RecyclerView ban đầu

        // URL để lấy TẤT CẢ đơn hàng của user (API cần hỗ trợ việc này)
        String url = BASE_URL + "api/orders/status/" + currentUserId; // <<< KIỂM TRA LẠI ENDPOINT API NÀY
        Log.d(TAG, "Fetching orders from URL: " + url);

        // === TODO: THÊM AUTHENTICATION TOKEN NẾU API YÊU CẦU ===
        // String authToken = getAuthTokenFromSomewhere(); // Lấy token (SharedPreferences,...)
        // if (authToken == null || authToken.isEmpty()) {
        //     showError("Lỗi xác thực.");
        //     showLoading(false);
        //     Log.e(TAG, "Authentication token is missing!");
        //     return;
        // }
        Request.Builder requestBuilder = new Request.Builder().url(url);
        // requestBuilder.addHeader("Authorization", "Bearer " + authToken); // Thêm header nếu cần
        Request request = requestBuilder.build();
        // =========================================================

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Fetch onFailure: ", e);
                // Cập nhật UI trên Main Thread và kiểm tra Fragment còn tồn tại
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        // Chỉ hiển thị lỗi mạng nếu chưa có lỗi nào khác được hiển thị
                        if (tvErrorMessage.getVisibility() == View.GONE) {
                            showError("Lỗi kết nối mạng. Vui lòng thử lại.");
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // Luôn kiểm tra response body và đóng nó sau khi dùng xong (try-with-resources)
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        // Xử lý lỗi từ server (4xx, 5xx)
                        final String errorMsg = "Lỗi Server: " + response.code() + " " + response.message();
                        String errorBodyStr = (responseBody != null) ? responseBody.string() : "null";
                        Log.e(TAG, "OkHttp Fetch onResponse Error: " + errorMsg + ", Body: " + errorBodyStr);
                        // Cập nhật UI trên Main Thread
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Lỗi " + response.code() + ": Không thể tải dữ liệu."); // Thông báo lỗi chung chung hơn
                            });
                        }
                        return; // Kết thúc xử lý nếu có lỗi
                    }

                    // Xử lý khi thành công (2xx)
                    if (responseBody == null) {
                        Log.e(TAG, "OkHttp Fetch onResponse Success but body is null");
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Không nhận được dữ liệu từ server.");
                            });
                        }
                        return;
                    }

                    // Đọc dữ liệu JSON từ response body
                    final String responseData = responseBody.string();
                    Log.d(TAG, "OkHttp Fetch onResponse Success JSON: " + responseData.substring(0, Math.min(responseData.length(), 500)) + "..."); // Log một phần dữ liệu

                    // Parse JSON sử dụng Gson
                    Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
                    final List<Order> allOrders = gson.fromJson(responseData, listType);

                    // Kiểm tra kết quả parse
                    if (allOrders == null) {
                        Log.e(TAG, "Parsed order list is null. JSON might be malformed or not an array.");
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Dữ liệu trả về không đúng định dạng.");
                            });
                        }
                        return;
                    }

                    // Lọc danh sách đơn hàng theo trạng thái targetStatus
                    List<Order> filteredOrders;
                    // Sử dụng Stream API cho Android N (API 24) trở lên
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        filteredOrders = allOrders.stream()
                                .filter(order -> order.getStatus() != null &&
                                        order.getStatus().name().equalsIgnoreCase(statusToFilter))
                                .collect(Collectors.toList());
                    } else { // Dùng vòng lặp cho phiên bản Android cũ hơn
                        filteredOrders = new ArrayList<>();
                        for (Order order : allOrders) {
                            // So sánh trạng thái không phân biệt chữ hoa/thường
                            if (order.getStatus() != null &&
                                    order.getStatus().name().equalsIgnoreCase(statusToFilter)) {
                                filteredOrders.add(order);
                            }
                        }
                    }
                    Log.d(TAG, "Total orders fetched: " + allOrders.size() + ", Filtered orders for status '" + statusToFilter + "': " + filteredOrders.size());

                    // Cập nhật UI trên Main Thread với danh sách đã lọc
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (filteredOrders.isEmpty()) {
                                showNoOrdersMessage(true); // Hiển thị thông báo "Không có đơn hàng"
                                orderAdapter.setOrders(new ArrayList<>()); // Xóa dữ liệu cũ trong adapter
                            } else {
                                orderAdapter.setOrders(filteredOrders); // Cập nhật adapter với dữ liệu mới
                                showNoOrdersMessage(false); // Ẩn thông báo "Không có đơn hàng"
                            }
                            showError(null); // Xóa thông báo lỗi (nếu có)
                        });
                    }

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON Parsing Error: ", e);
                    Log.e(TAG, "Failed to parse JSON, Raw Response Data was: " + "Data too long or unavailable"); // Tránh log response quá dài
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showError("Lỗi xử lý dữ liệu trả về (JSON).");
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException reading response body: ", e);
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (tvErrorMessage.getVisibility() == View.GONE) {
                                showError("Lỗi đọc dữ liệu từ server.");
                            }
                        });
                    }
                } catch (Exception e) { // Bắt các lỗi không mong muốn khác
                    Log.e(TAG, "Unexpected error in onResponse processing: ", e);
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (tvErrorMessage.getVisibility() == View.GONE) {
                                showError("Đã xảy ra lỗi không mong muốn.");
                            }
                        });
                    }
                }
            }
        });
    }


    // --- Gọi API hủy đơn hàng ---
    private void callCancelOrderApi(long orderId) {
        // Kiểm tra trạng thái Fragment và Context
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached or context is null, cannot call cancel API.");
            return;
        }

        // Hiển thị thông báo đang xử lý
        Toast.makeText(requireContext(), "Đang gửi yêu cầu hủy...", Toast.LENGTH_SHORT).show();
        showLoading(true); // Có thể hiển thị loading indicator

        // === TODO: THÊM AUTHENTICATION TOKEN NẾU API YÊU CẦU ===
        // String authToken = getAuthTokenFromSomewhere();
        // if (authToken == null || authToken.isEmpty()) {
        //     handleAuthError();
        //     return;
        // }
        String url = BASE_URL + "api/orders/" + orderId + "/cancel"; // <<< KIỂM TRA LẠI ENDPOINT
        Log.d(TAG, "Calling cancel API: " + url);

        // API hủy đơn thường không cần body, dùng PUT hoặc POST rỗng
        RequestBody emptyBody = RequestBody.create(new byte[0], null);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(emptyBody); // <<< KIỂM TRA PHƯƠNG THỨC HTTP (PUT/POST/...) VỚI API
        // requestBuilder.addHeader("Authorization", "Bearer " + authToken); // Thêm header nếu cần
        Request request = requestBuilder.build();
        // =========================================================

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Cancel Order onFailure: ", e);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Hủy đơn thất bại: Lỗi mạng", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // Đảm bảo xử lý trên UI thread và kiểm tra Fragment state
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        // Luôn đóng response body
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {
                                // Xử lý thành công
                                Log.d(TAG, "OkHttp Cancel Order Success. Code: " + response.code());
                                Toast.makeText(requireContext(), "Đã hủy đơn hàng #" + orderId, Toast.LENGTH_SHORT).show();
                                // Tải lại danh sách đơn hàng sau khi hủy thành công
                                if (userId != null && targetStatus != null) {
                                    fetchOrdersWithOkHttp(userId, targetStatus);
                                } else {
                                    showLoading(false); // Chỉ ẩn loading nếu không thể refresh
                                }

                            } else {
                                // Xử lý lỗi từ server
                                String errorMessage = "Hủy đơn thất bại: Mã lỗi " + response.code();
                                String errorBodyString = null;
                                if (responseBody != null) {
                                    try {
                                        errorBodyString = responseBody.string();
                                        // Cố gắng parse lỗi từ server nếu có cấu trúc
                                        // Ví dụ: errorMessage = parseErrorFromBody(errorBodyString);
                                        if (errorBodyString != null && !errorBodyString.isEmpty() && !errorBodyString.startsWith("<")) { // Tránh log HTML
                                            errorMessage = "Hủy thất bại: " + errorBodyString.substring(0, Math.min(errorBodyString.length(), 100)); // Cắt ngắn lỗi
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, "Error reading error body for cancel", e);
                                    }
                                }
                                Log.e(TAG, "OkHttp Cancel Order Error: " + errorMessage + (errorBodyString != null ? " (Raw Body Logged Separately)" : ""));
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                                showLoading(false); // Ẩn loading khi có lỗi
                            }
                        } catch (Exception e) { // Bắt lỗi không mong muốn khi xử lý response
                            Log.e(TAG, "Unexpected error processing cancel response", e);
                            Toast.makeText(requireContext(), "Lỗi không xác định khi hủy đơn.", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    });
                }
            }
        });
    }

    // --- Các hàm tiện ích hiển thị UI ---
    private void showLoading(boolean isLoading) {
        if (!isAdded() || progressBar == null) return; // Kiểm tra trước khi thao tác UI
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (!isAdded() || tvErrorMessage == null || recyclerViewOrders == null || tvNoOrders == null) return;
        if (message != null && !message.isEmpty()) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE); // Ẩn list khi có lỗi
            tvNoOrders.setVisibility(View.GONE);    // Ẩn thông báo "không có đơn"
        } else {
            tvErrorMessage.setVisibility(View.GONE); // Ẩn text lỗi
            // Không tự động hiện lại RecyclerView ở đây, để hàm fetch quyết định
        }
    }

    private void showNoOrdersMessage(boolean show) {
        if (!isAdded() || tvNoOrders == null || recyclerViewOrders == null || tvErrorMessage == null) return;
        tvNoOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        // Chỉ ẩn RecyclerView nếu thông báo "không có đơn" được hiển thị
        if (show) {
            recyclerViewOrders.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE); // Đảm bảo text lỗi cũng ẩn
        }
        // Không cần else để hiện RecyclerView, vì hàm fetch sẽ làm điều đó nếu có dữ liệu
    }
    // --- Kết thúc các hàm tiện ích UI ---


    // === IMPLEMENT CÁC PHƯƠNG THỨC TỪ OrderInteractionListener ===

    @Override
    public void onCancelOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return; // Kiểm tra trạng thái Fragment

        Log.d(TAG, "Cancel button clicked for order ID: " + order.getOrderId());
        // Hiển thị Dialog xác nhận trước khi gọi API
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + order.getOrderId() + " không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    // Chỉ gọi API nếu người dùng nhấn Đồng ý
                    callCancelOrderApi(order.getOrderId());
                })
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss()) // Đóng dialog nếu nhấn Không
                .setIcon(android.R.drawable.ic_dialog_alert) // Icon cảnh báo
                .show();
    }

    @Override
    public void onReviewOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;

        Log.d(TAG, "Review button clicked for order ID: " + order.getOrderId());

        // === TODO: Xác định logic lấy Product ID để đánh giá ===
        // Hiện tại đang lấy sản phẩm ĐẦU TIÊN trong danh sách
        // Cần xem xét nếu một đơn hàng có nhiều sản phẩm, muốn đánh giá sản phẩm nào?
        long productIdToReview = -1L; // Giá trị mặc định không hợp lệ

        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            OrderLine firstLine = order.getOrderLines().get(0); // Lấy dòng đầu tiên
            if (firstLine != null && firstLine.getProduct() != null) {
                productIdToReview = firstLine.getProduct().getProductId();
            } else {
                Log.w(TAG, "First order line or its product is null for order: " + order.getOrderId());
            }
        } else {
            Log.w(TAG, "Order lines are null or empty for order: " + order.getOrderId());
        }

        // Chỉ điều hướng nếu tìm thấy Product ID hợp lệ (giả sử ID > 0)
        if (productIdToReview > 0) {
            Log.i(TAG, "Navigating to ProductRatingActivity. ProductID: " + productIdToReview + ", OrderID: " + order.getOrderId());
            Intent intent = new Intent(requireContext(), ProductRatingActivity.class);
            intent.putExtra("PRODUCT_ID", productIdToReview); // <<< KIỂM TRA KEY NÀY VỚI RATING ACTIVITY
            // intent.putExtra("ORDER_ID", order.getOrderId()); // Có thể cần truyền cả Order ID
            startActivity(intent);
        } else {
            // Thông báo lỗi nếu không tìm thấy sản phẩm để đánh giá
            Toast.makeText(requireContext(), "Không tìm thấy thông tin sản phẩm để đánh giá.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No valid product found in order " + order.getOrderId() + " to navigate to rating screen.");
        }
        // ======================================================
    }

    @Override
    public void onRepurchaseOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;

        Log.d(TAG, "Repurchase button clicked for order ID: " + order.getOrderId());

        // === TODO: Implement logic thêm sản phẩm vào giỏ hàng ===
        // 1. Lặp qua order.getOrderLines()
        // 2. Với mỗi OrderLine, lấy productId và quantity
        // 3. Gọi API hoặc phương thức để thêm các sản phẩm này vào giỏ hàng hiện tại
        // 4. Hiển thị thông báo thành công/thất bại
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            // Ví dụ đơn giản: chỉ hiển thị Toast
            Toast.makeText(requireContext(), "Chức năng Mua lại đang được phát triển", Toast.LENGTH_SHORT).show();
            // Hoặc: callAddToCartApi(order.getOrderLines());
        } else {
            Toast.makeText(requireContext(), "Không có sản phẩm trong đơn hàng này để mua lại.", Toast.LENGTH_SHORT).show();
        }
        // ======================================================
    }

    @Override
    public void onViewDetailsClicked(Order order) {
        if (!isAdded() || getContext() == null) return;

        Log.d(TAG, "View Details button clicked for order ID: " + order.getOrderId());

        // === TODO: Implement điều hướng đến màn hình Chi tiết đơn hàng ===
        // 1. Tạo Intent đến OrderDetailActivity (hoặc tên tương tự)
        // 2. Truyền ORDER_ID (bắt buộc) và có thể cả đối tượng Order (nếu Parcelable)
        // 3. startActivity
        Intent intent = new Intent(requireContext(), OrderDetailActivity.class); // <<< THAY TÊN ACTIVITY NẾU KHÁC
        intent.putExtra("ORDER_ID", order.getOrderId()); // <<< KIỂM TRA KEY NÀY VỚI DETAIL ACTIVITY
        // Nếu Order là Parcelable/Serializable, bạn có thể truyền cả đối tượng:
        // intent.putExtra("ORDER_OBJECT", order);
        startActivity(intent);
        // ============================================================
    }

    // === Kết thúc phần implement OrderInteractionListener ===

} // Kết thúc class OrderListFragment