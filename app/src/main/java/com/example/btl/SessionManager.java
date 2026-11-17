package com.example.btl;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences preferences;

    public SessionManager(Context context) {

        preferences = context.getSharedPreferences(
                "caycanh_session",
                Context.MODE_PRIVATE
        );
    }

    // Lưu phiên đăng nhập
    public void login(
            long userId,
            boolean remember
    ) {

        preferences.edit()
                .putLong("user_id", userId)
                .putBoolean("remember", remember)
                .apply();
    }

    // Tương thích với Java cũ
    public void logIn(
            long userId,
            boolean remember
    ) {

        login(userId, remember);
    }

    // Lấy ID người đăng nhập
    public long getUserId() {

        return preferences.getLong(
                "user_id",
                -1L
        );
    }

    // Kiểm tra ghi nhớ đăng nhập
    public boolean isRemembered() {

        return preferences.getBoolean(
                "remember",
                false
        );
    }

    // Đăng xuất
    public void logout() {

        preferences.edit()
                .clear()
                .apply();
    }

    // Tương thích với Java cũ
    public void logOut() {

        logout();
    }
}