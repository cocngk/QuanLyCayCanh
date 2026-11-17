package com.example.quanlycaycanh;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.PopupMenu;

// Nút menu trên thanh tiêu đề: lối tắt sang tài khoản và đăng xuất.
final class HeaderMenu {

    private HeaderMenu() {
    }

    static void attach(Activity activity, View button) {

        if (button == null) {

            return;
        }

        button.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(activity, v);

            popup.getMenu().add("Quản lý tài khoản");
            popup.getMenu().add("Đăng xuất");

            popup.setOnMenuItemClickListener(item -> {

                if ("Đăng xuất".contentEquals(item.getTitle())) {

                    new SessionManager(activity).logout();

                    Intent intent = new Intent(activity, Login.class);

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );

                    activity.startActivity(intent);

                    return true;
                }

                activity.startActivity(
                        new Intent(activity, Account.class)
                );

                return true;
            });

            popup.show();
        });
    }
}
