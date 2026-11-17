package com.example.quanlycaycanh;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

// Thanh điều hướng dưới cùng, dùng chung cho các màn hình chính.
final class BottomNav {

    private BottomNav() {
    }

    static void bind(Activity activity) {

        open(activity, new int[]{R.id.navHome}, MainActivity.class);

        // Màn quản lý cây cảnh là phần của Người 3, chưa có trên nhánh này
        open(activity, new int[]{R.id.navPlants}, null);

        // Màn của Người 1 đặt tên tab hóa đơn là navOrders
        open(
                activity,
                new int[]{R.id.navBills, R.id.navOrders},
                QuanLyHoaDonActivity.class
        );

        open(activity, new int[]{R.id.navStats}, Statistics.class);
        open(activity, new int[]{R.id.navAccount}, Account.class);
    }

    private static void open(Activity activity, int[] ids, Class<?> target) {

        for (int id : ids) {

            View item = activity.findViewById(id);

            if (item == null) {

                continue;
            }

            item.setOnClickListener(v -> {

                if (target == null) {

                    Toast.makeText(
                            activity,
                            "Quản lý cây cảnh do Người 3 phụ trách",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (activity.getClass() == target) {

                    return;
                }

                activity.startActivity(new Intent(activity, target));
            });
        }
    }
}
