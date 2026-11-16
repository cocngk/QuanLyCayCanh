package com.example.quanlycaycanh.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.LoginActivity;
import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

/** Khung giao diện quản trị: 5 mục theo sitemap nhánh Quản trị viên. */
public class AdminActivity extends AppCompatActivity {

    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Repository repo = new Repository(this);
        Session session = new Session(this);
        TaiKhoan user = repo.user(session.maTaiKhoan());
        if (user == null) {
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
        bottomNav.setSelectedItemId(R.id.tab_dashboard);

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

    public void chonTabDonHang() {
        bottomNav.setSelectedItemId(R.id.tab_admin_orders);
    }

    public void chonTabTaiKhoan() {
        bottomNav.setSelectedItemId(R.id.tab_admin_account);
    }

    public void moTab(int itemId) {
        bottomNav.setSelectedItemId(itemId);
    }

    public void moTimSanPham(String query) {
        bottomNav.setSelectedItemId(R.id.tab_admin_products);
        Fragment productFragment = fragments.get(R.id.tab_admin_products);
        if (productFragment instanceof AdminProductFragment) {
            ((AdminProductFragment) productFragment).setSearchQuery(query);
        }
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
        if (itemId == R.id.tab_admin_account) {
            return new AdminAccountFragment();
        }
        return new AdminHomeFragment();
    }
}
