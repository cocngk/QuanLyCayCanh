package com.example.quanlycaycanh.admin;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.Ui;
import com.example.quanlycaycanh.data.Repository;
import com.example.quanlycaycanh.data.SanPham;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 5.8 — Thống kê doanh thu, sản phẩm đã bán, bán chạy, tồn kho. */
public class StatsFragment extends Fragment {

    private Repository repo;
    private EditText edTuNgay;
    private EditText edDenNgay;
    private LinearLayout khungBieuDo;
    private LinearLayout khungBanChay;
    private LinearLayout khungTonThap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        repo = new Repository(requireContext());
        edTuNgay = v.findViewById(R.id.edTuNgay);
        edDenNgay = v.findViewById(R.id.edDenNgay);
        khungBieuDo = v.findViewById(R.id.khungBieuDo);
        khungBanChay = v.findViewById(R.id.khungBanChay);
        khungTonThap = v.findViewById(R.id.khungTonThap);

        datNgay(edTuNgay, Repository.dayBefore(30));
        datNgay(edDenNgay, Repository.today());

        v.findViewById(R.id.btnLoc).setOnClickListener(x -> nap());
        v.findViewById(R.id.btn7Ngay).setOnClickListener(x -> {
            datNgay(edTuNgay, Repository.dayBefore(7));
            datNgay(edDenNgay, Repository.today());
            nap();
        });
        v.findViewById(R.id.btn30Ngay).setOnClickListener(x -> {
            datNgay(edTuNgay, Repository.dayBefore(30));
            datNgay(edDenNgay, Repository.today());
            nap();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        nap();
    }

    private void nap() {
        if (!isAdded()) {
            return;
        }
        String tu = docNgay(edTuNgay, Repository.dayBefore(30));
        String den = docNgay(edDenNgay, Repository.today());
        if (tu.compareTo(den) > 0) {
            String tmp = tu;
            tu = den;
            den = tmp;
            datNgay(edTuNgay, tu);
            datNgay(edDenNgay, den);
        }

        Repository.ThongKe tk = repo.thongKe(tu, den);
        ((TextView) requireView().findViewById(R.id.tvDoanhThu)).setText(Ui.vnd(tk.doanhThu));
        ((TextView) requireView().findViewById(R.id.tvSoDon)).setText(String.valueOf(tk.soDon));
        ((TextView) requireView().findViewById(R.id.tvSanPhamDaBan))
                .setText(String.valueOf(tk.sanPhamDaBan));
        ((TextView) requireView().findViewById(R.id.tvTonKho)).setText(String.valueOf(tk.tonKho));

        veBieuDo(repo.doanhThuTheoNgay(tu, den));
        veBanChay(repo.topProducts(tu, den, 5));
        veTonThap(repo.lowStock(Repository.NGUONG_TON_THAP));
    }

    private void datNgay(EditText ed, String iso) {
        ed.setText(Repository.dinhDangNgay(iso));
    }

    /** Nhận dd/MM/yyyy (hoặc yyyy-MM-dd) và trả về yyyy-MM-dd để truy vấn. */
    private String docNgay(EditText ed, String macDinh) {
        String s = text(ed).trim();
        String[] phan = s.split("/");
        if (phan.length == 3 && phan[2].length() == 4) {
            String ngay = phan[0].length() == 1 ? "0" + phan[0] : phan[0];
            String thang = phan[1].length() == 1 ? "0" + phan[1] : phan[1];
            return phan[2] + "-" + thang + "-" + ngay;
        }
        if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return s;
        }
        return macDinh;
    }

    private void veBieuDo(Map<String, Double> duLieu) {
        khungBieuDo.removeAllViews();
        Map<String, Double> coDoanhThu = new LinkedHashMap<>();
        double max = 0;
        for (Map.Entry<String, Double> e : duLieu.entrySet()) {
            if (e.getValue() != null && e.getValue() > 0) {
                coDoanhThu.put(e.getKey(), e.getValue());
                max = Math.max(max, e.getValue());
            }
        }
        if (coDoanhThu.isEmpty()) {
            khungBieuDo.addView(dongChu("Chưa có doanh thu trong khoảng này"));
            return;
        }
        for (Map.Entry<String, Double> e : coDoanhThu.entrySet()) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, Ui.dp(requireContext(), 2), 0, Ui.dp(requireContext(), 2));

            TextView ngay = new TextView(requireContext());
            ngay.setText(e.getKey().length() >= 10
                    ? e.getKey().substring(8, 10) + "/" + e.getKey().substring(5, 7) : e.getKey());
            ngay.setTextSize(10);
            ngay.setTextColor(requireContext().getColor(R.color.muted));
            ngay.setWidth(Ui.dp(requireContext(), 40));
            row.addView(ngay);

            LinearLayout thanh = new LinearLayout(requireContext());
            thanh.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams lpThanh = new LinearLayout.LayoutParams(
                    0, Ui.dp(requireContext(), 12), 1f);
            lpThanh.setMarginEnd(Ui.dp(requireContext(), 8));
            thanh.setLayoutParams(lpThanh);

            View day = new View(requireContext());
            float tiLe = (float) (e.getValue() / max);
            day.setBackground(Ui.boTron(requireContext(),
                    requireContext().getColor(R.color.brown), 6));
            thanh.addView(day, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.max(tiLe, 0.01f)));
            View trong = new View(requireContext());
            thanh.addView(trong, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1f - tiLe, 0.0001f)));
            row.addView(thanh);

            TextView giaTri = new TextView(requireContext());
            giaTri.setText(Ui.vnd(e.getValue()));
            giaTri.setTextSize(10);
            giaTri.setTextColor(requireContext().getColor(R.color.brown_dark));
            giaTri.setGravity(Gravity.END);
            giaTri.setWidth(Ui.dp(requireContext(), 86));
            row.addView(giaTri);

            khungBieuDo.addView(row);
        }
    }

    private void veBanChay(List<SanPham> ds) {
        khungBanChay.removeAllViews();
        if (ds.isEmpty()) {
            khungBanChay.addView(dongChu("Chưa có sản phẩm nào được bán trong khoảng này"));
            return;
        }
        int hang = 1;
        for (SanPham p : ds) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 4));

            TextView thu = new TextView(requireContext());
            thu.setText(hang++ + ".");
            thu.setTextSize(12);
            thu.setTextColor(requireContext().getColor(R.color.muted));
            thu.setWidth(Ui.dp(requireContext(), 22));
            row.addView(thu);

            TextView ten = new TextView(requireContext());
            ten.setText(p.hinhAnh + "  " + p.tenCay);
            ten.setTextSize(12);
            ten.setTextColor(requireContext().getColor(R.color.brown_dark));
            LinearLayout.LayoutParams lpTen = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            row.addView(ten, lpTen);

            TextView sl = new TextView(requireContext());
            sl.setText("đã bán " + p.soLuongDaBan);
            sl.setTextSize(11);
            sl.setTextColor(requireContext().getColor(R.color.green));
            row.addView(sl);

            khungBanChay.addView(row);
        }
    }

    private void veTonThap(List<SanPham> ds) {
        khungTonThap.removeAllViews();
        if (ds.isEmpty()) {
            khungTonThap.addView(dongChu("Tồn kho đang ở mức an toàn"));
            return;
        }
        for (SanPham p : ds) {
            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 4));

            TextView ten = new TextView(requireContext());
            ten.setText(p.hinhAnh + "  " + p.tenCay);
            ten.setTextSize(12);
            ten.setTextColor(requireContext().getColor(R.color.brown_dark));
            row.addView(ten, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView sl = new TextView(requireContext());
            sl.setText("còn " + p.soLuongTon);
            sl.setTextSize(11);
            sl.setTextColor(requireContext().getColor(R.color.amber));
            row.addView(sl);

            khungTonThap.addView(row);
        }
    }

    private TextView dongChu(String s) {
        TextView tv = new TextView(requireContext());
        tv.setText(s);
        tv.setTextSize(12);
        tv.setTextColor(requireContext().getColor(R.color.muted));
        tv.setPadding(0, Ui.dp(requireContext(), 4), 0, Ui.dp(requireContext(), 4));
        return tv;
    }

    private static String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }
}
