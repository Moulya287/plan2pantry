package com.plan2pantry.activities;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.plan2pantry.R;
import com.plan2pantry.utils.NotificationHelper;
import com.plan2pantry.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WaterTrackerActivity extends AppCompatActivity {

    private TextView tvWaterAmount, tvWaterTarget, tvWaterPercent;
    private ProgressBar progressWater;
    private Button btnAdd250, btnAdd500, btnAddCustom;
    private RecyclerView rvWaterEntries;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;
    private String todayDate;

    private int currentWaterMl = 0;
    private int targetWaterMl = 2500;
    private List<WaterEntry> waterEntries = new ArrayList<>();

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

        setContentView(R.layout.activity_water_tracker);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        targetWaterMl = (int) (session.getTargetWater() * 1000);

        initViews();
        setListeners();
        loadTodayData();
        loadWaterEntries();
    }

    private void initViews() {
        tvWaterAmount = findViewById(R.id.tv_water_amount);
        tvWaterTarget = findViewById(R.id.tv_water_target);
        tvWaterPercent = findViewById(R.id.tv_water_percent);
        progressWater = findViewById(R.id.progress_water);
        btnAdd250 = findViewById(R.id.btn_add_250);
        btnAdd500 = findViewById(R.id.btn_add_500);
        btnAddCustom = findViewById(R.id.btn_add_custom);
        rvWaterEntries = findViewById(R.id.rv_water_entries);
        rvWaterEntries.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setListeners() {
        btnAdd250.setOnClickListener(v -> addWater(250));
        btnAdd500.setOnClickListener(v -> addWater(500));
        btnAddCustom.setOnClickListener(v -> showCustomAmountDialog());
    }

    private void loadTodayData() {
        firestore.collection("waterIntake").document(userId)
                .collection(todayDate).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentWaterMl = document.getLong("totalMl") != null ?
                                document.getLong("totalMl").intValue() : 0;
                    } else {
                        currentWaterMl = 0;
                    }
                    updateWaterUI();
                });
    }

    private void loadWaterEntries() {
        firestore.collection("waterIntake").document(userId)
                .collection(todayDate).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    waterEntries.clear();
                    if (document.exists()) {
                        List<Map<String, Object>> entries = (List<Map<String, Object>>) document.get("entries");
                        if (entries != null) {
                            for (Map<String, Object> entry : entries) {
                                WaterEntry waterEntry = new WaterEntry();
                                waterEntry.time = (String) entry.get("time");
                                waterEntry.amount = ((Long) entry.get("amount")).intValue();
                                waterEntries.add(waterEntry);
                            }
                        }
                    }
                    updateEntriesList();
                });
    }

    private void addWater(int amountMl) {
        String timeStamp = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        firestore.collection("waterIntake").document(userId)
                .collection(todayDate).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    int currentTotal = 0;
                    List<Map<String, Object>> entries = new ArrayList<>();

                    if (document.exists()) {
                        currentTotal = document.getLong("totalMl") != null ?
                                document.getLong("totalMl").intValue() : 0;
                        entries = (List<Map<String, Object>>) document.get("entries");
                        if (entries == null) entries = new ArrayList<>();
                    }

                    int newTotal = currentTotal + amountMl;

                    Map<String, Object> newEntry = new HashMap<>();
                    newEntry.put("time", timeStamp);
                    newEntry.put("amount", amountMl);
                    entries.add(newEntry);

                    Map<String, Object> waterData = new HashMap<>();
                    waterData.put("totalMl", newTotal);
                    waterData.put("entries", entries);
                    waterData.put("lastUpdated", System.currentTimeMillis());

                    firestore.collection("waterIntake").document(userId)
                            .collection(todayDate).document("data")
                            .set(waterData)
                            .addOnSuccessListener(aVoid -> {
                                currentWaterMl = newTotal;
                                updateWaterUI();
                                loadWaterEntries();
                                Toast.makeText(WaterTrackerActivity.this,  amountMl + "ml water logged!", Toast.LENGTH_SHORT).show();

                                if (newTotal >= targetWaterMl) {
                                    NotificationHelper.sendWaterGoalAchievedNotification(WaterTrackerActivity.this);
                                }
                            });
                });
    }

    private void showCustomAmountDialog() {
        // Create dialog with custom style for rounded corners
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_water, null);
        builder.setView(dialogView);
        builder.setCancelable(true);

        EditText etAmount = dialogView.findViewById(R.id.et_custom_amount);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Apply rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirm.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (!amountStr.isEmpty()) {
                int amount = Integer.parseInt(amountStr);
                addWater(amount);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateWaterUI() {
        float waterL = currentWaterMl / 1000f;
        float targetL = targetWaterMl / 1000f;
        int percent = (int) ((float) currentWaterMl / targetWaterMl * 100);

        tvWaterAmount.setText(String.format(Locale.getDefault(), "%.1f", waterL));
        tvWaterTarget.setText(String.format(Locale.getDefault(), "/ %.1f L", targetL));
        tvWaterPercent.setText(percent + "%");
        progressWater.setMax(targetWaterMl);
        progressWater.setProgress(currentWaterMl);
    }

    private void updateEntriesList() {
        WaterEntryAdapter adapter = new WaterEntryAdapter(waterEntries);
        rvWaterEntries.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayData();
        loadWaterEntries();
    }

    // Water Entry Class
    public static class WaterEntry {
        public String time;
        public int amount;
    }

    // Water Entry Adapter
    public class WaterEntryAdapter extends RecyclerView.Adapter<WaterEntryAdapter.ViewHolder> {
        private List<WaterEntry> entries;

        public WaterEntryAdapter(List<WaterEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_water_entry, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WaterEntry entry = entries.get(position);
            holder.tvTime.setText(entry.time);
            holder.tvAmount.setText("+" + entry.amount + " ml");
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTime, tvAmount;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTime = itemView.findViewById(R.id.tv_time);
                tvAmount = itemView.findViewById(R.id.tv_amount);
            }
        }
    }
}