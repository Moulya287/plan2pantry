package com.plan2pantry.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.plan2pantry.R;

import java.util.List;

public class MealPlannerAdapter extends RecyclerView.Adapter<MealPlannerAdapter.ViewHolder> {

    private List<MealItem> meals;
    private OnMealDoneListener listener;

    public interface OnMealDoneListener {
        void onMealDone(MealItem meal, boolean isChecked);
    }

    public static class MealItem {
        public String type;
        public String icon;
        public String name;
        public int calories;
        public int protein;
        public int carbs;
        public int fats;
        public boolean isDone;

        public MealItem(String type, String icon, String name, int calories, int protein, int carbs, int fats, boolean isDone) {
            this.type = type;
            this.icon = icon;
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.isDone = isDone;
        }
    }

    public MealPlannerAdapter(List<MealItem> meals, OnMealDoneListener listener) {
        this.meals = meals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealItem meal = meals.get(position);

        holder.tvMealIcon.setText(meal.icon);
        holder.tvMealType.setText(meal.type);
        holder.tvMealName.setText(meal.name);
        holder.tvCalories.setText(meal.calories + " kcal");
        holder.tvMacros.setText("P:" + meal.protein + "g  C:" + meal.carbs + "g  F:" + meal.fats + "g");

        holder.checkDone.setOnCheckedChangeListener(null);
        holder.checkDone.setChecked(meal.isDone);
        holder.checkDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            meal.isDone = isChecked;
            if (listener != null) {
                listener.onMealDone(meal, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealIcon, tvMealType, tvMealName, tvCalories, tvMacros;
        CheckBox checkDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealIcon = itemView.findViewById(R.id.tv_meal_icon);
            tvMealType = itemView.findViewById(R.id.tv_meal_type);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvMacros = itemView.findViewById(R.id.tv_macros);
            checkDone = itemView.findViewById(R.id.check_done);
        }
    }
}