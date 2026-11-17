package com.example.quanlycaycanh;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

// Android 15 trở lên luôn vẽ nội dung tràn xuống dưới thanh trạng thái và
// thanh điều hướng, nên mọi màn phải tự chừa chỗ.
// Xử lý một lần ở đây để các màn hình không phải lặp lại.
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Vẽ tràn viền để nền các màn liền mạch, phần chừa chỗ do padContentRoot lo
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    @Override
    public void setContentView(int layoutResID) {

        super.setContentView(layoutResID);

        padContentRoot();
    }

    @Override
    public void setContentView(View view) {

        super.setContentView(view);

        padContentRoot();
    }

    // Đẩy layout gốc xuống dưới thanh hệ thống, giữ nguyên padding sẵn có
    private void padContentRoot() {

        ViewGroup content = findViewById(android.R.id.content);

        if (content == null || content.getChildCount() == 0) {

            return;
        }

        View root = content.getChildAt(0);

        int start = root.getPaddingLeft();
        int top = root.getPaddingTop();
        int end = root.getPaddingRight();
        int bottom = root.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {

            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    start + bars.left,
                    top + bars.top,
                    end + bars.right,
                    bottom + bars.bottom
            );

            return insets;
        });

        // Nền ứng dụng là màu sáng nên icon thanh hệ thống phải màu tối
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), root);

        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        ViewCompat.requestApplyInsets(root);
    }
}
