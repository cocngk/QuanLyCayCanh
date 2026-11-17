package com.example.quanlycaycanh;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Trang chủ tạm: lối vào các màn hình đã làm.
// Trang chủ chính thức là phần của Người 2.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.btnTaoHoaDon).setOnClickListener(v ->
                startActivity(new Intent(this, TaoHoaDonActivity.class))
        );

        findViewById(R.id.btnQuanLyHoaDon).setOnClickListener(v ->
                startActivity(new Intent(this, QuanLyHoaDonActivity.class))
        );

        findViewById(R.id.btnAccount).setOnClickListener(v ->
                startActivity(new Intent(this, Account.class))
        );
    }
}
