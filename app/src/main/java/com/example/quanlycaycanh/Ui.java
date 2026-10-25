package com.example.quanlycaycanh;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlycaycanh.data.DonHang;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Tiện ích hiển thị dùng chung: tiền Việt, nhãn trạng thái, màu danh mục. */
public final class Ui {

    private static final DecimalFormat MONEY = new DecimalFormat("#,###",
            DecimalFormatSymbols.getInstance(new Locale("vi", "VN")));

    private Ui() {
    }

    public static String vnd(double soTien) {
        return MONEY.format(soTien) + " ₫";
    }

    public static String soLuong(int n) {
        return MONEY.format(n);
    }

    /** Chừa chỗ cho thanh trạng thái, thanh điều hướng và bàn phím (edge-to-edge). */
    public static void anToan(Activity activity) {
        View root = activity.findViewById(android.R.id.content);
        if (root == null) {
            return;
        }
        final int pl = root.getPaddingLeft();
        final int pt = root.getPaddingTop();
        final int pr = root.getPaddingRight();
        final int pb = root.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.ime());
            v.setPadding(pl + bars.left, pt + bars.top, pr + bars.right, pb + bars.bottom);
            return insets;
        });
    }

    public static void toast(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    public static int dp(Context c, int value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    public static String nhanTrangThai(String trangThai) {
        if (trangThai == null) {
            return "";
        }
        switch (trangThai) {
            case DonHang.GIO:
                return "Giỏ nháp";
            case DonHang.CHO_XAC_NHAN:
                return "Chờ xác nhận";
            case DonHang.DANG_GIAO:
                return "Đang giao";
            case DonHang.DA_GIAO:
                return "Đã giao";
            case DonHang.DA_HUY:
                return "Đã huỷ";
            default:
                return trangThai;
        }
    }

    public static int mauTrangThai(Context c, String trangThai) {
        switch (trangThai == null ? "" : trangThai) {
            case DonHang.CHO_XAC_NHAN:
                return ContextCompat.getColor(c, R.color.amber);
            case DonHang.DANG_GIAO:
                return ContextCompat.getColor(c, R.color.blue);
            case DonHang.DA_GIAO:
                return ContextCompat.getColor(c, R.color.green);
            case DonHang.DA_HUY:
                return ContextCompat.getColor(c, R.color.red);
            default:
                return ContextCompat.getColor(c, R.color.muted);
        }
    }

    public static int nenTrangThai(Context c, String trangThai) {
        switch (trangThai == null ? "" : trangThai) {
            case DonHang.CHO_XAC_NHAN:
                return ContextCompat.getColor(c, R.color.amber_light);
            case DonHang.DANG_GIAO:
                return ContextCompat.getColor(c, R.color.blue_light);
            case DonHang.DA_GIAO:
                return ContextCompat.getColor(c, R.color.green_light);
            case DonHang.DA_HUY:
                return ContextCompat.getColor(c, R.color.red_light);
            default:
                return ContextCompat.getColor(c, R.color.brown_light);
        }
    }

    public static void ganNhanTrangThai(Context c, TextView tv, String trangThai) {
        tv.setText(nhanTrangThai(trangThai));
        tv.setTextColor(mauTrangThai(c, trangThai));
        tv.setBackground(pill(c, nenTrangThai(c, trangThai)));
    }

    public static GradientDrawable boTron(Context c, int mauNen, int banKinhDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(c, banKinhDp));
        d.setColor(mauNen);
        return d;
    }

    public static GradientDrawable pill(Context c, int mauNen) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setCornerRadius(dp(c, 20));
        d.setColor(mauNen);
        return d;
    }

    public static int nenDanhMuc(Context c, int maLoai) {
        switch (maLoai) {
            case 1:
                return ContextCompat.getColor(c, R.color.cat_desk);
            case 2:
                return ContextCompat.getColor(c, R.color.cat_indoor);
            case 3:
                return ContextCompat.getColor(c, R.color.cat_feng);
            default:
                return ContextCompat.getColor(c, R.color.cat_outdoor);
        }
    }

    public static void an(View v) {
        v.setVisibility(View.GONE);
    }

    public static void hien(View v) {
        v.setVisibility(View.VISIBLE);
    }
}
