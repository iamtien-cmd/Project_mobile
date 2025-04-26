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

public class ReviewFragment  extends Fragment {
    FragmentNeworderBinding binding;

    public ReviewFragment () {
    }
    private static final String ARG_ORDERS = "orders";

    public static ReviewFragment newInstance(ArrayList<Order> orders) {
        ReviewFragment fragment = new ReviewFragment();
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
