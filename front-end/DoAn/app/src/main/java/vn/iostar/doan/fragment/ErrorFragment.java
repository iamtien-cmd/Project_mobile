package vn.iostar.doan.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import vn.iostar.doan.databinding.FragmentNeworderBinding;
import vn.iostar.doan.model.Order;

import java.util.ArrayList;
import java.util.List;

public class ErrorFragment extends Fragment {
    private FragmentNeworderBinding binding;
    private List<Order> orders = new ArrayList<>();

    public ErrorFragment() {
        // Required empty public constructor
    }
    private static final String ARG_ORDERS = "orders";

    public static ErrorFragment newInstance(ArrayList<Order> orders) {
        ErrorFragment fragment = new ErrorFragment();
        Bundle args = new Bundle();
        // because Order implements Parcelable, you can put the list directly:
        args.putParcelableArrayList("orders", orders);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Cast the returned ArrayList<Parcelable> into ArrayList<Order>
            orders = getArguments().getParcelableArrayList("orders");
            // Optionally check for null:
            if (orders == null) {
                orders = new ArrayList<>();
            }
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            binding = FragmentNeworderBinding.inflate(inflater, container, false);
            return binding.getRoot();
        } catch (Exception e) {
            e.printStackTrace(); // Ghi lại lỗi nếu có
            return super.onCreateView(inflater, container, savedInstanceState); // fallback
        }
    }

}

