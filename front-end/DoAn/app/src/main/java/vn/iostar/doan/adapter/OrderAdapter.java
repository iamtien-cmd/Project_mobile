package vn.iostar.doan.adapter; // Đảm bảo đúng package

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat; // <<< Import để định dạng ngày
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // Optional: for time zone

import vn.iostar.doan.R;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.OrderLine;
import vn.iostar.doan.model.OrderStatus; // Đảm bảo import Enum đúng
import vn.iostar.doan.model.Product;
import vn.iostar.doan.model.Product2;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private final Context context;
    private final NumberFormat currencyFormatter;
    private final OrderInteractionListener listener;
    private final SimpleDateFormat dateFormatter; // <<< Định dạng ngày

    // --- Interface cho các sự kiện click ---
    // (Bạn nên đặt interface này ở file riêng hoặc trong Fragment/Activity)
    public interface OrderInteractionListener {
        void onCancelOrderClicked(Order order);
        void onReviewOrderClicked(Order order);
        void onRepurchaseOrderClicked(Order order);
        void onViewDetailsClicked(Order order); // <<< Thêm sự kiện xem chi tiết
    }

    // --- Constructor ---
    public OrderAdapter(Context context, OrderInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.orderList = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // <<< Định dạng ngày tháng mong muốn >>>
        this.dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        // Ví dụ: this.dateFormatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    // --- Phương thức cập nhật dữ liệu ---
    @SuppressLint("NotifyDataSetChanged")
    public void setOrders(List<Order> orders) {
        this.orderList.clear();
        if (orders != null) {
            this.orderList.addAll(orders);
        }
        notifyDataSetChanged();
    }

    // --- Override các phương thức của RecyclerView.Adapter ---
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng đúng tên file layout XML của bạn
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        // Truyền cả dateFormatter vào bind
        holder.bind(order, currencyFormatter, dateFormatter, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // ==================== ViewHolder Inner Class ====================
    static class OrderViewHolder extends RecyclerView.ViewHolder {

        // *** KHAI BÁO CÁC BIẾN VIEW ***
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPredictDate, tvTotalPrice, tvPaymentMethod;
        LinearLayout llProductLines;
        // *** KHAI BÁO TẤT CẢ CÁC NÚT ***
        Button btnCancelOrder, btnReviewOrder, btnRepurchaseOrder, btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            // *** ÁNH XẠ CÁC VIEW TỪ LAYOUT BẰNG findViewById ***
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvPredictDate = itemView.findViewById(R.id.tv_predict_date);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            llProductLines = itemView.findViewById(R.id.ll_product_lines);
            // *** ÁNH XẠ TẤT CẢ CÁC NÚT ***
            btnCancelOrder = itemView.findViewById(R.id.btn_cancel_order);
            btnReviewOrder = itemView.findViewById(R.id.btn_review_order);
            btnRepurchaseOrder = itemView.findViewById(R.id.btn_repurchase_order);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details); // <<< ÁNH XẠ NÚT MỚI
        }

        // --- Phương thức gắn dữ liệu (bind) ---
        // Thêm dateFormatter vào tham số
        @SuppressLint("SetTextI18n")
        public void bind(final Order order, NumberFormat currencyFormatter, SimpleDateFormat dateFormatter, final OrderInteractionListener listener) {
            if (order == null) return; // Luôn kiểm tra null

            // --- Gắn dữ liệu cơ bản ---
            tvOrderId.setText("Mã ĐH: #" + order.getOrderId());
            tvTotalPrice.setText("Tổng tiền: " + currencyFormatter.format(order.getTotalPrice()));

            // --- Xử lý trạng thái (Màu và Text) ---
            OrderStatus status = order.getStatus(); // Lấy Enum
            String statusDisplayString = getStatusDisplayString(status); // Dùng hàm helper với Enum
            tvOrderStatus.setText("Trạng thái: " + statusDisplayString);

            int statusColor = getStatusColor(itemView.getContext(), status); // Dùng hàm helper lấy màu
            tvOrderStatus.setTextColor(statusColor);

            // === XỬ LÝ HIỂN THỊ NGÀY THÁNG VÀ PHƯƠNG THỨC THANH TOÁN ===
            // Logic ẩn/hiện ngày và PT thanh toán có thể tùy chỉnh thêm nếu cần
            boolean hideDates = (status == OrderStatus.ERROR) || (status == OrderStatus.RECEIVED && order.isReviewed());

            tvOrderDate.setVisibility(hideDates || order.getOrderDate() == null ? View.GONE : View.VISIBLE);
            if (order.getOrderDate() != null && !hideDates) {
                tvOrderDate.setText("Ngày đặt: " + dateFormatter.format(order.getOrderDate())); // <<< Định dạng ngày
            }

            tvPredictDate.setVisibility(hideDates || order.getPredictReceiveDate() == null ? View.GONE : View.VISIBLE);
            if (order.getPredictReceiveDate() != null && !hideDates) {
                tvPredictDate.setText("Dự kiến: " + dateFormatter.format(order.getPredictReceiveDate())); // <<< Định dạng ngày
            }

            tvPaymentMethod.setVisibility(order.getPaymentMethod() == null ? View.GONE : View.VISIBLE);
            if (order.getPaymentMethod() != null) {
                tvPaymentMethod.setText("Thanh toán: " + order.getPaymentMethod());
            }
            // ==========================================================

            // --- Hiển thị danh sách sản phẩm ---
            llProductLines.removeAllViews(); // Quan trọng: Xóa các view sản phẩm cũ
            if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
                for (OrderLine line : order.getOrderLines()) {
                    Product2 product = line.getProduct();
                    if (product != null) {
                        TextView tvProduct = new TextView(itemView.getContext());
                        // Ví dụ hiển thị: "- Tên sản phẩm (SL: 2)"
                        tvProduct.setText("- " + product.getName() + " (SL: " + line.getQuantity() + ")");
                        tvProduct.setTextSize(14); // Cân nhắc dùng kích thước từ dimens.xml
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 4); // Khoảng cách giữa các dòng sản phẩm
                        tvProduct.setLayoutParams(params);
                        llProductLines.addView(tvProduct);
                    }
                }
            } else {
                // Optional: Hiển thị text "Không có sản phẩm" nếu danh sách rỗng
                // TextView tvNoItems = new TextView(itemView.getContext());
                // tvNoItems.setText("(Không có sản phẩm)");
                // llProductLines.addView(tvNoItems);
            }

            // --- XỬ LÝ HIỂN THỊ VÀ SỰ KIỆN CÁC NÚT BẤM ---
            // Reset tất cả về GONE trước
            btnCancelOrder.setVisibility(View.GONE);
            btnReviewOrder.setVisibility(View.GONE);
            btnRepurchaseOrder.setVisibility(View.GONE);
            btnViewDetails.setVisibility(View.VISIBLE); // Nút Xem chi tiết luôn hiện

            // Gán sự kiện cho nút Xem chi tiết (luôn luôn)
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) listener.onViewDetailsClicked(order);
            });

            // Hiển thị các nút khác dựa trên trạng thái (dùng Enum)
            if (status != null) {
                switch (status) {
                    case WAITING:
                        btnCancelOrder.setVisibility(View.VISIBLE);
                        btnCancelOrder.setOnClickListener(v -> {
                            if (listener != null) listener.onCancelOrderClicked(order);
                        });
                        break;
                    case RECEIVED:
                        if (order.isReviewed()) { // Đã nhận VÀ Đã đánh giá -> Mua lại
                            btnRepurchaseOrder.setVisibility(View.VISIBLE);
                            btnRepurchaseOrder.setOnClickListener(v -> {
                                if (listener != null) listener.onRepurchaseOrderClicked(order);
                            });
                        } else { // Đã nhận nhưng CHƯA đánh giá -> Đánh giá
                            btnReviewOrder.setVisibility(View.VISIBLE);
                            btnReviewOrder.setOnClickListener(v -> {
                                if (listener != null) listener.onReviewOrderClicked(order);
                            });
                        }
                        break;
                    // Có thể thêm nút Mua Lại cho trạng thái REVIEWED nếu muốn
                    // case REVIEWED:
                    //    btnRepurchaseOrder.setVisibility(View.VISIBLE);
                    //    btnRepurchaseOrder.setOnClickListener(v -> {
                    //       if (listener != null) listener.onRepurchaseOrderClicked(order);
                    //    });
                    //    break;
                    // Các trạng thái khác (SHIPPING, ERROR, ...) không hiển thị nút đặc biệt
                    case SHIPPING:
                    case ERROR:
                    default:
                        // Không cần làm gì, các nút đã được reset về GONE
                        break;
                }
            }
        }

        // --- Hàm helper chuyển đổi trạng thái Enum sang chuỗi hiển thị ---
        private String getStatusDisplayString(OrderStatus status) {
            if (status == null) return "N/A";
            switch (status) {
                case WAITING: return "Đang chờ";
                case REVIEWED: return "Đã duyệt"; // Hoặc tên khác tùy logic
                case SHIPPING: return "Đang vận chuyển";
                case RECEIVED: return "Đã nhận hàng";
                case ERROR: return "Đã hủy";
                default: return status.name(); // Trả về tên Enum nếu chưa định nghĩa
            }
        }

        // --- Hàm helper lấy màu dựa trên trạng thái Enum ---
        private int getStatusColor(Context context, OrderStatus status) {
            if (status == null) {
                return ContextCompat.getColor(context, R.color.my_grey_neutral); // Màu mặc định
            }
            switch (status) {
                case RECEIVED: return ContextCompat.getColor(context, R.color.my_green_success);
                case SHIPPING: return ContextCompat.getColor(context, R.color.my_orange_processing);
                case ERROR:    return ContextCompat.getColor(context, R.color.my_red_error);
                case WAITING:
                case REVIEWED: // Có thể dùng màu riêng cho REVIEWED
                default:       return ContextCompat.getColor(context, R.color.my_grey_neutral);
            }
        }
    }
    // ==================== Kết thúc ViewHolder ====================
}