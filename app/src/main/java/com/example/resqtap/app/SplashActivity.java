package com.example.resqtap.app;
import com.example.resqtap.R;

import com.example.resqtap.auth.GetStartedActivity;
import com.example.resqtap.auth.LoginActivity;
import com.example.resqtap.auth.RegisterActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;


/**
 * SplashActivity
 * Splash screen: check status login Firebase Auth sebelum masuk ke Main atau Login.
 */
public class SplashActivity extends BaseActivity {
    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View logoWrapper = findViewById(R.id.logo_wrapper);
        View appName = findViewById(R.id.app_name);
        View tagline = findViewById(R.id.tagline);

        if (logoWrapper != null) {
            logoWrapper.setVisibility(View.VISIBLE);
            Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo_in);
            logoWrapper.startAnimation(logoAnim);
        }

        if (appName != null) {
            appName.setVisibility(View.VISIBLE);
            Animation textAnim = AnimationUtils.loadAnimation(this, R.anim.splash_text_in);
            appName.startAnimation(textAnim);
        }

        if (tagline != null) {
            tagline.setVisibility(View.VISIBLE);
            Animation taglineAnim = AnimationUtils.loadAnimation(this, R.anim.splash_tagline_in);
            tagline.startAnimation(taglineAnim);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            com.google.firebase.auth.FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) {
                // If local profile is already complete, enter MainActivity immediately
                if (UserPrefs.isPersonalInfoComplete(this)) {
                    launchMain();
                    return;
                }

                // If local prefs are incomplete or fresh install, attempt to load from RTDB
                com.google.firebase.database.FirebaseDatabase.getInstance(com.example.resqtap.room.FirebaseRoomClient.DATABASE_URL)
                        .getReference("users")
                        .child(current.getUid())
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (isFinishing() || isDestroyed()) return;
                            if (snapshot != null && snapshot.exists()) {
                                UserPrefs.applyUserSnapshot(SplashActivity.this, snapshot);
                            }
                            // Always allow authenticated user into MainActivity!
                            launchMain();
                        })
                        .addOnFailureListener(e -> {
                            if (isFinishing() || isDestroyed()) return;
                            // Offline or network lag: still let authenticated user access MainActivity
                            launchMain();
                        });
            } else {
                Intent i = new Intent(SplashActivity.this, GetStartedActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                try {
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                } catch (Exception ignored) {
                }
                finish();
            }
        }, 2500);
    }

    private void launchMain() {
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        try {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } catch (Exception ignored) {
        }
        finish();
    }
}

