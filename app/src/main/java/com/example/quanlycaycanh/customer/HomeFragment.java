package com.example.quanlycaycanh.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.LoaiCay;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;
import com.example.quanlycaycanh.data.Session;
import com.example.quanlycaycanh.data.TaiKhoan;

import java.util.List;

/** 5.2 — Trang chủ khách hàng. */
public class HomeFragment extends Fragment {

    private static final String[] EMOJI_LOAI = {"🪴", "🪷", "🍀", "🌳"};

    private Repository repo;
    private Session session;
    private ProductAdapter adapterNoiBat;
    private ProductAdapter adapterBanChay;
    private LinearLayout hang1;
    private LinearLayout hang2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        session = new Session(requireContext());

        TaiKhoan tk = repo.user(session.maTaiKhoan());
        TextView tvChao = v.findViewById(R.id.tvChao);
        if (tk != null) {
            String ten = tk.hoTen == null ? "bạn" : tk.hoTen.trim();
            int space = ten.lastIndexOf(' ');
            if (space > 0) {
                ten = ten.substring(space + 1);
            }
            tvChao.setText("Xin chào, " + ten + " 👋");
        }

        ImageButton btnChat = v.findViewById(R.id.btnChat);
        ImageButton btnGioHang = v.findViewById(R.id.btnGioHang);
        TextView oTimKiem = v.findViewById(R.id.oTimKiem);
        TextView tvXemTatCa = v.findViewById(R.id.tvXemTatCa);
        View banner = v.findViewById(R.id.banner);
        View btnXemBoSuuTap = v.findViewById(R.id.btnXemBoSuuTap);

        btnChat.setOnClickListener(view -> {
            Intent i = new Intent(requireContext(), ChatActivity.class);
            i.putExtra(ChatActivity.EXTRA_KHACH, session.maTaiKhoan());
            startActivity(i);
        });
        btnGioHang.setOnClickListener(view -> ((CustomerActivity) requireActivity()).chonTabGioHang());
        View.OnClickListener moSanPham = view -> ((CustomerActivity) requireActivity()).chonTabSanPham();
        oTimKiem.setOnClickListener(moSanPham);
        tvXemTatCa.setOnClickListener(moSanPham);
        banner.setOnClickListener(moSanPham);
        btnXemBoSuuTap.setOnClickListener(moSanPham);

        hang1 = v.findViewById(R.id.hangDanhMuc1);
        hang2 = v.findViewById(R.id.hangDanhMuc2);

        RecyclerView rvNoiBat = v.findViewById(R.id.rvNoiBat);
        rvNoiBat.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvNoiBat.setNestedScrollingEnabled(false);
        adapterNoiBat = new ProductAdapter(false, new ProductAdapter.OnProductClick() {
            @Override
            public void onOpen(SanPham sanPham) {
                moChiTiet(sanPham.maSanPham);
            }

            @Override
            public void onAdd(SanPham sanPham) {
                themVaoGio(sanPham);
            }
        });
        rvNoiBat.setAdapter(adapterNoiBat);

        RecyclerView rvBanChay = v.findViewById(R.id.rvBanChay);
        rvBanChay.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.HORIZONTAL, false));
        rvBanChay.setNestedScrollingEnabled(false);
        adapterBanChay = new ProductAdapter(true, new ProductAdapter.OnProductClick() {
            @Override
            public void onOpen(SanPham sanPham) {
                moChiTiet(sanPham.maSanPham);
            }

            @Override
            public void onAdd(SanPham sanPham) {
                themVaoGio(sanPham);
            }
        });
        rvBanChay.setAdapter(adapterBanChay);

        dungDanhMuc();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (repo == null) {
            return;
        }
        adapterNoiBat.submit(repo.featured(6));
        adapterBanChay.submit(repo.bestSelling(6));
    }

    private void dungDanhMuc() {
        hang1.removeAllViews();
        hang2.removeAllViews();
        List<LoaiCay> ds = repo.categories();
        for (int i = 0; i < ds.size() && i < 4; i++) {
            LoaiCay loai = ds.get(i);
            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category, i < 2 ? hang1 : hang2, false);
            TextView emoji = item.findViewById(R.id.tvEmoji);
            TextView ten = item.findViewById(R.id.tvTen);
            emoji.setText(EMOJI_LOAI[i % EMOJI_LOAI.length]);
            ten.setText(loai.tenLoai);
            item.setBackground(Ui.boTron(requireContext(), Ui.nenDanhMuc(requireContext(), loai.maLoai), 16));
            item.setOnClickListener(v -> {
                CustomerActivity a = (CustomerActivity) requireActivity();
                a.locChoLoai = loai.maLoai;
                a.chonTabSanPham();
            });
            (i < 2 ? hang1 : hang2).addView(item);
        }
    }

    private void moChiTiet(int maSanPham) {
        startActivity(new Intent(requireContext(), ProductDetailActivity.class)
                .putExtra(ProductDetailActivity.EXTRA_MA_SAN_PHAM, maSanPham));
    }

    private void themVaoGio(SanPham p) {
        if (p.soLuongTon <= 0) {
            Ui.toast(requireContext(), "Sản phẩm đã hết hàng");
            return;
        }
        repo.addToCart(session.maTaiKhoan(), p.maSanPham, 1);
        Ui.toast(requireContext(), "Đã thêm " + p.tenCay + " vào giỏ");
    }
}
