package vn.iostar.doan.adapter;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.iostar.doan.R;
import vn.iostar.doan.model.Order;
import vn.iostar.doan.model.Product;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        if (order.getOrderLines() != null && !order.getOrderLines().isEmpty()) {
            Product product = order.getOrderLines().get(0).getProduct();
            holder.tvProductName.setText(product.getName());
            holder.tvQuantity.setText("x" + order.getOrderLines().get(0).getQuantity());
        }

        holder.tvTotal.setText("₹" + order.getTotalPrice());
        holder.tvAddress.setText("Giao đến: " + order.getUser().getFullName());
        holder.tvTime.setText("9:00 AM – 1:00 PM"); // Cứng theo giao diện demo

        holder.btnRepurchase.setOnClickListener(v -> {
            Toast.makeText(context, "Repurchase clicked!", Toast.LENGTH_SHORT).show();
            // Có thể mở activity chi tiết sản phẩm ở đây
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvQuantity, tvTotal, tvAddress, tvTime;
        Button btnRepurchase;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvTime = itemView.findViewById(R.id.tv_time);
            btnRepurchase = itemView.findViewById(R.id.btn_repurchase);
        }

    }


}
