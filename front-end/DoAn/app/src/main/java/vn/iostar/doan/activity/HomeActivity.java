package vn.iostar.doan.activity;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.ImagesViewPageAdapter;
import vn.iostar.doan.adapter.ProductAdapter;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.model.Product;
import vn.iostar.doan.image.Images;

public class HomeActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private List<Product> mListProduct;

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // Gọi API
        mListProduct = new ArrayList<>();
        callApiGetProducts();
    }*/

    private void callApiGetProducts() {
        ApiService.apiService.getListProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                mListProduct = response.body();
                ProductAdapter productAdapter = new ProductAdapter(mListProduct);
                recyclerView.setAdapter(productAdapter);
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "onFailure", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private List<Images> imagesList;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flipper_main);

        viewPager = findViewById(R.id.viewpage);
        circleIndicator = findViewById(R.id.circle_indicator);

        imagesList = getListImages();
        ImagesViewPageAdapter adapter = new ImagesViewPageAdapter(this, imagesList);
        viewPager.setAdapter(adapter);
        circleIndicator.setViewPager(viewPager);

        // Auto-run ViewPager
        runnable = new Runnable() {
            @Override
            public void run() {
                if (viewPager.getCurrentItem() == imagesList.size() - 1) {
                    viewPager.setCurrentItem(0);
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(runnable, 3000);

        // Lắng nghe sự kiện ViewPager
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private List<Images> getListImages() {
        List<Images> list = new ArrayList<>();
        list.add(new Images(R.drawable.qc1));
        list.add(new Images(R.drawable.qc2));
        list.add(new Images(R.drawable.qc3));
        list.add(new Images(R.drawable.qc4));
        return list;
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 3000);
    }
    public class ImagesViewPager2Adapter extends RecyclerView.Adapter<ImagesViewPager2Adapter.ImagesViewHolder> {
        private List<Images> imagesList;

        public ImagesViewPager2Adapter(List<Images> imagesList) {
            this.imagesList = imagesList;
        }

        @NonNull
        @Override
        public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_images, parent, false);
            return new ImagesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
            Images images = imagesList.get(position);
            if (images != null) {
                holder.imageView.setImageResource(images.getImagesId());
            }
        }

        @Override
        public int getItemCount() {
            return (imagesList != null) ? imagesList.size() : 0;
        }

        // ✅ Thêm class ViewHolder
        public class ImagesViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            public ImagesViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imgView); // Đảm bảo item_images.xml có ImageView với id này
            }
        }
    }

}
