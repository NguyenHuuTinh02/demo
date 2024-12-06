package com.examples.yumbox;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityPayOutBinding;
import com.examples.yumbox.Adapter.LpmAdapter;
import com.examples.yumbox.Adapter.NameTableAdapter;
import com.examples.yumbox.Fragment.CongratsBottomSheet;
import com.examples.yumbox.Model.CartItem;
import com.examples.yumbox.Model.OrderDetail;
import com.examples.yumbox.Utils.FormatString;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PayOutActivity extends AppCompatActivity {
    private ActivityPayOutBinding binding;
    private ArrayList<CartItem> cartItems;
    private ArrayList<CartItem> orderItems;

    // Order info
    private String name;
    private String address;
    private String phone;
    private String totalAmount;
    private String ownerUid;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPayOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        setUserData();

        // Get data from Intent
        orderItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("OrderItems");
        if (orderItems != null) {
            ownerUid = orderItems.get(0).getOwnerUid();
        }

        // Total amount
        totalAmount = String.valueOf(getTotalAmount());
        binding.totalAmount.setText(FormatString.formatAmount(Integer.parseInt(totalAmount)));

        // Place order
        binding.placeMyOrderButton.setOnClickListener(v -> {
            // Get input data
            name = binding.name.getText().toString();
            address = binding.address.getText().toString();
            phone = binding.phone.getText().toString();

            if (name.isBlank() || address.isBlank() || phone.isBlank()) {
                showToast("Vui lòng nhập đầy đủ thông tin");
            } else {
                NameTableAdapter.tableNameResult(row -> {
                    if (!row) {
                        if (!cartItems.isEmpty() && cartItems.size() > 3) {
                            orderItems.add(cartItems.get(orderItems.size()));
                            LpmAdapter.newRow();
                        }
                    } else {
                        placeOrder();
                    }
                });
            }
        });

        // Go back
        binding.backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void placeOrder() {
        userID = auth.getCurrentUser().getUid();
        Long time = System.currentTimeMillis();
        String itemPushKey = databaseRef.child("OrderDetails").push().getKey();
        OrderDetail orderDetail = new OrderDetail(userID, name, orderItems, address, phone, totalAmount, false, false, itemPushKey, time, ownerUid);

        DatabaseReference orderRef = databaseRef.child("OrderDetails").child(itemPushKey);
        orderRef.setValue(orderDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                showToast("Đặt món thành công");
                saveUserData();
                BottomSheetDialogFragment congratsBottomSheet = new CongratsBottomSheet();
                congratsBottomSheet.show(getSupportFragmentManager(), "CongratsBottomSheet");
                removeItemFromCart();
                addOrderToHistory(orderDetail);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showToast("Đặt món thất bại");
            }
        });
    }

    private void addOrderToHistory(OrderDetail orderDetail) {
        DatabaseReference historyRef = databaseRef.child("Users").child(userID).child("BuyHistory").child(orderDetail.getItemPushKey());
        historyRef.setValue(orderDetail).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("DX08472", "onFailure: " + e.getMessage());
            }
        });
    }

    private void removeItemFromCart() {
        DatabaseReference cartItemRef = databaseRef.child("Users").child(userID).child("CartItems");
        cartItemRef.removeValue();
    }

    private int getTotalAmount() {
        int total = 0;
        if (orderItems != null) {
            for (CartItem item : orderItems) {
                int price = Integer.parseInt(item.getFoodPrice());
                int quantity = item.getFoodQuantity();
                total += price * quantity;
            }
        }
        return total;
    }

    private void setUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userID = user.getUid();
            DatabaseReference userRef = databaseRef.child("Users").child(userID);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        name = snapshot.child("name").getValue(String.class);
                        address = snapshot.child("address").getValue(String.class);
                        phone = snapshot.child("phone").getValue(String.class);
                    }

                    binding.name.setText(name);
                    binding.address.setText(address);
                    binding.phone.setText(phone);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("DX08472", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void saveUserData() {
        DatabaseReference userRef = databaseRef.child("Users").child(userID);
        userRef.child("name").setValue(name);
        userRef.child("address").setValue(address);
        userRef.child("phone").setValue(phone);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}