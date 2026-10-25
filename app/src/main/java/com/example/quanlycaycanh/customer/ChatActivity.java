package com.example.quanlycaycanh.customer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TinNhan;

import java.util.List;

/** 5.9 — Trò chuyện: dùng chung cho khách hàng và quản trị viên. */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_KHACH = "ma_khach";
    public static final String EXTRA_CUOC_TRO_CHUYEN = "ma_cuoc_tro_chuyen";

    private static final long CHU_KY_LAM_MOI = 2500;

    private Repository repo;
    private Session session;
    private MessageAdapter adapter;
    private RecyclerView rv;
    private EditText edTinNhan;
    private long maCuocTroChuyen;
    private boolean admin;
    private int maNguoiDung;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable lamMoi = new Runnable() {
        @Override
        public void run() {
            napTinNhan(false);
            handler.postDelayed(this, CHU_KY_LAM_MOI);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Ui.anToan(this);

        repo = new Repository(this);
        session = new Session(this);
        maNguoiDung = session.maTaiKhoan();

        long maCuoc = getIntent().getLongExtra(EXTRA_CUOC_TRO_CHUYEN, -1);
        if (maCuoc > 0) {
            admin = true;
            maCuocTroChuyen = maCuoc;
        } else {
            int maKhach = getIntent().getIntExtra(EXTRA_KHACH, maNguoiDung);
            maCuocTroChuyen = repo.conversationForCustomer(maKhach);
        }

        TextView tvTieuDe = findViewById(R.id.tvTieuDe);
        TextView tvMoTa = findViewById(R.id.tvMoTa);
        rv = findViewById(R.id.rvTinNhan);
        edTinNhan = findViewById(R.id.edTinNhan);

        if (admin) {
            tvTieuDe.setText(repo.tenKhachCuaCuocTroChuyen(maCuocTroChuyen));
            tvMoTa.setText("Khách hàng · phản hồi tin nhắn");
            tvMoTa.setTextColor(getColor(R.color.muted));
        } else {
            tvTieuDe.setText("Cửa hàng Cây Cảnh");
            tvMoTa.setText("Đang hoạt động");
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter();
        rv.setAdapter(adapter);

        findViewById(R.id.btnQuayLai).setOnClickListener(v -> finish());
        ImageButton btnGui = findViewById(R.id.btnGui);
        btnGui.setOnClickListener(v -> gui());
        edTinNhan.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                gui();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        napTinNhan(true);
        handler.postDelayed(lamMoi, CHU_KY_LAM_MOI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(lamMoi);
    }

    private void gui() {
        String noiDung = edTinNhan.getText() == null ? "" : edTinNhan.getText().toString().trim();
        if (TextUtils.isEmpty(noiDung)) {
            return;
        }
        repo.sendMessage(maCuocTroChuyen, maNguoiDung, noiDung);
        edTinNhan.setText("");
        napTinNhan(true);
    }

    private void napTinNhan(boolean cuonXuong) {
        List<TinNhan> ds = repo.messages(maCuocTroChuyen);
        adapter.submit(ds, maNguoiDung);
        if (cuonXuong && !ds.isEmpty()) {
            rv.post(() -> rv.scrollToPosition(ds.size() - 1));
        }
    }
}
