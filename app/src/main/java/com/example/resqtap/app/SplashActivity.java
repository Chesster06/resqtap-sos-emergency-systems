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
                if (!UserPrefs.isPersonalInfoComplete(this)) {
                    Intent i = new Intent(SplashActivity.this, RegisterActivity.class);
                    i.putExtra("complete_profile", true);
                    i.putExtra("uid", current.getUid());
                    i.putExtra("email", current.getEmail());
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                    return;
                }
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                Intent i = new Intent(SplashActivity.this, GetStartedActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }
            try {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            } catch (Exception ignored) {
            }
            finish();
        }, 2500);
    }
}

