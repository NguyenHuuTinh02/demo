package com.examples.yumbox.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.databinding.BuyAgainItemBinding;
import com.examples.yumbox.Utils.FormatString;

import java.util.ArrayList;

public class BuyAgainAdapter extends RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder> {
    private ArrayList<String> foodNames, foodPrices, foodImages;
    private Context context;

    // Constructor
    public BuyAgainAdapter(ArrayList<String> foodNames, ArrayList<String> foodPrices, ArrayList<String> foodImages, Context context) {
        this.foodNames = foodNames;
        this.foodPrices = foodPrices;
        this.foodImages = foodImages;
        this.context = context;
    }

    @NonNull
    @Override
    public BuyAgainAdapter.BuyAgainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert XML to View
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        BuyAgainItemBinding binding = BuyAgainItemBinding.inflate(inflater, parent, false);
        return new BuyAgainViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BuyAgainAdapter.BuyAgainViewHolder holder, int position) {
        // Bind data to ViewHolder
        // Position is the index of the current item
        holder.binding(position);
    }

    @Override
    public int getItemCount() {
        return foodNames.size();
    }

    public class BuyAgainViewHolder extends RecyclerView.ViewHolder {

        private final BuyAgainItemBinding binding;

        // Constructor
        public BuyAgainViewHolder(BuyAgainItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Function to bind data to ViewHolder
        public void binding(int position) {
            binding.buyAgainFoodName.setText(foodNames.get(position));
            binding.buyAgainPrice.setText(FormatString.formatAmount(Integer.parseInt(foodPrices.get(position))));

            Uri uri = Uri.parse(foodImages.get(position));
            Glide.with(context).load(uri).into(binding.buyAgainImage);
        }
    }
}
