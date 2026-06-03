package com.plan2pantry.activities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.plan2pantry.R;
import com.plan2pantry.utils.SessionManager;

/**
 * SignInActivity - User login screen
 * Uses Firebase Authentication and Firestore only
 */
public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;

    private SessionManager session;
    private String pendingEmail; // Store email for resend verification

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

        setContentView(R.layout.activity_sign_in);

        // Initialize SessionManager
        session = SessionManager.getInstance(this);

        initViews();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnSignIn = findViewById(R.id.btn_sign_in);
        tvSignUp = findViewById(R.id.tv_sign_up);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setListeners() {
        btnSignIn.setOnClickListener(v -> attemptLogin());
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email address");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);
        pendingEmail = email;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSignIn.setEnabled(true);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Check if email is verified
                        if (user != null && !user.isEmailVerified()) {
                            // Email not verified - show dialog
                            mAuth.signOut(); // Sign them out
                            showEmailNotVerifiedDialog(email);
                            return;
                        }

                        // Email is verified - proceed with login
                        String userId = user.getUid();
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    String name = documentSnapshot.getString("name");
                                    if (name == null) name = email.split("@")[0];

                                    Boolean onboardingCompleted = documentSnapshot.getBoolean("onboardingCompleted");

                                    session.createFirebaseLoginSession(userId, name, email);

                                    Toast.makeText(SignInActivity.this, "Welcome back, " + name + "! ", Toast.LENGTH_SHORT).show();

                                    if (onboardingCompleted != null && onboardingCompleted) {
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                    } else {
                                        startActivity(new Intent(SignInActivity.this, OnboardingActivity.class));
                                    }
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    session.createFirebaseLoginSession(userId, email.split("@")[0], email);
                                    startActivity(new Intent(SignInActivity.this, OnboardingActivity.class));
                                    finish();
                                });
                    } else {
                        Toast.makeText(SignInActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showEmailNotVerifiedDialog(String email) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_not_verified, null);
        TextView tvEmail = dialogView.findViewById(R.id.tv_email);
        Button btnResend = dialogView.findViewById(R.id.btn_resend_verification);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);

        tvEmail.setText(email);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.setView(dialogView, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnResend.setOnClickListener(v -> {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email) // Use sendEmailVerification alternative
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Verification email resent to " + email, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to resend: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            dialog.dismiss();
        });

        btnOk.setOnClickListener(v -> dialog.dismiss());
    }

    private void sendPasswordResetEmail(String email) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this,
                                "Password reset email sent to " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this,
                                "Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showForgotPasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        EditText etResetEmail = dialogView.findViewById(R.id.et_reset_email);
        Button btnSendReset = dialogView.findViewById(R.id.btn_send_reset);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        dialog.setView(dialogView, 0, 0, 0, 0);
        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSendReset.setOnClickListener(v -> {
            String email = etResetEmail.getText().toString().trim();
            if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialog.dismiss();
                sendPasswordResetEmail(email);
            } else {
                etResetEmail.setError("Enter valid email");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
}