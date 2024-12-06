package com.examples.yumbox.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.databinding.FragmentMenuBottomSheetBinding;
import com.examples.yumbox.Adapter.MenuAdapter;
import com.examples.yumbox.Model.MenuItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MenuBottomSheetFragment extends BottomSheetDialogFragment {
    private FragmentMenuBottomSheetBinding binding;
    private ArrayList<MenuItem> menuItems;
    private FirebaseDatabase database;

    public MenuBottomSheetFragment() {
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
        binding = FragmentMenuBottomSheetBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retrieveMenuItems();

        // Go back
        binding.backButton.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void retrieveMenuItems() {
        database = FirebaseDatabase.getInstance();
        DatabaseReference foodRef = database.getReference().child("MenuItems");
        menuItems = new ArrayList<>();

        // Fetch data
        foodRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get each item and add to list
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MenuItem menuItem = dataSnapshot.getValue(MenuItem.class);
                    menuItems.add(menuItem);
                }
                setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DX09182", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void setAdapter() {
        MenuAdapter adapter = new MenuAdapter(menuItems, getContext());
        binding.menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.menuRecyclerView.setAdapter(adapter);
    }
}