package com.examples.yumbox.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.yumbox.databinding.FragmentSearchBinding;
import com.examples.yumbox.Adapter.MenuAdapter;
import com.examples.yumbox.Model.MenuItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private FragmentSearchBinding binding;
    private MenuAdapter menuAdapter;
    private ArrayList<MenuItem> menuItems;
    private ArrayList<MenuItem> filterMenuItems;

    // Firebase
    private FirebaseDatabase database;

    public SearchFragment() {
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
        binding = FragmentSearchBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retrieveMenuItems();
        setupSearchView();
    }

    private void retrieveMenuItems() {
        database = FirebaseDatabase.getInstance();
        DatabaseReference menuRef = database.getReference().child("MenuItems");
        menuItems = new ArrayList<>();

        // Fetch data
        menuRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get data from Firebase and add to menuItems
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MenuItem menuItem = dataSnapshot.getValue(MenuItem.class);
                    menuItems.add(menuItem);
                }

                showAllMenuItems();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DX09281", "onCancelled: " + error.getMessage());
            }
        });
    }

    private void showAllMenuItems() {
        filterMenuItems = new ArrayList<>(menuItems);
        setAdapter(filterMenuItems);
    }

    private void setAdapter(ArrayList<MenuItem> filterMenuItems) {
        menuAdapter = new MenuAdapter(filterMenuItems, getContext());
        binding.menuRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.menuRecyclerView.setAdapter(menuAdapter);
    }

    // Handle SearchView take and filter data
    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterMenuItems(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMenuItems(newText);
                return true;
            }
        });
    }

    // Filter menu items based on the search query
    private void filterMenuItems(String query) {
        filterMenuItems = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            showAllMenuItems();
        } else {
            for (MenuItem menuItem : menuItems) {
                if (menuItem.getFoodName().toLowerCase().contains(query.toLowerCase())) {
                    filterMenuItems.add(menuItem);
                }
            }
        }

        setAdapter(filterMenuItems);
    }
}