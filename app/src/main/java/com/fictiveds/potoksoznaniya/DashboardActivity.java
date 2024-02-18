package com.fictiveds.potoksoznaniya;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.fictiveds.potoksoznaniya.UI.AnimationManager;
import com.fictiveds.potoksoznaniya.UI.AppDatabase;
import com.fictiveds.potoksoznaniya.UI.AuthManager;
import com.fictiveds.potoksoznaniya.UI.DialogManager;
import com.fictiveds.potoksoznaniya.UI.ImageHandler;
import com.fictiveds.potoksoznaniya.UI.ImageManager;
import com.fictiveds.potoksoznaniya.UI.UserProfileManager;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import de.hdodenhof.circleimageview.CircleImageView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUsername;
    private EditText etUsernameEdit;
    private CircleImageView profileImage;

    private UserProfileManager userProfileManager;
    private ImageHandler imageHandler;
    private DialogManager dialogManager;
    private AuthManager authManager;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        userProfileManager = new UserProfileManager(this);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name").build();
        imageHandler = new ImageHandler(this, executorService, db);
        dialogManager = new DialogManager(this, imageHandler, profileImage);
        authManager = new AuthManager(this);
        AnimationManager animationManager = new AnimationManager(this);
        Appodeal.show(this, Appodeal.BANNER_BOTTOM);

        tvUsername = findViewById(R.id.tvUsername);
        etUsernameEdit = findViewById(R.id.etUsernameEdit);
        ImageButton btnEditUsername = findViewById(R.id.btnEditUsername);
        ImageButton btnSaveUsername = findViewById(R.id.btnSaveUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);
        profileImage = findViewById(R.id.profileImage);
        ImageButton btnEditProfileImage = findViewById(R.id.btnEditProfileImage);
        Appodeal.cache(this, Appodeal.INTERSTITIAL);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userProfileManager.loadUsernameFromFirebaseOrDefault(user.getUid(), new UserProfileManager.UsernameLoadListener() {
                @Override
                public void onUsernameLoaded(String username) {
                    tvUsername.setText(username);
                }
            });
            imageHandler.loadProfileImageFromFirebaseStorage(user.getUid(), profileImage, null);
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
               // userProfileManager.saveUsernameToSharedPreferences(user.getUid(), newUsername);
                tvUsername.setText(String.format("%s%s", getString(R.string.username), newUsername));
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
/*        btnCardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DashboardActivity.this, ProductCardActivity.class);
                startActivity(intent);
            }
        });*/

        btnCardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Проверяем, загружена ли интерстициальная реклама
                if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                    // Устанавливаем обратный вызов
                    Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                        @Override
                        public void onInterstitialLoaded(boolean isPrecache) {
                            // Реклама загружена, это место для логгирования или статистики, если требуется
                        }

                        @Override
                        public void onInterstitialFailedToLoad() {
                            // Реклама не загрузилась, переходим на активность
                            startProductCardActivity();
                        }

                        @Override
                        public void onInterstitialShown() {
                            // Реклама показана, это место для логгирования или статистики, если требуется
                        }

                        @Override
                        public void onInterstitialClicked() {
                            // Реклама кликнута, это место для логгирования или статистики, если требуется
                        }

                        @Override
                        public void onInterstitialClosed() {
                            // Пользователь закрыл рекламу, переходим на ProductCardActivity
                            startProductCardActivity();
                        }

                        @Override
                        public void onInterstitialShowFailed() {
                            // Показ рекламы не удался, переходим на активность
                            startProductCardActivity();
                        }

                        @Override
                        public void onInterstitialExpired() {
                            // Реклама "просрочилась" и не может быть показана
                            // Это случается, если реклама была загружена, но слишком долго не показывалась
                            startProductCardActivity();
                        }
                    });

                    // Показываем рекламу
                    Appodeal.show(DashboardActivity.this, Appodeal.INTERSTITIAL);
                } else {
                    // Реклама не загружена, сразу переходим на ProductCardActivity
                    startProductCardActivity();
                }
            }

            private void startProductCardActivity() {
                // Удаляем обратные вызовы, чтобы избежать повторных вызовов
                Appodeal.setInterstitialCallbacks(null);
                Intent intent = new Intent(DashboardActivity.this, ProductCardActivity.class);
                startActivity(intent);
            }
        });



        getLocationPermission();

        findViewById(R.id.btnFindFriend).setOnClickListener(v -> {
            Dialog dialog = new Dialog(DashboardActivity.this);
            dialog.setContentView(R.layout.dialog_find_friend);

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
            }
            Button btnFind = dialog.findViewById(R.id.btnFind);

            btnFind.setOnClickListener(view -> {
                if (!locationPermissionGranted) {
                    getLocationPermission();
                } else {
                    // Если разрешение уже есть, получаем геолокацию и сохраняем ее
                    getCurrentLocationAndSave();
                }
            });


            dialog.show();
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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

            imageHandler.handleActivityResult(requestCode, resultCode, data, profileImage, userId, new ImageHandler.UploadCallback() {
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

    // Метод для запроса разрешения
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                // Теперь мы можем безопасно вызвать метод для получения локации
                getCurrentLocationAndSave();
            } else {
                // Обработка отказа пользователя предоставить разрешение
                Toast.makeText(this, "Разрешение на геолокацию не предоставлено", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocationAndSave() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Получаем последнюю известную локацию
                        Location lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            // Здесь код для сохранения геолокации в Firebase
                            saveLocationInFirebase(lastKnownLocation);
                        }
                    } else {
                        Toast.makeText(DashboardActivity.this, "Невозможно получить текущее местоположение", Toast.LENGTH_LONG).show();
                    }
                });
            }
        } catch (SecurityException e)  {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveLocationInFirebase(Location location) {
        // Получаем ID текущего пользователя
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Сохраняем локацию в Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("UsersLocation");
        HashMap<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        ref.child(userId).setValue(locationData)
                .addOnSuccessListener(aVoid -> Toast.makeText(DashboardActivity.this, "Локация сохранена в Firebase", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(DashboardActivity.this, "Ошибка при сохранении локации", Toast.LENGTH_SHORT).show());
    }
}
