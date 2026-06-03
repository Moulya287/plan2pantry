package com.plan2pantry.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.plan2pantry.R;
import com.plan2pantry.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GroceryActivity extends AppCompatActivity {

    private RecyclerView rvGrocery;
    private LinearLayout btnAddItem, btnViewPantry;
    private TextView tvEmpty;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;

    private List<GroceryItem> groceryItems = new ArrayList<>();
    private GroceryAdapter adapter;

    private final String[] CATEGORIES = {"Vegetables", "Fruits", "Dairy", "Grains", "Meat", "Snacks", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        setContentView(R.layout.activity_grocery);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();

        initViews();
        setListeners();
        loadGroceryItems();
    }

    private void initViews() {
        rvGrocery = findViewById(R.id.rv_grocery);
        btnAddItem = findViewById(R.id.btn_add_item);
        btnViewPantry = findViewById(R.id.btn_view_pantry);
        tvEmpty = findViewById(R.id.tv_empty);
        rvGrocery.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setListeners() {
        btnAddItem.setOnClickListener(v -> showAddItemDialog());
        btnViewPantry.setOnClickListener(v -> {
            startActivity(new Intent(this, PantryActivity.class));
        });
    }

    private void loadGroceryItems() {
        firestore.collection("grocery").document(userId)
                .collection("items")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    groceryItems.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        GroceryItem item = new GroceryItem();
                        item.id = doc.getString("id");
                        item.name = doc.getString("name");
                        item.quantity = doc.getDouble("quantity") != null ? doc.getDouble("quantity").floatValue() : 0;
                        item.unit = doc.getString("unit");
                        item.category = doc.getString("category");
                        groceryItems.add(item);
                    }

                    if (groceryItems.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvGrocery.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvGrocery.setVisibility(View.VISIBLE);
                        adapter = new GroceryAdapter(groceryItems);
                        rvGrocery.setAdapter(adapter);
                    }
                });
    }

    private void showAddItemDialog() {
        // Create dialog with custom style for rounded corners
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_grocery, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        EditText etName = dialogView.findViewById(R.id.et_item_name);
        EditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        EditText etUnit = dialogView.findViewById(R.id.et_unit);
        Spinner spCategory = dialogView.findViewById(R.id.sp_category);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String qtyStr = etQuantity.getText().toString().trim();
            String unit = etUnit.getText().toString().trim();
            String category = spCategory.getSelectedItem().toString();

            if (name.isEmpty()) {
                etName.setError("Item name required");
                return;
            }
            float quantity = qtyStr.isEmpty() ? 1 : Float.parseFloat(qtyStr);
            if (unit.isEmpty()) unit = "pcs";

            addGroceryItem(name, quantity, unit, category);
            dialog.dismiss();
        });
    }

    private void addGroceryItem(String name, float quantity, String unit, String category) {
        String itemId = UUID.randomUUID().toString();

        Map<String, Object> item = new HashMap<>();
        item.put("id", itemId);
        item.put("name", name);
        item.put("quantity", quantity);
        item.put("unit", unit);
        item.put("category", category);
        item.put("status", "active");
        item.put("addedDate", System.currentTimeMillis());

        firestore.collection("grocery").document(userId)
                .collection("items").document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✓ " + name + " added to grocery list", Toast.LENGTH_SHORT).show();
                    loadGroceryItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void markAsBought(GroceryItem item) {
        firestore.collection("grocery").document(userId)
                .collection("items").document(item.id)
                .update("status", "bought")
                .addOnSuccessListener(aVoid -> {
                    // Add to pantry
                    Map<String, Object> pantryItem = new HashMap<>();
                    pantryItem.put("id", UUID.randomUUID().toString());
                    pantryItem.put("name", item.name);
                    pantryItem.put("quantity", item.quantity);
                    pantryItem.put("unit", item.unit);
                    pantryItem.put("category", item.category);
                    pantryItem.put("addedDate", System.currentTimeMillis());

                    firestore.collection("pantry").document(userId)
                            .collection("items").add(pantryItem)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "✓ " + item.name + " moved to pantry", Toast.LENGTH_SHORT).show();
                                loadGroceryItems();
                            });
                });
    }

    private String getCategoryIcon(String category) {
        switch (category) {
            case "Vegetables": return "🥬";
            case "Fruits": return "🍎";
            case "Dairy": return "🥛";
            case "Grains": return "🌾";
            case "Meat": return "🍗";
            case "Snacks": return "🍪";
            default: return "🛒";
        }
    }

    // GroceryItem Class
    public static class GroceryItem {
        public String id;
        public String name;
        public float quantity;
        public String unit;
        public String category;
    }

    // GroceryAdapter
    public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ViewHolder> {
        private List<GroceryItem> items;

        public GroceryAdapter(List<GroceryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_grocery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroceryItem item = items.get(position);

            holder.tvItemIcon.setText(getCategoryIcon(item.category));
            holder.tvItemCategory.setText(item.category);
            holder.tvItemName.setText(item.name);
            holder.tvItemQuantity.setText(item.quantity + " " + item.unit);

            holder.checkBought.setOnCheckedChangeListener(null);
            holder.checkBought.setChecked(false);

            holder.checkBought.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    markAsBought(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemIcon, tvItemCategory, tvItemName, tvItemQuantity;
            CheckBox checkBought;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemIcon = itemView.findViewById(R.id.tv_item_icon);
                tvItemCategory = itemView.findViewById(R.id.tv_item_category);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
                tvItemQuantity = itemView.findViewById(R.id.tv_item_quantity);
                checkBought = itemView.findViewById(R.id.check_bought);
            }
        }
    }
}