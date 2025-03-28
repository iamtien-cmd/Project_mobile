package vn.iostar.doan.image;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import vn.iostar.doan.R;

public class ImagesViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;
    public ImagesViewHolder(@NonNull View itemView){
        super(itemView);
        imageView = itemView.findViewById(R.id.imgView);
    }
}
