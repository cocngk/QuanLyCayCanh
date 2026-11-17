package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.PlantPhotos;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.LoaiCay;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/** Thêm, sửa sản phẩm và cập nhật tồn kho. */
public class ProductEditActivity extends AppCompatActivity {

    public static final String EXTRA_MA_SAN_PHAM = "ma_san_pham";

    private Repository repo;
    private SanPham sanPham;
    private List<LoaiCay> loaiCays;
    private Spinner spLoai;
    private TextInputEditText edTen;
    private TextInputEditText edGia;
    private TextInputEditText edTon;
    private TextInputEditText edMoTa;
    private TextInputEditText edChamSoc;
    private MaterialSwitch swDangBan;
    private ImageView imgAnhCay;
    private TextView tvPhotoFallback;
    private TextView tvNguonAnh;
    private String hinhAnh = "";
    private final ActivityResultLauncher<String[]> chonAnh =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Một số nhà cung cấp ảnh chỉ cấp quyền đọc tạm thời.
                }
                hinhAnh = uri.toString();
                hienAnh();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);
        Ui.anToan(this);

        repo = new Repository(this);
        loaiCays = repo.categories();

        ImageButton btnQuayLai = findViewById(R.id.btnQuayLai);
        btnQuayLai.setOnClickListener(v -> finish());

        TextView tvTieuDe = findViewById(R.id.tvTieuDe);
        spLoai = findViewById(R.id.spLoai);
        edTen = findViewById(R.id.edTen);
        imgAnhCay = findViewById(R.id.imgAnhCay);
        tvPhotoFallback = findViewById(R.id.tvPhotoFallback);
        tvNguonAnh = findViewById(R.id.tvNguonAnh);
        edGia = findViewById(R.id.edGia);
        edTon = findViewById(R.id.edTon);
        edMoTa = findViewById(R.id.edMoTa);
        edChamSoc = findViewById(R.id.edChamSoc);
        swDangBan = findViewById(R.id.swDangBan);

        List<String> tenLoai = new ArrayList<>();
        for (LoaiCay l : loaiCays) {
            tenLoai.add(l.tenLoai);
        }
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, tenLoai);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLoai.setAdapter(spAdapter);
        findViewById(R.id.btnChonAnh).setOnClickListener(v -> chonAnh.launch(new String[]{"image/*"}));

        int maSanPham = getIntent().getIntExtra(EXTRA_MA_SAN_PHAM, 0);
        if (maSanPham > 0) {
            sanPham = repo.product(maSanPham);
            tvTieuDe.setText("Sửa sản phẩm");
            findViewById(R.id.btnNgungBan).setVisibility(View.VISIBLE);
            if (sanPham != null) {
                edTen.setText(sanPham.tenCay);
                hinhAnh = sanPham.hinhAnh == null ? "" : sanPham.hinhAnh;
                hienAnh();
                edGia.setText(String.valueOf((long) sanPham.giaBan));
                edTon.setText(String.valueOf(sanPham.soLuongTon));
                edMoTa.setText(sanPham.moTa);
                edChamSoc.setText(sanPham.huongDanChamSoc);
                swDangBan.setChecked(sanPham.dangBan);
                for (int i = 0; i < loaiCays.size(); i++) {
                    if (loaiCays.get(i).maLoai == sanPham.maLoai) {
                        spLoai.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            hinhAnh = "";
            hienAnh();
        }

        findViewById(R.id.btnLuu).setOnClickListener(v -> luu());
        findViewById(R.id.btnNgungBan).setOnClickListener(v -> xoa());
    }

    private void luu() {
        String ten = text(edTen);
        if (TextUtils.isEmpty(ten)) {
            Ui.toast(this, "Nhập tên cây");
            return;
        }
        double gia = so(text(edGia));
        if (gia <= 0) {
            Ui.toast(this, "Giá bán phải lớn hơn 0");
            return;
        }
        SanPham p = sanPham == null ? new SanPham() : sanPham;
        p.tenCay = ten;
        p.maLoai = loaiCays.get(spLoai.getSelectedItemPosition()).maLoai;
        p.hinhAnh = TextUtils.isEmpty(hinhAnh) ? null : hinhAnh;
        p.giaBan = gia;
        p.soLuongTon = (int) so(text(edTon));
        p.moTa = text(edMoTa);
        p.huongDanChamSoc = text(edChamSoc);
        p.dangBan = swDangBan.isChecked();
        long id = repo.saveProduct(p);
        Ui.toast(this, id > 0 ? "Đã lưu sản phẩm" : "Không lưu được sản phẩm");
        if (id > 0) {
            finish();
        }
    }

    private void xoa() {
        if (sanPham == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Xoá / ngừng bán")
                .setMessage("Xoá \"" + sanPham.tenCay + "\"? Sản phẩm đã có trong đơn hàng sẽ chỉ bị "
                        + "chuyển sang Ngừng bán.")
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Xoá", (d, w) -> {
                    boolean daXoa = repo.deleteOrHideProduct(sanPham.maSanPham);
                    Ui.toast(this, daXoa ? "Đã xoá sản phẩm" : "Đã chuyển sang Ngừng bán");
                    finish();
                })
                .show();
    }

    private static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    private void hienAnh() {
        int maLoai = sanPham == null ? 0 : sanPham.maLoai;
        boolean coAnh = Ui.hienAnhCay(this, hinhAnh, imgAnhCay, tvPhotoFallback, maLoai);
        if (!coAnh) {
            tvPhotoFallback.setText("Chọn ảnh cây cảnh");
            tvNguonAnh.setVisibility(View.GONE);
            tvNguonAnh.setOnClickListener(null);
            return;
        }

        PlantPhotos.Credit credit = PlantPhotos.credit(hinhAnh);
        tvNguonAnh.setVisibility(View.VISIBLE);
        if (credit == null) {
            tvNguonAnh.setText("Ảnh do bạn chọn");
            tvNguonAnh.setOnClickListener(null);
            return;
        }
        tvNguonAnh.setText("Ảnh: " + credit.author + " · " + credit.license
                + " · Wikimedia Commons (nhấn để xem nguồn và giấy phép)");
        tvNguonAnh.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(credit.sourceUrl)));
            } catch (RuntimeException ignored) {
                Ui.toast(this, "Không mở được trang nguồn ảnh");
            }
        });
    }

    private static double so(String s) {
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
