package com.examples.yumbox;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.yumbox.R;
import com.example.yumbox.databinding.ActivityChooseLocationBinding;

import java.util.Arrays;
import java.util.List;

public class ChooseLocationActivity extends AppCompatActivity {
    ActivityChooseLocationBinding binding;
    List<String> locationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChooseLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // AutoCompleteTextView
        locationList = Arrays.asList("Tp.Thủ Dầu Một", "Tp.Thuận An", "Tp.Dĩ An", "Tp.Tân Uyên", "Tp.Bến Cát", "Bàu Bàng", "Bắc Tân Uyên", "Dầu Tiếng", "Phú Giáo");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ChooseLocationActivity.this, android.R.layout.simple_list_item_1, locationList);
        AutoCompleteTextView autoCompleteTextView = binding.listOfLocation;
        autoCompleteTextView.setAdapter(adapter);
    }
}