package com.fictiveds.potoksoznaniya;

import android.content.Intent;
import com.fictiveds.potoksoznaniya.UI.UserProfileManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final int RC_SIGN_IN = 9001;
    private UserProfileManager userProfileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


/*        FirebaseApp.initializeApp( context =  this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());*/

        setContentView(R.layout.activity_main);
        userProfileManager = new UserProfileManager(this);

        etUsername = findViewById(R.id.login);
        etPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();

        initializeUI();
        if (userIsLoggedIn()) {
          //  loadLanguageSettings(); // Загружаем языковые настройки для пользователя
            openDashboardActivity();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button_google).setOnClickListener(v -> signInWithGoogle());


        findViewById(R.id.emailInputLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.passwordInputLayout).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.sign_in_button).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.register_button).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        findViewById(R.id.sign_in_button_google).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

    }


    private boolean userIsLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null; // Возвращает true, если пользователь уже вошел
    }


    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        openDashboardActivity();
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(MainActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Метод для открытия RegisterActivity
    public void openRegisterActivity(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Анимация перехода
    }

    public void onSignInClicked(View view) {
        String email = etUsername.getText().toString().trim(); // Предполагаем, что etUsername на самом деле содержит email
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.enter_email_pass, Toast.LENGTH_SHORT).show();
        } else {
            signInWithEmailPassword(email, password);
        }
    }

    private void signInWithEmailPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Вход успешно выполнен, обновляем UI с информацией о пользователе
                        Log.d("MainActivity", "signInWithEmail:success");
                        openDashboardActivity();
                    } else {
                        // Если вход не удался, выводим сообщение пользователю
                        Log.w("MainActivity", "signInWithEmail:failure", task.getException());
                        Toast.makeText(MainActivity.this, R.string.auth_err,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserCredentials() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username", etUsername.getText().toString());
        editor.putString("password", etPassword.getText().toString());
        editor.apply();
    }

    private void initializeUI() {
        etUsername = findViewById(R.id.login);
        etPassword = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
      //  configureGoogleSignIn();

        findViewById(R.id.sign_in_button_google).setOnClickListener(v -> signInWithGoogle());
        // Анимации и другие элементы UI
    }

    private void openDashboardActivity() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Анимация перехода
        finish();
    }

}

