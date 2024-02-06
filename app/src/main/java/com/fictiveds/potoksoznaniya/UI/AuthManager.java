package com.fictiveds.potoksoznaniya.UI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fictiveds.potoksoznaniya.MainActivity;
import com.fictiveds.potoksoznaniya.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class AuthManager {

    private final Context context;
    private final FirebaseAuth mAuth;

    public AuthManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    public void logoutUser() {
        // Firebase sign-out
        mAuth.signOut();

        // Google Sign-In sign-out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            // Clear user's login data
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("email");
            editor.remove("isLoggedIn");
            editor.apply();

            // Transition back to MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
            // If you have an animation transition, add it here
        });
    }

}
