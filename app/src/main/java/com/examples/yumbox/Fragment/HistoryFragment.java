package com.examples.yumbox.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.yumbox.databinding.FragmentHistoryBinding;
import com.examples.yumbox.Adapter.BuyAgainAdapter;
import com.examples.yumbox.Model.OrderDetail;
import com.examples.yumbox.RecentOrderItemsActivity;
import com.examples.yumbox.Utils.FormatString;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class HistoryFragment extends Fragment {
    private FragmentHistoryBinding binding;
    private ArrayList<OrderDetail> listOfOrderItem;

    // Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private String userID;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHistoryBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        retrieveBuyHistory();

        // Show recent order details
        binding.recentBuyItem.setOnClickListener(v -> {
            seeItemsRecentBuyed();
        });

        // Received item
        binding.receivedButton.setOnClickListener(v -> {
            updateOrderStatus();
        });
    }
    private void updateOrderStatus() {
        String itemPushKey = listOfOrderItem.get(0).getItemPushKey();
        DatabaseReference historyOrderRef = database.getReference().child("Users").child(userID).child("BuyHistory").child(itemPushKey);
        DatabaseReference completeOrderRef = database.getReference().child("CompletedOrders").child(itemPushKey);
        completeOrderRef.child("paymentReceived").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                historyOrderRef.child("paymentReceived").setValue(true);
                binding.receivedButton.setVisibility(View.INVISIBLE);
                showToast("Chúc bạn ngon miệng (●'◡'●)");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DX09182", "onFailure: " + e.getMessage());
            }
        });
    }

    private void seeItemsRecentBuyed() {
        OrderDetail recentOrderItem = listOfOrderItem.get(0);
        Intent intent = new Intent(getContext(), RecentOrderItemsActivity.class);
        intent.putExtra("RecentOrderItem", recentOrderItem);
        startActivity(intent);
    }

    private void retrieveBuyHistory() {
        binding.recentBuyItem.setVisibility(View.INVISIBLE);
        userID = auth.getCurrentUser().getUid();
        DatabaseReference buyItemRef = database.getReference().child("Users").child(userID).child("BuyHistory");
        listOfOrderItem = new ArrayList<>();

        // Sort by currentTime
        Query shortingQuery = buyItemRef.orderByChild("currentTime");

        shortingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get data from firebase and add to list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    OrderDetail buyHistoryItem = dataSnapshot.getValue(OrderDetail.class);
                    listOfOrderItem.add(buyHistoryItem);
                }

                // Item at the end is the latest order so we need to reverse the list
                Collections.reverse(listOfOrderItem);

                if (!listOfOrderItem.isEmpty()) {
                    setDataInRecentBuyItem();
                    setPreviousBuyItemRecycler();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setDataInRecentBuyItem() {
        binding.recentBuyItem.setVisibility(View.VISIBLE);
        OrderDetail recentOrderItem = listOfOrderItem.get(0);

        binding.buyAgainFoodName.setText(recentOrderItem.getOrderItems().get(0).getFoodName());
        binding.buyAgainPrice.setText(FormatString.formatAmount(Integer.parseInt(recentOrderItem.getOrderItems().get(0).getFoodPrice())));

        // Load image using Glide
        Uri uri = Uri.parse(recentOrderItem.getOrderItems().get(0).getFoodImage());
        Glide.with(requireContext()).load(uri).into(binding.buyAgainImage);

        boolean isOrderIsAccepted = listOfOrderItem.get(0).getOrderAccepted();
        boolean isOrderIsReceived = listOfOrderItem.get(0).getPaymentReceived();
        if (isOrderIsAccepted && !isOrderIsReceived) {
            binding.orderStatus.setCardBackgroundColor(Color.GREEN);
            binding.receivedButton.setVisibility(View.VISIBLE);
        } else if (!isOrderIsAccepted) {
            binding.orderStatus.setCardBackgroundColor(Color.RED);
            binding.receivedButton.setVisibility(View.INVISIBLE);
        } else {
            binding.orderStatus.setCardBackgroundColor(Color.GREEN);
            binding.receivedButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setPreviousBuyItemRecycler() {
        ArrayList<String> buyAgainNames = new ArrayList<>();
        ArrayList<String> buyAgainPrices = new ArrayList<>();
        ArrayList<String> buyAgainImages = new ArrayList<>();

        for (int i = 1; i < listOfOrderItem.size(); i++) {
            buyAgainNames.add(listOfOrderItem.get(i).getOrderItems().get(0).getFoodName());
            buyAgainPrices.add(listOfOrderItem.get(i).getOrderItems().get(0).getFoodPrice());
            buyAgainImages.add(listOfOrderItem.get(i).getOrderItems().get(0).getFoodImage());
        }

        // Delete item duplicate
        HashSet<String> uniqueNames = new HashSet<>();
        for (int i = 0; i < buyAgainNames.size(); i++) {
            String name = buyAgainNames.get(i);

            if (!uniqueNames.add(name)) {
                buyAgainNames.remove(i);
                buyAgainPrices.remove(i);
                buyAgainImages.remove(i);
                i--;
            }
        }

        BuyAgainAdapter adapter = new BuyAgainAdapter(buyAgainNames, buyAgainPrices, buyAgainImages, getContext());
        binding.buyAgainRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.buyAgainRecyclerView.setAdapter(adapter);
    }

     private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
     }
}