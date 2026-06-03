package com.plan2pantry.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private int age;
    private float height;
    private float weight;
    private String gender;
    private String activityLevel;
    private String goal;
    private String dietType;
    private int targetCalories;
    private int targetProtein;
    private int targetCarbs;
    private int targetFats;
    private float targetWater;
    private boolean onboardingCompleted;

    public User() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }

    public float getWeight() { return weight; }
    public void setWeight(float weight) { this.weight = weight; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getDietType() { return dietType; }
    public void setDietType(String dietType) { this.dietType = dietType; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public int getTargetProtein() { return targetProtein; }
    public void setTargetProtein(int targetProtein) { this.targetProtein = targetProtein; }

    public int getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(int targetCarbs) { this.targetCarbs = targetCarbs; }

    public int getTargetFats() { return targetFats; }
    public void setTargetFats(int targetFats) { this.targetFats = targetFats; }

    public float getTargetWater() { return targetWater; }
    public void setTargetWater(float targetWater) { this.targetWater = targetWater; }

    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }
}