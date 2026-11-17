package com.example.btl;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Creates an admin-side sales invoice from plants currently in stock. */
public class CreateInvoiceActivity extends AppCompatActivity {
    private final Map<Integer, Integer> cart = new LinkedHashMap<>();
    private final Map<Integer, Integer> pickerQuantities = new LinkedHashMap<>();
    private final Locale vietnamese = new Locale("vi", "VN");

    private PlantDatabase plantDatabase;
    private InvoiceDatabase invoiceDatabase;
    private List<Plant> allPlants = new ArrayList<>();
    private EditText etCustomerName;
    private EditText etCustomerPhone;
    private EditText etSearchPlants;
    private LinearLayout productContainer;
    private LinearLayout cartContainer;
    private TextView tvCartEmpty;
    private TextView tvTotal;
    private TextView tvFilterDescription;
    private String selectedCategory = "Tất cả";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        plantDatabase = new PlantDatabase(this);
        invoiceDatabase = new InvoiceDatabase(this);
        allPlants = plantDatabase.getAllPlants();

        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerPhone = findViewById(R.id.etCustomerPhone);
        etSearchPlants = findViewById(R.id.etSearchPlants);
        productContainer = findViewById(R.id.layoutInvoicePlants);
        cartContainer = findViewById(R.id.layoutInvoiceCart);
        tvCartEmpty = findViewById(R.id.tvInvoiceCartEmpty);
        tvTotal = findViewById(R.id.tvInvoiceTotal);
        tvFilterDescription = findViewById(R.id.tvInvoicePlantFilter);

        findViewById(R.id.btnBackCreateInvoice).setOnClickListener(v -> finish());
        findViewById(R.id.btnCancelCreateInvoice).setOnClickListener(v -> finish());
        findViewById(R.id.btnSaveInvoice).setOnClickListener(v -> saveInvoice());
        findViewById(R.id.btnFilterPlants).setOnClickListener(this::showCategoryMenu);
        BottomNavHelper.setup(this, R.id.navBills);

        etSearchPlants.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderProducts();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        renderProducts();
        renderCart();
    }

    private void showCategoryMenu(View anchor) {
        List<String> categories = new ArrayList<>();
        categories.add("Tất cả");
        for (Plant plant : allPlants) {
            if (!categories.contains(plant.getCategory())) categories.add(plant.getCategory());
        }

        PopupMenu popup = new PopupMenu(this, anchor);
        for (int i = 0; i < categories.size(); i++) {
            MenuItem item = popup.getMenu().add(0, i, i, categories.get(i));
            item.setChecked(categories.get(i).equals(selectedCategory));
        }
        popup.setOnMenuItemClickListener(item -> {
            int index = item.getItemId();
            if (index < 0 || index >= categories.size()) return false;
            selectedCategory = categories.get(index);
            tvFilterDescription.setText("Đang lọc: " + selectedCategory);
            renderProducts();
            return true;
        });
        popup.show();
    }

    private void renderProducts() {
        productContainer.removeAllViews();
        String keyword = etSearchPlants.getText().toString().trim().toLowerCase(vietnamese);
        int count = 0;

        for (Plant plant : allPlants) {
            if (!"Tất cả".equals(selectedCategory)
                    && !selectedCategory.equals(plant.getCategory())) continue;
            if (!keyword.isEmpty()
                    && !plant.getName().toLowerCase(vietnamese).contains(keyword)) continue;
            productContainer.addView(createProductCard(plant));
            count++;
        }

        if (count == 0) {
            TextView empty = makeText("Không tìm thấy cây cảnh phù hợp",
                    14, ContextCompat.getColor(this, R.color.text_secondary), false);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(16), 0, dp(16));
            productContainer.addView(empty);
        }
    }

    private MaterialCardView createProductCard(Plant plant) {
        MaterialCardView card = makeCard();
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(12), dp(10), dp(12), dp(10));

        TextView name = makeText(plant.getName(), 16,
                ContextCompat.getColor(this, R.color.text_primary), true);
        TextView details = makeText("Giá: " + money(Math.round(plant.getPrice()))
                        + "  ·  Tồn kho: " + plant.getStock(),
                13, ContextCompat.getColor(this, R.color.text_secondary), false);
        details.setPadding(0, dp(3), 0, dp(8));
        content.addView(name);
        content.addView(details);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        controls.setOrientation(LinearLayout.HORIZONTAL);

        int selectedQuantity = pickerQuantities.containsKey(plant.getId())
                ? pickerQuantities.get(plant.getId()) : 1;
        TextView quantity = makeText(String.valueOf(selectedQuantity), 15,
                ContextCompat.getColor(this, R.color.text_primary), true);
        quantity.setGravity(Gravity.CENTER);
        quantity.setMinWidth(dp(38));

        MaterialButton minus = smallButton("−", false);
        MaterialButton plus = smallButton("+", false);
        MaterialButton add = smallButton("Thêm", true);
        add.setIconResource(R.drawable.ic_bill);
        add.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
        add.setEnabled(plant.getStock() > 0);
        if (plant.getStock() <= 0) add.setText("Hết hàng");

        minus.setOnClickListener(v -> {
            int value = pickerQuantities.getOrDefault(plant.getId(), 1);
            if (value > 1) pickerQuantities.put(plant.getId(), value - 1);
            renderProducts();
        });
        plus.setOnClickListener(v -> {
            int value = pickerQuantities.getOrDefault(plant.getId(), 1);
            int inCart = cart.getOrDefault(plant.getId(), 0);
            if (value + inCart < plant.getStock()) {
                pickerQuantities.put(plant.getId(), value + 1);
                renderProducts();
            }
        });
        add.setOnClickListener(v -> addToCart(plant));

        controls.addView(minus, new LinearLayout.LayoutParams(dp(42), dp(40)));
        controls.addView(quantity, new LinearLayout.LayoutParams(dp(40), dp(40)));
        controls.addView(plus, new LinearLayout.LayoutParams(dp(42), dp(40)));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(
                dp(104), dp(40));
        addParams.setMargins(dp(8), 0, 0, 0);
        controls.addView(add, addParams);
        content.addView(controls);
        card.addView(content);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, dp(5), 0, dp(5));
        card.setLayoutParams(params);
        return card;
    }

    private void addToCart(Plant plant) {
        int addQuantity = pickerQuantities.getOrDefault(plant.getId(), 1);
        int existing = cart.getOrDefault(plant.getId(), 0);
        if (existing + addQuantity > plant.getStock()) {
            Toast.makeText(this, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
            return;
        }
        cart.put(plant.getId(), existing + addQuantity);
        pickerQuantities.put(plant.getId(), 1);
        renderCart();
        renderProducts();
    }

    private void renderCart() {
        cartContainer.removeAllViews();
        tvCartEmpty.setVisibility(cart.isEmpty() ? View.VISIBLE : View.GONE);
        long total = 0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Plant plant = findPlant(entry.getKey());
            if (plant == null) continue;
            int quantity = entry.getValue();
            long unitPrice = Math.round(plant.getPrice());
            total = Math.addExact(total, Math.multiplyExact(unitPrice, (long) quantity));
            cartContainer.addView(createCartRow(plant, quantity, unitPrice));
        }
        tvTotal.setText(money(total));
    }

    private View createCartRow(Plant plant, int quantity, long unitPrice) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, dp(9), 0, dp(9));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = makeText(plant.getName(), 14,
                ContextCompat.getColor(this, R.color.text_primary), true);
        titleRow.addView(name, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        TextView lineTotal = makeText(money(unitPrice * quantity), 14,
                ContextCompat.getColor(this, R.color.danger), true);
        titleRow.addView(lineTotal);
        row.addView(titleRow);

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        TextView price = makeText("Đơn giá " + money(unitPrice), 12,
                ContextCompat.getColor(this, R.color.text_secondary), false);
        controls.addView(price, new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        MaterialButton minus = smallButton("−", false);
        TextView quantityText = makeText(String.valueOf(quantity), 14,
                ContextCompat.getColor(this, R.color.text_primary), true);
        quantityText.setGravity(Gravity.CENTER);
        quantityText.setMinWidth(dp(32));
        MaterialButton plus = smallButton("+", false);
        MaterialButton remove = smallButton("×", false);
        remove.setTextColor(ContextCompat.getColor(this, R.color.danger));
        remove.setStrokeColor(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.danger)));

        minus.setOnClickListener(v -> {
            int current = cart.getOrDefault(plant.getId(), 0);
            if (current <= 1) cart.remove(plant.getId());
            else cart.put(plant.getId(), current - 1);
            renderCart();
            renderProducts();
        });
        plus.setOnClickListener(v -> {
            int current = cart.getOrDefault(plant.getId(), 0);
            if (current < plant.getStock()) cart.put(plant.getId(), current + 1);
            else Toast.makeText(this, "Đã đạt số lượng tồn kho", Toast.LENGTH_SHORT).show();
            renderCart();
            renderProducts();
        });
        remove.setOnClickListener(v -> {
            cart.remove(plant.getId());
            renderCart();
            renderProducts();
        });

        controls.addView(minus, new LinearLayout.LayoutParams(dp(38), dp(36)));
        controls.addView(quantityText, new LinearLayout.LayoutParams(dp(30), dp(36)));
        controls.addView(plus, new LinearLayout.LayoutParams(dp(38), dp(36)));
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(dp(38), dp(36));
        removeParams.setMargins(dp(6), 0, 0, 0);
        controls.addView(remove, removeParams);
        row.addView(controls);

        View divider = new View(this);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.input_border));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.setMargins(0, dp(8), 0, 0);
        row.addView(divider, dividerParams);
        return row;
    }

    private void saveInvoice() {
        String customerName = etCustomerName.getText().toString().trim();
        String phoneInput = etCustomerPhone.getText().toString().trim();
        String phone = phoneInput.replaceAll("[^0-9]", "");

        if (customerName.isEmpty()) {
            etCustomerName.setError("Vui lòng nhập tên khách hàng");
            etCustomerName.requestFocus();
            return;
        }
        if (!phone.matches("0[0-9]{9}")) {
            etCustomerPhone.setError("Số điện thoại cần có 10 chữ số và bắt đầu bằng 0");
            etCustomerPhone.requestFocus();
            return;
        }
        if (cart.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất một cây cảnh",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long invoiceId = invoiceDatabase.createInvoice(customerName, phone, cart);
            Toast.makeText(this, "Đã lưu hóa đơn "
                    + InvoiceDatabase.formatInvoiceCode(invoiceId), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, InvoiceManagementActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        } catch (Exception error) {
            Toast.makeText(this, error.getMessage() == null
                    ? "Không thể lưu hóa đơn" : error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Plant findPlant(int id) {
        for (Plant plant : allPlants) {
            if (plant.getId() == id) return plant;
        }
        return null;
    }

    private MaterialCardView makeCard() {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
        card.setRadius(dp(12));
        card.setCardElevation(dp(1));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(ContextCompat.getColor(this, R.color.input_border));
        return card;
    }

    private MaterialButton smallButton(String label, boolean primary) {
        MaterialButton button = new MaterialButton(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setCornerRadius(dp(10));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        button.setMinHeight(dp(36));
        button.setPadding(dp(5), 0, dp(5), 0);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(
                this, primary ? R.color.green_primary : R.color.white)));
        button.setTextColor(ContextCompat.getColor(
                this, primary ? R.color.white : R.color.green_primary));
        if (!primary) {
            button.setStrokeColor(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.green_primary)));
            button.setStrokeWidth(dp(1));
        }
        return button;
    }

    private TextView makeText(String text, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private String money(long amount) {
        return NumberFormat.getNumberInstance(vietnamese).format(amount) + " đ";
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (plantDatabase != null) plantDatabase.close();
        if (invoiceDatabase != null) invoiceDatabase.close();
        super.onDestroy();
    }
}
