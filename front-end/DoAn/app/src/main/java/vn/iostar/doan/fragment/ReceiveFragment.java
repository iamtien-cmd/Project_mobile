package vn.iostar.doan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import vn.iostar.doan.databinding.FragmentNeworderBinding;
import vn.iostar.doan.model.Order;

public class ReceiveFragment  extends Fragment {
    FragmentNeworderBinding binding;

    public ReceiveFragment () {
    }
    private static final String ARG_ORDERS = "orders";

    public static ReceiveFragment newInstance(ArrayList<Order> orders) {
        ReceiveFragment fragment = new ReceiveFragment();
        Bundle args = new Bundle();
        // because Order implements Parcelable, you can put the list directly:
        args.putParcelableArrayList("orders", orders);
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
