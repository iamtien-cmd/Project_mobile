package vn.iostar.doan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import vn.iostar.doan.databinding.FragmentNeworderBinding;
import vn.iostar.doan.model.Order;

public class ShippingFragment  extends Fragment {
    FragmentNeworderBinding binding;

    public ShippingFragment () {
    }
    private static final String ARG_ORDERS = "orders";

    public static ShippingFragment newInstance(ArrayList<Order> orders) {
        ShippingFragment fragment = new ShippingFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDERS, new ArrayList<>(orders)); // Chuyển List<Order> thành ArrayList<Order>
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = FragmentNeworderBinding.inflate(inflater, container, false);

        // recyclerView

        return binding.getRoot();
    }
}
