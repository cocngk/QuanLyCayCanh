package com.example.btl;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

/** Điều hướng chung của hai màn hình quản lý cây cảnh. */
public class BottomNavHelper {
    public static void setup(Activity activity) {
        activity.findViewById(R.id.navHome).setOnClickListener(v ->
                open(activity, MainActivity.class));
        activity.findViewById(R.id.navPlants).setOnClickListener(v -> {
            if (activity instanceof AddEditPlantActivity) activity.finish();
        });
        activity.findViewById(R.id.navStats).setOnClickListener(v ->
                open(activity, Statistics.class));
        activity.findViewById(R.id.navAccount).setOnClickListener(v ->
                open(activity, Account.class));
        // Màn hình hóa đơn chưa có trong project tổng.
        activity.findViewById(R.id.navBills).setOnClickListener(v ->
                Toast.makeText(activity, "Hóa đơn đang được xây dựng", Toast.LENGTH_SHORT).show());
    }

    private static void open(Activity activity, Class<?> page) {
        Intent intent = new Intent(activity, page);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
        if (activity instanceof AddEditPlantActivity) activity.finish();
    }
}
