package com.example.resqtap.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.utils.ThemeUtils;

/**
 * GetStartedActivity
 * Onboarding screen matching the modern reference design.
 */
public class GetStartedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_started);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);

            View bottomCard = findViewById(R.id.bottom_card);
            if (bottomCard != null) {
                int basePaddingBottom = (int) (36 * getResources().getDisplayMetrics().density);
                bottomCard.setPadding(
                        bottomCard.getPaddingLeft(),
                        bottomCard.getPaddingTop(),
                        bottomCard.getPaddingRight(),
                        basePaddingBottom + systemBars.bottom
                );
            }
            return insets;
        });

        Button btnGetStarted = findViewById(R.id.btn_get_started);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> {
                Intent intent = new Intent(GetStartedActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
