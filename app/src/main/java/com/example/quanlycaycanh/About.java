package com.example.quanlycaycanh;

import android.os.Bundle;


public class About extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Hiển thị giao diện giới thiệu
        setContentView(R.layout.activity_about);

        // =====================================
        // NÚT QUAY LẠI
        // =====================================

        findViewById(R.id.btnBack).setOnClickListener(v -> {

            finish();
        });
    }
}