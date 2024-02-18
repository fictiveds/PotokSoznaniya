package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
// import android.content.SharedPreferences;
import android.util.Log;

import com.fictiveds.potoksoznaniya.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.room.Room;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class UserProfileManager {

    private final Context context;
   // private final SharedPreferences sharedPreferences;
    private final FirebaseAuth mAuth;
    private final ExecutorService executorService;
    private AppDatabase db;

    public interface UsernameLoadListener {
        void onUsernameLoaded(String username);
    }

    public UserProfileManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        // this.sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        this.executorService = Executors.newSingleThreadExecutor(); // Используйте single thread executor для последовательного выполнения задач
        this.db = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class, "database-name").build();
    }

    public void loadUsernameFromFirebaseOrDefault(String userId, UsernameLoadListener listener) {
        executorService.execute(() -> {
            User user = db.userDao().getUserById(userId);
            if (user != null && user.username != null) {
                // Имя пользователя загружено из базы данных Room
                listener.onUsernameLoaded(context.getString(R.string.username) + user.username);
            } else {
                // Загрузка имени пользователя из Firebase
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
                userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String firebaseUsername = dataSnapshot.getValue(String.class);
                            // Сохраняем имя пользователя в Room
                            executorService.execute(() -> db.userDao().insertUser(new User(userId, firebaseUsername, null)));
                            listener.onUsernameLoaded(firebaseUsername);
                        } else {
                            listener.onUsernameLoaded("DefaultUsername");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("FirebaseDB", "Error fetching username: " + databaseError.getMessage());
                        listener.onUsernameLoaded("DefaultUsername");
                    }
                });
            }
        });
    }

    public void saveUsernameToFirebaseDatabase(String userId, String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("username").setValue(username).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Выполняем вставку в базу данных в фоновом потоке
                executorService.execute(() -> {
                    User user = new User(userId, username, null); // Создаём объект User
                    db.userDao().insertUser(user); // Вставляем пользователя в базу данных Room
                });
            } else {
                if (task.getException() != null) {
                    Log.e("FirebaseDB", "Failed to save username: " + task.getException().getMessage());
                }
            }
        });
    }

    public void saveUserProfileImageToFirebaseDatabase(String userId, String imageUrl) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("profileImageUrl").setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Выполняем обновление базы данных в фоновом потоке
                executorService.execute(() -> {
                    User user = db.userDao().getUserById(userId);
                    if (user == null) {
                        user = new User(userId, null, imageUrl); // Предполагаем, что имя пользователя пока неизвестно
                    } else {
                        user.profileImagePath = imageUrl;
                    }
                    db.userDao().insertUser(user);
                });
            } else {
                if (task.getException() != null) {
                    Log.e("FirebaseDB", "Failed to save profile image URL: " + task.getException().getMessage());
                }
            }
        });
    }


}

