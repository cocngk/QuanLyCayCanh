package com.example.btl;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class About extends AppCompatActivity {

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