package vn.iostar.doan.fragment;

import android.app.AlertDialog;
import android.content.Intent; // *** Đảm bảo đã import Intent ***
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import vn.iostar.doan.R;
import vn.iostar.doan.activity.ProductRatingActivity; // <<< Đảm bảo Import đúng Activity
import vn.iostar.doan.adapter.OrderAdapter;
import vn.iostar.doan.adapter.OrderInteractionListener;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine; // <<< Import OrderLine
import vn.iostar.doan.model.OrderStatus;
import vn.iostar.doan.model.Product; // <<< Import Product


public class OrderListFragment extends Fragment implements OrderInteractionListener {

    // ... (Các biến và phương thức khác giữ nguyên: TAG, ARG_USER_ID, ..., client, gson, BASE_URL, ...)
    private static final String TAG = "OrderListFragment";
    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_ORDER_STATUS = "order_status";

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter; // Adapter cho RecyclerView
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private TextView tvNoOrders;

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss") // Hoặc định dạng Date phù hợp
            .create();

    private static final String BASE_URL = "http://192.168.1.7:8080/"; // Đảm bảo chính xác

    private Long userId;
    private String targetStatus;


    // --- Factory Method (Giữ nguyên) ---
    public static OrderListFragment newInstance(Long userId, String status) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        args.putString(ARG_ORDER_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    // --- Lifecycle Methods (Giữ nguyên) ---
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
            targetStatus = getArguments().getString(ARG_ORDER_STATUS);
        } else {
            Log.e(TAG, "Fragment created without required arguments!");
            userId = 1L; // Giá trị mặc định
            targetStatus = null;
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

        // Ánh xạ Views
        recyclerViewOrders = view.findViewById(R.id.recycler_view_orders_fragment);
        progressBar = view.findViewById(R.id.progress_bar_fragment);
        tvErrorMessage = view.findViewById(R.id.tv_error_message_fragment);
        tvNoOrders = view.findViewById(R.id.tv_no_orders_fragment);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Gọi API
        if (userId != null && userId > 0 && targetStatus != null) {
            fetchOrdersWithOkHttp(userId, targetStatus);
        } else {
            if (targetStatus == null) {
                showError("Không xác định được trạng thái.");
            } else {
                showError("Thiếu User ID.");
            }
        }
    }

    // --- Helper Methods ---
    private void setupRecyclerView() {
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        // Truyền 'this' (Fragment) làm listener cho adapter
        orderAdapter = new OrderAdapter(getContext(), this);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    // --- Fetch Orders using OkHttp (Giữ nguyên) ---
    private void fetchOrdersWithOkHttp(Long userId, final String statusToFilter) {
        // ... (Code fetch như cũ) ...
        showLoading(true);
        tvErrorMessage.setVisibility(View.GONE);
        tvNoOrders.setVisibility(View.GONE);
        recyclerViewOrders.setVisibility(View.VISIBLE);

        String url = BASE_URL + "api/orders/status/" + userId;
        // *** NHỚ THÊM TOKEN VÀO ĐÂY NẾU API YÊU CẦU ***
        // String authToken = getAuthToken();
        // if (authToken == null) { /* xử lý lỗi thiếu token */ return; }
        Request request = new Request.Builder()
                .url(url)
                // .addHeader("Authorization", "Bearer " + authToken) // Thêm dòng này nếu cần
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Fetch onFailure: ", e);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        if (tvErrorMessage.getVisibility() == View.GONE) {
                            showError("Lỗi kết nối mạng. Vui lòng thử lại.");
                        }
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                String responseData = null;
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        final String errorMsg = "Lỗi Server: " + response.code() + " " + response.message();
                        String errorBodyStr = (responseBody != null) ? responseBody.string() : "null";
                        Log.e(TAG, "OkHttp Fetch onResponse Error: " + errorMsg + ", Body: " + errorBodyStr);
                        // Kiểm tra lỗi 403 cụ thể


                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError(errorMsg);
                            });
                        }
                        return;
                    }

                    if (responseBody != null) {
                        responseData = responseBody.string();
                        Log.d(TAG, "OkHttp Fetch onResponse Success JSON: " + responseData);
                    } else {
                        Log.e(TAG, "OkHttp Fetch onResponse Error: Response body is null");
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Không nhận được dữ liệu từ server.");
                            });
                        }
                        return;
                    }

                    Type listType = new TypeToken<ArrayList<Order>>() {}.getType();
                    final List<Order> allOrders = gson.fromJson(responseData, listType);

                    if (allOrders == null) {
                        Log.e(TAG, "Parsed order list is null.");
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showError("Dữ liệu trả về không đúng định dạng.");
                            });
                        }
                        return;
                    }

                    // Filter list
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

                    // Update UI
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            if (filteredOrders.isEmpty()) {
                                showNoOrdersMessage(true);
                            } else {
                                orderAdapter.setOrders(filteredOrders);
                                showNoOrdersMessage(false);
                            }
                            showError(null);
                        });
                    }

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "JSON Parsing Error: ", e);
                    Log.e(TAG, "Failed to parse JSON: " + responseData);
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
                    Log.e(TAG, "Unexpected error in onResponse: ", e);
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


    // --- Call Cancel Order API using OkHttp (Giữ nguyên) ---
    private void callCancelOrderApi(long orderId) {
        // ... (Code gọi API hủy đơn như cũ, nhớ thêm token nếu cần) ...
        if (!isAdded() || getContext() == null) {
            Log.w(TAG, "Fragment not attached, cannot call cancel API.");
            return;
        }

        Toast.makeText(requireContext(), "Đang gửi yêu cầu hủy...", Toast.LENGTH_SHORT).show();

        // *** NHỚ THÊM TOKEN VÀO ĐÂY NẾU API YÊU CẦU ***
        // String authToken = getAuthToken();
        // if (authToken == null) { /* xử lý lỗi thiếu token */ return; }

        String url = BASE_URL + "api/orders/" + orderId + "/cancel";
        RequestBody emptyBody = RequestBody.create(new byte[0], null);
        Request request = new Request.Builder()
                .url(url)
                .put(emptyBody) // Hoặc .post(emptyBody)
                // .addHeader("Authorization", "Bearer " + authToken) // Thêm dòng này nếu cần
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "OkHttp Cancel Order onFailure: ", e);
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Hủy đơn thất bại: Lỗi mạng", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        try (ResponseBody responseBody = response.body()) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "OkHttp Cancel Order Success. Code: " + response.code());
                                Toast.makeText(requireContext(), "Đã hủy đơn hàng #" + orderId, Toast.LENGTH_SHORT).show();
                                if (userId != null && targetStatus != null) {
                                    fetchOrdersWithOkHttp(userId, targetStatus); // Refresh list
                                } else {
                                    showLoading(false);
                                }

                            } else {
                                String errorMessage = "Hủy đơn thất bại: Mã lỗi " + response.code();
                                String errorBodyString = null;
                                if (responseBody != null) {
                                    try { // Thêm try-catch nhỏ ở đây khi đọc body lỗi
                                        errorBodyString = responseBody.string();
                                        if (!errorBodyString.isEmpty()) {
                                            errorMessage = "Hủy đơn thất bại: " + errorBodyString;
                                        }
                                    } catch (IOException e) {
                                        Log.e(TAG, "Error reading error body for cancel", e);
                                    }
                                }
                                Log.e(TAG, "OkHttp Cancel Order Error: " + errorMessage + (errorBodyString != null ? " (Raw Body: " + errorBodyString + ")" : ""));
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                                showLoading(false);
                            }
                        } catch (Exception e) { // Bắt lỗi chung khi xử lý response
                            Log.e(TAG, "Unexpected error processing cancel response", e);
                            Toast.makeText(requireContext(), "Lỗi không xác định khi hủy đơn.", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    });
                }
            }
        });
    }


    // --- UI Helper Methods (Giữ nguyên) ---
    private void showLoading(boolean isLoading) {
        // ... (Code như cũ) ...
        if (!isAdded() || progressBar == null) return;
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        // ... (Code như cũ) ...
        if (!isAdded() || tvErrorMessage == null || recyclerViewOrders == null || tvNoOrders == null) return;
        if (message != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
            recyclerViewOrders.setVisibility(View.GONE);
            tvNoOrders.setVisibility(View.GONE);
        } else {
            tvErrorMessage.setVisibility(View.GONE);
        }
    }

    private void showNoOrdersMessage(boolean show) {
        // ... (Code như cũ) ...
        if (!isAdded() || tvNoOrders == null || recyclerViewOrders == null || tvErrorMessage == null) return;
        tvNoOrders.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerViewOrders.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.GONE);
        } else {
            recyclerViewOrders.setVisibility(View.VISIBLE);
            tvErrorMessage.setVisibility(View.GONE);
        }
    }
    // --- End UI Helper Methods ---


    // === IMPLEMENT OrderInteractionListener METHODS ===
    @Override
    public void onCancelOrderClicked(Order order) {
        // ... (Code AlertDialog và gọi callCancelOrderApi như cũ) ...
        if (!isAdded()) return;

        Log.d(TAG, "Cancel clicked for order: " + order.getOrderId());
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc muốn hủy đơn hàng #" + order.getOrderId() + "?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    callCancelOrderApi(order.getOrderId());
                })
                .setNegativeButton("Không", null)
                .show();
    }

    // *** BỎ COMMENT PHẦN NÀY ĐỂ KÍCH HOẠT LOGIC ***
    @Override
    public void onReviewOrderClicked(Order order) {
        if (!isAdded() || getContext() == null) return; // Check fragment state

        Log.d(TAG, "Review clicked for order: " + order.getOrderId());

        long productIdToReview = -1L; // Default invalid ID (-1 hoặc 0 tùy bạn)

        // Safely get the first product ID from the order lines
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            OrderLine firstOrderLine = order.getOrderLines().get(0); // Get the first item
            if (firstOrderLine != null) {
                Product product = firstOrderLine.getProduct();
                if (product != null) {
                    productIdToReview = product.getProductId(); // Get the actual product ID
                } else {
                    Log.w(TAG, "Product object in the first order line is null for order: " + order.getOrderId());
                }
            } else {
                Log.w(TAG, "First order line is null for order: " + order.getOrderId());
            }
        } else {
            Log.w(TAG, "Order lines list is null or empty for order: " + order.getOrderId());
        }

        // Proceed only if a valid product ID was found
        if (productIdToReview > 0) { // Check if ID is valid (assuming IDs are positive > 0)
            Log.i(TAG, "Navigating to ProductRatingActivity with PRODUCT_ID: " + productIdToReview);

            // Create Intent for ProductRatingActivity
            Intent intent = new Intent(requireContext(), ProductRatingActivity.class);
            // Pass the PRODUCT_ID as an extra
            intent.putExtra("PRODUCT_ID", productIdToReview);
            // Optional: You might still want to pass orderId if the Rating Activity needs it
            // intent.putExtra("ORIGINAL_ORDER_ID", order.getOrderId());

            startActivity(intent); // Start the activity

        } else {
            // Handle the case where no valid product was found
            Log.e(TAG, "Could not find a valid product ID to review in order: " + order.getOrderId());
            Toast.makeText(requireContext(), "Không tìm thấy thông tin sản phẩm để đánh giá.", Toast.LENGTH_SHORT).show();
        }
    }
    // *** KẾT THÚC BỎ COMMENT ***

    @Override
    public void onRepurchaseOrderClicked(Order order) {
        // ... (Code mua lại như cũ) ...
        if (!isAdded()) return;
        Log.d(TAG, "Repurchase clicked for order: " + order.getOrderId());
        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            Toast.makeText(requireContext(), "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Không có thông tin sản phẩm để mua lại.", Toast.LENGTH_SHORT).show();
        }
    }
    // =============================================================
}