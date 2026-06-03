package com.plan2pantry.models;

public class Meal {
    private String id;
    private String name;
    private String type;      // Breakfast, Lunch, Dinner, Snack
    private String dietType;  // vegetarian, non-vegetarian, eggitarian
    private int calories;
    private int protein;
    private int carbs;
    private int fats;

    public Meal() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getDietType() { return dietType; }
    public int getCalories() { return calories; }
    public int getProtein() { return protein; }
    public int getCarbs() { return carbs; }
    public int getFats() { return fats; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setDietType(String dietType) { this.dietType = dietType; }
    public void setCalories(int calories) { this.calories = calories; }
    public void setProtein(int protein) { this.protein = protein; }
    public void setCarbs(int carbs) { this.carbs = carbs; }
    public void setFats(int fats) { this.fats = fats; }
}