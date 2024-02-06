package com.fictiveds.potoksoznaniya;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fictiveds.potoksoznaniya.UI.AnimationManager;
import com.fictiveds.potoksoznaniya.UI.AuthManager;
import com.fictiveds.potoksoznaniya.UI.DialogManager;
import com.fictiveds.potoksoznaniya.UI.ImageHandler;
import com.fictiveds.potoksoznaniya.UI.ImageManager;
import com.fictiveds.potoksoznaniya.UI.UserProfileManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private EditText etUsernameEdit;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;

    private UserProfileManager userProfileManager;
    private ImageManager imageManager;
    private ImageHandler imageHandler;
    private DialogManager dialogManager;
    private AuthManager authManager;
    private AnimationManager animationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        userProfileManager = new UserProfileManager(this);
        imageManager = new ImageManager(this);
        dialogManager = new DialogManager(this, imageManager, profileImage);
        authManager = new AuthManager(this);
        animationManager = new AnimationManager(this);

        tvUsername = findViewById(R.id.tvUsername);
        etUsernameEdit = findViewById(R.id.etUsernameEdit);
        ImageButton btnEditUsername = findViewById(R.id.btnEditUsername);
        ImageButton btnSaveUsername = findViewById(R.id.btnSaveUsername);
        tvEmail = findViewById(R.id.tvEmail);
        profileImage = findViewById(R.id.profileImage);
        ImageButton btnEditProfileImage = findViewById(R.id.btnEditProfileImage);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userProfileManager.loadUsernameFromFirebaseOrDefault(user.getUid(), new UserProfileManager.UsernameLoadListener() {
                @Override
                public void onUsernameLoaded(String username) {
                    tvUsername.setText(username);
                }
            });
            imageManager.loadProfileImageFromFirebaseStorage(user.getUid(), profileImage, null);
        }

        btnEditUsername.setOnClickListener(v -> {
            String currentUsername = tvUsername.getText().toString();
            currentUsername = currentUsername.replace(getString(R.string.username), "");
            etUsernameEdit.setText(currentUsername);
            etUsernameEdit.requestFocus();
            etUsernameEdit.selectAll();
            toggleViewVisibility(etUsernameEdit, btnSaveUsername, tvUsername, btnEditUsername);
        });

        btnSaveUsername.setOnClickListener(v -> {
            String newUsername = etUsernameEdit.getText().toString();
            if (!newUsername.isEmpty() && user != null) {
                userProfileManager.saveUsernameToFirebaseDatabase(user.getUid(), newUsername);
                userProfileManager.saveUsernameToSharedPreferences(user.getUid(), newUsername);
                tvUsername.setText(getString(R.string.username) + newUsername);
                toggleViewVisibility(etUsernameEdit, btnSaveUsername, tvUsername, btnEditUsername);
               // String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            }
        });

        if (user != null) {
            tvEmail.setText(String.format("E-mail: %s", user.getEmail()));
        }

        MaterialButton btnChangePassword = findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> dialogManager.showChangePasswordDialog());

        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> authManager.logoutUser());

        animationManager.startFadeInAnimation(tvEmail, R.anim.fade_in);
        animationManager.startFadeInAnimation(tvUsername, R.anim.fade_in);
        animationManager.startFadeInAnimation(logoutButton, R.anim.fade_in);
        animationManager.startFadeInAnimation(btnChangePassword, R.anim.fade_in);

        btnEditProfileImage.setOnClickListener(v -> dialogManager.showEditProfileImageDialog());

        MaterialButton btnCardProduct = findViewById(R.id.btnCardProduct);
        btnCardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ProductCardActivity.class);
                startActivity(intent);
            }
        });
    }

    private void toggleViewVisibility(View... views) {
        for (View view : views) {
            view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && resultCode == RESULT_OK) {
            String userId = user.getUid();

            imageManager.handleActivityResult(requestCode, resultCode, data, profileImage, userId, new ImageHandler.UploadCallback() {
                @Override
                public void onUploadSuccess(Uri downloadUri) {
                    // Изображение успешно загружено и обновлено в интерфейсе пользователя
                    // Опционально: сохранение URL изображения в Firebase Database
                    userProfileManager.saveUserProfileImageToFirebaseDatabase(userId, downloadUri.toString());
                }

                @Override
                public void onUploadFailure(Exception exception) {
                    // Обработка ошибок при загрузке изображения
                    Toast.makeText(DashboardActivity.this, getString(R.string.err_load) + exception.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }


}
