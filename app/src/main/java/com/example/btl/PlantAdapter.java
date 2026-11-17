package com.example.btl;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Adapter: chuyển Plant thành một Card trên màn hình danh sách. */
public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    public interface OnPlantActionListener {
        void onEdit(Plant plant);
        void onDelete(Plant plant);
    }

    private final List<Plant> plants = new ArrayList<>();
    private final OnPlantActionListener listener;
    private final Context context;

    public PlantAdapter(Context context, OnPlantActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<Plant> page) {
        plants.clear();
        plants.addAll(page);
        notifyDataSetChanged(); // Mỗi trang tối đa 5 dòng nên đủ nhẹ cho bài tập.
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plants.get(position);
        holder.tvName.setText(plant.getName());
        holder.tvCategory.setText(plant.getCategory());
        holder.tvStock.setText("Tồn kho: " + plant.getStock());
        ImageStorage.display(holder.image, plant.getImageUri());

        String prefix = "Giá: ";
        String formatted = NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(Math.round(plant.getPrice())) + " đ";
        SpannableString priceText = new SpannableString(prefix + formatted);
        int from = prefix.length();
        priceText.setSpan(new ForegroundColorSpan(
                androidx.core.content.ContextCompat.getColor(context, R.color.danger)),
                from, priceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceText.setSpan(new StyleSpan(Typeface.BOLD),
                from, priceText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvPrice.setText(priceText);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(plant));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(plant));
    }

    @Override
    public int getItemCount() { return plants.size(); }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView tvName, tvCategory, tvPrice, tvStock;
        final ImageButton btnEdit, btnDelete;

        PlantViewHolder(@NonNull View item) {
            super(item);
            image = item.findViewById(R.id.imgPlant);
            tvName = item.findViewById(R.id.tvPlantName);
            tvCategory = item.findViewById(R.id.tvCategory);
            tvPrice = item.findViewById(R.id.tvPrice);
            tvStock = item.findViewById(R.id.tvStock);
            btnEdit = item.findViewById(R.id.btnEdit);
            btnDelete = item.findViewById(R.id.btnDelete);
        }
    }
}
