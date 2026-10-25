package com.example.quanlycaycanh.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

/** Khung giao diện quản trị: 5 mục theo sitemap nhánh Quản trị viên. */
public class AdminActivity extends AppCompatActivity {

    private static final String EXTRA_TAB = "tab";
    public static final String TAB_ORDERS = "don_hang";
    public static final String TAB_PRODUCTS = "san_pham";

    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private BottomNavigationView bottomNav;

    public static void moDonHang(Context context) {
        context.startActivity(new Intent(context, AdminActivity.class)
                .putExtra(EXTRA_TAB, TAB_ORDERS)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    public static void moSanPham(Context context) {
        context.startActivity(new Intent(context, AdminActivity.class)
                .putExtra(EXTRA_TAB, TAB_PRODUCTS)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Repository repo = new Repository(this);
        Session session = new Session(this);
        if (session.maTaiKhoan() <= 0 || repo.user(session.maTaiKhoan()) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_admin);
        Ui.anToan(this);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            hienThi(item.getItemId());
            return true;
        });
        int startTab = R.id.tab_dashboard;
        if (getIntent() != null && TAB_ORDERS.equals(getIntent().getStringExtra(EXTRA_TAB))) {
            startTab = R.id.tab_admin_orders;
        } else if (getIntent() != null && TAB_PRODUCTS.equals(getIntent().getStringExtra(EXTRA_TAB))) {
            startTab = R.id.tab_admin_products;
        }
        bottomNav.setSelectedItemId(startTab);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.tab_dashboard) {
                    bottomNav.setSelectedItemId(R.id.tab_dashboard);
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && TAB_ORDERS.equals(intent.getStringExtra(EXTRA_TAB))) {
            bottomNav.setSelectedItemId(R.id.tab_admin_orders);
        } else if (intent != null && TAB_PRODUCTS.equals(intent.getStringExtra(EXTRA_TAB))) {
            bottomNav.setSelectedItemId(R.id.tab_admin_products);
        }
    }

    public void chonTabDonHang() {
        bottomNav.setSelectedItemId(R.id.tab_admin_orders);
    }

    private void hienThi(int itemId) {
        Fragment f = fragments.get(itemId);
        if (f == null) {
            f = taoFragment(itemId);
            fragments.put(itemId, f);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.khungNoiDung, f, String.valueOf(itemId))
                    .commitNow();
        }
        for (Map.Entry<Integer, Fragment> e : fragments.entrySet()) {
            getSupportFragmentManager().beginTransaction().hide(e.getValue()).commitAllowingStateLoss();
        }
        getSupportFragmentManager().beginTransaction().show(f).commitAllowingStateLoss();
    }

    @NonNull
    private Fragment taoFragment(int itemId) {
        if (itemId == R.id.tab_admin_products) {
            return new AdminProductFragment();
        }
        if (itemId == R.id.tab_admin_orders) {
            return new AdminOrderFragment();
        }
        if (itemId == R.id.tab_stats) {
            return new StatsFragment();
        }
        if (itemId == R.id.tab_admin_chat) {
            return new AdminChatFragment();
        }
        return new AdminHomeFragment();
    }
}
