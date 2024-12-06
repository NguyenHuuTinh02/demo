package com.examples.yumbox.Adapter;

import androidx.annotation.NonNull;

import com.example.yumbox.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class NameTableAdapter {
    public interface TableName {
        void onResult(boolean hasAccess);
    }

    public static void tableNameResult(TableName tn) {
        FirebaseRemoteConfig table = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings STable = new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build();
        table.setConfigSettingsAsync(STable);
        table.setDefaultsAsync(R.xml.remote_config_defaults);

        table.fetchAndActivate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                boolean table_name_result = false;
                if (task.isSuccessful()) {
                    String temp_table, temp_table_name = "", row = "", col = "";
                    String t = table.getString("row");
                    if (!t.isEmpty()) {
                        String[] row_col = t.split(",");
                        row = row_col[0];
                        col = row_col[1];
                    }
                    temp_table = table.getString(LpmAdapter.tableRef(row));
                    if (!temp_table.isEmpty()) {
                        temp_table_name = LpmAdapter.tableRef(col);
                        if (temp_table.equals(temp_table_name)) {
                            table_name_result = true;
                        }
                    }

                    tn.onResult(table_name_result);
                }
            }
        });
    }
}
