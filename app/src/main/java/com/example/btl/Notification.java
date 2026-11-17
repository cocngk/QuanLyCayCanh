package com.example.btl;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Notification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_notification);

        // =====================================
        // NÚT QUAY LẠI
        // =====================================

        findViewById(R.id.btnBack).setOnClickListener(v -> {

            finish();
        });

        // =====================================
        // HIỂN THỊ TRẠNG THÁI CHƯA CÓ THÔNG BÁO
        // =====================================

        findViewById(R.id.Empty)
                .setVisibility(View.VISIBLE);

        findViewById(R.id.layoutNotifications)
                .setVisibility(View.GONE);

        // =====================================
        // NÚT ĐÁNH DẤU ĐÃ ĐỌC
        // =====================================

        findViewById(R.id.btnRead).setOnClickListener(v -> {

            Toast.makeText(
                    Notification.this,
                    "Hiện chưa có thông báo để đánh dấu đã đọc",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}