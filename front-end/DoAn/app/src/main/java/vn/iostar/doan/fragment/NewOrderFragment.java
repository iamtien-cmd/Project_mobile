package vn.iostar.doan.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.iostar.doan.R;
import vn.iostar.doan.adapter.OrderAdapter;
import vn.iostar.doan.databinding.FragmentNeworderBinding;
import vn.iostar.doan.model.Order;

public class NewOrderFragment extends Fragment {
    private List<Order> orders = new ArrayList<>();
    private FragmentNeworderBinding binding;

    private static final String ARG_ORDERS = "orders";

    public NewOrderFragment() {}

    public static NewOrderFragment newInstance(List<Order> orders) {
        NewOrderFragment fragment = new NewOrderFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ORDERS, new ArrayList<>(orders));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orders = getArguments().getParcelableArrayList(ARG_ORDERS);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNeworderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Truy cập RecyclerView thông qua binding
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Kiểm tra xem orders có null hoặc rỗng không
        if (orders == null || orders.isEmpty()) {
            // Hiển thị thông báo rỗng nếu cần thiết
            Log.d("NewOrderFragment", "Danh sách đơn hàng rỗng.");
        } else {
            // Tạo adapter với danh sách đơn hàng
            OrderAdapter adapter = new OrderAdapter(orders, getContext());
            recyclerView.setAdapter(adapter);
            Log.d("NewOrderFragment", "Danh sách đơn hàng được gọi.");
        }
    }



}
