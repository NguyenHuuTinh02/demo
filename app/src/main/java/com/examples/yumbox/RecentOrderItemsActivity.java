package com.examples.yumbox;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityRecentOrderItemsBinding;
import com.examples.yumbox.Adapter.RecentBuyAdapter;
import com.examples.yumbox.Model.CartItem;
import com.examples.yumbox.Model.OrderDetail;

import java.util.ArrayList;

public class RecentOrderItemsActivity extends AppCompatActivity {
    private ActivityRecentOrderItemsBinding binding;
    private OrderDetail recentOrderItems;
    private ArrayList<String> foodNames, foodPrices, foodImages;
    private ArrayList<Integer> foodQuantities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecentOrderItemsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recentOrderItems = (OrderDetail) getIntent().getSerializableExtra("RecentOrderItem");

        if (recentOrderItems != null) {
            foodNames = new ArrayList<>();
            foodPrices = new ArrayList<>();
            foodQuantities = new ArrayList<>();
            foodImages = new ArrayList<>();

            for (CartItem cartItem : recentOrderItems.getOrderItems()) {
                foodNames.add(cartItem.getFoodName());
                foodPrices.add(cartItem.getFoodPrice());
                foodQuantities.add(cartItem.getFoodQuantity());
                foodImages.add(cartItem.getFoodImage());
            }
        }

        setAdapter();

        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void setAdapter() {
        RecentBuyAdapter recentBuyAdapter = new RecentBuyAdapter(foodNames, foodPrices, foodImages, foodQuantities, this);
        binding.recentBuyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recentBuyRecyclerView.setAdapter(recentBuyAdapter);
    }

    // Data remote
//    private void getFoodInfo(ArrayList<OrderDetail> recentOrderItems) {
//        foodNames = new ArrayList<>();
//        foodPrices = new ArrayList<>();
//        foodQuantities = new ArrayList<>();
//        foodImages = new ArrayList<>();
//
//        for (OrderDetail item : recentOrderItems) {
//            for (CartItem cartItem : item.getOrderItems()) {
//                foodNames.add(cartItem.getFoodName());
//                foodPrices.add(cartItem.getFoodPrice());
//                foodQuantities.add(cartItem.getFoodQuantity());
//                foodImages.add(cartItem.getFoodImage());
//            }
//        }
//    }
}