package com.example.btl;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Màn hình 3: danh sách, tìm, lọc, sắp xếp, phân trang và xóa. */
public class PlantManagementActivity extends AppCompatActivity {
    private static final int PAGE_SIZE = 5;

    private PlantDatabase database;
    private PlantAdapter adapter;
    private RecyclerView recycler;
    private TextView tvTotal, tvPage, tvEmpty;
    private ImageButton btnPrevious, btnNext;
    private MaterialButton chipAll, chipInterior, chipDesktop, chipFengShui, btnFilter;
    private EditText etSearch;

    private String selectedCategory = "Tất cả";
    private String sortOption = "Mặc định";
    private int currentPage = 0; // Trang được đánh số từ 0 trong Java.
    private List<Plant> filteredPlants = new ArrayList<>();

    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // Khi vừa thêm, tự chuyển tới trang cuối để thấy dòng mới.
                    if (data != null && "insert".equals(data.getStringExtra("ACTION"))) {
                        currentPage = Integer.MAX_VALUE;
                    }
                    refreshPlants();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_management);

        database = new PlantDatabase(this);
        recycler = findViewById(R.id.recyclerPlants);
        tvTotal = findViewById(R.id.tvTotal);
        tvPage = findViewById(R.id.tvPage);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        chipAll = findViewById(R.id.chipAll);
        chipInterior = findViewById(R.id.chipInterior);
        chipDesktop = findViewById(R.id.chipDesktop);
        chipFengShui = findViewById(R.id.chipFengShui);
        btnFilter = findViewById(R.id.btnFilter);
        etSearch = findViewById(R.id.etSearch);

        adapter = new PlantAdapter(this, new PlantAdapter.OnPlantActionListener() {
            @Override public void onEdit(Plant plant) { openForm(plant.getId()); }
            @Override public void onDelete(Plant plant) { confirmDelete(plant); }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddPlant).setOnClickListener(v -> openForm(-1));
        BottomNavHelper.setup(this);

        chipAll.setOnClickListener(v -> selectCategory("Tất cả"));
        chipInterior.setOnClickListener(v -> selectCategory("Cây nội thất"));
        chipDesktop.setOnClickListener(v -> selectCategory("Cây để bàn"));
        chipFengShui.setOnClickListener(v -> selectCategory("Cây phong thủy"));
        btnFilter.setOnClickListener(this::showFilterMenu);
        btnPrevious.setOnClickListener(v -> changePage(-1));
        btnNext.setOnClickListener(v -> changePage(1));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPage = 0;
                refreshPlants();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Nếu tìm từ Trang chủ, giữ luôn từ khóa ở màn hình danh sách.
        String keyword = getIntent().getStringExtra("SEARCH_QUERY");
        if (keyword != null) etSearch.setText(keyword);
        updateChips();
        refreshPlants();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (database != null && adapter != null) refreshPlants();
    }

    private void openForm(int plantId) {
        Intent intent = new Intent(this, AddEditPlantActivity.class);
        if (plantId > 0) intent.putExtra("PLANT_ID", plantId);
        editLauncher.launch(intent);
    }

    private void selectCategory(String category) {
        selectedCategory = category;
        currentPage = 0;
        updateChips();
        refreshPlants();
    }

    private void updateChips() {
        styleChip(chipAll, "Tất cả".equals(selectedCategory));
        styleChip(chipInterior, "Cây nội thất".equals(selectedCategory));
        styleChip(chipDesktop, "Cây để bàn".equals(selectedCategory));
        styleChip(chipFengShui, "Cây phong thủy".equals(selectedCategory));
    }

    private void styleChip(MaterialButton button, boolean selected) {
        int green = ContextCompat.getColor(this, R.color.green_primary);
        int white = ContextCompat.getColor(this, R.color.white);
        int stroke = ContextCompat.getColor(this, R.color.input_border);
        button.setBackgroundTintList(ColorStateList.valueOf(selected ? green : white));
        button.setStrokeColor(ColorStateList.valueOf(selected ? green : stroke));
        button.setTextColor(selected ? white : ContextCompat.getColor(this, R.color.text_primary));
        button.setIconTint(ColorStateList.valueOf(selected ? white : green));
    }

    private void showFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, 0, 0, "DANH MỤC").setEnabled(false);
        menu.add(1, 1, 1, "Cây cảnh hoa");
        menu.add(1, 2, 2, "Cây khác");
        menu.add(Menu.NONE, 0, 3, "SẮP XẾP").setEnabled(false);
        menu.add(2, 3, 4, "Mặc định");
        menu.add(2, 4, 5, "Giá tăng dần");
        menu.add(2, 5, 6, "Giá giảm dần");
        menu.add(2, 6, 7, "Tồn kho tăng dần");
        menu.add(2, 7, 8, "Tồn kho giảm dần");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: selectCategory("Cây cảnh hoa"); return true;
                case 2: selectCategory("Cây khác"); return true;
                case 3: sortOption = "Mặc định"; break;
                case 4: sortOption = "Giá tăng dần"; break;
                case 5: sortOption = "Giá giảm dần"; break;
                case 6: sortOption = "Tồn kho tăng dần"; break;
                case 7: sortOption = "Tồn kho giảm dần"; break;
                default: return false;
            }
            currentPage = 0;
            refreshPlants();
            return true;
        });
        popup.show();
    }

    private void refreshPlants() {
        filteredPlants = database.searchAndFilterPlants(
                etSearch.getText().toString(), selectedCategory, sortOption);
        int size = filteredPlants.size();
        int pageCount = Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
        currentPage = Math.max(0, Math.min(currentPage, pageCount - 1));

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, size);
        adapter.submitList(new ArrayList<>(filteredPlants.subList(start, end)));
        tvEmpty.setVisibility(size == 0 ? View.VISIBLE : View.GONE);

        String prefix = "Tổng: ";
        String number = String.valueOf(size);
        SpannableString total = new SpannableString(prefix + number + " cây cảnh");
        int color = ContextCompat.getColor(this, R.color.green_primary);
        total.setSpan(new ForegroundColorSpan(color), prefix.length(),
                prefix.length() + number.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        total.setSpan(new StyleSpan(Typeface.BOLD), prefix.length(),
                prefix.length() + number.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTotal.setText(total);
        tvPage.setText((currentPage + 1) + " / " + pageCount);

        btnPrevious.setEnabled(currentPage > 0);
        btnPrevious.setAlpha(currentPage > 0 ? 1f : 0.3f);
        btnNext.setEnabled(currentPage < pageCount - 1);
        btnNext.setAlpha(currentPage < pageCount - 1 ? 1f : 0.3f);
    }

    private void changePage(int direction) {
        int size = filteredPlants.size();
        int pageCount = Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
        int newPage = currentPage + direction;
        if (newPage >= 0 && newPage < pageCount) {
            currentPage = newPage;
            refreshPlants();
            recycler.smoothScrollToPosition(0);
        }
    }

    private void confirmDelete(Plant plant) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa cây cảnh?")
                .setMessage("Bạn có chắc muốn xóa ‘" + plant.getName() + "’ không?")
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Xóa", (dialog, which) -> {
                    try {
                        if (database.deletePlant(plant.getId()) > 0) {
                            ImageStorage.deleteIfLocal(this, plant.getImageUri());
                            Toast.makeText(this, "Đã xóa cây cảnh", Toast.LENGTH_SHORT).show();
                            refreshPlants();
                        } else {
                            Toast.makeText(this, "Không tìm thấy cây để xóa", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception error) {
                        Toast.makeText(this, "Lỗi xóa: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
