package com.plan2pantry.models;

public class MealPlan {
    private String breakfastId;
    private String breakfastName;
    private int breakfastCalories;
    private int breakfastProtein;
    private int breakfastCarbs;
    private int breakfastFats;
    private boolean breakfastDone;

    private String lunchId;
    private String lunchName;
    private int lunchCalories;
    private int lunchProtein;
    private int lunchCarbs;
    private int lunchFats;
    private boolean lunchDone;

    private String dinnerId;
    private String dinnerName;
    private int dinnerCalories;
    private int dinnerProtein;
    private int dinnerCarbs;
    private int dinnerFats;
    private boolean dinnerDone;

    private String snackId;
    private String snackName;
    private int snackCalories;
    private int snackProtein;
    private int snackCarbs;
    private int snackFats;
    private boolean snackDone;

    private int day;
    private String date;

    public MealPlan() {}

    // Getters and Setters for Breakfast
    public String getBreakfastId() { return breakfastId; }
    public void setBreakfastId(String breakfastId) { this.breakfastId = breakfastId; }
    public String getBreakfastName() { return breakfastName; }
    public void setBreakfastName(String breakfastName) { this.breakfastName = breakfastName; }
    public int getBreakfastCalories() { return breakfastCalories; }
    public void setBreakfastCalories(int breakfastCalories) { this.breakfastCalories = breakfastCalories; }
    public int getBreakfastProtein() { return breakfastProtein; }
    public void setBreakfastProtein(int breakfastProtein) { this.breakfastProtein = breakfastProtein; }
    public int getBreakfastCarbs() { return breakfastCarbs; }
    public void setBreakfastCarbs(int breakfastCarbs) { this.breakfastCarbs = breakfastCarbs; }
    public int getBreakfastFats() { return breakfastFats; }
    public void setBreakfastFats(int breakfastFats) { this.breakfastFats = breakfastFats; }
    public boolean isBreakfastDone() { return breakfastDone; }
    public void setBreakfastDone(boolean breakfastDone) { this.breakfastDone = breakfastDone; }

    // Lunch Getters and Setters
    public String getLunchId() { return lunchId; }
    public void setLunchId(String lunchId) { this.lunchId = lunchId; }
    public String getLunchName() { return lunchName; }
    public void setLunchName(String lunchName) { this.lunchName = lunchName; }
    public int getLunchCalories() { return lunchCalories; }
    public void setLunchCalories(int lunchCalories) { this.lunchCalories = lunchCalories; }
    public int getLunchProtein() { return lunchProtein; }
    public void setLunchProtein(int lunchProtein) { this.lunchProtein = lunchProtein; }
    public int getLunchCarbs() { return lunchCarbs; }
    public void setLunchCarbs(int lunchCarbs) { this.lunchCarbs = lunchCarbs; }
    public int getLunchFats() { return lunchFats; }
    public void setLunchFats(int lunchFats) { this.lunchFats = lunchFats; }
    public boolean isLunchDone() { return lunchDone; }
    public void setLunchDone(boolean lunchDone) { this.lunchDone = lunchDone; }

    // Dinner Getters and Setters
    public String getDinnerId() { return dinnerId; }
    public void setDinnerId(String dinnerId) { this.dinnerId = dinnerId; }
    public String getDinnerName() { return dinnerName; }
    public void setDinnerName(String dinnerName) { this.dinnerName = dinnerName; }
    public int getDinnerCalories() { return dinnerCalories; }
    public void setDinnerCalories(int dinnerCalories) { this.dinnerCalories = dinnerCalories; }
    public int getDinnerProtein() { return dinnerProtein; }
    public void setDinnerProtein(int dinnerProtein) { this.dinnerProtein = dinnerProtein; }
    public int getDinnerCarbs() { return dinnerCarbs; }
    public void setDinnerCarbs(int dinnerCarbs) { this.dinnerCarbs = dinnerCarbs; }
    public int getDinnerFats() { return dinnerFats; }
    public void setDinnerFats(int dinnerFats) { this.dinnerFats = dinnerFats; }
    public boolean isDinnerDone() { return dinnerDone; }
    public void setDinnerDone(boolean dinnerDone) { this.dinnerDone = dinnerDone; }

    // Snack Getters and Setters
    public String getSnackId() { return snackId; }
    public void setSnackId(String snackId) { this.snackId = snackId; }
    public String getSnackName() { return snackName; }
    public void setSnackName(String snackName) { this.snackName = snackName; }
    public int getSnackCalories() { return snackCalories; }
    public void setSnackCalories(int snackCalories) { this.snackCalories = snackCalories; }
    public int getSnackProtein() { return snackProtein; }
    public void setSnackProtein(int snackProtein) { this.snackProtein = snackProtein; }
    public int getSnackCarbs() { return snackCarbs; }
    public void setSnackCarbs(int snackCarbs) { this.snackCarbs = snackCarbs; }
    public int getSnackFats() { return snackFats; }
    public void setSnackFats(int snackFats) { this.snackFats = snackFats; }
    public boolean isSnackDone() { return snackDone; }
    public void setSnackDone(boolean snackDone) { this.snackDone = snackDone; }

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // Helper to get completed calories
    public int getCompletedCalories() {
        int total = 0;
        if (breakfastDone) total += breakfastCalories;
        if (lunchDone) total += lunchCalories;
        if (dinnerDone) total += dinnerCalories;
        if (snackDone) total += snackCalories;
        return total;
    }
}