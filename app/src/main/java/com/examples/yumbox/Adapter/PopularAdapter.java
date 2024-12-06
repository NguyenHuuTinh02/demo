package com.examples.yumbox.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examples.yumbox.DetailsActivity;
import com.example.yumbox.databinding.PopularItemBinding;
import com.examples.yumbox.Utils.FormatString;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.PopularViewHolder> {
    private List<String> items;
    private List<String> prices;
    private List<Integer> images;
    private Context requiredContext;

    // Constructor
    public PopularAdapter(List<String> items, List<String> prices, List<Integer> images, Context context) {
        this.items = items;
        this.prices = prices;
        this.images = images;
        this.requiredContext = context;
    }

    @NonNull
    @Override
    public PopularAdapter.PopularViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        // Inflate convert XML to View
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PopularItemBinding binding = PopularItemBinding.inflate(inflater, parent, false);
        return new PopularViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.PopularViewHolder holder, int position) {
        // Position is the index of the current item
        String item = items.get(position);
        String price = prices.get(position);
        int image = images.get(position);

        // Bind data to ViewHolder
        holder.bind(item, image, price);

        // Handle move to details when click on item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send data to DetailsActivity through Intent
                Intent intent = new Intent(requiredContext, DetailsActivity.class);
                intent.putExtra("MenuItemName", item);
                intent.putExtra("MenuItemImage", image);
                requiredContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class PopularViewHolder extends RecyclerView.ViewHolder {

        private PopularItemBinding binding;

        // Constructor
        public PopularViewHolder(PopularItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Function to bind data to ViewHolder
        public void bind(String item, int image, String price) {
            binding.foodNamePopular.setText(item);
            binding.imageFoodPopular.setImageResource(image);
            binding.pricePopular.setText(FormatString.formatAmount(Integer.parseInt(price)));
        }
    }
}
