package com.plan2pantry.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealDatabaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Complete meal database - 60+ meals across all categories
    public static void uploadAllMeals() {

        // ==================== BREAKFAST (Vegetarian) ====================
        addMeal("Breakfast", "Oats with Banana and Honey", "vegetarian", 350, 12, 60, 8);
        addMeal("Breakfast", "Vegetable Poha", "vegetarian", 300, 8, 50, 10);
        addMeal("Breakfast", "Upma with Vegetables", "vegetarian", 280, 7, 45, 9);
        addMeal("Breakfast", "Idli with Sambar", "vegetarian", 250, 8, 45, 5);
        addMeal("Breakfast", "Dosa with Coconut Chutney", "vegetarian", 320, 9, 55, 8);
        addMeal("Breakfast", "Chilla (Besan Pancake)", "vegetarian", 280, 12, 35, 10);
        addMeal("Breakfast", "Fruit Smoothie Bowl", "vegetarian", 300, 8, 55, 6);
        addMeal("Breakfast", "Almond Butter Toast", "vegetarian", 320, 12, 35, 15);
        addMeal("Breakfast", "Greek Yogurt with Berries", "vegetarian", 280, 18, 30, 8);
        addMeal("Breakfast", "Quinoa Porridge", "vegetarian", 310, 11, 50, 7);

        // ==================== BREAKFAST (Non-Vegetarian) ====================
        addMeal("Breakfast", "Egg Omelette with Veggies", "non-vegetarian", 280, 18, 5, 18);
        addMeal("Breakfast", "Scrambled Eggs with Toast", "non-vegetarian", 320, 20, 25, 16);
        addMeal("Breakfast", "Chicken Sausage with Eggs", "non-vegetarian", 380, 25, 15, 22);
        addMeal("Breakfast", "Boiled Eggs (2) with Avocado", "non-vegetarian", 300, 14, 8, 22);
        addMeal("Breakfast", "Turkey Sandwich", "non-vegetarian", 350, 22, 40, 12);
        addMeal("Breakfast", "Egg Bhurji", "non-vegetarian", 250, 16, 8, 16);

        // ==================== BREAKFAST (Eggitarian) ====================
        addMeal("Breakfast", "Vegetable Omelette", "eggitarian", 280, 16, 8, 16);
        addMeal("Breakfast", "Egg and Cheese Sandwich", "eggitarian", 340, 18, 35, 14);
        addMeal("Breakfast", "Egg White Scramble", "eggitarian", 200, 20, 5, 8);

        // ==================== LUNCH (Vegetarian) ====================
        addMeal("Lunch", "Vegetable Rice with Dal", "vegetarian", 500, 15, 70, 12);
        addMeal("Lunch", "Paneer Butter Masala with Roti", "vegetarian", 550, 20, 45, 25);
        addMeal("Lunch", "Chole Bhature", "vegetarian", 600, 18, 75, 22);
        addMeal("Lunch", "Rajma Chawal", "vegetarian", 520, 16, 72, 14);
        addMeal("Lunch", "Vegetable Biryani with Raita", "vegetarian", 550, 14, 80, 16);
        addMeal("Lunch", "Dal Makhani with Rice", "vegetarian", 480, 14, 65, 15);
        addMeal("Lunch", "Palak Paneer with Roti", "vegetarian", 520, 18, 40, 22);
        addMeal("Lunch", "Vegetable Khichdi", "vegetarian", 380, 12, 60, 10);
        addMeal("Lunch", "Quinoa Salad with Chickpeas", "vegetarian", 420, 15, 55, 14);
        addMeal("Lunch", "Mushroom Curry with Rice", "vegetarian", 450, 12, 60, 16);
        addMeal("Lunch", "Stuffed Bell Peppers", "vegetarian", 400, 10, 55, 15);
        addMeal("Lunch", "Vegetable Fried Rice", "vegetarian", 480, 10, 75, 14);

        // ==================== LUNCH (Non-Vegetarian) ====================
        addMeal("Lunch", "Grilled Chicken with Rice", "non-vegetarian", 520, 35, 45, 18);
        addMeal("Lunch", "Chicken Curry with Roti", "non-vegetarian", 550, 38, 35, 22);
        addMeal("Lunch", "Fish Curry with Brown Rice", "non-vegetarian", 480, 32, 50, 18);
        addMeal("Lunch", "Egg Curry with Rice", "non-vegetarian", 500, 22, 55, 20);
        addMeal("Lunch", "Chicken Biryani", "non-vegetarian", 580, 35, 70, 18);
        addMeal("Lunch", "Butter Chicken with Naan", "non-vegetarian", 620, 38, 55, 28);
        addMeal("Lunch", "Tuna Sandwich with Salad", "non-vegetarian", 420, 28, 45, 14);
        addMeal("Lunch", "Chicken Salad Bowl", "non-vegetarian", 380, 32, 20, 18);
        addMeal("Lunch", "Prawn Curry with Rice", "non-vegetarian", 450, 30, 55, 14);
        addMeal("Lunch", "Mutton Curry with Roti", "non-vegetarian", 580, 40, 35, 28);

        // ==================== LUNCH (Eggitarian) ====================
        addMeal("Lunch", "Egg Curry with Rice", "eggitarian", 480, 20, 55, 18);
        addMeal("Lunch", "Egg Fried Rice", "eggitarian", 450, 18, 60, 15);

        // ==================== DINNER (Vegetarian) ====================
        addMeal("Dinner", "Mixed Vegetable Curry with Roti", "vegetarian", 450, 14, 55, 15);
        addMeal("Dinner", "Vegetable Soup with Bread", "vegetarian", 320, 10, 45, 10);
        addMeal("Dinner", "Tofu Stir Fry with Rice", "vegetarian", 420, 20, 45, 18);
        addMeal("Dinner", "Lentil Soup with Toast", "vegetarian", 350, 18, 45, 8);
        addMeal("Dinner", "Stuffed Capsicum", "vegetarian", 380, 12, 45, 14);
        addMeal("Dinner", "Vegetable Pulao", "vegetarian", 420, 10, 70, 10);
        addMeal("Dinner", "Aloo Gobi with Roti", "vegetarian", 400, 10, 55, 12);
        addMeal("Dinner", "Baingan Bharta with Roti", "vegetarian", 380, 8, 45, 15);
        addMeal("Dinner", "Methi Malai Paneer", "vegetarian", 480, 16, 30, 28);
        addMeal("Dinner", "Vegetable Korma", "vegetarian", 450, 10, 50, 20);

        // ==================== DINNER (Non-Vegetarian) ====================
        addMeal("Dinner", "Grilled Chicken with Vegetables", "non-vegetarian", 450, 40, 15, 22);
        addMeal("Dinner", "Fish Tikka with Salad", "non-vegetarian", 380, 35, 10, 20);
        addMeal("Dinner", "Chicken Soup with Crackers", "non-vegetarian", 300, 25, 20, 12);
        addMeal("Dinner", "Tandoori Chicken", "non-vegetarian", 420, 42, 5, 24);
        addMeal("Dinner", "Chicken Salad", "non-vegetarian", 350, 35, 10, 18);
        addMeal("Dinner", "Egg Drop Soup", "non-vegetarian", 250, 15, 15, 12);
        addMeal("Dinner", "Baked Fish with Herbs", "non-vegetarian", 400, 38, 10, 22);

        // ==================== SNACKS (Vegetarian) ====================
        addMeal("Snack", "Fresh Fruit (Apple/Banana/Orange)", "vegetarian", 100, 1, 25, 0);
        addMeal("Snack", "Handful of Mixed Nuts", "vegetarian", 180, 6, 10, 14);
        addMeal("Snack", "Greek Yogurt", "vegetarian", 120, 10, 8, 5);
        addMeal("Snack", "Roasted Chickpeas", "vegetarian", 140, 7, 20, 4);
        addMeal("Snack", "Vegetable Sticks with Hummus", "vegetarian", 150, 4, 15, 8);
        addMeal("Snack", "Fruit Smoothie", "vegetarian", 160, 4, 35, 2);
        addMeal("Snack", "Cottage Cheese (Paneer) Cubes", "vegetarian", 120, 10, 5, 7);
        addMeal("Snack", "Popcorn (Air-popped)", "vegetarian", 100, 3, 18, 2);
        addMeal("Snack", "Granola Bar", "vegetarian", 150, 4, 25, 5);
        addMeal("Snack", "Rice Cake with Peanut Butter", "vegetarian", 130, 4, 15, 6);

        // ==================== SNACKS (Non-Vegetarian) ====================
        addMeal("Snack", "Boiled Egg (1)", "non-vegetarian", 70, 6, 1, 5);
        addMeal("Snack", "Protein Bar", "non-vegetarian", 200, 15, 20, 8);
        addMeal("Snack", "Chicken Seekh Kebab (1 piece)", "non-vegetarian", 120, 12, 3, 7);
        addMeal("Snack", "Tuna Salad on Crackers", "non-vegetarian", 160, 15, 10, 7);
        addMeal("Snack", "Egg Salad", "non-vegetarian", 140, 12, 4, 8);

        // ==================== SNACKS (Eggitarian) ====================
        addMeal("Snack", "Boiled Egg (1)", "eggitarian", 70, 6, 1, 5);
        addMeal("Snack", "Egg Salad", "eggitarian", 140, 12, 4, 8);

        System.out.println("All meals uploaded to Firestore!");
    }

    private static void addMeal(String type, String name, String dietType,
                                int calories, int protein, int carbs, int fats) {
        Map<String, Object> meal = new HashMap<>();
        meal.put("type", type);
        meal.put("name", name);
        meal.put("dietType", dietType);
        meal.put("calories", calories);
        meal.put("protein", protein);
        meal.put("carbs", carbs);
        meal.put("fats", fats);

        db.collection("meals").add(meal)
                .addOnSuccessListener(docRef ->
                        System.out.println("✓ Added: " + type + " - " + name))
                .addOnFailureListener(e ->
                        System.err.println("✗ Failed: " + name + " - " + e.getMessage()));
    }
    // Delete all existing eggitarian meals
    public static void deleteAllEggitarianMeals() {
        db.collection("meals")
                .whereEqualTo("dietType", "eggitarian")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        doc.getReference().delete();
                        count++;
                        System.out.println("🗑️ Deleted: " + doc.getString("name"));
                    }
                    System.out.println("✅ Deleted " + count + " eggitarian meals from Firestore");
                })
                .addOnFailureListener(e -> {
                    System.err.println("❌ Failed to delete: " + e.getMessage());
                });
    }
    // Add all new eggitarian meals
    public static void addAllEggitarianMeals() {
        // ==================== BREAKFAST ====================
        addMeal("Breakfast", "Vegetable Omelette (2 eggs)", "eggitarian", 280, 18, 8, 18);
        addMeal("Breakfast", "Egg and Cheese Sandwich", "eggitarian", 340, 20, 35, 15);
        addMeal("Breakfast", "Egg White Scramble with Spinach", "eggitarian", 220, 22, 6, 10);
        addMeal("Breakfast", "Boiled Eggs (2) with Toast", "eggitarian", 250, 14, 20, 14);
        addMeal("Breakfast", "Egg Bhurji with Whole Wheat Bread", "eggitarian", 300, 16, 25, 16);
        addMeal("Breakfast", "Mushroom and Egg Wrap", "eggitarian", 320, 18, 30, 15);
        addMeal("Breakfast", "Egg and Avocado Toast", "eggitarian", 350, 16, 25, 22);
        addMeal("Breakfast", "Spanish Omelette with Potatoes", "eggitarian", 310, 17, 28, 17);

        // ==================== LUNCH ====================
        addMeal("Lunch", "Egg Curry with Rice", "eggitarian", 480, 22, 55, 20);
        addMeal("Lunch", "Egg Fried Rice", "eggitarian", 450, 18, 60, 15);
        addMeal("Lunch", "Egg Biryani", "eggitarian", 520, 20, 65, 18);
        addMeal("Lunch", "Egg Salad Sandwich", "eggitarian", 380, 16, 40, 16);
        addMeal("Lunch", "Egg and Vegetable Noodles", "eggitarian", 420, 17, 55, 14);
        addMeal("Lunch", "Egg Paratha", "eggitarian", 400, 15, 45, 18);
        addMeal("Lunch", "Egg Curry with Roti", "eggitarian", 460, 20, 50, 19);
        addMeal("Lunch", "Deviled Eggs with Quinoa Salad", "eggitarian", 390, 19, 35, 20);

        // ==================== DINNER ====================
        addMeal("Dinner", "Egg Curry with Brown Rice", "eggitarian", 470, 21, 52, 19);
        addMeal("Dinner", "Egg Fried Rice with Vegetables", "eggitarian", 440, 17, 58, 14);
        addMeal("Dinner", "Egg and Spinach Curry", "eggitarian", 400, 18, 35, 20);
        addMeal("Dinner", "Egg Noodle Soup", "eggitarian", 350, 15, 40, 12);
        addMeal("Dinner", "Egg and Cheese Quesadilla", "eggitarian", 420, 19, 38, 20);
        addMeal("Dinner", "Eggplant and Egg Curry", "eggitarian", 390, 16, 42, 17);
        addMeal("Dinner", "Egg White Omelette Dinner", "eggitarian", 300, 25, 10, 16);
        addMeal("Dinner", "Egg Stir Fry with Vegetables", "eggitarian", 370, 18, 30, 18);

        // ==================== SNACKS ====================
        addMeal("Snack", "Boiled Egg (1)", "eggitarian", 70, 6, 1, 5);
        addMeal("Snack", "Egg Salad on Crackers", "eggitarian", 140, 12, 4, 8);
        addMeal("Snack", "Egg White Bites", "eggitarian", 100, 10, 3, 5);
        addMeal("Snack", "Mini Egg Muffins", "eggitarian", 120, 8, 6, 7);
        addMeal("Snack", "Egg and Cheese Roll", "eggitarian", 150, 10, 8, 9);
    }
    // Run this once to reset all eggitarian meals
    public static void resetEggitarianMeals() {
        deleteAllEggitarianMeals();
        // Wait 2 seconds for delete to complete, then add new meals
        new android.os.Handler().postDelayed(() -> addAllEggitarianMeals(), 2000);
    }
}