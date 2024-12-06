package com.examples.yumbox.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.yumbox.databinding.FragmentProfileBinding;
import com.examples.yumbox.LoginActivity;
import com.examples.yumbox.Model.UserModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;

    // User info
    private UserModel userProfile;
    private String name;
    private String address;
    private String email;
    private String phone;

    // Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private DatabaseReference userRef;
    private String userID;

    public ProfileFragment() {
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
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Disable edit text
        disableEditText();

        setUserData();

        // Save updated user info
        binding.saveInfoButton.setOnClickListener(v -> {
            name = binding.name.getText().toString().trim();
            address = binding.address.getText().toString().trim();
            email = binding.email.getText().toString().trim();
            phone = binding.phone.getText().toString().trim();

            updateUserData(name, address, email, phone);
            disableEditText();
        });

        binding.editButton.setOnClickListener(v -> {
            binding.name.setEnabled(!binding.name.isEnabled());
            binding.address.setEnabled(!binding.address.isEnabled());
            binding.email.setEnabled(!binding.email.isEnabled());
            binding.phone.setEnabled(!binding.phone.isEnabled());
        });

        binding.logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getContext(), LoginActivity.class));
            showToast("Đã đăng xuất");
        });
    }

    private void disableEditText() {
        binding.name.setEnabled(false);
        binding.address.setEnabled(false);
        binding.email.setEnabled(false);
        binding.phone.setEnabled(false);
    }

    private void updateUserData(String name, String address, String email, String phone) {
        userID = auth.getCurrentUser().getUid();
        if (userID != null) {
            userRef = databaseRef.child("Users").child(userID);

            HashMap<String, String> userData = new HashMap<>();
            userData.put("name", name);
            userData.put("address", address);
            userData.put("email", email);
            userData.put("phone", phone);

            userRef.setValue(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    showToast("Cập nhật thông tin cá nhân thành công");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    showToast("Cập nhật thông tin cá nhân thất bại");
                }
            });
        }
    }

    private void setUserData() {
        userID = auth.getCurrentUser().getUid();
        if (userID != null) {
            userRef = databaseRef.child("Users").child(userID);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userProfile = snapshot.getValue(UserModel.class);
                        if (userProfile != null) {
                            binding.name.setText(userProfile.getName());
                            binding.address.setText(userProfile.getAddress());
                            binding.email.setText(userProfile.getEmail());
                            binding.phone.setText(userProfile.getPhone());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("DX09473", "onCancelled: " + error.getMessage());
                }
            });
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}