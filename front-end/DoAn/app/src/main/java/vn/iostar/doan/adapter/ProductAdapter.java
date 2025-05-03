package vn.iostar.doan.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.iostar.doan.R;
import vn.iostar.doan.activity.ProductDetailActivity;
import vn.iostar.doan.model.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private String token;
    private Context context;
    private List<Product> productList;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }
    public ProductAdapter(Context context, List<Product> productList, String token) {
        this.context = context;
        this.productList = productList;
        this.token = token; // <<< Lưu token
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        if (product == null) return;
        try {
            Glide.with(context)
                    .load(product.getImage())  // getImage() trả về URL
                    .placeholder(R.drawable.ic_launcher_foreground)  // ảnh mặc định khi loading
                    .error(R.drawable.edit)           // ảnh báo lỗi nếu tải thất bại
                    .into(holder.imageView);
        } catch (Resources.NotFoundException e) {
            Log.e("ProductAdapter", "Resource not found: " + product.getImage());
        }
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(String.format("%,.0f VNĐ", product.getPrice()));

    }
    public void updateList(List<Product> newList) {
        productList = newList;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        if (productList != null){
            return productList.size();
        }
        return 0;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvProductName, tvProductPrice, tvProductDescription, tvProductCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Product clickedProduct = productList.get(position);

                        Intent intent = new Intent(context, ProductDetailActivity.class);
                        intent.putExtra("productId", clickedProduct.getProductId());  // truyền id sản phẩm
                        intent.putExtra("token", token);
                        context.startActivity(intent);
                    }
                }
            });

            if (tvProductName == null || tvProductPrice == null) {
                Log.e("ProductAdapter", "ViewHolder: TextView không được tìm thấy trong layout!");
            }
        }
    }

}
