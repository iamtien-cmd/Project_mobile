package vn.iostar.doan.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

// import java.time.LocalDateTime; // Xóa hoặc comment
// import java.time.format.DateTimeFormatter; // Xóa hoặc comment
import java.text.SimpleDateFormat; // Thêm
import java.util.ArrayList;
import java.util.Date; // Thêm
import java.util.List;
import java.util.Locale; // Thêm

import vn.iostar.doan.R;
import vn.iostar.doan.model.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;
    private final Context context; // Thêm final
    // private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"); // Thay thế
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()); // Bằng dòng này


    public CommentAdapter(Context context) {
        this.context = context; // Gán trong constructor
        this.commentList = new ArrayList<>();
    }

    public void setComments(List<Comment> comments) {
        this.commentList = (comments == null) ? new ArrayList<>() : comments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvCustomerName;
        TextView tvCommentDate;
        LinearLayout llRatingStars;
        ImageView ivCommentImage;
        TextView tvCommentContent;
        ImageView[] stars = new ImageView[5];

        ConstraintLayout.LayoutParams contentLayoutParams;


        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCommentDate = itemView.findViewById(R.id.tvCommentDate);
            llRatingStars = itemView.findViewById(R.id.llRatingStars);
            ivCommentImage = itemView.findViewById(R.id.ivCommentImage);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);

            stars[0] = itemView.findViewById(R.id.star1);
            stars[1] = itemView.findViewById(R.id.star2);
            stars[2] = itemView.findViewById(R.id.star3);
            stars[3] = itemView.findViewById(R.id.star4);
            stars[4] = itemView.findViewById(R.id.star5);

            contentLayoutParams = (ConstraintLayout.LayoutParams) tvCommentContent.getLayoutParams();
        }

        void bind(Comment comment) {
            tvCustomerName.setText(comment.getFullname());
            tvCommentContent.setText(comment.getContent());

            // Format và hiển thị ngày giờ bằng SimpleDateFormat
            Date createdAt = comment.getCreatedAt();
            if (createdAt != null) {
                // Chỗ này gọi format của SimpleDateFormat
                tvCommentDate.setText(DATE_FORMAT.format(createdAt));
            } else {
                tvCommentDate.setText("");
            }

            // Hiển thị Rating Stars
            int rating = comment.getRating();
            for (int i = 0; i < 5; i++) {
                // Sử dụng R.drawable.ic_star_filled (thay vì ic_star_border khi đầy)
                if (i < rating) {
                    stars[i].setImageResource(R.drawable.ic_star_filled);
                } else {
                    stars[i].setImageResource(R.drawable.ic_star_border); // Chỗ này dùng đúng tên file
                }
            }

            // Tải Avatar (Sử dụng ảnh placeholder hoặc URL nếu có)
            String avatarUrl = comment.getAvatar(); // Lấy URL avatar từ model (nếu có)
            Glide.with(context)
                    .load(TextUtils.isEmpty(avatarUrl) ? R.drawable.ic_person_placeholder : avatarUrl) // Ưu tiên URL thật, nếu không có thì dùng placeholder
                    .placeholder(R.drawable.ic_person_placeholder) // Ảnh chờ tải
                    .error(R.drawable.ic_person_placeholder) // Ảnh khi lỗi
                    .circleCrop()
                    .into(ivAvatar);

            // Tải ảnh Comment (con voi)
            String imageUrl = comment.getImage();
            if (!TextUtils.isEmpty(imageUrl)) {
                ivCommentImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.image_placeholder) // Dùng đúng tên file
                        .error(R.drawable.image_placeholder) // Dùng đúng tên file
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivCommentImage);
                Log.d("CommentAdapter", "Binding Comment - Fullname: '" + comment.getFullname() + "', Avatar: '" + comment.getAvatar() + "'");
                // Lấy giá trị dimension từ R.dimen
                contentLayoutParams.setMarginStart(context.getResources().getDimensionPixelSize(R.dimen.comment_content_margin_with_image)); // Dùng đúng tên dimen
            } else {
                ivCommentImage.setVisibility(View.GONE);
                contentLayoutParams.setMarginStart(0);
            }
            tvCommentContent.setLayoutParams(contentLayoutParams);
        }
    }
}