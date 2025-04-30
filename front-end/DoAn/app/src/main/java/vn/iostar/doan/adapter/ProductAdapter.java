package vn.iostar.doan.adapter;

import android.annotation.SuppressLint; // Import nếu cần
import android.content.Context; // <<< THÊM IMPORT CONTEXT
import android.content.res.Resources; // Có thể bỏ import này nếu không dùng try-catch nữa
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <<< THÊM IMPORT ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// *** THÊM IMPORT CHO GLIDE ***
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Tùy chọn caching
import com.bumptech.glide.request.RequestOptions; // Tùy chọn placeholder/error

import java.text.NumberFormat; // <<< THÊM IMPORT NumberFormat
import java.util.ArrayList; // <<< THÊM IMPORT ArrayList
import java.util.List;
import java.util.Locale; // <<< THÊM IMPORT Locale

import vn.iostar.doan.R;
import vn.iostar.doan.model.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> productList;
    private Context context; // <<< Thêm biến Context
    private NumberFormat currencyFormatter; // <<< Thêm biến định dạng tiền tệ
    // Thêm listener nếu bạn cần xử lý click
    // private OnProductClickListener listener;
    // public interface OnProductClickListener { void onProductClick(Product product); }

    // --- SỬA CONSTRUCTOR ---
    // public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) { // Nếu có listener
    public ProductAdapter(Context context, List<Product> productList) { // Constructor chỉ cần Context và List
        this.context = context; // <<< Lưu Context
        // Khởi tạo list rỗng nếu đầu vào là null để tránh lỗi
        this.productList = (productList != null) ? productList : new ArrayList<>();
        // Khởi tạo định dạng tiền tệ (ví dụ: VNĐ)
        this.currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        // this.listener = listener; // Nếu có listener
    }
    // ---------------------

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng context đã lưu để lấy LayoutInflater
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false); // <<< Đảm bảo layout là item_product.xml
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        // Gọi hàm bind trong ViewHolder để gán dữ liệu
        holder.bind(product, currencyFormatter /*, listener*/); // Truyền formatter, listener nếu có
    }

    @Override
    public int getItemCount() {
        // Không cần kiểm tra null ở đây nếu đã khởi tạo trong constructor
        return productList.size();
    }

    // --- ViewHolder ---
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Đảm bảo khai báo đúng các View bạn có trong item_product.xml
        ImageView imageView;
        TextView tvProductName, tvProductPrice;
        // Bỏ các TextView khác nếu không dùng hoặc chưa ánh xạ
        // TextView tvProductDescription, tvProductCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ View từ layout item_product.xml
            imageView = itemView.findViewById(R.id.imageView); // <<< Đảm bảo ID này đúng
            tvProductName = itemView.findViewById(R.id.tvProductName); // <<< Đảm bảo ID này đúng
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice); // <<< Đảm bảo ID này đúng

            // Kiểm tra null (tùy chọn, nhưng tốt)
            if (imageView == null || tvProductName == null || tvProductPrice == null) {
                Log.e("ProductAdapter", "ViewHolder: Một hoặc nhiều View không được tìm thấy trong layout item_product.xml!");
            }
        }

        // --- Phương thức Bind Data (Quan trọng) ---
        // public void bind(final Product product, NumberFormat formatter, final OnProductClickListener listener) { // Nếu có listener
        public void bind(final Product product, NumberFormat formatter) {
            if (product == null) {
                // Xử lý trường hợp product null nếu cần
                return;
            }

            // Gắn dữ liệu text
            tvProductName.setText(product.getName());
            // Định dạng giá tiền và gắn vào TextView
            tvProductPrice.setText(formatter.format(product.getPrice()));

            // === SỬ DỤNG GLIDE ĐỂ TẢI ẢNH ===
            String imageUrlOrPath = product.getImage(); // Lấy đường dẫn/tên file ảnh dạng String

            // Tạo tùy chọn cho ảnh placeholder và lỗi
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_placeholder_image) // <<< THAY BẰNG drawable placeholder của bạn
                    .error(R.drawable.ic_error_image);          // <<< THAY BẰNG drawable lỗi của bạn

            if (imageUrlOrPath != null && !imageUrlOrPath.isEmpty()) {
                String finalImageUrl;
                // Kiểm tra xem có phải URL đầy đủ không
                if (imageUrlOrPath.startsWith("http://") || imageUrlOrPath.startsWith("https://")) {
                    finalImageUrl = imageUrlOrPath; // Là URL đầy đủ
                } else {
                    // Nếu chỉ là tên file, ghép với Base URL ảnh
                    // *** THAY ĐỔI URL NÀY CHO ĐÚNG VỚI BACKEND CỦA BẠN ***
                    String BASE_IMAGE_URL = "http://192.168.1.7:8080/api/v1/images/"; // <<< Ví dụ URL API ảnh
                    finalImageUrl = BASE_IMAGE_URL + imageUrlOrPath;
                }

                // Dùng Glide để tải ảnh
                Glide.with(itemView.getContext()) // Lấy context từ itemView là tốt nhất
                        .load(finalImageUrl)      // Tải từ URL đã xử lý
                        .apply(requestOptions)    // Áp dụng placeholder/error
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Bật cache (tùy chọn)
                        .into(imageView);         // Hiển thị vào ImageView

            } else {
                // Không có đường dẫn ảnh -> hiển thị ảnh lỗi/placeholder
                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_error_image) // Hoặc R.drawable.ic_placeholder_image
                        .into(imageView);
            }
            // ==================================

            // Thêm sự kiện click nếu cần
            // itemView.setOnClickListener(v -> {
            //     if (listener != null) {
            //         listener.onProductClick(product);
            //     }
            // });
        }
        // ------------------------------------
    }

    // (Tùy chọn) Phương thức để cập nhật dữ liệu cho adapter
    @SuppressLint("NotifyDataSetChanged")
    public void updateProducts(List<Product> newProductList) {
        this.productList.clear();
        if (newProductList != null) {
            this.productList.addAll(newProductList);
        }
        notifyDataSetChanged(); // Thông báo thay đổi dữ liệu
    }
}