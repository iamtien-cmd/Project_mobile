package vn.iostar.doan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.iostar.doan.R;

public class ImagesViewPager2Adapter extends RecyclerView.Adapter<ImagesViewPager2Adapter.ImagesViewHolder> {

    private List<String> imageUrls; // Sử dụng List<String> cho URL

    public ImagesViewPager2Adapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }



    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        // Dùng Glide để load ảnh từ URL
        Glide.with(holder.imageView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ
                .error(R.drawable.edit) // Ảnh lỗi
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return (imageUrls != null) ? imageUrls.size() : 0;
    }

    // Bên trong file ImagesViewPager2Adapter.java
    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item và tạo ViewHolder Ở ĐÂY (trong Adapter)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_slider, parent, false);
        return new ImagesViewHolder(view); // Gọi constructor của ViewHolder
    }

    // ViewHolder nên được định nghĩa là inner class hoặc static inner class trong Adapter
    public static class ImagesViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView; // Đã sửa thành public

        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewPager);
            if (imageView == null) {
                android.util.Log.e("ImagesViewHolderInner", "ImageView R.id.imageViewPager not found!");
            }
        }
    }
}