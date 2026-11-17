package com.example.btl;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

/** Màn hình 4: một Activity dùng chung cho thêm mới và chỉnh sửa cây cảnh. */
public class AddEditPlantActivity extends AppCompatActivity {
    private PlantDatabase database;
    private EditText etName, etPrice, etStock, etDescription;
    private Spinner spCategory;
    private ImageView imgChosen;
    private View imagePlaceholder;

    private int editingId = -1;
    private Plant editingPlant;
    private String pendingImageUri; // File ảnh mới chọn, chưa lưu vào database.
    private boolean saved = false;

    private final List<String> categories = new ArrayList<>();
    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
            "Cây nội thất", "Cây để bàn", "Cây phong thủy", "Cây cảnh hoa");

    // GetContent được yêu cầu trong đề; ảnh được copy vào thư mục riêng ngay khi chọn.
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), sourceUri -> {
                if (sourceUri == null) return;
                try {
                    String newUri = ImageStorage.copyIntoApp(this, sourceUri);
                    ImageStorage.deleteIfLocal(this, pendingImageUri);
                    pendingImageUri = newUri;
                    showImage(newUri);
                } catch (IOException | SecurityException error) {
                    Toast.makeText(this, "Không thể chọn ảnh: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_plant);
        database = new PlantDatabase(this);

        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etStock = findViewById(R.id.etStock);
        etDescription = findViewById(R.id.etDescription);
        spCategory = findViewById(R.id.spCategory);
        imgChosen = findViewById(R.id.imgChosen);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        FrameLayout imagePickerBox = findViewById(R.id.imagePickerBox);

        LinkedHashSet<String> availableCategories = new LinkedHashSet<>();
        availableCategories.add("Chọn loại cây...");
        availableCategories.addAll(DEFAULT_CATEGORIES);
        availableCategories.addAll(database.getCategories());
        categories.addAll(availableCategories);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(spinnerAdapter);

        markAsteriskRed(findViewById(R.id.lblName));
        markAsteriskRed(findViewById(R.id.lblCategory));
        markAsteriskRed(findViewById(R.id.lblPrice));
        markAsteriskRed(findViewById(R.id.lblStock));

        editingId = getIntent().getIntExtra("PLANT_ID", -1);
        if (editingId > 0) {
            editingPlant = database.getPlantById(editingId);
            if (editingPlant == null) {
                Toast.makeText(this, "Không tìm thấy cây cảnh cần sửa", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            ((TextView) findViewById(R.id.tvFormTitle)).setText("Sửa cây cảnh");
            fillOldInformation(editingPlant);
        }

        if (savedInstanceState != null) {
            pendingImageUri = savedInstanceState.getString("PENDING_URI");
            if (pendingImageUri != null) showImage(pendingImageUri);
        }

        imagePickerBox.setOnClickListener(v -> imagePicker.launch("image/*"));
        findViewById(R.id.btnBackForm).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> validateAndSave());
        BottomNavHelper.setup(this);
    }

    private void markAsteriskRed(TextView label) {
        String raw = label.getText().toString();
        int position = raw.lastIndexOf('*');
        if (position < 0) return;
        SpannableString styled = new SpannableString(raw);
        styled.setSpan(new ForegroundColorSpan(Color.RED), position, position + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        label.setText(styled);
    }

    private void fillOldInformation(Plant plant) {
        etName.setText(plant.getName());
        etPrice.setText(String.valueOf(Math.round(plant.getPrice())));
        etStock.setText(String.valueOf(plant.getStock()));
        etDescription.setText(plant.getDescription() == null ? "" : plant.getDescription());
        int index = categories.indexOf(plant.getCategory());
        if (index > 0) spCategory.setSelection(index);
        if (plant.getImageUri() != null && !plant.getImageUri().isEmpty()) {
            showImage(plant.getImageUri());
        }
    }

    private void showImage(String uri) {
        imagePlaceholder.setVisibility(View.GONE);
        imgChosen.setVisibility(View.VISIBLE);
        ImageStorage.display(imgChosen, uri);
    }

    private void validateAndSave() {
        String name = etName.getText().toString().trim();
        String priceRaw = etPrice.getText().toString().trim();
        String stockRaw = etStock.getText().toString().trim();
        String category = spCategory.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập tên cây cảnh");
            etName.requestFocus();
            return;
        }
        if (spCategory.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Vui lòng chọn loại cây", Toast.LENGTH_SHORT).show();
            spCategory.performClick();
            return;
        }
        if (priceRaw.isEmpty()) {
            etPrice.setError("Vui lòng nhập giá bán");
            etPrice.requestFocus();
            return;
        }
        if (stockRaw.isEmpty()) {
            etStock.setError("Vui lòng nhập số lượng");
            etStock.requestFocus();
            return;
        }

        long price;
        int stock;
        try {
            price = Long.parseLong(priceRaw.replace(",", "").replace(".", ""));
            if (price <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etPrice.setError("Giá bán phải lớn hơn 0");
            etPrice.requestFocus();
            return;
        }
        try {
            stock = Integer.parseInt(stockRaw);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            etStock.setError("Số lượng phải là số nguyên không âm");
            etStock.requestFocus();
            return;
        }

        String finalImage = pendingImageUri != null ? pendingImageUri
                : editingPlant == null ? "" : editingPlant.getImageUri();
        Plant plant = new Plant(editingId, name, category, price, stock,
                description, finalImage);
        try {
            boolean successful;
            String action;
            if (editingPlant == null) {
                long newId = database.insertPlant(plant);
                successful = newId > 0;
                action = "insert";
            } else {
                successful = database.updatePlant(plant) > 0;
                action = "update";
            }
            if (!successful) {
                Toast.makeText(this, "Không thể lưu cây cảnh", Toast.LENGTH_LONG).show();
                return;
            }

            // Chỉ xóa ảnh cũ sau khi UPDATE đã thành công.
            if (editingPlant != null && pendingImageUri != null) {
                ImageStorage.deleteIfLocal(this, editingPlant.getImageUri());
            }
            saved = true; // Không dọn ảnh vừa lưu trong onDestroy.
            Toast.makeText(this, editingPlant == null
                    ? "Thêm cây cảnh thành công" : "Cập nhật cây cảnh thành công",
                    Toast.LENGTH_SHORT).show();
            Intent result = new Intent();
            result.putExtra("ACTION", action);
            setResult(Activity.RESULT_OK, result);
            finish();
        } catch (Exception error) {
            Toast.makeText(this, "Lỗi lưu dữ liệu: " + error.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("PENDING_URI", pendingImageUri);
    }

    @Override
    protected void onDestroy() {
        // Hủy hoặc Back: không để sót ảnh đã chọn nhưng chưa INSERT/UPDATE.
        if (!saved && !isChangingConfigurations()) {
            ImageStorage.deleteIfLocal(this, pendingImageUri);
        }
        if (database != null) database.close();
        super.onDestroy();
    }
}
