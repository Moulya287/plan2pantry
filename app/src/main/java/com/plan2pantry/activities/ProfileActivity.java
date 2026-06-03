package com.plan2pantry.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.plan2pantry.R;
import com.plan2pantry.utils.DailyTipReceiver;
import com.plan2pantry.utils.NotificationHelper;
import com.plan2pantry.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private de.hdodenhof.circleimageview.CircleImageView ivProfilePhoto;
    private TextView tvProfileInitials;
    private TextView tvUserName, tvUserEmail, tvMemberSince;
    private TextView tvDisplayName, tvAge, tvGender;
    private TextView tvWeight, tvHeight, tvActivity;
    private TextView tvGoal, tvTargetWeight;
    private TextView tvDiet;
    private TextView tvCaloriesTarget, tvProteinTarget, tvCarbsTarget, tvFatsTarget, tvWaterTarget;
    private TextView tvNotifications, tvReminders;

    private SessionManager session;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String userId;

    private String currentName, currentGender, currentGoal, currentDiet, currentActivity;
    private int currentAge;
    private float currentWeight, currentHeight, currentTargetWeight;
    private int currentCalories, currentProtein, currentCarbs, currentFats;
    private float currentWater;

    private Uri selectedImageUri = null;
    private Uri cameraImageUri = null;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        savePhotoToFirestore(selectedImageUri);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                    savePhotoToFirestore(cameraImageUri);
                }
            });

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

        setContentView(R.layout.activity_profile);

        session = SessionManager.getInstance(this);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = session.getFirebaseUid();

        initViews();
        loadUserData();
        setClickListeners();

        // Schedule daily tip if enabled
        if (session.isDailyTipsEnabled()) {
            scheduleDailyTip();
        }
    }

    private void initViews() {
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);
        tvProfileInitials = findViewById(R.id.tv_profile_initials);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvMemberSince = findViewById(R.id.tv_member_since);
        tvDisplayName = findViewById(R.id.tv_display_name);
        tvAge = findViewById(R.id.tv_age);
        tvGender = findViewById(R.id.tv_gender);
        tvWeight = findViewById(R.id.tv_weight);
        tvHeight = findViewById(R.id.tv_height);
        tvActivity = findViewById(R.id.tv_activity);
        tvGoal = findViewById(R.id.tv_goal);
        tvTargetWeight = findViewById(R.id.tv_target_weight);
        tvDiet = findViewById(R.id.tv_diet);
        tvCaloriesTarget = findViewById(R.id.tv_calories_target);
        tvProteinTarget = findViewById(R.id.tv_protein_target);
        tvCarbsTarget = findViewById(R.id.tv_carbs_target);
        tvFatsTarget = findViewById(R.id.tv_fats_target);
        tvWaterTarget = findViewById(R.id.tv_water_target);
        tvNotifications = findViewById(R.id.tv_notifications);
        tvReminders = findViewById(R.id.tv_reminders);
    }

    private void loadUserData() {
        String name = session.getUserName();
        String email = session.getUserEmail();

        tvUserName.setText(name != null ? name : "User");
        tvUserEmail.setText(email != null ? email : "user@email.com");
        tvDisplayName.setText(name != null ? name : "Not set");
        updateInitials(name);

        if (auth.getCurrentUser() != null) {
            long creationTime = auth.getCurrentUser().getMetadata().getCreationTimestamp();
            String memberSince = new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(new Date(creationTime));
            tvMemberSince.setText("Member since: " + memberSince);
        }

        tvNotifications.setText(session.isDailyTipsEnabled() ? "On" : "Off");
        tvReminders.setText(session.getDailyReportTime());

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        currentAge = document.getLong("age") != null ? document.getLong("age").intValue() : 0;
                        currentGender = document.getString("gender");
                        tvAge.setText(currentAge > 0 ? currentAge + " years" : "Not set");
                        tvGender.setText(currentGender != null ? currentGender : "Not set");

                        currentWeight = document.getDouble("weight") != null ? document.getDouble("weight").floatValue() : 0;
                        currentHeight = document.getDouble("height") != null ? document.getDouble("height").floatValue() : 0;
                        currentActivity = document.getString("activityLevel");
                        tvWeight.setText(currentWeight > 0 ? currentWeight + " kg" : "Not set");
                        tvHeight.setText(currentHeight > 0 ? currentHeight + " cm" : "Not set");
                        tvActivity.setText(currentActivity != null ? currentActivity : "Not set");

                        currentGoal = document.getString("goal");
                        currentTargetWeight = document.getDouble("targetWeight") != null ? document.getDouble("targetWeight").floatValue() : 0;
                        tvGoal.setText(currentGoal != null ? currentGoal : "Not set");
                        tvTargetWeight.setText(currentTargetWeight > 0 ? currentTargetWeight + " kg" : "Not set");

                        currentDiet = document.getString("dietType");
                        tvDiet.setText(currentDiet != null ? currentDiet : "Not set");

                        currentCalories = document.getLong("targetCalories") != null ? document.getLong("targetCalories").intValue() : 0;
                        currentProtein = document.getLong("targetProtein") != null ? document.getLong("targetProtein").intValue() : 0;
                        currentCarbs = document.getLong("targetCarbs") != null ? document.getLong("targetCarbs").intValue() : 0;
                        currentFats = document.getLong("targetFats") != null ? document.getLong("targetFats").intValue() : 0;
                        currentWater = document.getDouble("targetWater") != null ? document.getDouble("targetWater").floatValue() : 0;

                        tvCaloriesTarget.setText(currentCalories > 0 ? currentCalories + " kcal" : "0 kcal");
                        tvProteinTarget.setText(currentProtein > 0 ? currentProtein + " g" : "0 g");
                        tvCarbsTarget.setText(currentCarbs > 0 ? currentCarbs + " g" : "0 g");
                        tvFatsTarget.setText(currentFats > 0 ? currentFats + " g" : "0 g");
                        tvWaterTarget.setText(currentWater > 0 ? currentWater + " L" : "0 L");

                        loadProfilePhotoFromFirestore(document);
                    }
                });
    }

    private void updateInitials(String name) {
        if (name != null && !name.isEmpty() && !name.equals("Not set")) {
            String[] nameParts = name.split(" ");
            String initials;
            if (nameParts.length >= 2) {
                initials = String.valueOf(nameParts[0].charAt(0)) + String.valueOf(nameParts[1].charAt(0));
            } else {
                initials = String.valueOf(nameParts[0].charAt(0));
            }
            tvProfileInitials.setText(initials.toUpperCase());
        }
    }

    private void loadProfilePhotoFromFirestore(com.google.firebase.firestore.DocumentSnapshot document) {
        String photoBase64 = document.getString("profilePhotoBase64");
        if (photoBase64 != null && !photoBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(photoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivProfilePhoto.setImageBitmap(decodedByte);
                ivProfilePhoto.setVisibility(View.VISIBLE);
                tvProfileInitials.setVisibility(View.GONE);
            } catch (Exception e) {
                ivProfilePhoto.setVisibility(View.GONE);
                tvProfileInitials.setVisibility(View.VISIBLE);
            }
        } else {
            ivProfilePhoto.setVisibility(View.GONE);
            tvProfileInitials.setVisibility(View.VISIBLE);
        }
    }

    private void savePhotoToFirestore(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            int maxSize = 300;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float aspectRatio = (float) width / (float) height;

            if (width > maxSize) {
                width = maxSize;
                height = Math.round(maxSize / aspectRatio);
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            Map<String, Object> updates = new HashMap<>();
            updates.put("profilePhotoBase64", imageBase64);

            firestore.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
                        ivProfilePhoto.setImageBitmap(resizedBitmap);
                        ivProfilePhoto.setVisibility(View.VISIBLE);
                        tvProfileInitials.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save photo", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteProfilePhoto() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_delete_photo, null);
        Button btnCancel = view.findViewById(R.id.btn_delete_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_delete_confirm);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            Map<String, Object> updates = new HashMap<>();
            updates.put("profilePhotoBase64", null);
            firestore.collection("users").document(userId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Photo deleted", Toast.LENGTH_SHORT).show();
                        ivProfilePhoto.setVisibility(View.GONE);
                        tvProfileInitials.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
        });
    }

    private void showPhotoOptionsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_photo_options, null);
        LinearLayout optionTakePhoto = view.findViewById(R.id.option_take_photo);
        LinearLayout optionChooseGallery = view.findViewById(R.id.option_choose_gallery);
        LinearLayout optionDeletePhoto = view.findViewById(R.id.option_delete_photo);
        Button btnCancel = view.findViewById(R.id.btn_cancel);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        optionTakePhoto.setOnClickListener(v -> { openCamera(); dialog.dismiss(); });
        optionChooseGallery.setOnClickListener(v -> { openGallery(); dialog.dismiss(); });
        optionDeletePhoto.setOnClickListener(v -> { deleteProfilePhoto(); dialog.dismiss(); });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                cameraLauncher.launch(intent);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void setClickListeners() {
        findViewById(R.id.btn_change_photo).setOnClickListener(v -> showPhotoOptionsDialog());
        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showEditTextDialog("Full Name", "Enter your name", tvDisplayName.getText().toString(), value -> {
            currentName = value;
            tvDisplayName.setText(value);
            tvUserName.setText(value);
            updateInitials(value);
        }));
        findViewById(R.id.btn_edit_age).setOnClickListener(v -> showNumberInputDialog("Age", "Enter your age", value -> {
            currentAge = Integer.parseInt(value);
            tvAge.setText(value + " years");
        }));
        findViewById(R.id.btn_edit_gender).setOnClickListener(v -> showGenderDialog());
        findViewById(R.id.btn_edit_weight).setOnClickListener(v -> showDecimalInputDialog("Weight", "Enter your weight in kg", value -> {
            currentWeight = Float.parseFloat(value);
            tvWeight.setText(value + " kg");
        }));
        findViewById(R.id.btn_edit_height).setOnClickListener(v -> showDecimalInputDialog("Height", "Enter your height in cm", value -> {
            currentHeight = Float.parseFloat(value);
            tvHeight.setText(value + " cm");
        }));
        findViewById(R.id.btn_edit_activity).setOnClickListener(v -> showActivityDialog());
        findViewById(R.id.btn_edit_goal).setOnClickListener(v -> showGoalDialog());
        findViewById(R.id.btn_edit_target_weight).setOnClickListener(v -> showDecimalInputDialog("Target Weight", "Enter your target weight in kg", value -> {
            currentTargetWeight = Float.parseFloat(value);
            tvTargetWeight.setText(value + " kg");
        }));
        findViewById(R.id.btn_edit_diet).setOnClickListener(v -> showDietDialog());
        findViewById(R.id.btn_edit_calories).setOnClickListener(v -> showNumberInputDialog("Daily Calories", "Enter daily calorie target", value -> {
            currentCalories = Integer.parseInt(value);
            tvCaloriesTarget.setText(value + " kcal");
        }));
        findViewById(R.id.btn_edit_protein).setOnClickListener(v -> showNumberInputDialog("Daily Protein", "Enter daily protein target in grams", value -> {
            currentProtein = Integer.parseInt(value);
            tvProteinTarget.setText(value + " g");
        }));
        findViewById(R.id.btn_edit_carbs).setOnClickListener(v -> showNumberInputDialog("Daily Carbs", "Enter daily carbs target in grams", value -> {
            currentCarbs = Integer.parseInt(value);
            tvCarbsTarget.setText(value + " g");
        }));
        findViewById(R.id.btn_edit_fats).setOnClickListener(v -> showNumberInputDialog("Daily Fats", "Enter daily fats target in grams", value -> {
            currentFats = Integer.parseInt(value);
            tvFatsTarget.setText(value + " g");
        }));
        findViewById(R.id.btn_edit_water).setOnClickListener(v -> showDecimalInputDialog("Daily Water", "Enter daily water target in liters", value -> {
            currentWater = Float.parseFloat(value);
            tvWaterTarget.setText(value + " L");
        }));
        findViewById(R.id.btn_auto_calculate).setOnClickListener(v -> autoCalculateTargets());
        findViewById(R.id.btn_notifications).setOnClickListener(v -> showDailyTipsDialog());
        findViewById(R.id.btn_reminders).setOnClickListener(v -> showTimePickerDialog());
        findViewById(R.id.btn_save_changes).setOnClickListener(v -> saveAllChanges());
        findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    private void showDailyTipsDialog() {
        String[] options = {"On", "Off"};
        showSelectionDialog("Daily Tips", options, selected -> {
            boolean enabled = selected.equals("On");
            tvNotifications.setText(selected);
            session.setDailyTipsEnabled(enabled);

            if (enabled) {
                scheduleDailyTip();
                Toast.makeText(this, "Daily Tips ON - Reminder at " + session.getDailyReportTime(), Toast.LENGTH_SHORT).show();
            } else {
                cancelDailyTipAlarm();
                Toast.makeText(this, "Daily Tips OFF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePickerDialog() {
        String currentTime = session.getDailyReportTime();
        String[] timeParts = currentTime.split(":");
        int currentHour = Integer.parseInt(timeParts[0]);
        int currentMinute = Integer.parseInt(timeParts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.CustomTimePicker,
                (view, hourOfDay, minute) -> {
                    String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    session.setDailyReportTime(newTime);
                    tvReminders.setText(newTime);
                    cancelDailyTipAlarm();
                    scheduleDailyTip();
                    Toast.makeText(this, "Daily report time set to " + newTime, Toast.LENGTH_SHORT).show();
                }, currentHour, currentMinute, true);
        timePickerDialog.show();
    }

    // ==================== ALARM METHODS ====================

    private void cancelDailyTipAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, DailyTipReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 999, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    private void scheduleDailyTip() {
        String time = session.getDailyReportTime();
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, DailyTipReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 999, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Use setRepeating for daily alarms
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);

        // Also set an exact alarm for the first trigger (for newer Android versions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        Toast.makeText(this, "Daily reminder set for " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
    }

    // ==================== DIALOG METHODS ====================

    private void showEditTextDialog(String title, String hint, String currentValue, OnValueSetListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText editText = view.findViewById(R.id.et_input);
        Button btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = view.findViewById(R.id.btn_dialog_save);

        tvTitle.setText(title);
        editText.setHint(hint);
        editText.setText(currentValue.equals("Not set") ? "" : currentValue);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String value = editText.getText().toString().trim();
            if (!value.isEmpty()) {
                listener.onValueSet(value);
                dialog.dismiss();
            } else {
                editText.setError("Please enter a value");
            }
        });
    }

    private void showNumberInputDialog(String title, String hint, OnValueSetListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText editText = view.findViewById(R.id.et_input);
        Button btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = view.findViewById(R.id.btn_dialog_save);

        tvTitle.setText(title);
        editText.setHint(hint);
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String value = editText.getText().toString().trim();
            if (!value.isEmpty()) {
                listener.onValueSet(value);
                dialog.dismiss();
            } else {
                editText.setError("Please enter a value");
            }
        });
    }

    private void showDecimalInputDialog(String title, String hint, OnValueSetListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null);
        TextView tvTitle = view.findViewById(R.id.tv_dialog_title);
        EditText editText = view.findViewById(R.id.et_input);
        Button btnCancel = view.findViewById(R.id.btn_dialog_cancel);
        Button btnSave = view.findViewById(R.id.btn_dialog_save);

        tvTitle.setText(title);
        editText.setHint(hint);
        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String value = editText.getText().toString().trim();
            if (!value.isEmpty()) {
                listener.onValueSet(value);
                dialog.dismiss();
            } else {
                editText.setError("Please enter a value");
            }
        });
    }

    private void showSelectionDialog(String title, String[] options, OnSelectionListener listener) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_selection, null);
        TextView tvTitle = view.findViewById(R.id.tv_selection_title);
        ListView listView = view.findViewById(R.id.lv_options);
        Button btnCancel = view.findViewById(R.id.btn_selection_cancel);

        tvTitle.setText(title);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            listener.onSelected(options[position]);
            dialog.dismiss();
        });
    }

    private void showGenderDialog() {
        String[] options = {"Male", "Female", "Other"};
        showSelectionDialog("Select Gender", options, selected -> {
            currentGender = selected;
            tvGender.setText(currentGender);
        });
    }

    private void showActivityDialog() {
        String[] options = {"Sedentary", "Lightly Active", "Moderately Active", "Very Active"};
        showSelectionDialog("Activity Level", options, selected -> {
            currentActivity = selected;
            tvActivity.setText(currentActivity);
        });
    }

    private void showGoalDialog() {
        String[] options = {"Lose Weight", "Maintain Weight", "Gain Muscle"};
        showSelectionDialog("Health Goal", options, selected -> {
            currentGoal = selected;
            tvGoal.setText(currentGoal);
        });
    }

    private void showDietDialog() {
        String[] options = {"Vegetarian", "Vegan", "Non-Vegetarian", "Keto", "Pescatarian"};
        showSelectionDialog("Diet Type", options, selected -> {
            currentDiet = selected;
            tvDiet.setText(currentDiet);
        });
    }

    private void autoCalculateTargets() {
        if (currentWeight <= 0 || currentHeight <= 0 || currentAge <= 0 || currentGender == null) {
            Toast.makeText(this, "Please set weight, height, age and gender first", Toast.LENGTH_SHORT).show();
            return;
        }

        double bmr;
        if (currentGender.equals("Male")) {
            bmr = (10 * currentWeight) + (6.25 * currentHeight) - (5 * currentAge) + 5;
        } else {
            bmr = (10 * currentWeight) + (6.25 * currentHeight) - (5 * currentAge) - 161;
        }

        double activityMultiplier = 1.2;
        if (currentActivity != null) {
            switch (currentActivity) {
                case "Lightly Active": activityMultiplier = 1.375; break;
                case "Moderately Active": activityMultiplier = 1.55; break;
                case "Very Active": activityMultiplier = 1.725; break;
            }
        }

        double tdee = bmr * activityMultiplier;
        if (currentGoal != null) {
            if (currentGoal.equals("Lose Weight")) tdee -= 500;
            else if (currentGoal.equals("Gain Muscle")) tdee += 300;
        }

        currentCalories = (int) Math.max(tdee, 1200);
        currentProtein = (int) (currentCalories * 0.30 / 4);
        currentCarbs = (int) (currentCalories * 0.40 / 4);
        currentFats = (int) (currentCalories * 0.30 / 9);
        currentWater = currentWeight * 0.033f;

        tvCaloriesTarget.setText(currentCalories + " kcal");
        tvProteinTarget.setText(currentProtein + " g");
        tvCarbsTarget.setText(currentCarbs + " g");
        tvFatsTarget.setText(currentFats + " g");
        tvWaterTarget.setText(String.format(Locale.getDefault(), "%.1f L", currentWater));

        Toast.makeText(this, "Targets calculated based on your profile", Toast.LENGTH_SHORT).show();
    }

    private void saveAllChanges() {
        Map<String, Object> updates = new HashMap<>();
        if (currentName != null) updates.put("name", currentName);
        if (currentAge > 0) updates.put("age", currentAge);
        if (currentGender != null) updates.put("gender", currentGender);
        if (currentWeight > 0) updates.put("weight", currentWeight);
        if (currentHeight > 0) updates.put("height", currentHeight);
        if (currentActivity != null) updates.put("activityLevel", currentActivity);
        if (currentGoal != null) updates.put("goal", currentGoal);
        if (currentTargetWeight > 0) updates.put("targetWeight", currentTargetWeight);
        if (currentDiet != null) updates.put("dietType", currentDiet);
        if (currentCalories > 0) updates.put("targetCalories", currentCalories);
        if (currentProtein > 0) updates.put("targetProtein", currentProtein);
        if (currentCarbs > 0) updates.put("targetCarbs", currentCarbs);
        if (currentFats > 0) updates.put("targetFats", currentFats);
        if (currentWater > 0) updates.put("targetWater", currentWater);

        if (updates.isEmpty()) {
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (currentName != null) session.updateUserName(currentName);
                    if (currentCalories > 0) session.updateTargetCalories(currentCalories);
                    if (currentProtein > 0) session.updateTargetProtein(currentProtein);
                    if (currentCarbs > 0) session.updateTargetCarbs(currentCarbs);
                    if (currentFats > 0) session.updateTargetFats(currentFats);
                    if (currentWater > 0) session.updateTargetWater(currentWater);
                    if (currentDiet != null) session.updateDietType(currentDiet);
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showLogoutDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_logout, null);
        Button btnCancel = view.findViewById(R.id.btn_logout_cancel);
        Button btnConfirm = view.findViewById(R.id.btn_logout_confirm);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(view).setCancelable(true).create();
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            session.logout();
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
            finishAffinity();
        });
    }

    interface OnValueSetListener {
        void onValueSet(String value);
    }

    interface OnSelectionListener {
        void onSelected(String selected);
    }
}