package com.example.quanlycaycanh.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Phiên đăng nhập, lưu MaTaiKhoan trong SharedPreferences. */
public class Session {

    private static final String PREF = "phien_dang_nhap";
    private static final String KEY_MA_TK = "ma_tai_khoan";
    private static final String KEY_GHI_NHO = "ghi_nho";

    private final SharedPreferences prefs;

    public Session(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void dangNhap(int maTaiKhoan, boolean ghiNho) {
        prefs.edit().putInt(KEY_MA_TK, maTaiKhoan).putBoolean(KEY_GHI_NHO, ghiNho).apply();
    }

    public boolean daDangNhap() {
        return prefs.getInt(KEY_MA_TK, -1) > 0;
    }

    public int maTaiKhoan() {
        return prefs.getInt(KEY_MA_TK, -1);
    }

    public boolean ghiNho() {
        return prefs.getBoolean(KEY_GHI_NHO, false);
    }

    public void dangXuat() {
        prefs.edit().clear().apply();
    }
}
