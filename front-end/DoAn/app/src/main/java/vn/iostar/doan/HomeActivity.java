package vn.iostar.doan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.adapter.ProductAdapter;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.entity.Product;

public class HomeActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private List<Product> mListProduct;

    @Override
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
    }

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

}
