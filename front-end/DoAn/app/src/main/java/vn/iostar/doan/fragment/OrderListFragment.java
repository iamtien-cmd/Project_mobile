package vn.iostar.doan.fragment; // <<<< THAY ĐỔI CHO ĐÚNG PACKAGE CỦA BẠN

import android.app.AlertDialog;
import android.content.Intent;
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
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import vn.iostar.doan.R; // <<<< Đảm bảo R đúng
import vn.iostar.doan.activity.OrderDetailActivity;
import vn.iostar.doan.activity.OrderHistoryActivity; // Để gọi launchReviewActivity
// import vn.iostar.doan.activity.ProductRatingActivity; // Không gọi trực tiếp từ đây nữa
import vn.iostar.doan.adapter.OrderAdapter;
import vn.iostar.doan.model.Order;
// import vn.iostar.doan.model.OrderLine; // Không cần trực tiếp ở đây nếu OrderDetailActivity xử lý
// import vn.iostar.doan.model.OrderStatus; // Không cần trực tiếp ở đây nếu Order model có

public class OrderListFragment extends Fragment implements OrderAdapter.OrderInteractionListener {

    private static final String TAG = "OrderListFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_ORDER_STATUS = "order_status";

    // Key để OrderDetailActivity biết nó đang ở chế độ chọn sản phẩm để đánh giá
    public static final String ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW = "ACTION_SELECT_PRODUCT_FOR_REVIEW";
    public static final String EXTRA_USER_ID_FOR_REVIEW = "USER_ID_FOR_REVIEW";


    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvNoOrders;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // ISO 8601 với milliseconds và timezone offset
            .create();

    // Đảm bảo URL này đúng và có thể truy cập từ emulator/thiết bị
    // Sử dụng localhost của máy host từ emulator là 10.0.2.2
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // <<<< KIỂM TRA URL NÀY

    private Long userId;
    private String targetStatus; // Trạng thái của Fragment này (vd: "WAITING", "RECEIVED")

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
            userId = -1L;
            targetStatus = "UNKNOWN";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewOrders = view.findViewById(R.id.recycler_view_orders_fragment);
        progressBar = view.findViewById(R.id.progress_bar_fragment);
        tvErrorMessage = view.findViewById(R.id.tv_error_message_fragment);
        tvNoOrders = view.findViewById(R.id.tv_no_orders_fragment);

        setupRecyclerView();

        if (userId != null && userId > 0 && targetStatus != null && !targetStatus.equals("UNKNOWN")) {
            fetchOrdersWithOkHttp(userId, targetStatus);
        } else {
            String errorMsg = "Lỗi cấu hình: Không thể tải danh sách đơn hàng (UserID: " + userId + ", Status: " + targetStatus + ")";
            Log.e(TAG, errorMsg);
            showError(errorMsg);
        }
    }

    private void setupRecyclerView() {
        if (getContext() == null) {
            Log.e(TAG, "Context is null during setupRecyclerView");
            return;
        }
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        // Truyền targetStatus vào OrderAdapter nếu Adapter cần nó để hiển thị nút "Đánh giá" có điều kiện
        orderAdapter = new OrderAdapter(getContext(), this, targetStatus);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void fetchOrdersWithOkHttp(Long currentUserId, final String statusToFilter) {
        showLoading(true);
        showError(null); // Xóa lỗi cũ
        showNoOrdersMessage(false); // Ẩn thông báo "không có đơn"
        // recyclerViewOrders.setVisibility(View.VISIBLE); // Đảm bảo RecyclerView hiển thị khi bắt đầu fetch

        // API lý tưởng: /api/orders/user/{userId}/status/{statusName}
        // API hiện tại (ví dụ): /api/orders/user/{userId} -> Lấy tất cả rồi lọc ở client
        // Hoặc nếu API là /api/orders/status/{statusName}?userId={userId}
        String url = BASE_URL + "api/orders/status/" + currentUserId; // <<<< KIỂM TRA LẠI API ENDPOINT NÀY
        Log.d(TAG, "Fetching orders from URL: " + url + " (will filter for status: " + statusToFilter + ")");

        // TODO: Thêm Authentication Token nếu API yêu cầu
        // String authToken = SharedPreferencesUtils.getString(getContext(), "token", null);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        // if (authToken != null && !authToken.isEmpty()) {
        //     requestBuilder.addHeader("Authorization", "Bearer " + authToken);
        // }
        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Fetch onFailure: ", e);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        if (tvErrorMessage.getVisibility() == View.GONE && tvNoOrders.getVisibility() == View.GONE) {
                            showError("Lỗi kết nối mạng. Vui lòng thử lại.");
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        final String errorMsg = "Lỗi Server: " + response.code() + " " + response.message();
                        String errorBodyStr = (responseBody != null) ? responseBody.string() : "null";
                        Log.e(TAG, "OkHttp Fetch onResponse Error: " + errorMsg + ", Body: " + errorBodyStr);
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Lỗi " + response.code() + ": Không thể tải dữ liệu.");
                            });
                        }
                        return;
                    }

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

                    final String responseData = responseBody.string();
                    Log.d(TAG, "OkHttp Fetch onResponse Success JSON (first 500 chars): " + responseData.substring(0, Math.min(responseData.length(), 500)) + "...");

                    Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
                    final List<Order> allOrders = gson.fromJson(responseData, listType);

                    if (allOrders == null) {
                        Log.e(TAG, "Parsed order list is null. JSON might be malformed or not an array: " + responseData);
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Dữ liệu trả về không đúng định dạng.");
                            });
                        }
                        return;
                    }

                    List<Order> filteredOrders;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        filteredOrders = allOrders.stream()
                                .filter(order -> order.getStatus() != null &&
                                        order.getStatus().name().equalsIgnoreCase(statusToFilter))
                                .collect(Collectors.toList());
                    } else {
                        filteredOrders = new ArrayList<>();
                        for (Order order : allOrders) {
                            if (order.getStatus() != null &&
                                    order.getStatus().name().equalsIgnoreCase(statusToFilter)) {
                                filteredOrders.add(order);
                            }
                        }
                    }
                    Log.d(TAG, "Total orders fetched: " + allOrders.size() + ", Filtered orders for status '" + statusToFilter + "': " + filteredOrders.size());

                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (filteredOrders.isEmpty()) {
                                showNoOrdersMessage(true);
                                if (orderAdapter != null) orderAdapter.setOrders(new ArrayList<>());
                            } else {
                                if (orderAdapter != null) orderAdapter.setOrders(filteredOrders);
                                showNoOrdersMessage(false);
                            }
                            showError(null);
                        });
                    }

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON Parsing Error: ", e);
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
                } catch (Exception e) {
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

    private void callCancelOrderApi(long orderIdToCancel) {
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached or context is null, cannot call cancel API.");
            return;
        }
        Toast.makeText(requireContext(), "Đang gửi yêu cầu hủy...", Toast.LENGTH_SHORT).show();
        showLoading(true);

        String url = BASE_URL + "api/orders/" + orderIdToCancel + "/cancel";
        Log.d(TAG, "Calling cancel API: " + url);

        // TODO: Thêm Authentication Token nếu API yêu cầu
        RequestBody emptyBody = RequestBody.create(new byte[0], null);
        Request.Builder requestBuilder = new Request.Builder().url(url).put(emptyBody);
        // Thêm header Auth nếu cần
        Request request = requestBuilder.build();

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
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "OkHttp Cancel Order Success. Code: " + response.code());
                                Toast.makeText(requireContext(), "Đã hủy đơn hàng #" + orderIdToCancel, Toast.LENGTH_SHORT).show();
                                if (userId != null && targetStatus != null && !targetStatus.equals("UNKNOWN")) {
                                    fetchOrdersWithOkHttp(userId, targetStatus);
                                } else {
                                    showLoading(false);
                                }
                            } else {
                                String errorMessage = "Hủy đơn thất bại: Mã lỗi " + response.code();
                                String errorBodyString = null;
                                if (responseBody != null) {
                                    try { errorBodyString = responseBody.string();
                                        if (errorBodyString != null && !errorBodyString.isEmpty() && !errorBodyString.startsWith("<")) {
                                            errorMessage = "Hủy thất bại: " + errorBodyString.substring(0, Math.min(errorBodyString.length(), 100));
                                        }
                                    } catch (IOException e) { Log.e(TAG, "Error reading error body for cancel", e); }
                                }
                                Log.e(TAG, "OkHttp Cancel Order Error: " + errorMessage + (errorBodyString != null ? " (Raw Body: " + errorBodyString + ")" : ""));
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                                showLoading(false);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Unexpected error processing cancel response", e);
                            Toast.makeText(requireContext(), "Lỗi không xác định khi hủy đơn.", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    });
                }
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (!isAdded() || progressBar == null) return;
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // Khi loading, ẩn các thông báo khác
        if (isLoading) {
            if (tvErrorMessage != null) tvErrorMessage.setVisibility(View.GONE);
            if (tvNoOrders != null) tvNoOrders.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        if (!isAdded() || tvErrorMessage == null || recyclerViewOrders == null || tvNoOrders == null) return;
        if (message != null && !message.isEmpty()) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            tvNoOrders.setVisibility(View.GONE);
        } else {
            tvErrorMessage.setVisibility(View.GONE);
            // Không tự động hiện RecyclerView ở đây, để fetchOrders hoặc showNoOrdersMessage quyết định
        }
    }

    private void showNoOrdersMessage(boolean show) {
        if (!isAdded() || tvNoOrders == null || recyclerViewOrders == null || tvErrorMessage == null) return;
        tvNoOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerViewOrders.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
        }
        // Nếu không show (tức là có đơn), thì fetchOrders sẽ xử lý việc hiện RecyclerView
    }

    // === IMPLEMENT OrderInteractionListener ===

    @Override
    public void onCancelOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "Cancel button clicked for order ID: " + order.getOrderId());
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + order.getOrderId() + " không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> callCancelOrderApi(order.getOrderId()))
                .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Khi người dùng nhấn "Đánh giá" trên một đơn hàng.
     * Chuyển hướng đến OrderDetailActivity để người dùng chọn sản phẩm cụ thể từ đơn hàng đó để đánh giá.
     */
    @Override
    public void onReviewOrderClicked(Order order) {
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.e(TAG, "Fragment not attached or context/activity is null. Cannot proceed to review.");
            return;
        }

        Log.d(TAG, "Review button clicked for Order ID: " + order.getOrderId());

        Intent intentToOrderDetail = new Intent(requireContext(), OrderDetailActivity.class);
        intentToOrderDetail.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId()); // Truyền Order ID
        intentToOrderDetail.putExtra("ACTION_MODE", ACTION_MODE_SELECT_PRODUCT_FOR_REVIEW); // Truyền Action Mode

        // Truyền userId để OrderDetailActivity có thể sử dụng hoặc truyền tiếp
        if (userId != null && userId > 0) {
            intentToOrderDetail.putExtra(EXTRA_USER_ID_FOR_REVIEW, userId);
            Log.d(TAG, "Passing USER_ID_FOR_REVIEW: " + userId + " to OrderDetailActivity for OrderID: " + order.getOrderId());
        } else {
            Log.e(TAG, "User ID (" + userId + ") is invalid when trying to review order. This should not happen if fragment initialized correctly.");
            Toast.makeText(getContext(), "Lỗi: Không thể xác định người dùng để đánh giá.", Toast.LENGTH_SHORT).show();
            return; // Không tiếp tục nếu userId không hợp lệ
        }

        // OrderHistoryActivity sẽ xử lý việc khởi chạy OrderDetailActivity
        // và sau đó là ProductRatingActivity, đồng thời quản lý việc làm mới.
        if (getActivity() instanceof OrderHistoryActivity) {
            ((OrderHistoryActivity) getActivity()).launchReviewActivity(intentToOrderDetail);
        } else {
            Log.e(TAG, "Parent activity is not OrderHistoryActivity. Launching OrderDetailActivity directly (refresh might not work as expected).");
            startActivity(intentToOrderDetail);
        }
    }


    @Override
    public void onRepurchaseOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "Repurchase button clicked for order ID: " + order.getOrderId());
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            Toast.makeText(requireContext(), "Chức năng Mua lại đang được phát triển.", Toast.LENGTH_SHORT).show();
            // TODO: Implement logic: Lấy các orderLines, tạo yêu cầu thêm vào giỏ hàng
            // Ví dụ:
            // for (OrderLine line : order.getOrderLines()) {
            //     // call api add to cart for line.getProductId(), line.getQuantity()
            // }
        } else {
            Toast.makeText(requireContext(), "Không có sản phẩm trong đơn hàng này để mua lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetailsClicked(Order order) {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "View Details button clicked for order ID: " + order.getOrderId());
        Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId());
        // Không cần truyền ACTION_MODE cho việc xem chi tiết thông thường
        startActivity(intent);
    }

    // Hàm này có thể được gọi từ OrderHistoryActivity nếu cần refresh fragment cụ thể
    public void refreshOrders() {
        Log.d(TAG, "refreshOrders called for status: " + targetStatus);
        if (userId != null && userId > 0 && targetStatus != null && !targetStatus.equals("UNKNOWN") && isAdded()) {
            fetchOrdersWithOkHttp(userId, targetStatus);
        }
    }
}