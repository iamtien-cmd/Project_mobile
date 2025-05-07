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
import vn.iostar.doan.activity.ProductRatingActivity; // Mặc dù không dùng trực tiếp nhưng để đó nếu cần
import vn.iostar.doan.adapter.OrderAdapter;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine; // Cần cho logic API
import vn.iostar.doan.model.OrderStatus;
// import vn.iostar.doan.model.Product; // Không dùng trực tiếp ở đây

public class OrderListFragment extends Fragment implements OrderAdapter.OrderInteractionListener {

    private static final String TAG = "OrderListFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_ORDER_STATUS = "order_status";

    public static final String ACTION_SELECT_PRODUCT_FOR_REVIEW = "ACTION_SELECT_PRODUCT_FOR_REVIEW";

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvNoOrders;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Đảm bảo khớp với API
            .create();

    // Đảm bảo URL này đúng và có thể truy cập từ emulator/thiết bị
    private static final String BASE_URL = "http://10.0.2.2:8080/";

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
            // Cân nhắc xử lý lỗi này một cách phù hợp hơn là dùng giá trị mặc định
            // ví dụ: hiển thị thông báo lỗi và không fetch data.
            userId = -1L; // Giá trị không hợp lệ
            targetStatus = "UNKNOWN";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo bạn có file layout fragment_order_list.xml
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
        // SỬA Ở ĐÂY: TRUYỀN THÊM targetStatus VÀO CONSTRUCTOR CỦA OrderAdapter
        orderAdapter = new OrderAdapter(getContext(), this, targetStatus);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void fetchOrdersWithOkHttp(Long currentUserId, final String statusToFilter) {
        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE); // Hiển thị lại RecyclerView khi bắt đầu fetch

        // URL này cần được điều chỉnh cho phù hợp với API của bạn
        // Hiện tại nó lấy TẤT CẢ đơn hàng của user, sau đó lọc ở client.
        // Lý tưởng hơn là API hỗ trợ lọc theo status: /api/orders/user/{userId}/status/{status}
        String url = BASE_URL + "api/orders/status/" + currentUserId; // Hoặc URL đúng của bạn
        Log.d(TAG, "Fetching orders from URL: " + url + " (will filter for status: " + statusToFilter + ")");

        // TODO: Thêm Authentication Token nếu API yêu cầu
        // String authToken = SharedPreferencesUtils.getToken(getContext());
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
                        // Chỉ hiển thị lỗi nếu chưa có lỗi nào khác
                        if (tvErrorMessage.getVisibility() == View.GONE && tvNoOrders.getVisibility() == View.GONE) {
                            showError("Lỗi kết nối mạng. Vui lòng thử lại.");
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // Luôn đảm bảo responseBody được đóng
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        final String errorMsg = "Lỗi Server: " + response.code() + " " + response.message();
                        String errorBodyStr = (responseBody != null) ? responseBody.string() : "null"; // Đọc errorBody một lần
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

                    final String responseData = responseBody.string(); // Đọc body một lần
                    Log.d(TAG, "OkHttp Fetch onResponse Success JSON (first 500 chars): " + responseData.substring(0, Math.min(responseData.length(), 500)) + "...");

                    // Parse JSON
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

                    // Lọc danh sách đơn hàng theo statusToFilter (không phân biệt hoa thường)
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

                    // Cập nhật UI trên Main Thread
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (filteredOrders.isEmpty()) {
                                showNoOrdersMessage(true);
                                if (orderAdapter != null) orderAdapter.setOrders(new ArrayList<>()); // Xóa adapter nếu không có đơn
                            } else {
                                if (orderAdapter != null) orderAdapter.setOrders(filteredOrders);
                                showNoOrdersMessage(false); // Ẩn thông báo "ko có đơn" và hiện recycler
                            }
                            showError(null); // Xóa thông báo lỗi nếu có
                        });
                    }

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON Parsing Error: ", e);
                    // Không log responseData ở đây vì nó có thể rất lớn.
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
                            if (tvErrorMessage.getVisibility() == View.GONE) { // Chỉ hiện lỗi nếu chưa có lỗi khác
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

    private void callCancelOrderApi(long orderIdToCancel) { // Đổi tên tham số để tránh nhầm lẫn
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached or context is null, cannot call cancel API.");
            return;
        }
        Toast.makeText(requireContext(), "Đang gửi yêu cầu hủy...", Toast.LENGTH_SHORT).show();
        showLoading(true);

        // TODO: Thêm Authentication Token nếu API yêu cầu
        String url = BASE_URL + "api/orders/" + orderIdToCancel + "/cancel"; // API endpoint để hủy đơn
        Log.d(TAG, "Calling cancel API: " + url);

        // API hủy đơn thường dùng PUT hoặc POST với body rỗng (hoặc có thể cần body)
        RequestBody emptyBody = RequestBody.create(new byte[0], null); // Body rỗng
        Request.Builder requestBuilder = new Request.Builder().url(url).put(emptyBody); // Giả sử dùng PUT
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
                        try (ResponseBody responseBody = response.body()) { // Luôn đóng body
                            if (response.isSuccessful()) {
                                Log.d(TAG, "OkHttp Cancel Order Success. Code: " + response.code());
                                Toast.makeText(requireContext(), "Đã hủy đơn hàng #" + orderIdToCancel, Toast.LENGTH_SHORT).show();
                                // Tải lại danh sách đơn hàng sau khi hủy thành công
                                if (userId != null && targetStatus != null && !targetStatus.equals("UNKNOWN")) {
                                    fetchOrdersWithOkHttp(userId, targetStatus);
                                } else {
                                    showLoading(false); // Chỉ ẩn loading nếu không thể refresh
                                }
                            } else {
                                String errorMessage = "Hủy đơn thất bại: Mã lỗi " + response.code();
                                String errorBodyString = null;
                                if (responseBody != null) {
                                    try {
                                        errorBodyString = responseBody.string();
                                        // Cố gắng parse lỗi cụ thể hơn từ server nếu có
                                        if (errorBodyString != null && !errorBodyString.isEmpty() && !errorBodyString.startsWith("<")) {
                                            errorMessage = "Hủy thất bại: " + errorBodyString.substring(0, Math.min(errorBodyString.length(), 100));
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, "Error reading error body for cancel", e);
                                    }
                                }
                                Log.e(TAG, "OkHttp Cancel Order Error: " + errorMessage + (errorBodyString != null ? " (Raw Body: " + errorBodyString + ")" : ""));
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                                showLoading(false);
                            }
                        } catch (Exception e) { // Bắt các lỗi không mong muốn khác
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
    }

    private void showError(String message) {
        if (!isAdded() || tvErrorMessage == null || recyclerViewOrders == null || tvNoOrders == null) return;
        if (message != null && !message.isEmpty()) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE); // Ẩn list khi có lỗi
            tvNoOrders.setVisibility(View.GONE);    // Ẩn thông báo "không có đơn"
        } else {
            tvErrorMessage.setVisibility(View.GONE); // Xóa thông báo lỗi
            // Không tự động hiện RecyclerView ở đây, để fetchOrders hoặc showNoOrdersMessage quyết định
        }
    }

    private void showNoOrdersMessage(boolean show) {
        if (!isAdded() || tvNoOrders == null || recyclerViewOrders == null || tvErrorMessage == null) return;
        tvNoOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) { // Nếu hiện "không có đơn"
            recyclerViewOrders.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE); // Đảm bảo text lỗi cũng ẩn
        } else { // Nếu không hiện "không có đơn" (tức là có đơn hoặc đang loading/lỗi khác)
            // Để fetchOrders quyết định việc hiển thị RecyclerView
        }
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
                .setIcon(android.R.drawable.ic_dialog_alert) // Icon cảnh báo mặc định
                .show();
    }

    @Override
    public void onReviewOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;

        Log.d(TAG, "Review button clicked for order ID: " + order.getOrderId() + " (Redirecting to OrderDetailActivity for product selection)");

        Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId());
        intent.putExtra("ACTION_MODE", ACTION_SELECT_PRODUCT_FOR_REVIEW); // Truyền cờ action

        if (userId != null && userId > 0) {
            intent.putExtra("USER_ID_FOR_REVIEW", userId);
            Log.d(TAG, "Passing USER_ID_FOR_REVIEW: " + userId + " to OrderDetailActivity");
        } else {
            Log.e(TAG, "User ID (" + userId + ") is invalid when trying to review order. Cannot proceed.");
            Toast.makeText(getContext(), "Lỗi: Không thể xác định người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
    }

    @Override
    public void onRepurchaseOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "Repurchase button clicked for order ID: " + order.getOrderId());
        // TODO: Implement logic mua lại sản phẩm (thêm vào giỏ hàng, etc.)
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            Toast.makeText(requireContext(), "Chức năng Mua lại đang được phát triển", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Không có sản phẩm trong đơn hàng này để mua lại.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewDetailsClicked(Order order) {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "View Details button clicked for order ID: " + order.getOrderId());
        Intent intent = new Intent(requireContext(), OrderDetailActivity.class);
        // Chỉ truyền orderId, không truyền action mode cho xem chi tiết thông thường
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrderId());
        startActivity(intent);
    }
}