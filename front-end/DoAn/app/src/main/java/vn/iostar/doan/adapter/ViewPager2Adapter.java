package vn.iostar.doan.adapter;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import vn.iostar.doan.fragment.ErrorFragment;
import vn.iostar.doan.fragment.NewOrderFragment;
import vn.iostar.doan.fragment.ReceiveFragment;
import vn.iostar.doan.fragment.ReviewFragment;
import vn.iostar.doan.fragment.ShippingFragment;
import vn.iostar.doan.model.Order;

import java.util.ArrayList;
import java.util.List;

public class ViewPager2Adapter extends FragmentStateAdapter {

    private ArrayList<ArrayList<Order>> ordersForTabs = new ArrayList<>();

    public ViewPager2Adapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        // Khởi tạo danh sách orders cho mỗi tab
        for (int i = 0; i < 5; i++) {
            ordersForTabs.add(new ArrayList<Order>());
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về fragment tương ứng với vị trí tab
        switch (position) {
            case 0:
                return NewOrderFragment.newInstance(ordersForTabs.get(0));
            case 1:
                return ShippingFragment.newInstance(ordersForTabs.get(1));
            case 2:
                return ReceiveFragment.newInstance(ordersForTabs.get(2));
            case 3:
                return ReviewFragment.newInstance(ordersForTabs.get(3));
            case 4:
                return ErrorFragment.newInstance(ordersForTabs.get(4));
            default:
                return NewOrderFragment.newInstance(ordersForTabs.get(0));
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public void setOrdersForTab(int tabIndex, ArrayList<Order> orders) {
        ordersForTabs.set(tabIndex, orders);
        notifyItemChanged(tabIndex);
    }
}

