package com.plan2pantry.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plan2pantry.R;
import com.plan2pantry.utils.NutritionCalculator;
import com.plan2pantry.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * OnboardingActivity - Collect age, height, weight, activity, goal, diet type
 * Calculates and stores ALL nutrition targets in Firestore
 */
public class OnboardingActivity extends AppCompatActivity {

    private EditText etAge, etHeight, etWeight;
    private Spinner spGender, spActivity, spGoal, spDietType;
    private Button btnCalculate;
    private TextView tvResult;

    private SessionManager session;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

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

        setContentView(R.layout.activity_onboarding);

        // Initialize Firebase and Session
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        session = SessionManager.getInstance(this);

        initViews();
        setupSpinners();
        setListeners();
    }

    private void initViews() {
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        spGender = findViewById(R.id.sp_gender);
        spActivity = findViewById(R.id.sp_activity);
        spGoal = findViewById(R.id.sp_goal);
        spDietType = findViewById(R.id.sp_diet_type);
        btnCalculate = findViewById(R.id.btn_calculate);
        tvResult = findViewById(R.id.tv_result);
    }

    private void setupSpinners() {
        // Gender Spinner
        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this, R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGender.setAdapter(genderAdapter);

        // Activity Spinner
        ArrayAdapter<CharSequence> activityAdapter = ArrayAdapter.createFromResource(
                this, R.array.activity_levels, android.R.layout.simple_spinner_item);
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spActivity.setAdapter(activityAdapter);

        // Goal Spinner
        ArrayAdapter<CharSequence> goalAdapter = ArrayAdapter.createFromResource(
                this, R.array.goals, android.R.layout.simple_spinner_item);
        goalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spGoal.setAdapter(goalAdapter);

        // Diet Type Spinner
        ArrayAdapter<CharSequence> dietAdapter = ArrayAdapter.createFromResource(
                this, R.array.diet_types, android.R.layout.simple_spinner_item);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDietType.setAdapter(dietAdapter);
    }

    private void setListeners() {
        btnCalculate.setOnClickListener(v -> saveProfileAndContinue());
    }

    private void saveProfileAndContinue() {
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(ageStr)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(heightStr)) {
            etHeight.setError("Height is required");
            etHeight.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(weightStr)) {
            etWeight.setError("Weight is required");
            etWeight.requestFocus();
            return;
        }

        int age = Integer.parseInt(ageStr);
        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);

        if (age < 10 || age > 120) {
            etAge.setError("Enter a valid age (10–120)");
            return;
        }
        if (height < 50 || height > 300) {
            etHeight.setError("Enter height in cm (50–300)");
            return;
        }
        if (weight < 20 || weight > 500) {
            etWeight.setError("Enter weight in kg (20–500)");
            return;
        }

        String gender = spGender.getSelectedItem().toString();
        String activityLevel = spActivity.getSelectedItem().toString();
        String goal = spGoal.getSelectedItem().toString();
        String dietType = spDietType.getSelectedItem().toString().toLowerCase();

        // Calculate ALL nutrition targets based on user inputs
        int calories = NutritionCalculator.calculateCalories(gender, weight, height, age, activityLevel, goal);
        int[] macros = NutritionCalculator.calculateMacros(calories, weight, goal);
        float waterL = macros[4] / 1000f;

        // Show result to user
        String resultText = String.format(
                " Your Daily Targets:\n\n" +
                        " Calories: %d kcal\n" +
                        " Protein: %dg\n" +
                        " Carbs: %dg\n" +
                        " Fats: %dg\n" +
                        " Fiber: %dg\n" +
                        " Water: %.1fL\n\n" +
                        "Based on your:\n" +
                        "• Age: %d | Gender: %s\n" +
                        "• Height: %.1f cm | Weight: %.1f kg\n" +
                        "• Activity: %s | Goal: %s\n" +
                        "• Diet: %s",
                calories, macros[0], macros[1], macros[2], macros[3], waterL,
                age, gender, height, weight, activityLevel, goal, dietType
        );
        tvResult.setText(resultText);
        tvResult.setVisibility(View.VISIBLE);

        // Save ALL calculated data to Firestore
        String userId = mAuth.getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("age", age);
        userData.put("height", height);
        userData.put("weight", weight);
        userData.put("gender", gender);
        userData.put("activityLevel", activityLevel);
        userData.put("goal", goal);
        userData.put("dietType", dietType);
        userData.put("targetCalories", calories);
        userData.put("targetProtein", macros[0]);
        userData.put("targetCarbs", macros[1]);
        userData.put("targetFats", macros[2]);
        userData.put("targetFiber", macros[3]);
        userData.put("targetWater", waterL);
        userData.put("onboardingCompleted", true);

        firestore.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener(aVoid -> {
                    // Update session
                    session.updateNutritionTargets(calories, macros[0], macros[1], macros[2], waterL, dietType);
                    session.setOnboardingDone(true);

                    Toast.makeText(OnboardingActivity.this, "Profile saved! ", Toast.LENGTH_SHORT).show();

                    // Proceed to Main Activity
                    btnCalculate.setText("Continue →");
                    btnCalculate.setOnClickListener(v -> {
                        startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                        finish();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OnboardingActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}