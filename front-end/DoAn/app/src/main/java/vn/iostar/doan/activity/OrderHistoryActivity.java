package vn.iostar.doan.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.iostar.doan.R;
import vn.iostar.doan.adapter.ViewPager2Adapter;
import vn.iostar.doan.api.ApiService;
import vn.iostar.doan.databinding.ActivityOrderHistoryBinding;
import vn.iostar.doan.fragment.ErrorFragment;
import vn.iostar.doan.fragment.NewOrderFragment;
import vn.iostar.doan.model.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;
    private ViewPager2Adapter viewPager2Adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ánh xạ view bằng ViewBinding
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        // Thiết lập Toolbar
        setSupportActionBar(binding.toolBar);

        // Thiết lập FloatingActionButton (FAB)
        binding.fabAction.setOnClickListener(view ->
                Snackbar.make(view, "Chức năng đang phát triển", Snackbar.LENGTH_LONG).show()
        );

        setupTabsAndViewPager();

        // Gọi API để lấy đơn hàng của người dùng
        getOrders();
    }

    private void setupTabsAndViewPager() {
        // Thêm các tab vào TabLayout
        String[] tabTitles = {"Waiting", "Shipping", "Received", "Reviewed", "Error"};
        for (String title : tabTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(title));
        }

        // Khởi tạo adapter và gán cho ViewPager2
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager2Adapter = new ViewPager2Adapter(fragmentManager, getLifecycle());
        binding.viewPager2.setAdapter(viewPager2Adapter);

        // Đồng bộ giữa TabLayout và ViewPager2
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager2.setCurrentItem(tab.getPosition());
                changeFabIcon(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });
    }

    private void getOrders() {
        // Giả sử bạn đã có ID người dùng, ví dụ là 1
        long userId = 1;

        // Gọi API để lấy danh sách đơn hàng
        ApiService.apiService.getOrdersByUserId(userId).enqueue(new Callback<ArrayList<Order>>() {
            @Override
            public void onResponse(Call<ArrayList<Order>> call, Response<ArrayList<Order>> response) {
                if (response.isSuccessful()) {
                    ArrayList<Order> orders = response.body();
                    if (orders != null && !orders.isEmpty()) {
                        // Phân loại các đơn hàng theo trạng thái
                        List<Order> waitingOrders = new ArrayList<>();
                        List<Order> shippingOrders = new ArrayList<>();
                        List<Order> receivedOrders = new ArrayList<>();
                        List<Order> reviewedOrders = new ArrayList<>();
                        List<Order> errorOrders = new ArrayList<>();

                        // Phân loại các đơn hàng theo trạng thái
                        for (Order order : orders) {
                            switch (order.getStatus()) {
                                case WAITING:
                                    waitingOrders.add(order);
                                    Log.d("OrderHistory", "Đơn hàng đang chờ: " + order.toString());
                                    break;
                                case SHIPPING:
                                    shippingOrders.add(order);
                                    break;
                                case RECEIVED:
                                    receivedOrders.add(order);
                                    break;
                                case REVIEWED:
                                    reviewedOrders.add(order);
                                    break;
                                case ERROR:
                                    errorOrders.add(order);
                                    break;
                            }
                        }

                        // Cập nhật lại ViewPager2Adapter với các danh sách đơn hàng

                        viewPager2Adapter.setOrdersForTab(0, new ArrayList<>(waitingOrders)); // Tab 0 cho "Waiting"
                        viewPager2Adapter.setOrdersForTab(1, new ArrayList<>(shippingOrders)); // Tab 1 cho "Shipping"
                        viewPager2Adapter.setOrdersForTab(2, new ArrayList<>(receivedOrders)); // Tab 2 cho "Received"
                        viewPager2Adapter.setOrdersForTab(3, new ArrayList<>(reviewedOrders)); // Tab 3 cho "Reviewed"
                        viewPager2Adapter.setOrdersForTab(4, new ArrayList<>(errorOrders)); // Tab 4 cho "Error"

                        // Thông báo số lượng đơn hàng đã nhận được
                        showToast("Đã nhận được " + orders.size() + " đơn hàng");
                    } else {
                        showToast("Không có đơn hàng");
                    }
                } else {
                    showToast("Lỗi khi lấy dữ liệu");
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Order>> call, Throwable t) {
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    // Đổi icon FAB theo tab
    private void changeFabIcon(final int index) {
        binding.fabAction.hide();
        new Handler().postDelayed(() -> {
            int iconRes;
            switch (index) {
                case 0:
                    iconRes = R.drawable.icon_avatar;
                    break;
                case 1:
                    iconRes = R.drawable.icon_avatar;
                    break;
                case 2:
                    iconRes = R.drawable.icon_avatar;
                    break;
                default:
                    iconRes = R.drawable.icon_avatar; // fallback icon
                    break;
            }
            binding.fabAction.setImageDrawable(getDrawable(iconRes));
            binding.fabAction.show();
        }, 500); // Giảm thời gian cho mượt mà
    }

    // Inflate menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    // Xử lý sự kiện khi chọn menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuSearch) {
            showToast("Bạn đang chọn Search");
            return true;
        } else if (id == R.id.menuNewGroup || id == R.id.menuBroadcast || id == R.id.menuWeb || id == R.id.menuMessage) {
            showToast("Bạn đang chọn More");
            return true;
        } else if (id == R.id.menuSetting) {
            showToast("Bạn đang chọn Setting");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
