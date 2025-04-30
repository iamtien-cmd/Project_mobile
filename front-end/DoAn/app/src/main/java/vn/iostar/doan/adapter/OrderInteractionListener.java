package vn.iostar.doan.adapter;

import vn.iostar.doan.model.Order;

public interface OrderInteractionListener {
    void onCancelOrderClicked(Order order);
    void onReviewOrderClicked(Order order);
    void onRepurchaseOrderClicked(Order order);
}
