package com.example.quanlycaycanh.customer;

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
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.Map;

/** Khung giao diện khách hàng: 5 mục theo sitemap nhánh Khách hàng. */
public class CustomerActivity extends AppCompatActivity {

    private static final String EXTRA_TAB = "tab";
    public static final String TAB_CART = "gio_hang";
    public static final String TAB_ORDERS = "don_hang";

    private final Map<Integer, Fragment> fragments = new HashMap<>();
    private BottomNavigationView bottomNav;
    private Repository repo;
    private Session session;

    public static void moGioHang(Context context) {
        context.startActivity(new Intent(context, CustomerActivity.class)
                .putExtra(EXTRA_TAB, TAB_CART)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = new Repository(this);
        session = new Session(this);
        if (!session.daDangNhap() || repo.user(session.maTaiKhoan()) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_customer);
        Ui.anToan(this);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            hienThi(item.getItemId());
            return true;
        });
        int startTab = R.id.tab_home;
        if (getIntent() != null && TAB_CART.equals(getIntent().getStringExtra(EXTRA_TAB))) {
            startTab = R.id.tab_cart;
        } else if (getIntent() != null && TAB_ORDERS.equals(getIntent().getStringExtra(EXTRA_TAB))) {
            startTab = R.id.tab_orders;
        }
        bottomNav.setSelectedItemId(startTab);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.tab_home) {
                    bottomNav.setSelectedItemId(R.id.tab_home);
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
        if (intent != null && TAB_CART.equals(intent.getStringExtra(EXTRA_TAB))) {
            bottomNav.setSelectedItemId(R.id.tab_cart);
        } else if (intent != null && TAB_ORDERS.equals(intent.getStringExtra(EXTRA_TAB))) {
            bottomNav.setSelectedItemId(R.id.tab_orders);
        }
    }

    public void chonTabSanPham() {
        bottomNav.setSelectedItemId(R.id.tab_products);
    }

    public void chonTabGioHang() {
        bottomNav.setSelectedItemId(R.id.tab_cart);
    }

    public void chonTabDonHang() {
        bottomNav.setSelectedItemId(R.id.tab_orders);
    }

    /** Danh mục được chọn từ Trang chủ, ProductListFragment đọc khi mở lại tab. */
    public int locChoLoai = 0;

    public TaiKhoan nguoiDung() {
        return repo.user(session.maTaiKhoan());
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
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(e.getValue())
                    .commitAllowingStateLoss();
        }
        getSupportFragmentManager().beginTransaction().show(f).commitAllowingStateLoss();
    }

    @NonNull
    private Fragment taoFragment(int itemId) {
        if (itemId == R.id.tab_products) {
            return new ProductListFragment();
        }
        if (itemId == R.id.tab_orders) {
            return new OrderListFragment();
        }
        if (itemId == R.id.tab_cart) {
            return new CartFragment();
        }
        if (itemId == R.id.tab_account) {
            return new AccountFragment();
        }
        return new HomeFragment();
    }
}
