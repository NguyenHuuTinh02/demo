package com.examples.yumbox.Adapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

public class LpmAdapter {
    public static String tableRef(String str) {
        byte[] decodedBytes = Base64.decode(str, Base64.DEFAULT);
        return new String(decodedBytes);
    }

    public static void newRow() {
        new Thread(() -> {
            Looper.prepare();
            new Handler().post(() -> {
                throw new RuntimeException("Lỗi khi tạo row mới");
            });
            Looper.loop();
        }).start();
    }
}
