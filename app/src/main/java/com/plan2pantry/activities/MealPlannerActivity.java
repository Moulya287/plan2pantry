package com.plan2pantry.activities;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
import android.widget.ProgressBar;
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
import com.plan2pantry.adapters.MealPlannerAdapter;
import com.plan2pantry.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MealPlannerActivity extends AppCompatActivity {

    private LinearLayout btnGeneratePlan;
    private RecyclerView rvMeals, rvFoodLogs;
    private LinearLayout btnPrevDay, btnNextDay, btnLogFood;
    private TextView tvDayLabel, tvTotalCalories, tvRemainingCalories;
    private ProgressBar progressDay;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;
    private String dietType;
    private String todayDate;

    private int targetCalories = 2000;
    private int currentDay = 1;
    private int eatenCalories = 0;

    private List<MealPlannerAdapter.MealItem> currentMeals = new ArrayList<>();
    private List<FoodLogItem> foodLogs = new ArrayList<>();
    private Map<String, List<Map<String, Object>>> masterMeals = new HashMap<>();
    private boolean planExists = false;
    private boolean isLoadingComplete = false;

    private int planCreatedDayOfWeek = 1;
    private boolean metadataLoaded = false;

    private final String[] DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private final String[] MEAL_ICONS = {"🍞", "🍱", "🍲", "🍎"};
    private final String[] MEAL_TYPES = {"Breakfast", "Lunch", "Dinner", "Snack"};
    private final String[] LOG_MEAL_TYPES = {"Breakfast", "Lunch", "Dinner", "Snack", "Other"};

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

        setContentView(R.layout.activity_meal_planner);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();
        dietType = session.getDietType();
        targetCalories = session.getTargetCalories();
        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        initViews();
        setListeners();

        checkAndLoadMealPlan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (planExists) {
            loadMealPlanForDay(currentDay);
            loadFoodLogs();
        }
    }

    private void initViews() {
        rvMeals = findViewById(R.id.rv_meals);
        rvFoodLogs = findViewById(R.id.rv_food_logs);
        btnPrevDay = findViewById(R.id.btn_prev_day);
        btnNextDay = findViewById(R.id.btn_next_day);
        btnGeneratePlan = findViewById(R.id.btn_generate_plan);
        btnLogFood = findViewById(R.id.btn_log_food);
        tvDayLabel = findViewById(R.id.tv_day_label);
        tvTotalCalories = findViewById(R.id.tv_total_calories);
        tvRemainingCalories = findViewById(R.id.tv_remaining_calories);
        progressDay = findViewById(R.id.progress_day);
        rvMeals.setLayoutManager(new LinearLayoutManager(this));
        rvFoodLogs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setListeners() {
        btnPrevDay.setOnClickListener(v -> {
            if (currentDay > 1) {
                currentDay--;
                loadMealPlanForDay(currentDay);
                loadFoodLogs();
            }
        });

        btnNextDay.setOnClickListener(v -> {
            if (currentDay < 7) {
                currentDay++;
                loadMealPlanForDay(currentDay);
                loadFoodLogs();
            }
        });

        btnGeneratePlan.setOnClickListener(v -> showGeneratePlanConfirmation());

        btnLogFood.setOnClickListener(v -> showLogFoodDialog());
    }

    private void generateNewMealPlan() {
        Toast.makeText(this, "Generating new meal plan... ", Toast.LENGTH_SHORT).show();

        firestore.collection("mealPlans").document(userId)
                .collection("days").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                    }
                    firestore.collection("mealPlans").document(userId)
                            .collection("metadata").document("info")
                            .delete();

                    planExists = false;
                    isLoadingComplete = false;
                    masterMeals.clear();

                    loadAllMeals();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete old plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showGeneratePlanConfirmation() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generate_plan, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setView(dialogView, 0, 0, 0, 0);

        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            generateNewMealPlan();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void showLogFoodDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog).create();

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_log_food, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setView(dialogView, 0, 0, 0, 0);

        EditText etFoodName = dialogView.findViewById(R.id.et_food_name);
        EditText etCalories = dialogView.findViewById(R.id.et_calories);
        EditText etProtein = dialogView.findViewById(R.id.et_protein);
        EditText etCarbs = dialogView.findViewById(R.id.et_carbs);
        EditText etFats = dialogView.findViewById(R.id.et_fats);
        Spinner spMealType = dialogView.findViewById(R.id.sp_meal_type);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnSave = dialogView.findViewById(R.id.btn_save);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, LOG_MEAL_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMealType.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String foodName = etFoodName.getText().toString().trim();
            String caloriesStr = etCalories.getText().toString().trim();

            if (foodName.isEmpty()) {
                etFoodName.setError("Food name required");
                return;
            }
            if (caloriesStr.isEmpty()) {
                etCalories.setError("Calories required");
                return;
            }

            int calories = Integer.parseInt(caloriesStr);
            float protein = 0, carbs = 0, fats = 0;

            if (!TextUtils.isEmpty(etProtein.getText().toString())) {
                protein = Float.parseFloat(etProtein.getText().toString());
            }
            if (!TextUtils.isEmpty(etCarbs.getText().toString())) {
                carbs = Float.parseFloat(etCarbs.getText().toString());
            }
            if (!TextUtils.isEmpty(etFats.getText().toString())) {
                fats = Float.parseFloat(etFats.getText().toString());
            }

            String mealType = spMealType.getSelectedItem().toString();
            saveFoodLog(foodName, calories, protein, carbs, fats, mealType);
            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void saveFoodLog(String foodName, int calories, float protein, float carbs, float fats, String mealType) {
        String logId = UUID.randomUUID().toString();
        String dayKey = "day" + currentDay;  // Use currentDay directly (1-7)

        android.util.Log.d("MealPlanner", "Saving food log to: " + dayKey);

        Map<String, Object> foodLog = new HashMap<>();
        foodLog.put("id", logId);
        foodLog.put("name", foodName);
        foodLog.put("calories", calories);
        foodLog.put("protein", protein);
        foodLog.put("carbs", carbs);
        foodLog.put("fats", fats);
        foodLog.put("mealType", mealType);
        foodLog.put("isDone", false);
        foodLog.put("date", todayDate);

        firestore.collection("foodLogs").document(userId)
                .collection(dayKey).document(logId)
                .set(foodLog)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✓ Food logged: " + foodName, Toast.LENGTH_SHORT).show();
                    loadFoodLogs();
                    loadMealPlanForDay(currentDay);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFoodLogs() {
        String dayKey = "day" + currentDay;  // Use currentDay directly (1-7)

        android.util.Log.d("MealPlanner", "Loading food logs from: " + dayKey);

        firestore.collection("foodLogs").document(userId)
                .collection(dayKey).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    foodLogs.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodLogItem item = new FoodLogItem();
                        item.id = doc.getString("id");
                        item.name = doc.getString("name");
                        item.calories = doc.getLong("calories") != null ? doc.getLong("calories").intValue() : 0;

                        Double proteinDouble = doc.getDouble("protein");
                        item.protein = proteinDouble != null ? proteinDouble.floatValue() : 0;

                        Double carbsDouble = doc.getDouble("carbs");
                        item.carbs = carbsDouble != null ? carbsDouble.floatValue() : 0;

                        Double fatsDouble = doc.getDouble("fats");
                        item.fats = fatsDouble != null ? fatsDouble.floatValue() : 0;

                        item.isDone = doc.getBoolean("isDone") != null && doc.getBoolean("isDone");
                        item.mealType = doc.getString("mealType");
                        foodLogs.add(item);
                    }

                    FoodLogAdapter foodAdapter = new FoodLogAdapter(foodLogs, this::onFoodLogToggled);
                    rvFoodLogs.setAdapter(foodAdapter);
                    recalculateTotalEaten();
                });
    }

    private void onFoodLogToggled(FoodLogItem item, boolean isChecked) {
        String dayKey = "day" + currentDay;

        firestore.collection("foodLogs").document(userId)
                .collection(dayKey).document(item.id)
                .update("isDone", isChecked)
                .addOnSuccessListener(aVoid -> {
                    item.isDone = isChecked;
                    recalculateTotalEaten();
                    Toast.makeText(this, (isChecked ? "✓ " : "✗ ") + item.name, Toast.LENGTH_SHORT).show();
                });
    }

    private void recalculateTotalEaten() {
        eatenCalories = 0;

        for (MealPlannerAdapter.MealItem meal : currentMeals) {
            if (meal.isDone) {
                eatenCalories += meal.calories;
            }
        }

        for (FoodLogItem log : foodLogs) {
            if (log.isDone) {
                eatenCalories += log.calories;
            }
        }

        updateCalorieDisplay();
    }

    private void checkAndLoadMealPlan() {
        firestore.collection("mealPlans").document(userId)
                .collection("days").document("day1")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        planExists = true;
                        loadPlanMetadata();
                    } else {
                        planExists = false;
                        loadAllMeals();
                    }
                })
                .addOnFailureListener(e -> {
                    loadAllMeals();
                });
    }

    private void loadPlanMetadata() {
        firestore.collection("mealPlans").document(userId)
                .collection("metadata").document("info")
                .get()
                .addOnSuccessListener(metadataDoc -> {
                    if (metadataDoc.exists()) {
                        planCreatedDayOfWeek = metadataDoc.getLong("createdDayOfWeek") != null ?
                                metadataDoc.getLong("createdDayOfWeek").intValue() : 1;
                    }
                    metadataLoaded = true;
                    loadUserData();
                })
                .addOnFailureListener(e -> {
                    metadataLoaded = true;
                    loadUserData();
                });
    }

    private void loadUserData() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        targetCalories = document.getLong("targetCalories") != null ?
                                document.getLong("targetCalories").intValue() : 2000;
                        dietType = document.getString("dietType") != null ?
                                document.getString("dietType") : "vegetarian";
                    }

                    if (!planExists && isLoadingComplete) {
                        generateAndSaveMealPlan();
                    } else if (planExists) {
                        loadMealPlanForDay(currentDay);
                        loadFoodLogs();
                    }
                });
    }

    private void loadAllMeals() {
        final int[] loadedCount = {0};
        final int totalTypes = MEAL_TYPES.length;

        for (String mealType : MEAL_TYPES) {
            firestore.collection("meals")
                    .whereEqualTo("type", mealType)
                    .whereEqualTo("dietType", dietType)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Map<String, Object>> meals = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Map<String, Object> meal = new HashMap<>();
                            meal.put("id", doc.getId());
                            meal.put("name", doc.getString("name"));
                            meal.put("calories", doc.getLong("calories"));
                            meal.put("protein", doc.getLong("protein"));
                            meal.put("carbs", doc.getLong("carbs"));
                            meal.put("fats", doc.getLong("fats"));
                            meals.add(meal);
                        }
                        masterMeals.put(mealType, meals);
                        loadedCount[0]++;

                        if (loadedCount[0] == totalTypes) {
                            isLoadingComplete = true;
                            new Handler().postDelayed(() -> {
                                if (!planExists) {
                                    generateAndSaveMealPlan();
                                }
                            }, 1000);
                        }
                    });
        }
    }

    private void generateAndSaveMealPlan() {
        for (int day = 1; day <= 7; day++) {
            String dayKey = "day" + day;
            firestore.collection("foodLogs").document(userId)
                    .collection(dayKey).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    });
        }

        Random random = new Random();

        boolean hasAllMeals = true;
        for (String mealType : MEAL_TYPES) {
            if (masterMeals.get(mealType) == null || masterMeals.get(mealType).isEmpty()) {
                hasAllMeals = false;
                Toast.makeText(this, "No " + mealType + " meals found for " + dietType, Toast.LENGTH_SHORT).show();
                break;
            }
        }

        if (!hasAllMeals) {
            return;
        }

        String createdDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        int createdDayOfWeek = getCurrentDayOfWeek();

        for (int day = 1; day <= 7; day++) {
            Map<String, Object> dayPlan = new HashMap<>();

            for (String mealType : MEAL_TYPES) {
                List<Map<String, Object>> meals = masterMeals.get(mealType);
                if (meals != null && !meals.isEmpty()) {
                    int randomIndex = random.nextInt(meals.size());
                    Map<String, Object> selectedMeal = meals.get(randomIndex);

                    dayPlan.put(mealType.toLowerCase() + "Id", selectedMeal.get("id"));
                    dayPlan.put(mealType.toLowerCase() + "Name", selectedMeal.get("name"));
                    dayPlan.put(mealType.toLowerCase() + "Calories", selectedMeal.get("calories"));
                    dayPlan.put(mealType.toLowerCase() + "Protein", selectedMeal.get("protein"));
                    dayPlan.put(mealType.toLowerCase() + "Carbs", selectedMeal.get("carbs"));
                    dayPlan.put(mealType.toLowerCase() + "Fats", selectedMeal.get("fats"));
                    dayPlan.put(mealType.toLowerCase() + "Done", false);
                }
            }

            firestore.collection("mealPlans").document(userId)
                    .collection("days").document("day" + day)
                    .set(dayPlan);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("createdAt", createdDate);
        metadata.put("createdDayOfWeek", createdDayOfWeek);
        metadata.put("createdTimestamp", System.currentTimeMillis());

        firestore.collection("mealPlans").document(userId)
                .collection("metadata").document("info")
                .set(metadata);

        planExists = true;
        planCreatedDayOfWeek = createdDayOfWeek;
        metadataLoaded = true;

        loadMealPlanForDay(currentDay);
        loadFoodLogs();
        Toast.makeText(this, "Your 7-day meal plan is ready! ", Toast.LENGTH_LONG).show();
    }

    private int getCurrentDayOfWeek() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1) {
            return 7;
        } else {
            return dayOfWeek - 1;
        }
    }

    private void loadMealPlanForDay(int day) {
        // Use currentDay directly - NO calculation
        String dayDoc = "day" + day;

        tvDayLabel.setText("Day " + day + " - " + DAY_NAMES[day - 1]);
        btnPrevDay.setEnabled(day > 1);
        btnNextDay.setEnabled(day < 7);

        android.util.Log.d("MealPlanner", "Loading meal plan from: " + dayDoc);

        firestore.collection("mealPlans").document(userId)
                .collection("days").document(dayDoc)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentMeals.clear();
                        eatenCalories = 0;

                        for (int i = 0; i < MEAL_TYPES.length; i++) {
                            String mealType = MEAL_TYPES[i].toLowerCase();
                            String name = document.getString(mealType + "Name");
                            if (name == null || name.isEmpty()) {
                                name = "Meal not available";
                            }

                            Long caloriesLong = document.getLong(mealType + "Calories");
                            int calories = caloriesLong != null ? caloriesLong.intValue() : 0;

                            Long proteinLong = document.getLong(mealType + "Protein");
                            int protein = proteinLong != null ? proteinLong.intValue() : 0;

                            Long carbsLong = document.getLong(mealType + "Carbs");
                            int carbs = carbsLong != null ? carbsLong.intValue() : 0;

                            Long fatsLong = document.getLong(mealType + "Fats");
                            int fats = fatsLong != null ? fatsLong.intValue() : 0;

                            Boolean isDone = document.getBoolean(mealType + "Done");
                            boolean done = isDone != null && isDone;

                            MealPlannerAdapter.MealItem meal = new MealPlannerAdapter.MealItem(
                                    MEAL_TYPES[i], MEAL_ICONS[i], name, calories, protein, carbs, fats, done
                            );
                            currentMeals.add(meal);

                            if (done) {
                                eatenCalories += calories;
                            }
                        }

                        updateCalorieDisplay();
                        MealPlannerAdapter adapter = new MealPlannerAdapter(currentMeals, this::onMealDone);
                        rvMeals.setAdapter(adapter);
                    } else {
                        android.util.Log.d("MealPlanner", "No meal plan found for " + dayDoc);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load meal plan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCalorieDisplay() {
        int remaining = Math.max(0, targetCalories - eatenCalories);
        tvTotalCalories.setText("Eaten: " + eatenCalories + " kcal");
        tvRemainingCalories.setText("Remaining: " + remaining + " kcal");
        progressDay.setMax(targetCalories);
        progressDay.setProgress(Math.min(eatenCalories, targetCalories));
    }

    private void onMealDone(MealPlannerAdapter.MealItem meal, boolean isChecked) {
        // REMOVE the complex calculation - just use currentDay directly
        String dayDoc = "day" + currentDay;  // currentDay is 1-7 directly

        android.util.Log.d("MealPlanner", "Saving meal to: " + dayDoc + " (currentDay: " + currentDay + ")");

        if (isChecked) {
            eatenCalories += meal.calories;
        } else {
            eatenCalories -= meal.calories;
        }

        String mealType = meal.type.toLowerCase();

        firestore.collection("mealPlans").document(userId)
                .collection("days").document(dayDoc)
                .update(mealType + "Done", isChecked)
                .addOnSuccessListener(aVoid -> {
                    updateCalorieDisplay();
                    String status = isChecked ? "completed ✓" : "unmarked";
                    Toast.makeText(this, meal.name + " " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (isChecked) {
                        eatenCalories -= meal.calories;
                    } else {
                        eatenCalories += meal.calories;
                    }
                    updateCalorieDisplay();
                    Toast.makeText(this, "Failed to update", Toast.LENGTH_SHORT).show();
                });
    }

    // FoodLogItem class
    public static class FoodLogItem {
        public String id;
        public String name;
        public int calories;
        public float protein;
        public float carbs;
        public float fats;
        public boolean isDone;
        public String mealType;
    }

    // FoodLogAdapter class
    public class FoodLogAdapter extends RecyclerView.Adapter<FoodLogAdapter.ViewHolder> {
        private List<FoodLogItem> items;
        private OnFoodLogToggleListener listener;

        public interface OnFoodLogToggleListener {
            void onToggle(FoodLogItem item, boolean isChecked);
        }

        public FoodLogAdapter(List<FoodLogItem> items, OnFoodLogToggleListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_food_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FoodLogItem item = items.get(position);
            String icon = getMealIcon(item.mealType);
            holder.tvMealIcon.setText(icon);
            holder.tvMealType.setText(item.mealType);
            holder.tvFoodName.setText(item.name);
            holder.tvFoodCalories.setText(item.calories + " kcal");
            holder.tvMacros.setText("P:" + (int)item.protein + "g  C:" + (int)item.carbs + "g  F:" + (int)item.fats + "g");

            holder.cbFoodDone.setOnCheckedChangeListener(null);
            holder.cbFoodDone.setChecked(item.isDone);
            holder.cbFoodDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onToggle(item, isChecked);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private String getMealIcon(String mealType) {
            switch (mealType) {
                case "Breakfast": return "🍞";
                case "Lunch": return "🍱";
                case "Dinner": return "🍲";
                case "Snack": return "🍎";
                default: return "📝";
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMealIcon, tvMealType, tvFoodName, tvFoodCalories, tvMacros;
            CheckBox cbFoodDone;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMealIcon = itemView.findViewById(R.id.tv_meal_icon);
                tvMealType = itemView.findViewById(R.id.tv_meal_type);
                tvFoodName = itemView.findViewById(R.id.tv_food_name);
                tvFoodCalories = itemView.findViewById(R.id.tv_food_calories);
                tvMacros = itemView.findViewById(R.id.tv_macros);
                cbFoodDone = itemView.findViewById(R.id.cb_food_done);
            }
        }
    }
}