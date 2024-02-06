package com.fictiveds.potoksoznaniya;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.email); // Убедитесь, что в layout есть соответствующие поля
        etPassword = findViewById(R.id.password);
        etConfirmPassword = findViewById(R.id.confirm_password);

        findViewById(R.id.email).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.password).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.confirm_password).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.register_button).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        if (userIsLoggedIn()) {
            openDashboardActivity();
        }
    }

    public void onRegisterClicked(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!isEmailValid(email)) {
            Toast.makeText(this, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, getString(R.string.pass_dont_match), Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, getString(R.string.pass_6_characters), Toast.LENGTH_SHORT).show();
            return;
        }

        registerUser(email, password);
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("RegisterActivity", "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUIAfterRegistration(user);
                    } else {
                        if (task.getException() != null) {
                            Log.w("RegisterActivity", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, getString(R.string.auth_err) + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.w("RegisterActivity", "createUserWithEmail:failure, exception was null");
                            Toast.makeText(RegisterActivity.this, getString(R.string.unk_auth_err),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUIAfterRegistration(FirebaseUser user) {
        if (user != null) {
            saveUserCredentials(user.getEmail());
            openDashboardActivity();
        }
    }

    private void saveUserCredentials(String email) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private boolean userIsLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("isLoggedIn", false);
    }

    private void openDashboardActivity() {
        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Слайд анимация перехода
        finish();
    }
}
