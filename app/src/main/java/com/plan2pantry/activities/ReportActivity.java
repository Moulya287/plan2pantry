package com.plan2pantry.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.plan2pantry.R;
import com.plan2pantry.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportActivity extends AppCompatActivity {

    private TextView tvCurrentStreak, tvBestStreak, tvAdherenceScore, tvAdherenceMessage;
    private TextView tvTotalCalories, tvAvgProtein, tvAvgCarbs, tvAvgFats;
    private LinearLayout waterChartContainer, mealGridContainer, badgesContainer;


    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;

    private int targetCalories;

    // Store weekly data
    private Map<String, List<Boolean>> weeklyMealCompletion = new HashMap<>();
    private Map<String, Float> weeklyWaterIntake = new HashMap<>();
    private Map<String, Integer> weeklyCalories = new HashMap<>();
    private Map<String, Float> weeklyProtein = new HashMap<>();
    private Map<String, Float> weeklyCarbs = new HashMap<>();
    private Map<String, Float> weeklyFats = new HashMap<>();

    private String[] dayNames = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String[] dayDocs = {"day1", "day2", "day3", "day4", "day5", "day6", "day7"};
    private String[] mealTypes = {"breakfast", "lunch", "dinner", "snack"};
    private String[] mealIcons = {"🍞", "🍱", "🍲", "🍎"};

    private int dataLoadedCount = 0;
    private final int totalDays = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }

        setContentView(R.layout.activity_report);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();
        targetCalories = session.getTargetCalories();

        initViews();
        loadUserData();
        loadWeeklyData();
    }

    private void initViews() {
        tvCurrentStreak = findViewById(R.id.tv_current_streak);
        tvBestStreak = findViewById(R.id.tv_best_streak);
        tvAdherenceScore = findViewById(R.id.tv_adherence_score);
        tvAdherenceMessage = findViewById(R.id.tv_adherence_message);
        tvTotalCalories = findViewById(R.id.tv_total_calories);
        tvAvgProtein = findViewById(R.id.tv_avg_protein);
        tvAvgCarbs = findViewById(R.id.tv_avg_carbs);
        tvAvgFats = findViewById(R.id.tv_avg_fats);
        waterChartContainer = findViewById(R.id.water_chart_container);
        mealGridContainer = findViewById(R.id.meal_grid_container);
        badgesContainer = findViewById(R.id.badges_container);

    }

    private void loadUserData() {
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        targetCalories = document.getLong("targetCalories") != null ?
                                document.getLong("targetCalories").intValue() : 2000;
                    }
                });
    }

    private void loadWeeklyData() {
        for (int i = 0; i < dayDocs.length; i++) {
            final int index = i;
            final String dayDoc = dayDocs[i];

            // Load meal plan data
            firestore.collection("mealPlans").document(userId)
                    .collection("days").document(dayDoc)
                    .get()
                    .addOnSuccessListener(document -> {
                        loadMealDataForDay(dayDoc, document, index);
                    });

            // Load water intake
            loadWaterForDay(dayDoc, index);

            // Load food logs
            loadFoodLogsForDay(dayDoc, index);
        }
    }

    private void loadMealDataForDay(String dayDoc, DocumentSnapshot document, int dayIndex) {
        List<Boolean> mealsCompleted = new ArrayList<>();
        int dayCalories = 0;
        float dayProtein = 0, dayCarbs = 0, dayFats = 0;

        if (document.exists()) {
            for (String mealType : mealTypes) {
                Boolean isDone = document.getBoolean(mealType + "Done");
                mealsCompleted.add(isDone != null && isDone);

                if (isDone != null && isDone) {
                    Long calories = document.getLong(mealType + "Calories");
                    Long protein = document.getLong(mealType + "Protein");
                    Long carbs = document.getLong(mealType + "Carbs");
                    Long fats = document.getLong(mealType + "Fats");

                    if (calories != null) dayCalories += calories;
                    if (protein != null) dayProtein += protein;
                    if (carbs != null) dayCarbs += carbs;
                    if (fats != null) dayFats += fats;
                }
            }
        } else {
            for (int i = 0; i < mealTypes.length; i++) {
                mealsCompleted.add(false);
            }
        }

        weeklyMealCompletion.put(dayDoc, mealsCompleted);

        // Add to weekly totals
        if (!weeklyCalories.containsKey(dayDoc)) weeklyCalories.put(dayDoc, 0);
        weeklyCalories.put(dayDoc, weeklyCalories.get(dayDoc) + dayCalories);

        if (!weeklyProtein.containsKey(dayDoc)) weeklyProtein.put(dayDoc, 0f);
        weeklyProtein.put(dayDoc, weeklyProtein.get(dayDoc) + dayProtein);

        if (!weeklyCarbs.containsKey(dayDoc)) weeklyCarbs.put(dayDoc, 0f);
        weeklyCarbs.put(dayDoc, weeklyCarbs.get(dayDoc) + dayCarbs);

        if (!weeklyFats.containsKey(dayDoc)) weeklyFats.put(dayDoc, 0f);
        weeklyFats.put(dayDoc, weeklyFats.get(dayDoc) + dayFats);

        checkIfAllDataLoaded();
    }

    private void loadFoodLogsForDay(String dayDoc, int dayIndex) {
        firestore.collection("foodLogs").document(userId)
                .collection(dayDoc).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int dayCalories = 0;
                    float dayProtein = 0, dayCarbs = 0, dayFats = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean isDone = doc.getBoolean("isDone");
                        if (isDone != null && isDone) {
                            Long calories = doc.getLong("calories");
                            Double protein = doc.getDouble("protein");
                            Double carbs = doc.getDouble("carbs");
                            Double fats = doc.getDouble("fats");

                            if (calories != null) dayCalories += calories;
                            if (protein != null) dayProtein += protein;
                            if (carbs != null) dayCarbs += carbs;
                            if (fats != null) dayFats += fats;
                        }
                    }

                    // Add to weekly totals
                    if (!weeklyCalories.containsKey(dayDoc)) weeklyCalories.put(dayDoc, 0);
                    weeklyCalories.put(dayDoc, weeklyCalories.get(dayDoc) + dayCalories);

                    if (!weeklyProtein.containsKey(dayDoc)) weeklyProtein.put(dayDoc, 0f);
                    weeklyProtein.put(dayDoc, weeklyProtein.get(dayDoc) + dayProtein);

                    if (!weeklyCarbs.containsKey(dayDoc)) weeklyCarbs.put(dayDoc, 0f);
                    weeklyCarbs.put(dayDoc, weeklyCarbs.get(dayDoc) + dayCarbs);

                    if (!weeklyFats.containsKey(dayDoc)) weeklyFats.put(dayDoc, 0f);
                    weeklyFats.put(dayDoc, weeklyFats.get(dayDoc) + dayFats);

                    checkIfAllDataLoaded();
                });
    }

    private void loadWaterForDay(String dayDoc, int dayIndex) {
        String date = getDateForDay(dayIndex);

        firestore.collection("waterIntake").document(userId)
                .collection(date).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    int currentMl = 0;
                    if (document.exists()) {
                        currentMl = document.getLong("totalMl") != null ?
                                document.getLong("totalMl").intValue() : 0;
                    }
                    float waterL = currentMl / 1000f;
                    weeklyWaterIntake.put(dayDoc, waterL);
                    checkIfAllDataLoaded();
                });
    }

    private String getDateForDay(int dayIndex) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Convert to Monday=1 format
        int todayIndex;
        if (currentDayOfWeek == Calendar.SUNDAY) {
            todayIndex = 7;
        } else {
            todayIndex = currentDayOfWeek - 1;
        }

        int offset = dayIndex + 1 - todayIndex;
        calendar.add(Calendar.DAY_OF_YEAR, offset);

        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
    }

    private void checkIfAllDataLoaded() {
        dataLoadedCount++;
        if (dataLoadedCount >= totalDays * 3) {
            dataLoadedCount = 0;
            runOnUiThread(() -> {
                updateStreak();
                updateAdherenceScore();
                updateWaterChart();
                updateMealGrid();
                updateWeeklySummary();
                updateBadges();
            });
        }
    }

    private void updateStreak() {
        int currentStreak = 0;
        int bestStreak = 0;
        int todayIndex = getTodayPlanDay() - 1;

        // Calculate current streak
        for (int i = todayIndex; i >= 0; i--) {
            String dayDoc = dayDocs[i];
            List<Boolean> meals = weeklyMealCompletion.get(dayDoc);
            int completedCount = 0;
            if (meals != null) {
                for (Boolean completed : meals) {
                    if (completed) completedCount++;
                }
            }
            int totalPossible = 4;
            float percentage = (completedCount * 100f) / totalPossible;

            if (percentage >= 50) {
                currentStreak++;
            } else {
                break;
            }
        }

        // Calculate best streak
        int tempStreak = 0;
        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            List<Boolean> meals = weeklyMealCompletion.get(dayDoc);
            int completedCount = 0;
            if (meals != null) {
                for (Boolean completed : meals) {
                    if (completed) completedCount++;
                }
            }
            int totalPossible = 4;
            float percentage = (completedCount * 100f) / totalPossible;

            if (percentage >= 50) {
                tempStreak++;
                bestStreak = Math.max(bestStreak, tempStreak);
            } else {
                tempStreak = 0;
            }
        }

        tvCurrentStreak.setText(String.valueOf(currentStreak));
        tvBestStreak.setText(String.valueOf(bestStreak));
    }

    private void updateAdherenceScore() {
        int totalCompleted = 0;
        int totalPossible = totalDays * 4;

        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            List<Boolean> meals = weeklyMealCompletion.get(dayDoc);
            if (meals != null) {
                for (Boolean completed : meals) {
                    if (completed) totalCompleted++;
                }
            }
        }

        int score = (totalCompleted * 100) / totalPossible;
        tvAdherenceScore.setText(score + "%");

        String message;
        if (score >= 90) {
            message = "🌟 Outstanding! You're a meal master! Keep it up!";
        } else if (score >= 70) {
            message = "💪 Great job! You're building healthy habits!";
        } else if (score >= 50) {
            message = "👍 Good progress! Let's aim higher next week!";
        } else if (score >= 30) {
            message = "🌱 Every meal counts! You're getting there!";
        } else {
            message = "🎯 Start small! Try completing one meal each day!";
        }
        tvAdherenceMessage.setText(message);
    }

    private void updateWaterChart() {
        waterChartContainer.removeAllViews();

        float targetWater = session.getTargetWater();

        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            float waterAmount = weeklyWaterIntake.getOrDefault(dayDoc, 0f);
            int percentage = (int) ((waterAmount * 100) / targetWater);
            percentage = Math.min(percentage, 100);

            LinearLayout dayRow = new LinearLayout(this);
            dayRow.setOrientation(LinearLayout.HORIZONTAL);
            dayRow.setPadding(0, 12, 0, 12);
            dayRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView dayLabel = new TextView(this);
            dayLabel.setText(dayNames[i].substring(0, 3));
            dayLabel.setTextColor(Color.parseColor("#341C02"));
            dayLabel.setTextSize(12);
            dayLabel.setWidth(80);

            LinearLayout progressContainer = new LinearLayout(this);
            progressContainer.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            progressContainer.setLayoutParams(containerParams);
            progressContainer.setBackgroundColor(Color.parseColor("#33000000"));

            View fillBar = new View(this);
            fillBar.setBackgroundColor(Color.parseColor("#341C02"));
            fillBar.setLayoutParams(new LinearLayout.LayoutParams(0, 30));
            progressContainer.addView(fillBar);

            TextView waterText = new TextView(this);
            waterText.setText(String.format(Locale.getDefault(), "%.1fL", waterAmount));
            waterText.setTextColor(Color.parseColor("#341C02"));
            waterText.setTextSize(12);
            waterText.setPadding(8, 0, 8, 0);
            waterText.setSingleLine(true);  // Force single line
            waterText.setWidth(80);  // Increased width to 80dp

            dayRow.addView(dayLabel);
            dayRow.addView(progressContainer);
            dayRow.addView(waterText);

            waterChartContainer.addView(dayRow);

            // Update fill bar width after layout
            final int finalPercentage = percentage;
            fillBar.post(() -> {
                int containerWidth = progressContainer.getWidth();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fillBar.getLayoutParams();
                params.width = (containerWidth * finalPercentage) / 100;
                fillBar.setLayoutParams(params);
            });
        }
    }

    private void updateMealGrid() {
        mealGridContainer.removeAllViews();

        // Remove container padding to use full width
        mealGridContainer.setPadding(0, 0, 0, 0);

        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            List<Boolean> meals = weeklyMealCompletion.get(dayDoc);

            // Day row with full width
            LinearLayout dayRow = new LinearLayout(this);
            dayRow.setOrientation(LinearLayout.HORIZONTAL);
            dayRow.setPadding(8, 16, 8, 16);
            dayRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Add separator line between days
            if (i > 0) {
                View divider = new View(this);
                divider.setBackgroundColor(Color.parseColor("#33FFFFFF"));
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                dividerParams.setMargins(8, 0, 8, 0);
                divider.setLayoutParams(dividerParams);
                mealGridContainer.addView(divider);
            }

            // Day label - using weight instead of fixed width
            TextView dayLabel = new TextView(this);
            dayLabel.setText(dayNames[i]);
            dayLabel.setTextColor(Color.parseColor("#FFFFFF"));
            dayLabel.setTextSize(13);
            dayLabel.setPadding(0, 0, 8, 0);
            dayLabel.setSingleLine(true);
            // Give day label 25% of the space (adjust as needed)
            dayLabel.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.25f));

            // Highlight current day
            int todayPlanDay = getTodayPlanDay();
            if (i + 1 == todayPlanDay) {
                dayLabel.setTextColor(Color.parseColor("#341C02"));
                dayLabel.setTypeface(dayLabel.getTypeface(), android.graphics.Typeface.BOLD);
            }

            // Meals container - spread evenly across remaining space
            LinearLayout mealsContainer = new LinearLayout(this);
            mealsContainer.setOrientation(LinearLayout.HORIZONTAL);
            mealsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.75f));  // Take 75% of space
            mealsContainer.setWeightSum(4);

            if (meals != null) {
                for (int j = 0; j < meals.size(); j++) {
                    Boolean completed = meals.get(j);

                    // Each meal item - spread evenly
                    LinearLayout mealItem = new LinearLayout(this);
                    mealItem.setOrientation(LinearLayout.HORIZONTAL);
                    mealItem.setGravity(android.view.Gravity.CENTER);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                    mealItem.setLayoutParams(params);
                    mealItem.setPadding(4, 0, 4, 0);

                    // Meal icon
                    TextView mealIcon = new TextView(this);
                    mealIcon.setText(mealIcons[j]);
                    mealIcon.setTextSize(16);
                    mealIcon.setPadding(0, 0, 4, 0);

                    // Status indicator
                    TextView statusIcon = new TextView(this);
                    if (completed) {
                        statusIcon.setText("✅");
                        statusIcon.setTextColor(Color.parseColor("#4CAF50"));
                    } else {
                        statusIcon.setText("❌");
                        statusIcon.setTextColor(Color.parseColor("#FF5722"));
                    }
                    statusIcon.setTextSize(12);

                    mealItem.addView(mealIcon);
                    mealItem.addView(statusIcon);
                    mealsContainer.addView(mealItem);
                }
            }

            dayRow.addView(dayLabel);
            dayRow.addView(mealsContainer);
            mealGridContainer.addView(dayRow);
        }
    }

    private void updateWeeklySummary() {
        int totalWeekCalories = 0;
        float totalProtein = 0, totalCarbs = 0, totalFats = 0;

        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            totalWeekCalories += weeklyCalories.getOrDefault(dayDoc, 0);
            totalProtein += weeklyProtein.getOrDefault(dayDoc, 0f);
            totalCarbs += weeklyCarbs.getOrDefault(dayDoc, 0f);
            totalFats += weeklyFats.getOrDefault(dayDoc, 0f);
        }

        tvTotalCalories.setText(String.valueOf(totalWeekCalories));
        tvAvgProtein.setText(Math.round(totalProtein / totalDays) + "g");
        tvAvgCarbs.setText(Math.round(totalCarbs / totalDays) + "g");
        tvAvgFats.setText(Math.round(totalFats / totalDays) + "g");
    }

    private void updateBadges() {
        badgesContainer.removeAllViews();

        int totalCompleted = 0;
        int totalPossible = totalDays * 4;
        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            List<Boolean> meals = weeklyMealCompletion.get(dayDoc);
            if (meals != null) {
                for (Boolean completed : meals) {
                    if (completed) totalCompleted++;
                }
            }
        }
        int adherenceScore = (totalCompleted * 100) / totalPossible;

        // Check water badges
        int perfectWaterDays = 0;
        float targetWater = session.getTargetWater();
        for (int i = 0; i < totalDays; i++) {
            String dayDoc = dayDocs[i];
            float waterAmount = weeklyWaterIntake.getOrDefault(dayDoc, 0f);
            if (waterAmount >= targetWater) {
                perfectWaterDays++;
            }
        }

        // Check streak
        int currentStreak = Integer.parseInt(tvCurrentStreak.getText().toString());

        List<String> earnedBadges = new ArrayList<>();

        if (adherenceScore >= 70) {
            earnedBadges.add("🍽️ Meal Master");
        }
        if (perfectWaterDays >= 5) {
            earnedBadges.add("💧 Hydration Hero");
        }
        if (currentStreak >= 3) {
            earnedBadges.add("🔥 Streak Warrior");
        }
        if (adherenceScore >= 90) {
            earnedBadges.add("🌟 Perfect Week");
        }

        if (earnedBadges.isEmpty()) {
            TextView noBadges = new TextView(this);
            noBadges.setText("Complete meals and stay hydrated to unlock badges! 🎯");
            noBadges.setTextColor(Color.parseColor("#341C02"));
            noBadges.setTextSize(12);
            badgesContainer.addView(noBadges);
        } else {
            LinearLayout badgesRow = new LinearLayout(this);
            badgesRow.setOrientation(LinearLayout.HORIZONTAL);
            badgesRow.setPadding(0, 8, 0, 8);

            for (String badge : earnedBadges) {
                TextView badgeView = new TextView(this);
                badgeView.setText(badge);
                badgeView.setTextColor(Color.parseColor("#341C02"));
                badgeView.setTextSize(13);
                badgeView.setPadding(0, 8, 16, 8);
                badgesRow.addView(badgeView);
            }
            badgesContainer.addView(badgesRow);
        }
    }

    private int getTodayPlanDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.MONDAY: return 1;
            case Calendar.TUESDAY: return 2;
            case Calendar.WEDNESDAY: return 3;
            case Calendar.THURSDAY: return 4;
            case Calendar.FRIDAY: return 5;
            case Calendar.SATURDAY: return 6;
            case Calendar.SUNDAY: return 7;
            default: return 1;
        }
    }
}