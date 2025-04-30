package vn.iostar.doan.adapter; // Đảm bảo đúng package

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import Button
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import ContextCompat để lấy màu
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat; // Để định dạng tiền tệ
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Để định dạng tiền tệ theo khu vực

import vn.iostar.doan.R; // Import R của project bạn
import vn.iostar.doan.model.Order; // Import model Order của bạn
import vn.iostar.doan.model.OrderLine; // Import OrderLine
import vn.iostar.doan.model.OrderStatus; // Import Enum OrderStatus
import vn.iostar.doan.model.Product; // Import Product

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private Context context;
    private NumberFormat currencyFormatter;
    private OrderInteractionListener listener; // Listener để xử lý click nút

    // --- Constructor ---
    public OrderAdapter(Context context, OrderInteractionListener listener) {
        this.context = context;
        this.listener = listener;
        this.orderList = new ArrayList<>();
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    // --- Phương thức cập nhật dữ liệu ---
    @SuppressLint("NotifyDataSetChanged")
    public void setOrders(List<Order> orders) {
        this.orderList.clear();
        if (orders != null) {
            this.orderList.addAll(orders);
        }
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    // --- Override các phương thức của RecyclerView.Adapter ---
    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo View cho mỗi item từ layout XML
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_order, parent, false);
        return new OrderViewHolder(view); // Trả về ViewHolder mới
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        // Lấy dữ liệu tại vị trí hiện tại
        Order order = orderList.get(position);
        // Gắn dữ liệu lên ViewHolder và truyền listener
        holder.bind(order, currencyFormatter, listener);
    }

    @Override
    public int getItemCount() {
        // Trả về tổng số lượng item
        return orderList.size();
    }

    // ==================== ViewHolder Inner Class ====================
    // Giữ tham chiếu đến các View của một item và gắn dữ liệu vào chúng
    static class OrderViewHolder extends RecyclerView.ViewHolder {

        // *** KHAI BÁO CÁC BIẾN VIEW ***
        TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPredictDate, tvTotalPrice, tvPaymentMethod;
        LinearLayout llProductLines;
        Button btnCancelOrder, btnReviewOrder, btnRepurchaseOrder;

        // --- Constructor của ViewHolder ---
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView); // Gọi constructor của lớp cha

            // *** ÁNH XẠ CÁC VIEW TỪ LAYOUT BẰNG findViewById ***
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvPredictDate = itemView.findViewById(R.id.tv_predict_date);
            tvTotalPrice = itemView.findViewById(R.id.tv_total_price);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            llProductLines = itemView.findViewById(R.id.ll_product_lines);
            btnCancelOrder = itemView.findViewById(R.id.btn_cancel_order);
            btnReviewOrder = itemView.findViewById(R.id.btn_review_order);
            btnRepurchaseOrder = itemView.findViewById(R.id.btn_repurchase_order);
        }

        // --- Phương thức gắn dữ liệu (bind) ---
        public void bind(final Order order, NumberFormat formatter, final OrderInteractionListener listener) {
            // Kiểm tra null trước khi truy cập đối tượng order
            if (order == null) {
                return; // Không làm gì nếu order null
            }

            // --- Gắn dữ liệu cơ bản ---
            tvOrderId.setText("Mã ĐH: #" + order.getOrderId());
            tvTotalPrice.setText("Tổng tiền: " + formatter.format(order.getTotalPrice()));

            // --- Xử lý trạng thái (Màu và Text) ---
            String statusString = (order.getStatus() != null) ? order.getStatus().name() : null;
            tvOrderStatus.setText("Trạng thái: " + getStatusDisplayString(statusString)); // Sử dụng hàm helper đã cập nhật
            // Đặt màu cho trạng thái
            int statusColor;
            switch (statusString != null ? statusString.toUpperCase() : "") {
                case "RECEIVED": statusColor = ContextCompat.getColor(itemView.getContext(), R.color.my_green_success); break;
                case "SHIPPING": statusColor = ContextCompat.getColor(itemView.getContext(), R.color.my_orange_processing); break;
                case "ERROR": // ERROR bây giờ có nghĩa là Đã hủy
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.my_red_error); // Dùng màu đỏ
                    break;
                case "WAITING":
                case "REVIEWED": // Trạng thái Reviewed cũng dùng màu mặc định
                default:
                    statusColor = ContextCompat.getColor(itemView.getContext(), R.color.my_grey_neutral); // Màu xám mặc định
                    break;
            }
            tvOrderStatus.setTextColor(statusColor); // Áp dụng màu

            // === XỬ LÝ HIỂN THỊ NGÀY THÁNG VÀ PHƯƠNG THỨC THANH TOÁN ===
            boolean hideDates = false; // Cờ để quyết định ẩn ngày
            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case RECEIVED:
                        if (order.isReviewed()) { // Chỉ ẩn khi RECEIVED và ĐÃ REVIEW
                            hideDates = true;
                        }
                        break;
                    case ERROR: // Luôn ẩn khi ERROR (Đã hủy)
                        hideDates = true;
                        break;
                    // Các trạng thái khác hiển thị ngày
                }
            }

            // Ẩn/hiện và đặt text cho Ngày đặt
            if (hideDates) {
                tvOrderDate.setVisibility(View.GONE);
            } else {
                tvOrderDate.setVisibility(View.VISIBLE);
                // Cẩn thận với toString() của Date, có thể cần SimpleDateFormat nếu muốn định dạng cụ thể
                tvOrderDate.setText("Ngày đặt: " + (order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A"));
            }

            // Ẩn/hiện và đặt text cho Ngày dự kiến
            if (hideDates || order.getPredictReceiveDate() == null) {
                tvPredictDate.setVisibility(View.GONE);
            } else {
                tvPredictDate.setVisibility(View.VISIBLE);
                tvPredictDate.setText("Dự kiến nhận: " + order.getPredictReceiveDate().toString());
            }

            // Ẩn/hiện và đặt text cho Phương thức thanh toán
            if (order.getPaymentMethod() == null) {
                tvPaymentMethod.setVisibility(View.GONE);
            } else {
                tvPaymentMethod.setVisibility(View.VISIBLE);
                tvPaymentMethod.setText("Thanh toán: " + order.getPaymentMethod());
            }
            // ==========================================================

            // --- Hiển thị danh sách sản phẩm ---
            llProductLines.removeAllViews(); // Xóa view cũ
            if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
                for (OrderLine line : order.getOrderLines()) {
                    Product product = line.getProduct();
                    if (product != null) {
                        TextView tvProduct = new TextView(itemView.getContext());
                        tvProduct.setText("- " + product.getName() + " (SL: " + line.getQuantity() + ")");
                        tvProduct.setTextSize(14);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 0, 0, 4);
                        tvProduct.setLayoutParams(params);
                        llProductLines.addView(tvProduct);
                    }
                }
            } else {
                // Tùy chọn: Hiển thị thông báo nếu không có sản phẩm
                TextView tvNoProduct = new TextView(itemView.getContext());
                tvNoProduct.setText("(Không có thông tin sản phẩm)");
                tvNoProduct.setTextSize(14);
                tvNoProduct.setVisibility(View.GONE); // Nên ẩn đi
                llProductLines.addView(tvNoProduct);
            }

            // --- Xử lý hiển thị nút bấm và đặt Listener ---
            // Reset visibility
            btnCancelOrder.setVisibility(View.GONE);
            btnReviewOrder.setVisibility(View.GONE);
            btnRepurchaseOrder.setVisibility(View.GONE);

            // Hiển thị nút dựa trên trạng thái
            if (order.getStatus() != null) {
                switch (order.getStatus()) {
                    case WAITING:
                        btnCancelOrder.setVisibility(View.VISIBLE);
                        btnCancelOrder.setOnClickListener(v -> {
                            if (listener != null) listener.onCancelOrderClicked(order);
                        });
                        break;
                    case RECEIVED:
                        if (order.isReviewed()) { // Đã đánh giá -> Mua lại
                            btnRepurchaseOrder.setVisibility(View.VISIBLE);
                            btnRepurchaseOrder.setOnClickListener(v -> {
                                if (listener != null) listener.onRepurchaseOrderClicked(order);
                            });
                        } else { // Chưa đánh giá -> Đánh giá
                            btnReviewOrder.setVisibility(View.VISIBLE);
                            btnReviewOrder.setOnClickListener(v -> {
                                if (listener != null) listener.onReviewOrderClicked(order);
                            });
                        }
                        break;
                    // Các trạng thái SHIPPING, REVIEWED, ERROR không hiển thị nút
                    case SHIPPING:
                    case REVIEWED:
                    case ERROR: // Đã hủy (ERROR) cũng không hiển thị nút
                    default:
                        break; // Không làm gì, các nút đã GONE
                }
            }
        }

        // --- Hàm helper chuyển đổi trạng thái ---
        private String getStatusDisplayString(String status) {
            if (status == null) return "N/A";
            switch (status.toUpperCase()) {
                case "WAITING": return "Đang chờ";
                case "REVIEWED": return "Đã duyệt";
                case "SHIPPING": return "Đang vận chuyển";
                case "RECEIVED": return "Đã nhận hàng";
                // *** CẬP NHẬT TEXT CHO ERROR ***
                case "ERROR": return "Đã hủy"; // Hoặc ý nghĩa tương đương bạn muốn
                default: return status; // Trả về trạng thái gốc nếu không khớp
            }
        }
    }
    // ==================== Kết thúc ViewHolder ====================
}