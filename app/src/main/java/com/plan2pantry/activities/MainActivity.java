package com.plan2pantry.activities;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.recyclerview.widget.LinearSnapHelper;
import com.plan2pantry.adapters.QuoteAdapter;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plan2pantry.R;
import com.plan2pantry.adapters.QuoteAdapter;
import com.plan2pantry.utils.NotificationHelper;
import com.plan2pantry.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    private ImageView ivImage1, ivImage2;

    private RecyclerView rvQuoteCarousel;
    private LinearLayout dotsContainer;
    private QuoteAdapter quoteAdapter;
    private List<Integer> imageList = new ArrayList<>();
    private int currentPosition = 0;
    private LinearLayout bottomNavHome, bottomNavMeals, bottomNavReport, bottomNavProfile;
    private de.hdodenhof.circleimageview.CircleImageView ivProfileCircle;
    private TextView tvProfileCircleInitials;
    private LinearLayout btnProfileCircle;
    private TextView tvGreeting, tvDate, tvCaloriesEaten, tvCaloriesTarget;
    private TextView tvProteinVal, tvCarbsVal, tvFatsVal;
    private TextView tvWaterAmount, tvGroceryCount;
    private ProgressBar progressCalories, progressProtein, progressCarbs, progressFats;
    private Button btnAddWater;
    private LinearLayout cardMealPlanner, cardGrocery, cardReport, cardProfile, cardWater, cardGroceryMain;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private String userId;

    private float currentEatenCalories = 0;
    private float currentEatenProtein = 0;
    private float currentEatenCarbs = 0;
    private float currentEatenFats = 0;

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

        setContentView(R.layout.activity_main);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        userId = session.getFirebaseUid();

        initViews();
        loadDashboard();
        ivImage1.setImageResource(R.drawable.image_1);
        ivImage2.setImageResource(R.drawable.image_2);
        setupQuoteCarousel();
        setListeners();
        requestNotificationPermission();
        NotificationHelper.createNotificationChannels(this);
        NotificationHelper.scheduleWaterReminders(this);
        setupBottomNavigationPadding();
        View scrollView = findViewById(R.id.scrollView);


        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int sidePadding = (int) (16 * getResources().getDisplayMetrics().density);
            int navHeight = (int) (110 * getResources().getDisplayMetrics().density);

            v.setPadding(sidePadding, sidePadding, sidePadding, bottomInset + navHeight);

            return insets;
        });
    }

    private void initViews() {
        bottomNavHome = findViewById(R.id.bottom_nav_home);
        bottomNavMeals = findViewById(R.id.bottom_nav_meals);
        bottomNavReport = findViewById(R.id.bottom_nav_report);
        bottomNavProfile = findViewById(R.id.bottom_nav_profile);
        ivProfileCircle = findViewById(R.id.iv_profile_circle);
        tvProfileCircleInitials = findViewById(R.id.tv_profile_circle_initials);
        btnProfileCircle = findViewById(R.id.btn_profile_circle);
        tvGreeting = findViewById(R.id.tv_greeting);
        tvDate = findViewById(R.id.tv_date);
        tvCaloriesEaten = findViewById(R.id.tv_calories_eaten);
        tvCaloriesTarget = findViewById(R.id.tv_calories_target);
        tvProteinVal = findViewById(R.id.tv_protein_val);
        tvCarbsVal = findViewById(R.id.tv_carbs_val);
        tvFatsVal = findViewById(R.id.tv_fats_val);
        tvWaterAmount = findViewById(R.id.tv_water_amount);
        tvGroceryCount = findViewById(R.id.tv_grocery_count);
        progressCalories = findViewById(R.id.progress_calories);
        progressProtein = findViewById(R.id.progress_protein);
        progressCarbs = findViewById(R.id.progress_carbs);
        progressFats = findViewById(R.id.progress_fats);
        btnAddWater = findViewById(R.id.btn_add_water);
        cardMealPlanner = findViewById(R.id.card_ai_meal_plan);
        cardGrocery = findViewById(R.id.card_grocery);
        cardReport = findViewById(R.id.card_report);
        cardProfile = findViewById(R.id.card_profile);
        cardWater = findViewById(R.id.card_water);
        cardGroceryMain = findViewById(R.id.card_grocery_main);
        rvQuoteCarousel = findViewById(R.id.rv_quote_carousel);
        dotsContainer = findViewById(R.id.dots_container);
        ivImage1 = findViewById(R.id.iv_image_1);
        ivImage2 = findViewById(R.id.iv_image_2);
    }

    private void loadDashboard() {
        String name = session.getUserName();
        String greeting = getGreeting();
        tvGreeting.setText(greeting + ", " + name);

        String currentDate = new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(currentDate);

        tvCaloriesEaten.setText("0");
        tvCaloriesTarget.setText("/ --- kcal");
        tvProteinVal.setText("0g / ---");
        tvCarbsVal.setText("0g / ---");
        tvFatsVal.setText("0g / ---");
        tvWaterAmount.setText("--- / ---");

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        updateUIWithUserData(documentSnapshot);
                        loadTodayData();
                    } else {
                        Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Get today's plan day number based on REAL calendar day
     * SAME MAPPING AS MEAL PLANNER:
     * Monday = Day 1
     * Tuesday = Day 2
     * Wednesday = Day 3
     * Thursday = Day 4
     * Friday = Day 5
     * Saturday = Day 6
     * Sunday = Day 7
     */
    private int getTodayPlanDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Convert Android's day format to our fixed mapping
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

    private String getDayName(int day) {
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return day >= 1 && day <= 7 ? days[day] : "Unknown";
    }

    private void loadTodayData() {
        int todayPlanDay = getTodayPlanDay();
        String dayDoc = "day" + todayPlanDay;

        // Log for debugging
        android.util.Log.d("MainActivity", "=====================================");
        android.util.Log.d("MainActivity", "TODAY IS: " + getDayName(todayPlanDay) + " (Day " + todayPlanDay + ")");
        android.util.Log.d("MainActivity", "Loading data from Firestore: " + dayDoc);
        android.util.Log.d("MainActivity", "=====================================");

        // Reset values
        currentEatenCalories = 0;
        currentEatenProtein = 0;
        currentEatenCarbs = 0;
        currentEatenFats = 0;

        // Load meal plan for today's real day
        loadMealPlanForToday(dayDoc);
    }

    private void loadMealPlanForToday(String dayDoc) {
        firestore.collection("mealPlans").document(userId)
                .collection("days").document(dayDoc)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String[] mealTypes = {"breakfast", "lunch", "dinner", "snack"};
                        for (String mealType : mealTypes) {
                            Boolean isDone = document.getBoolean(mealType + "Done");
                            if (isDone != null && isDone) {
                                float calories = getFloatValue(document.get(mealType + "Calories"));
                                float protein = getFloatValue(document.get(mealType + "Protein"));
                                float carbs = getFloatValue(document.get(mealType + "Carbs"));
                                float fats = getFloatValue(document.get(mealType + "Fats"));

                                currentEatenCalories += calories;
                                currentEatenProtein += protein;
                                currentEatenCarbs += carbs;
                                currentEatenFats += fats;

                                android.util.Log.d("MainActivity", "✓ Meal: " + mealType + " (Done) - Calories: " + calories);
                            }
                        }
                    } else {
                        android.util.Log.d("MainActivity", "No meal plan found for " + dayDoc);
                    }
                    loadFoodLogsForToday(dayDoc);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MainActivity", "Error loading meal plan", e);
                    loadFoodLogsForToday(dayDoc);
                });
    }

    private void loadFoodLogsForToday(String dayDoc) {
        firestore.collection("foodLogs").document(userId)
                .collection(dayDoc).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int logCount = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Boolean isDone = doc.getBoolean("isDone");
                        if (isDone != null && isDone) {
                            logCount++;
                            float calories = getFloatValue(doc.get("calories"));
                            float protein = getFloatValue(doc.get("protein"));
                            float carbs = getFloatValue(doc.get("carbs"));
                            float fats = getFloatValue(doc.get("fats"));

                            currentEatenCalories += calories;
                            currentEatenProtein += protein;
                            currentEatenCarbs += carbs;
                            currentEatenFats += fats;

                            android.util.Log.d("MainActivity", "✓ Food Log: " + doc.getString("name") + " - Calories: " + calories);
                        }
                    }
                    android.util.Log.d("MainActivity", "Total completed food logs: " + logCount);
                    android.util.Log.d("MainActivity", "TOTAL CALORIES FOR TODAY: " + currentEatenCalories);
                    android.util.Log.d("MainActivity", "=====================================");
                    updateEatenUI();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MainActivity", "Error loading food logs", e);
                    updateEatenUI();
                });
    }

    private float getFloatValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Long) return ((Long) value).floatValue();
        if (value instanceof Double) return ((Double) value).floatValue();
        if (value instanceof Integer) return ((Integer) value).floatValue();
        return 0;
    }

    private void updateUIWithUserData(DocumentSnapshot document) {
        int targetCalories = document.getLong("targetCalories") != null ?
                document.getLong("targetCalories").intValue() : 2000;
        int targetProtein = document.getLong("targetProtein") != null ?
                document.getLong("targetProtein").intValue() : 100;
        int targetCarbs = document.getLong("targetCarbs") != null ?
                document.getLong("targetCarbs").intValue() : 200;
        int targetFats = document.getLong("targetFats") != null ?
                document.getLong("targetFats").intValue() : 50;
        double targetWater = document.getDouble("targetWater") != null ?
                document.getDouble("targetWater") : 2.5;

        session.updateTargetCalories(targetCalories);
        session.updateTargetProtein(targetProtein);
        session.updateTargetCarbs(targetCarbs);
        session.updateTargetFats(targetFats);

        tvCaloriesTarget.setText("/ " + targetCalories + " kcal");
        tvWaterAmount.setText(String.format(Locale.getDefault(), "0.0L / %.1fL", targetWater));

        progressCalories.setMax(targetCalories);
        progressProtein.setMax(targetProtein);
        progressCarbs.setMax(targetCarbs);
        progressFats.setMax(targetFats);
    }

    private void updateEatenUI() {
        int targetCalories = session.getTargetCalories();
        int targetProtein = session.getTargetProtein();
        int targetCarbs = session.getTargetCarbs();
        int targetFats = session.getTargetFats();

        int displayCalories = Math.round(currentEatenCalories);
        int displayProtein = Math.round(currentEatenProtein);
        int displayCarbs = Math.round(currentEatenCarbs);
        int displayFats = Math.round(currentEatenFats);

        tvCaloriesEaten.setText(String.valueOf(displayCalories));
        tvProteinVal.setText(displayProtein + "g / " + targetProtein + "g");
        tvCarbsVal.setText(displayCarbs + "g / " + targetCarbs + "g");
        tvFatsVal.setText(displayFats + "g / " + targetFats + "g");

        progressCalories.setProgress(Math.min(displayCalories, targetCalories));
        progressProtein.setProgress(Math.min(displayProtein, targetProtein));
        progressCarbs.setProgress(Math.min(displayCarbs, targetCarbs));
        progressFats.setProgress(Math.min(displayFats, targetFats));
    }

    private String getGreeting() {
        int hour = Integer.parseInt(new SimpleDateFormat("HH", Locale.getDefault()).format(new Date()));
        if (hour < 12) return "Good Morning";
        else if (hour < 17) return "Good Afternoon";
        else return "Good Evening";
    }

    private void setListeners() {
        // Bottom Navigation Listeners
        bottomNavHome.setOnClickListener(v -> {
            // Already on home, just scroll to top
            // Optional: add scroll to top functionality
        });

        bottomNavMeals.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MealPlannerActivity.class)));

        bottomNavReport.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ReportActivity.class)));

        bottomNavProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        cardWater.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WaterTrackerActivity.class)));
        btnAddWater.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WaterTrackerActivity.class)));
        cardMealPlanner.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MealPlannerActivity.class)));
        cardGrocery.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GroceryActivity.class)));
        cardGroceryMain.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GroceryActivity.class)));
        cardReport.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ReportActivity.class));
        });
        btnProfileCircle.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        cardProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh all data when returning to main page
        loadTodayData();
        loadTodayWater();
        loadGroceryCount();
        loadProfileCircle();
    }

    private void loadTodayWater() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        firestore.collection("waterIntake").document(userId)
                .collection(todayDate).document("data")
                .get()
                .addOnSuccessListener(document -> {
                    int currentMl = 0;
                    if (document.exists()) {
                        currentMl = document.getLong("totalMl") != null ?
                                document.getLong("totalMl").intValue() : 0;
                    }
                    float waterL = currentMl / 1000f;
                    float targetWater = session.getTargetWater();
                    tvWaterAmount.setText(String.format(Locale.getDefault(), "%.1fL / %.1fL", waterL, targetWater));
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void loadGroceryCount() {
        firestore.collection("grocery").document(userId)
                .collection("items")
                .whereEqualTo("status", "active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    tvGroceryCount.setText(count + " items");
                })
                .addOnFailureListener(e -> {
                    tvGroceryCount.setText("0 items");
                });
    }
    private void loadProfileCircle() {
        String name = session.getUserName();

        // Set initials
        if (name != null && !name.isEmpty()) {
            String[] nameParts = name.split(" ");
            String initials;
            if (nameParts.length >= 2) {
                initials = String.valueOf(nameParts[0].charAt(0)) +
                        String.valueOf(nameParts[1].charAt(0));
            } else {
                initials = String.valueOf(nameParts[0].charAt(0));
            }
            tvProfileCircleInitials.setText(initials.toUpperCase());
        }

        // Load profile photo from Firestore
        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String photoBase64 = document.getString("profilePhotoBase64");
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            try {
                                byte[] decodedString = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                ivProfileCircle.setImageBitmap(decodedByte);
                                ivProfileCircle.setVisibility(View.VISIBLE);
                                tvProfileCircleInitials.setVisibility(View.GONE);
                            } catch (Exception e) {
                                ivProfileCircle.setVisibility(View.GONE);
                                tvProfileCircleInitials.setVisibility(View.VISIBLE);
                            }
                        } else {
                            ivProfileCircle.setVisibility(View.GONE);
                            tvProfileCircleInitials.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void setupBottomNavigationPadding() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+
            getWindow().setDecorFitsSystemWindows(false);
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_nav_container), (v, insets) -> {
                findViewById(R.id.bottom_nav_container)
                        .setPadding(0, 12, 0, getNavigationBarHeight() + 20);
                return insets;
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // For Android 5-10
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            findViewById(R.id.bottom_nav_container).setPadding(0, 0, 0, getNavigationBarHeight());
        }
    }

    private int getNavigationBarHeight() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    private void setupQuoteCarousel() {
        // Add your 5 images
        imageList.add(R.drawable.card_1);
        imageList.add(R.drawable.card_2);
        imageList.add(R.drawable.card_3);
        imageList.add(R.drawable.card_4);
        imageList.add(R.drawable.card_5);

        quoteAdapter = new QuoteAdapter(imageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvQuoteCarousel.setLayoutManager(layoutManager);
        rvQuoteCarousel.setAdapter(quoteAdapter);

        // Add snap helper for smooth scrolling
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvQuoteCarousel);

        // Add dots indicator
        addDotsIndicator();

        // Track scroll to update dots
        rvQuoteCarousel.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateCurrentPosition();
                }
            }
        });
    }

    private void addDotsIndicator() {
        dotsContainer.removeAllViews();
        for (int i = 0; i < imageList.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsContainer.addView(dot);
        }
        updateDots(0);
    }

    private void updateCurrentPosition() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvQuoteCarousel.getLayoutManager();
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        if (firstVisible != RecyclerView.NO_POSITION && firstVisible != currentPosition) {
            currentPosition = firstVisible;
            updateDots(currentPosition);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            if (i == position) {
                dot.setBackgroundResource(R.drawable.dot_active);
            } else {
                dot.setBackgroundResource(R.drawable.dot_inactive);
            }
        }
    }
}