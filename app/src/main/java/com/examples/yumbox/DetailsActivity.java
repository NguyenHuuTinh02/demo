package com.examples.yumbox;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityDetailsBinding;
import com.examples.yumbox.Model.CartItem;
import com.examples.yumbox.Model.MenuItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity {
    private ActivityDetailsBinding binding;

    // Food info
    private String foodName;
    private String foodPrice;
    private String foodImage;
    private String foodDescription;
    private String foodIngredients;
    private String ownerUid;
    private String restaurantName;
    private MenuItem menuItem;

    // Firebase
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding instead of findViewByID
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase and auth
        auth = FirebaseAuth.getInstance();

        // Get data from intent
        menuItem = (MenuItem) getIntent().getSerializableExtra("MenuItem");
        if (menuItem != null) {
            foodName = menuItem.getFoodName();
            foodPrice = menuItem.getFoodPrice();
            foodImage = menuItem.getFoodImage();
            foodDescription = menuItem.getFoodDescription();
            foodIngredients = menuItem.getFoodIngredients();
            ownerUid = menuItem.getOwnerUid();
            restaurantName = menuItem.getNameOfRestaurant();
        }

        binding.detailsFoodName.setText(foodName);
        binding.detailsFoodDescription.setText(foodDescription);
        binding.detailsFoodIngredients.setText(foodIngredients);
        binding.restaurantName.setText(restaurantName);

        // Load image using Glide
        Uri uri = Uri.parse(foodImage);
        Glide.with(this).load(uri).into(binding.detailsFoodImage);

        // Go back
        binding.backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Add item to cart
        binding.addItemButton.setOnClickListener(v -> {
            addItemToCart();
        });
    }

    private void addItemToCart() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
        String userID = auth.getCurrentUser().getUid();

        // Check second food has same restaurant with first?
        databaseRef.child("Users").child(userID).child("CartItems").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot firstChild : snapshot.getChildren()) {
                        String firstOwnerUid = firstChild.getValue(CartItem.class).getOwnerUid();
                        firstOwnerUid = firstOwnerUid == null ? "" : firstOwnerUid;
                        if (firstOwnerUid.equals(ownerUid)) {
                            addItem();
                        } else {
                            showToast("Chỉ hỗ trợ đặt nhiều món cùng 1 nhà hàng");
                        }
                    }
                } else {
                    addItem();
                }
            }

            private void addItem() {
                CartItem cartItem = new CartItem(foodName, foodPrice, foodImage, foodDescription, foodIngredients, 1, ownerUid, restaurantName);

                databaseRef.child("Users").child(userID).child("CartItems").push().setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        showToast("Thêm vào giỏ hàng thành công");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast("Thêm vào giỏ hàng thất bại");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}