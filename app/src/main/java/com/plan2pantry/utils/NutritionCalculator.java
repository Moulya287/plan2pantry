package com.plan2pantry.utils;

public class NutritionCalculator {

    // Calculate BMR using Mifflin-St Jeor Equation
    public static int calculateBMR(String gender, float weight, float height, int age) {
        if (gender.equalsIgnoreCase("Male")) {
            return (int) (10 * weight + 6.25 * height - 5 * age + 5);
        } else {
            return (int) (10 * weight + 6.25 * height - 5 * age - 161);
        }
    }

    // Calculate daily calories based on activity and goal
    public static int calculateCalories(String gender, float weight, float height, int age,
                                        String activityLevel, String goal) {
        int bmr = calculateBMR(gender, weight, height, age);

        double activityMultiplier;
        switch (activityLevel) {
            case "Sedentary (little or no exercise)":
                activityMultiplier = 1.2;
                break;
            case "Lightly active (exercise 1-3 days/week)":
                activityMultiplier = 1.375;
                break;
            case "Moderately active (exercise 3-5 days/week)":
                activityMultiplier = 1.55;
                break;
            case "Very active (exercise 6-7 days/week)":
                activityMultiplier = 1.725;
                break;
            default:
                activityMultiplier = 1.2;
        }

        int calories = (int) (bmr * activityMultiplier);

        // Apply goal adjustment
        if (goal.equalsIgnoreCase("Lose weight")) {
            calories = (int) (calories * 0.8);
        } else if (goal.equalsIgnoreCase("Gain muscle")) {
            calories = (int) (calories * 1.1);
        }

        return Math.max(calories, 1200);
    }

    // Calculate macros based on calories and weight
    // Returns: [protein(g), carbs(g), fats(g), fiber(g), water(ml)]
    public static int[] calculateMacros(int calories, float weight, String goal) {
        int[] macros = new int[5];

        // Protein based on goal (grams per kg body weight)
        float proteinPerKg;
        if (goal.equalsIgnoreCase("Lose weight")) {
            proteinPerKg = 1.8f;
        } else if (goal.equalsIgnoreCase("Gain muscle")) {
            proteinPerKg = 2.2f;
        } else {
            proteinPerKg = 1.5f;
        }

        int proteinCalories = (int) (weight * proteinPerKg * 4);
        int protein = (int) (weight * proteinPerKg);

        // Fats (25% of total calories)
        int fatCalories = (int) (calories * 0.25);
        int fats = fatCalories / 9;

        // Carbs (remaining calories)
        int carbCalories = calories - proteinCalories - fatCalories;
        int carbs = carbCalories / 4;

        macros[0] = protein;           // Protein in grams
        macros[1] = Math.max(carbs, 100);  // Carbs in grams (minimum 100)
        macros[2] = Math.max(fats, 30);    // Fats in grams (minimum 30)
        macros[3] = 25;                // Fiber in grams (standard)
        macros[4] = (int) (weight * 35);   // Water in ml (35ml per kg)

        return macros;
    }

    // Calculate percentage
    public static int percentageOf(float value, int total) {
        if (total == 0) return 0;
        int pct = (int) ((value / total) * 100);
        return Math.min(pct, 100);
    }

    public static int percentageOf(int value, int total) {
        if (total == 0) return 0;
        int pct = (int) ((value / (float) total) * 100);
        return Math.min(pct, 100);
    }
}