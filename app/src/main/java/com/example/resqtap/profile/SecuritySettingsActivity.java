package com.example.resqtap.profile;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.utils.BottomNavUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.resqtap.security.SetPinActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SecuritySettingsActivity
 * Tetapan Keselamatan: Kunci aplikasi (PIN 4 digit), Biometrik / Cap Jari,
 * Kemas kini kata laluan, Status keselamatan peranti, dan Pelindung Duress SOS.
 */
public class SecuritySettingsActivity extends BaseActivity {

    private final ActivityResultLauncher<Intent> setPinLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                syncSecuritySettingsUI();
            });

    private SwitchMaterial toggleAppLock;
    private SwitchMaterial toggleBiometric;
    private SwitchMaterial toggleDuressSafeguard;
    private View rowChangePin;
    private View rowBiometric;
    private boolean isSyncingUI = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_security_settings);

        View root = findViewById(R.id.main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        BottomNavUtils.setup(bottomNav, this, R.id.nav_bottom_profile);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            try {
                int pl = bottomNav.getPaddingLeft();
                int pt = bottomNav.getPaddingTop();
                int pr = bottomNav.getPaddingRight();
                bottomNav.setPadding(pl, pt, pr, systemBars.bottom);
            } catch (Exception ignored) {
            }
            return insets;
        });

        View back = findViewById(R.id.btn_back);
        if (back != null) {
            back.setOnClickListener(v -> finish());
        }

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncSecuritySettingsUI();
    }

    private void initViews() {
        toggleAppLock = findViewById(R.id.toggle_app_lock);
        toggleBiometric = findViewById(R.id.toggle_biometric);
        toggleDuressSafeguard = findViewById(R.id.toggle_duress_safeguard);
        rowChangePin = findViewById(R.id.row_change_pin);
        rowBiometric = findViewById(R.id.row_biometric);

        syncSecuritySettingsUI();
    }

    private void syncSecuritySettingsUI() {
        isSyncingUI = true;
        try {
            boolean appLockOn = UserPrefs.isAppLockEnabled(this) && UserPrefs.getAppLockPin(this).length() == 4;
            boolean biometricOn = UserPrefs.isFingerprintEnabled(this);
            boolean duressOn = UserPrefs.isDuressSafeguardEnabled(this);

            if (toggleAppLock != null) {
                toggleAppLock.setChecked(appLockOn);
            }

            // Semak sokongan perkakasan biometrik (Fingerprint / Device PIN)
            BiometricManager biometricManager = BiometricManager.from(this);
            int authenticators = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    ? (BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    : (BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

            int canAuth = biometricManager.canAuthenticate(authenticators);

            if (canAuth == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ||
                canAuth == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE) {
                // Peranti tiada sebarang sensor atau kunci peranti - sembunyikan baris
                if (rowBiometric != null) {
                    rowBiometric.setVisibility(View.GONE);
                }
                if (toggleBiometric != null) {
                    toggleBiometric.setChecked(false);
                    toggleBiometric.setEnabled(false);
                }
                UserPrefs.setFingerprintEnabled(this, false);
            } else {
                // Peranti menyokong biometrik / kunci peranti
                if (rowBiometric != null) {
                    rowBiometric.setVisibility(View.VISIBLE);
                }
                if (toggleBiometric != null) {
                    toggleBiometric.setChecked(biometricOn);
                    toggleBiometric.setEnabled(true);
                }
            }

            if (toggleDuressSafeguard != null) {
                toggleDuressSafeguard.setChecked(duressOn);
            }
            if (rowChangePin != null) {
                rowChangePin.setVisibility(appLockOn ? View.VISIBLE : View.GONE);
            }
        } finally {
            isSyncingUI = false;
        }
    }

    private void setupListeners() {
        View rowAppLock = findViewById(R.id.row_app_lock);
        if (rowAppLock != null && toggleAppLock != null) {
            rowAppLock.setOnClickListener(v -> toggleAppLock.toggle());
        }

        if (rowBiometric != null && toggleBiometric != null) {
            rowBiometric.setOnClickListener(v -> toggleBiometric.toggle());
        }

        View rowDuress = findViewById(R.id.row_duress_safeguard);
        if (rowDuress != null && toggleDuressSafeguard != null) {
            rowDuress.setOnClickListener(v -> toggleDuressSafeguard.toggle());
        }

        if (toggleAppLock != null) {
            toggleAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isSyncingUI) return;
                if (isChecked) {
                    // Setiap kali Pin Lock di-enable semula, buka UI Pin (SetPinActivity)
                    if (!UserPrefs.isAppLockEnabled(this) || UserPrefs.getAppLockPin(this).length() != 4) {
                        openSetPin(false);
                    } else {
                        if (rowChangePin != null) rowChangePin.setVisibility(View.VISIBLE);
                    }
                } else {
                    boolean wasEnabled = UserPrefs.isAppLockEnabled(this);
                    UserPrefs.setAppLockEnabled(this, false);
                    UserPrefs.setAppLockPin(this, "");
                    if (rowChangePin != null) {
                        rowChangePin.setVisibility(View.GONE);
                    }
                    if (wasEnabled) {
                        Toast.makeText(this, R.string.security_app_lock_disabled, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (toggleBiometric != null) {
            toggleBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isSyncingUI) return;
                if (isChecked) {
                    BiometricManager bm = BiometricManager.from(this);
                    int authenticators = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                            ? (BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                            : (BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

                    int authResult = bm.canAuthenticate(authenticators);
                    if (authResult == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                        Toast.makeText(this, R.string.security_biometric_not_enrolled, Toast.LENGTH_LONG).show();
                        toggleBiometric.setChecked(false);
                        UserPrefs.setFingerprintEnabled(this, false);
                        try {
                            Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                            startActivity(enrollIntent);
                        } catch (Exception ignored) {
                        }
                        return;
                    } else if (authResult != BiometricManager.BIOMETRIC_SUCCESS) {
                        Toast.makeText(this, R.string.security_biometric_unsupported, Toast.LENGTH_SHORT).show();
                        toggleBiometric.setChecked(false);
                        UserPrefs.setFingerprintEnabled(this, false);
                        return;
                    }
                }
                UserPrefs.setFingerprintEnabled(this, isChecked);
            });
        }

        if (toggleDuressSafeguard != null) {
            toggleDuressSafeguard.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isSyncingUI) return;
                UserPrefs.setDuressSafeguardEnabled(this, isChecked);
            });
        }

        if (rowChangePin != null) {
            rowChangePin.setOnClickListener(v -> openSetPin(true));
        }

        View rowChangePassword = findViewById(R.id.row_change_password);
        if (rowChangePassword != null) {
            rowChangePassword.setOnClickListener(v -> handlePasswordReset());
        }
    }

    private void openSetPin(boolean isChanging) {
        Intent intent = new Intent(this, SetPinActivity.class);
        intent.putExtra(SetPinActivity.EXTRA_IS_CHANGING, isChanging);
        setPinLauncher.launch(intent);
    }

    private void handlePasswordReset() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String email = user != null ? user.getEmail() : UserPrefs.getEmail(this);
        if (user == null || email == null || email.trim().isEmpty()) {
            Toast.makeText(this, R.string.security_password_reset_no_email, Toast.LENGTH_SHORT).show();
            return;
        }

        View content = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null, false);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(content)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText inputCurrent = content.findViewById(R.id.input_current_password);
        EditText inputNew = content.findViewById(R.id.input_new_password);
        EditText inputConfirm = content.findViewById(R.id.input_confirm_password);
        View btnCancel = content.findViewById(R.id.btn_cancel_password);
        View btnSave = content.findViewById(R.id.btn_save_password);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String curPass = inputCurrent != null ? inputCurrent.getText().toString().trim() : "";
                String newPass = inputNew != null ? inputNew.getText().toString().trim() : "";
                String confirmPass = inputConfirm != null ? inputConfirm.getText().toString().trim() : "";

                if (curPass.isEmpty()) {
                    Toast.makeText(this, R.string.security_current_password_hint, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPass.length() < 6) {
                    Toast.makeText(this, R.string.security_password_too_short, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass.equals(confirmPass)) {
                    Toast.makeText(this, R.string.security_password_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                btnSave.setEnabled(false);

                // Re-authenticate user with current password
                com.google.firebase.auth.AuthCredential credential =
                        com.google.firebase.auth.EmailAuthProvider.getCredential(email.trim(), curPass);

                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> {
                            user.updatePassword(newPass)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, R.string.security_password_updated, Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        btnSave.setEnabled(true);
                                        Toast.makeText(this, e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(this, R.string.security_password_incorrect, Toast.LENGTH_SHORT).show();
                        });
            });
        }

        dialog.show();
    }
}
