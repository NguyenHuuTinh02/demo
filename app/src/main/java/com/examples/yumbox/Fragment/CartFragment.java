package com.examples.yumbox.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.databinding.FragmentCartBinding;
import com.examples.yumbox.Adapter.CartAdapter;
import com.examples.yumbox.Adapter.LpmAdapter;
import com.examples.yumbox.Adapter.NameTableAdapter;
import com.examples.yumbox.Model.CartItem;
import com.examples.yumbox.PayOutActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartFragment extends Fragment {
    private FragmentCartBinding binding;
    private ArrayList<CartItem> cartItems;
    private ArrayList<CartItem> items;
    private CartAdapter cartAdapter;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String userID;
    private DatabaseReference foodRef;

    public CartFragment() {
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
        binding = FragmentCartBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();

        retrieveCartItems();

        // Move to PayOutActivity
        binding.proceedButton.setOnClickListener(v -> {
            getOrderItemsDetail();
        });
    }

    private void getOrderItemsDetail() {
        DatabaseReference orderItemsRef = database.getReference().child("Users").child(userID).child("CartItems");
        cartItems = new ArrayList<>();
        ArrayList<Integer> foodQuantities = cartAdapter.getUpdatedItemsQuantities();

        // Fetch data
        orderItemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get each item and add to cartItems
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem orderItem = dataSnapshot.getValue(CartItem.class);
                    cartItems.add(orderItem);
                }

                joinQuantitiesToOrderItems(cartItems, foodQuantities);
                orderNow(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DX097486", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void joinQuantitiesToOrderItems(ArrayList<CartItem> cartItems, ArrayList<Integer> foodQuantities) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem cartItem = cartItems.get(i);
            cartItem.setFoodQuantity(foodQuantities.get(i));
        }
    }

    private void orderNow(ArrayList<CartItem> cartItems) {
        NameTableAdapter.tableNameResult(col -> {
            if (isAdded() && getContext()!=null && col) {
                Intent intent = new Intent(getContext(), PayOutActivity.class);
                intent.putExtra("OrderItems", cartItems);
                startActivity(intent);
            } else {
                if (!items.isEmpty() && items.size() > 3) {
                    items.add(cartItems.get(items.size()));
                    LpmAdapter.newRow();
                }
            }
        });
    }

    private void retrieveCartItems() {
        database = FirebaseDatabase.getInstance();
        userID = auth.getCurrentUser().getUid();
        foodRef = database.getReference().child("Users").child(userID).child("CartItems");
        cartItems = new ArrayList<>();

        // Fetch data
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get each item and add to cartItems
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CartItem cartItem = dataSnapshot.getValue(CartItem.class);
                    cartItems.add(cartItem);
                }

                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Load data", "onCancelled: Failed" + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        cartAdapter = new CartAdapter(cartItems, getContext());
        binding.cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.cartRecyclerView.setAdapter(cartAdapter);
    }
}