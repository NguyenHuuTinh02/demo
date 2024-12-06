package com.examples.yumbox.Utils;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatString {
    public static String formatAmount(int amount) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount).replace(",", ".") + "đ";
    }
}
