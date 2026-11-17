package com.example.quanlycaycanh;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Định dạng tiền và ngày giờ dùng chung cho các màn hình hóa đơn
final class Format {

    private static final DecimalFormat MONEY = createMoneyFormat();

    private static final SimpleDateFormat DATE_TIME =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    private static final SimpleDateFormat DAY =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private static final SimpleDateFormat MONTH =
            new SimpleDateFormat("MM/yyyy", Locale.getDefault());

    private Format() {
    }

    // 120000 -> "120.000 đ"
    static String money(long amount) {

        return MONEY.format(amount) + " đ";
    }

    // Mốc thời gian -> "16/11/2026 10:05"
    static String dateTime(long millis) {

        return DATE_TIME.format(new Date(millis));
    }

    // Mốc thời gian -> "16/11/2026", dùng để lọc theo ngày
    static String day(long millis) {

        return DAY.format(new Date(millis));
    }

    // Mốc thời gian -> "11/2026", dùng để lọc theo tháng
    static String month(long millis) {

        return MONTH.format(new Date(millis));
    }

    private static DecimalFormat createMoneyFormat() {

        DecimalFormatSymbols symbols =
                new DecimalFormatSymbols(Locale.getDefault());

        symbols.setGroupingSeparator('.');

        return new DecimalFormat("#,###", symbols);
    }
}
