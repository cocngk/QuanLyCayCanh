package com.example.quanlycaycanh.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.ChiTietDonHang;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/** 5.5 — Giỏ hàng: đơn nháp trạng thái GIO, đặt hàng = đổi trạng thái. */
public class CartFragment extends Fragment implements CartAdapter.OnCartChange {

    private Repository repo;
    private Session session;
    private CartAdapter adapter;
    private RecyclerView rv;
    private LinearLayout khungTrong;
    private LinearLayout khungThanhToan;
    private TextView tvTamTinh;
    private TextView tvTongTien;
    private EditText edGhiChu;
    private long maDonNhap = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());

        rv = v.findViewById(R.id.rvGioHang);
        khungTrong = v.findViewById(R.id.khungTrong);
        khungThanhToan = v.findViewById(R.id.khungThanhToan);
        tvTamTinh = v.findViewById(R.id.tvTamTinh);
        tvTongTien = v.findViewById(R.id.tvTongTien);
        edGhiChu = v.findViewById(R.id.edGhiChu);

        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(this);
        rv.setAdapter(adapter);

        v.findViewById(R.id.btnTiepTucMua).setOnClickListener(x ->
                ((CustomerActivity) requireActivity()).chonTabSanPham());
        MaterialButton btnDat = v.findViewById(R.id.btnTienHanhDat);
        btnDat.setOnClickListener(x -> datHang());
    }

    @Override
    public void onResume() {
        super.onResume();
        napGio();
    }

    private void napGio() {
        if (!isAdded()) {
            return;
        }
        maDonNhap = repo.cartId(session.maTaiKhoan());
        List<ChiTietDonHang> ds = repo.cartLines(maDonNhap);
        adapter.submit(ds);
        boolean rong = ds.isEmpty();
        khungTrong.setVisibility(rong ? View.VISIBLE : View.GONE);
        rv.setVisibility(rong ? View.GONE : View.VISIBLE);
        khungThanhToan.setVisibility(rong ? View.GONE : View.VISIBLE);

        int soMon = 0;
        for (ChiTietDonHang d : ds) {
            soMon += d.soLuong;
        }
        double tamTinh = repo.linesTotal(maDonNhap);
        tvTamTinh.setText(soMon + " sản phẩm — Tạm tính");
        tvTongTien.setText(Ui.vnd(tamTinh));
    }

    @Override
    public void onSoLuong(ChiTietDonHang dong, int soLuongMoi) {
        repo.setLineQty(maDonNhap, dong.maSanPham, soLuongMoi);
        napGio();
    }

    @Override
    public void onXoa(ChiTietDonHang dong) {
        new AlertDialog.Builder(requireContext())
                .setMessage("Xoá \"" + dong.tenCay + "\" khỏi giỏ hàng?")
                .setNegativeButton("Giữ lại", null)
                .setPositiveButton("Xoá", (d, w) -> {
                    repo.removeLine(maDonNhap, dong.maSanPham);
                    napGio();
                })
                .show();
    }

    private void datHang() {
        TaiKhoan tk = repo.user(session.maTaiKhoan());
        String diaChi = tk == null || tk.diaChi == null ? "" : tk.diaChi.trim();
        if (TextUtils.isEmpty(diaChi)) {
            hoiDiaChi();
            return;
        }
        chonThanhToan(diaChi);
    }

    private void hoiDiaChi() {
        EditText ed = new EditText(requireContext());
        ed.setHint("Số nhà, đường, quận, tỉnh/thành");
        ed.setPadding(Ui.dp(requireContext(), 20), Ui.dp(requireContext(), 12),
                Ui.dp(requireContext(), 20), Ui.dp(requireContext(), 12));
        new AlertDialog.Builder(requireContext())
                .setTitle("Địa chỉ giao hàng")
                .setView(ed)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Tiếp tục", (d, w) -> {
                    String diaChi = ed.getText().toString().trim();
                    if (TextUtils.isEmpty(diaChi)) {
                        Ui.toast(requireContext(), "Chưa có địa chỉ giao hàng");
                        return;
                    }
                    TaiKhoan tk = repo.user(session.maTaiKhoan());
                    if (tk != null) {
                        tk.diaChi = diaChi;
                        repo.updateProfile(tk);
                    }
                    chonThanhToan(diaChi);
                })
                .show();
    }

    private void chonThanhToan(String diaChi) {
        String[] cachThuc = {"Thanh toán khi nhận hàng (COD)", "Chuyển khoản ngân hàng"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Phương thức thanh toán")
                .setItems(cachThuc, (d, which) -> {
                    String thanhToan = which == 0 ? "COD" : "Chuyển khoản";
                    String ghiChu = edGhiChu.getText() == null ? "" : edGhiChu.getText().toString().trim();
                    long maDon = repo.placeOrder(session.maTaiKhoan(), diaChi, ghiChu, thanhToan);
                    if (maDon == -1) {
                        Ui.toast(requireContext(), "Giỏ hàng đang trống");
                        return;
                    }
                    if (maDon == -2) {
                        Ui.toast(requireContext(), "Có sản phẩm vượt quá tồn kho, vui lòng giảm số lượng");
                        return;
                    }
                    edGhiChu.setText("");
                    napGio();
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Đặt hàng thành công")
                            .setMessage("Mã đơn #" + maDon + "\nPhí vận chuyển "
                                    + Ui.vnd(Repository.PHI_VAN_CHUYEN) + " đã được cộng vào tổng tiền.")
                            .setPositiveButton("Xem đơn hàng", (dd, w) ->
                                    ((CustomerActivity) requireActivity()).chonTabDonHang())
                            .show();
                })
                .show();
    }
}
