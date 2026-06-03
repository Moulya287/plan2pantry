package com.plan2pantry.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plan2pantry.models.User;
import java.util.HashMap;
import java.util.Map;

public class FirebaseService {

    private static FirebaseService instance;
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    private FirebaseService() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        return mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }

    // Sign Up
    public void signUp(String email, String password, String name, AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && getCurrentUserId() != null) {
                        saveUserToFirestore(getCurrentUserId(), name, email, callback);
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Sign up failed");
                    }
                });
    }

    private void saveUserToFirestore(String userId, String name, String email, AuthCallback callback) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("onboardingCompleted", false);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Sign In
    public void signIn(String email, String password, AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Sign in failed");
                    }
                });
    }

    // Save Onboarding Profile
    public void saveUserProfile(User user, OnboardingCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        Map<String, Object> profile = new HashMap<>();
        profile.put("age", user.getAge());
        profile.put("height", user.getHeight());
        profile.put("weight", user.getWeight());
        profile.put("gender", user.getGender());
        profile.put("activityLevel", user.getActivityLevel());
        profile.put("goal", user.getGoal());
        profile.put("dietType", user.getDietType());
        profile.put("targetCalories", user.getTargetCalories());
        profile.put("targetProtein", user.getTargetProtein());
        profile.put("targetCarbs", user.getTargetCarbs());
        profile.put("targetFats", user.getTargetFats());
        profile.put("targetWater", user.getTargetWater());
        profile.put("onboardingCompleted", true);

        db.collection("users").document(userId)
                .update(profile)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Get User Profile
    public void getUserProfile(UserCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onFailure("User not logged in");
            return;
        }

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = new User();
                        user.setName(documentSnapshot.getString("name"));
                        user.setEmail(documentSnapshot.getString("email"));
                        user.setAge(documentSnapshot.getLong("age") != null ?
                                documentSnapshot.getLong("age").intValue() : 0);
                        user.setHeight(documentSnapshot.getDouble("height") != null ?
                                documentSnapshot.getDouble("height").floatValue() : 0);
                        user.setWeight(documentSnapshot.getDouble("weight") != null ?
                                documentSnapshot.getDouble("weight").floatValue() : 0);
                        user.setGender(documentSnapshot.getString("gender"));
                        user.setActivityLevel(documentSnapshot.getString("activityLevel"));
                        user.setGoal(documentSnapshot.getString("goal"));
                        user.setDietType(documentSnapshot.getString("dietType"));
                        user.setTargetCalories(documentSnapshot.getLong("targetCalories") != null ?
                                documentSnapshot.getLong("targetCalories").intValue() : 0);
                        user.setTargetProtein(documentSnapshot.getLong("targetProtein") != null ?
                                documentSnapshot.getLong("targetProtein").intValue() : 0);
                        user.setTargetCarbs(documentSnapshot.getLong("targetCarbs") != null ?
                                documentSnapshot.getLong("targetCarbs").intValue() : 0);
                        user.setTargetFats(documentSnapshot.getLong("targetFats") != null ?
                                documentSnapshot.getLong("targetFats").intValue() : 0);
                        user.setTargetWater(documentSnapshot.getDouble("targetWater") != null ?
                                documentSnapshot.getDouble("targetWater").floatValue() : 2.5f);
                        user.setOnboardingCompleted(documentSnapshot.getBoolean("onboardingCompleted") != null ?
                                documentSnapshot.getBoolean("onboardingCompleted") : false);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Sign Out
    public void signOut() {
        mAuth.signOut();
    }

    // Callback Interfaces
    public interface AuthCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnboardingCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }
}