package com.example.quanlycaycanh.customer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quanlycaycanh.R;
import com.example.quanlycaycanh.data.LoaiCay;
import com.example.quanlycaycanh.data.Repository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/** Modal "Lọc sản phẩm" (5.3): loại cây, khoảng giá, sắp xếp. */
public class FilterDialog extends BottomSheetDialogFragment {

    public interface Listener {
        void onApply(int maLoai, double giaTu, double giaDen, String sort);
    }

    private int maLoai;
    private double giaTu;
    private double giaDen;
    private String sort = "mac_dinh";

    public void setThamSo(int maLoai, double giaTu, double giaDen, String sort) {
        this.maLoai = maLoai;
        this.giaTu = giaTu;
        this.giaDen = giaDen;
        this.sort = sort;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        ChipGroup chipLoai = v.findViewById(R.id.chipLoai);
        EditText edGiaTu = v.findViewById(R.id.edGiaTu);
        EditText edGiaDen = v.findViewById(R.id.edGiaDen);
        RadioGroup rgSapXep = v.findViewById(R.id.rgSapXep);

        chipLoai.removeAllViews();
        chipLoai.addView(taoChip("Tất cả", 0));
        for (LoaiCay loai : new Repository(requireContext()).categories()) {
            chipLoai.addView(taoChip(loai.tenLoai, loai.maLoai));
        }
        if (giaTu > 0) {
            edGiaTu.setText(String.valueOf((long) giaTu));
        }
        if (giaDen > 0) {
            edGiaDen.setText(String.valueOf((long) giaDen));
        }
        if ("gia_tang".equals(sort)) {
            rgSapXep.check(R.id.rbGiaTang);
        } else if ("gia_giam".equals(sort)) {
            rgSapXep.check(R.id.rbGiaGiam);
        } else if ("moi_nhat".equals(sort)) {
            rgSapXep.check(R.id.rbMoiNhat);
        } else {
            rgSapXep.check(R.id.rbMacDinh);
        }

        v.findViewById(R.id.btnXoaLoc).setOnClickListener(view -> {
            maLoai = 0;
            edGiaTu.setText("");
            edGiaDen.setText("");
            rgSapXep.check(R.id.rbMacDinh);
            apDung(0, 0, 0, "mac_dinh");
            dismiss();
        });
        v.findViewById(R.id.btnApDung).setOnClickListener(view -> {
            double tu = so(edGiaTu);
            double den = so(edGiaDen);
            apDung(maLoai, tu, den, sortTuRadio(rgSapXep));
            dismiss();
        });
    }

    private Chip taoChip(String ten, int giaTri) {
        Chip chip = new Chip(requireContext());
        chip.setText(ten);
        chip.setCheckable(true);
        chip.setChecked(giaTri == maLoai);
        chip.setOnClickListener(v -> maLoai = giaTri);
        return chip;
    }

    private void apDung(int maLoaiMoi, double giaTuMoi, double giaDenMoi, String sortMoi) {
        Listener l = null;
        if (getParentFragment() instanceof Listener) {
            l = (Listener) getParentFragment();
        } else if (getActivity() instanceof Listener) {
            l = (Listener) getActivity();
        }
        if (l != null) {
            l.onApply(maLoaiMoi, giaTuMoi, giaDenMoi, sortMoi);
        }
    }

    private static double so(EditText e) {
        String s = e.getText() == null ? "" : e.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String sortTuRadio(RadioGroup rg) {
        int id = rg.getCheckedRadioButtonId();
        if (id == R.id.rbGiaTang) {
            return "gia_tang";
        }
        if (id == R.id.rbGiaGiam) {
            return "gia_giam";
        }
        if (id == R.id.rbMoiNhat) {
            return "moi_nhat";
        }
        return "mac_dinh";
    }
}
