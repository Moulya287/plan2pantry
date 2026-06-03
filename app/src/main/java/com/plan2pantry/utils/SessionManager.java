package com.plan2pantry.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "Plan2PantryPrefs";
    private static final String KEY_DAILY_TIPS_ENABLED = "dailyTipsEnabled";
    private static final String KEY_DAILY_REPORT_TIME = "dailyReportTime";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_FIREBASE_UID = "firebaseUid";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_DIET_TYPE = "dietType";
    private static final String KEY_TARGET_CALORIES = "targetCalories";
    private static final String KEY_TARGET_PROTEIN = "targetProtein";
    private static final String KEY_TARGET_CARBS = "targetCarbs";
    private static final String KEY_TARGET_FATS = "targetFats";
    private static final String KEY_TARGET_WATER = "targetWater";
    private static final String KEY_ONBOARDING_DONE = "onboardingDone";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private static SessionManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    // ==================== CREATE SESSION ====================

    public void createFirebaseLoginSession(String uid, String name, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_FIREBASE_UID, uid);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    // ==================== UPDATE METHODS ====================

    public void updateNutritionTargets(int calories, int protein, int carbs, int fats, float water, String dietType) {
        editor.putInt(KEY_TARGET_CALORIES, calories);
        editor.putInt(KEY_TARGET_PROTEIN, protein);
        editor.putInt(KEY_TARGET_CARBS, carbs);
        editor.putInt(KEY_TARGET_FATS, fats);
        editor.putFloat(KEY_TARGET_WATER, water);
        editor.putString(KEY_DIET_TYPE, dietType);
        editor.apply();
    }

    public void updateTargetCalories(int calories) {
        editor.putInt(KEY_TARGET_CALORIES, calories);
        editor.apply();
    }

    public void updateTargetProtein(int protein) {
        editor.putInt(KEY_TARGET_PROTEIN, protein);
        editor.apply();
    }

    public void updateTargetCarbs(int carbs) {
        editor.putInt(KEY_TARGET_CARBS, carbs);
        editor.apply();
    }

    public void updateTargetFats(int fats) {
        editor.putInt(KEY_TARGET_FATS, fats);
        editor.apply();
    }

    public void updateTargetWater(float targetWater) {
        editor.putFloat(KEY_TARGET_WATER, targetWater);
        editor.apply();
    }

    public void updateDietType(String dietType) {
        editor.putString(KEY_DIET_TYPE, dietType);
        editor.apply();
    }

    public void updateUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public void setOnboardingDone(boolean done) {
        editor.putBoolean(KEY_ONBOARDING_DONE, done);
        editor.apply();
    }

    // ==================== GETTERS ====================

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getFirebaseUid() {
        return sharedPreferences.getString(KEY_FIREBASE_UID, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getDietType() {
        return sharedPreferences.getString(KEY_DIET_TYPE, "vegetarian");
    }

    public int getTargetCalories() {
        return sharedPreferences.getInt(KEY_TARGET_CALORIES, 2000);
    }

    public int getTargetProtein() {
        return sharedPreferences.getInt(KEY_TARGET_PROTEIN, 0);
    }

    public int getTargetCarbs() {
        return sharedPreferences.getInt(KEY_TARGET_CARBS, 0);
    }

    public int getTargetFats() {
        return sharedPreferences.getInt(KEY_TARGET_FATS, 0);
    }

    public float getTargetWater() {
        return sharedPreferences.getFloat(KEY_TARGET_WATER, 2.5f);
    }

    public boolean isOnboardingDone() {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public boolean getNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    // ==================== LOGOUT ====================

    public void logout() {
        editor.clear();
        editor.apply();
    }
    public void setDailyTipsEnabled(boolean enabled) {
        editor.putBoolean(KEY_DAILY_TIPS_ENABLED, enabled);
        editor.apply();
    }

    public boolean isDailyTipsEnabled() {
        return sharedPreferences.getBoolean(KEY_DAILY_TIPS_ENABLED, true);
    }

    public void setDailyReportTime(String time) {
        editor.putString(KEY_DAILY_REPORT_TIME, time);
        editor.apply();
    }

    public String getDailyReportTime() {
        return sharedPreferences.getString(KEY_DAILY_REPORT_TIME, "20:00");
    }
}