package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fictiveds.potoksoznaniya.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileManager {

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final FirebaseAuth mAuth;

    public interface UsernameLoadListener {
        void onUsernameLoaded(String username);
    }

    public UserProfileManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
    }

    public void loadUsernameFromFirebaseOrDefault(String userId, UsernameLoadListener listener) {
        String usernamePrefix = context.getString(R.string.username);
        String defaultUsername = context.getString(R.string.default_username);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firebaseUsername = dataSnapshot.getValue(String.class);
                    Log.d("FirebaseDB", "Username found in Firebase Database: " + firebaseUsername);
                    saveUsernameToSharedPreferences(userId, firebaseUsername);
                    listener.onUsernameLoaded(usernamePrefix + firebaseUsername);
                } else {
                    String username = loadUsernameFromSharedPreferences(userId, defaultUsername);
                    listener.onUsernameLoaded(usernamePrefix + username);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseDB", "Error fetching username: " + databaseError.getMessage());
                String username = loadUsernameFromSharedPreferences(userId, defaultUsername);
                listener.onUsernameLoaded(usernamePrefix + username);
            }
        });
    }

    public String loadUsernameFromSharedPreferences(String userId, String defaultUsername) {
        String username = sharedPreferences.getString(userId + "_username", defaultUsername);
        Log.d("SharedPreferences", "Username loaded from SharedPreferences: " + username);
        return username;
    }

    public void saveUsernameToSharedPreferences(String userId, String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(userId + "_username", username);
        editor.apply();
        Log.d("SharedPreferences", "Username saved to SharedPreferences.");
    }

    public void saveUsernameToFirebaseDatabase(String userId, String username) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("username").setValue(username).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseDB", "Username for userId: " + userId + " was successfully saved in Firebase Database.");
            } else {
                if (task.getException() != null) {
                    Log.e("FirebaseDB", "Failed to save username for userId: " + userId + ". Error: " + task.getException().getMessage());
                } else {
                    Log.e("FirebaseDB", "Failed to save username for userId: " + userId + ". No further details were provided.");
                }
            }
        });
    }

    public void saveUserProfileImageToFirebaseDatabase(String userId, String imageUrl) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("profileImageUrl").setValue(imageUrl).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("FirebaseDB", "Profile image URL for userId: " + userId + " was successfully saved in Firebase Database.");
            } else {
                if (task.getException() != null) {
                    Log.e("FirebaseDB", "Failed to save profile image URL for userId: " + userId + ". Error: " + task.getException().getMessage());
                } else {
                    Log.e("FirebaseDB", "Failed to save profile image URL for userId: " + userId + ". No further details were provided.");
                }
            }
        });
    }

}

