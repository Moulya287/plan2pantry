package com.plan2pantry.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
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
import java.util.List;

public class PantryActivity extends AppCompatActivity {

    private RecyclerView rvPantry;
    private TextView tvEmpty;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;

    private List<PantryItem> pantryItems = new ArrayList<>();
    private PantryAdapter adapter;

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

        setContentView(R.layout.activity_pantry);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();

        initViews();
        loadPantryItems();
    }

    private void initViews() {
        rvPantry = findViewById(R.id.rv_pantry);
        tvEmpty = findViewById(R.id.tv_empty);
        rvPantry.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadPantryItems() {
        firestore.collection("pantry").document(userId)
                .collection("items").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pantryItems.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PantryItem item = new PantryItem();
                        item.id = doc.getId();
                        item.name = doc.getString("name");
                        item.quantity = doc.getDouble("quantity") != null ? doc.getDouble("quantity").floatValue() : 0;
                        item.unit = doc.getString("unit");
                        item.category = doc.getString("category");
                        pantryItems.add(item);
                    }

                    if (pantryItems.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvPantry.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvPantry.setVisibility(View.VISIBLE);
                        adapter = new PantryAdapter(pantryItems);
                        rvPantry.setAdapter(adapter);
                    }
                });
    }

    private void removeFromPantry(PantryItem item) {
        firestore.collection("pantry").document(userId)
                .collection("items").document(item.id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "❌ " + item.name + " used up and removed", Toast.LENGTH_SHORT).show();                    loadPantryItems();
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
            default: return "📦";
        }
    }

    // PantryItem Class
    public static class PantryItem {
        public String id;
        public String name;
        public float quantity;
        public String unit;
        public String category;
    }

    // PantryAdapter
    public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {
        private List<PantryItem> items;

        public PantryAdapter(List<PantryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pantry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PantryItem item = items.get(position);

            holder.tvItemIcon.setText(getCategoryIcon(item.category));
            holder.tvItemCategory.setText(item.category);
            holder.tvItemName.setText(item.name);
            holder.tvItemQuantity.setText(item.quantity + " " + item.unit);

            holder.cbUsedUp.setOnCheckedChangeListener(null);
            holder.cbUsedUp.setChecked(false);

            holder.cbUsedUp.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    removeFromPantry(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemIcon, tvItemCategory, tvItemName, tvItemQuantity;
            CheckBox cbUsedUp;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemIcon = itemView.findViewById(R.id.tv_item_icon);
                tvItemCategory = itemView.findViewById(R.id.tv_item_category);
                tvItemName = itemView.findViewById(R.id.tv_item_name);
                tvItemQuantity = itemView.findViewById(R.id.tv_item_quantity);
                cbUsedUp = itemView.findViewById(R.id.cb_used_up);
            }
        }
    }
}