package com.examples.yumbox.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yumbox.databinding.RecentBuyItemBinding;

import java.util.ArrayList;

public class RecentBuyAdapter extends RecyclerView.Adapter<RecentBuyAdapter.RecentBuyViewHolder> {
    private ArrayList<String> foodNames;
    private ArrayList<String> foodPrices;
    private ArrayList<String> foodImages;
    private ArrayList<Integer> foodQuantities;
    private Context context;

    public RecentBuyAdapter(ArrayList<String> foodNames, ArrayList<String> foodPrices, ArrayList<String> foodImages, ArrayList<Integer> foodQuantities, Context context) {
        this.foodNames = foodNames;
        this.foodPrices = foodPrices;
        this.foodImages = foodImages;
        this.foodQuantities = foodQuantities;
        this.context = context;
    }

    @NonNull
    @Override
    public RecentBuyAdapter.RecentBuyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecentBuyItemBinding binding = RecentBuyItemBinding.inflate(inflater, parent, false);
        return new RecentBuyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentBuyAdapter.RecentBuyViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return foodNames.size();
    }

    public class RecentBuyViewHolder extends RecyclerView.ViewHolder {
        private final RecentBuyItemBinding binding;

        public RecentBuyViewHolder(RecentBuyItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(int position) {
            binding.foodName.setText(foodNames.get(position));
            binding.foodPrice.setText(foodPrices.get(position));
            binding.foodQuantity.setText(String.valueOf(foodQuantities.get(position)));

            Uri uri = Uri.parse(foodImages.get(position));
            Glide.with(context).load(uri).into(binding.foodImage);
        }
    }
}
