package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;

/** Thông tin phiên bản và các chức năng chính của ứng dụng. */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Ui.anToan(this);

        ImageButton back = findViewById(R.id.btnQuayLai);
        back.setOnClickListener(v -> finish());

        String version = "1.0";
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (info.versionName != null) {
                version = info.versionName;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
            // Keep the bundled fallback version.
        }
        ((TextView) findViewById(R.id.tvPhienBan)).setText("Phiên bản " + version);
        ((TextView) findViewById(R.id.tvThongTinUngDung)).setText(
                "Tên ứng dụng: Quản lý cửa hàng cây cảnh"
                        + "\nPhiên bản: " + version
                        + "\nNền tảng: Android");
    }
}
