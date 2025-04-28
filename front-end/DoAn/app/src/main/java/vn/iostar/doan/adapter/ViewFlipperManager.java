package vn.iostar.doan.adapter;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import vn.iostar.doan.R;

public class ViewFlipperManager {

    public static void setupViewFlipper(ViewFlipper viewFlipper, Context context) {
        List<String> arrayListFlipper = new ArrayList<>();
        arrayListFlipper.add("https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcRiABEDVjWbbHXmVTIa7yujyIGzI1vBSjkzCZH5vL4TqNVQrRSe");
        arrayListFlipper.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRWevEIZX4Zs1opHgjS60LzN6hHhV2UKZeB7SAl2jvDTDkulnFe");
        arrayListFlipper.add("https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcRrg5-i1RpqJoR-sMhBC7FSmmPft5w_x-7XaZg_J_6L2hDLmVqu");
        arrayListFlipper.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJ_NoFyKHpBF_UoVlISDhj1EUlPcbE-AKYYoVaxr65EnOHuDKT");
        for (String url : arrayListFlipper) {
            ImageView imageView = new ImageView(context);
            Glide.with(context).load(url).into(imageView);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            viewFlipper.addView(imageView);
        }

        viewFlipper.setFlipInterval(3000);
        viewFlipper.setAutoStart(true);

        Animation slide_in = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        Animation slide_out = AnimationUtils.loadAnimation(context, R.anim.slide_out_right);

        viewFlipper.setInAnimation(slide_in);
        viewFlipper.setOutAnimation(slide_out);
    }
}