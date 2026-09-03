package com.example.resqtap.auth;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.resqtap.contacts.EmergencyContact;
import com.example.resqtap.sos.SosServiceStarter;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * LoginActivity
 * Skrin Login: verify email & password guna Firebase Auth, ada check format Gmail dan auto-redirect.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    /** Fungsi untuk shouldAnimateContentIn. */
    protected boolean shouldAnimateContentIn() {
        return false;
    }

    /** Inisialisasi paparan dan komponen UI. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            View authForm = findViewById(R.id.auth_form);
            if (authForm != null) {
                int basePaddingBottom = (int) (36 * getResources().getDisplayMetrics().density);
                authForm.setPadding(
                        authForm.getPaddingLeft(),
                        authForm.getPaddingTop(),
                        authForm.getPaddingRight(),
                        basePaddingBottom + systemBars.bottom
                );
            }
            return insets;
        });

        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        if (current != null) {
            // Fast path: kalau profile dah lengkap secara lokal, terus ke MainActivity tanpa DB read
            if (UserPrefs.isPersonalInfoComplete(this)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }
            // Slow path: perlu check DB untuk profile tak lengkap / baru
            FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                    .getReference("users")
                    .child(current.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot != null && snapshot.exists()) {
                            applyUserSnapshot(snapshot);
                            if (!UserPrefs.isPersonalInfoComplete(this)) {
                                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                intent.putExtra("complete_profile", true);
                                intent.putExtra("uid", current.getUid());
                                intent.putExtra("email", current.getEmail());
                                startActivity(intent);
                                finish();
                                return;
                            }
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("complete_profile", true);
                            intent.putExtra("uid", current.getUid());
                            intent.putExtra("email", current.getEmail());
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {

                    });
        }

        if (getIntent() != null && getIntent().getBooleanExtra("registered_success", false)) {
            Toast.makeText(this, R.string.toast_register_success_login, Toast.LENGTH_LONG).show();
        }

        androidx.core.widget.NestedScrollView authPanel = findViewById(R.id.auth_panel);
        TextInputLayout emailLayout = findViewById(R.id.layout_email);
        TextInputLayout passwordLayout = findViewById(R.id.layout_password);
        TextInputEditText email = findViewById(R.id.input_email);
        TextInputEditText password = findViewById(R.id.input_password);
        MaterialButton btnLogin = findViewById(R.id.btn_login);
        TextView btnGoRegister = findViewById(R.id.btn_go_register);
        TextView forgotPassword = findViewById(R.id.tv_forgot_password);
        forceHeavyText(findViewById(R.id.title));
        forceHeavyText(btnGoRegister);

        if (btnGoRegister != null) {
            btnGoRegister.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        View btnSocialApple = findViewById(R.id.btn_social_apple);
        View btnSocialGoogle = findViewById(R.id.btn_social_google);
        if (btnSocialApple != null) {
            btnSocialApple.setOnClickListener(v -> Toast.makeText(this, "Apple Sign-In is coming soon", Toast.LENGTH_SHORT).show());
        }
        if (btnSocialGoogle != null) {
            btnSocialGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Sign-In is coming soon", Toast.LENGTH_SHORT).show());
        }

        playLoginEntranceAnimations();

        emailLayout.setEndIconOnClickListener(v -> {});
        emailLayout.setEndIconCheckable(false);

        email.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String value = s == null ? "" : s.toString().trim();
                if (loginEmailDebounce != null) loginHandler.removeCallbacks(loginEmailDebounce);
                loginEmailKnownRegistered = false;
                btnLogin.setEnabled(true);

                if (value.isEmpty()) {
                    emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                    emailLayout.setEndIconDrawable(null);
                    emailLayout.setEndIconTintList(null);
                    return;
                }

                if (!isGmail(value)) {
                    emailLayout.setErrorEnabled(true);
                    emailLayout.setError(getString(R.string.gmail_only_warning));
                    emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                    emailLayout.setEndIconTintList(null);
                    return;
                }

                if (emailLayout.getError() != null) {
                    emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                }
                emailLayout.setEndIconDrawable(null);
                emailLayout.setEndIconTintList(null);

                loginEmailDebounce = () -> checkLoginEmailRegistered(value, emailLayout, btnLogin);
                loginHandler.postDelayed(loginEmailDebounce, 80);
            }
        });

        password.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                if (passwordLayout != null) { passwordLayout.setError(null); passwordLayout.setErrorEnabled(false); }
            }
        });

        btnLogin.setOnClickListener(view -> {
            String emailValue = email.getText() == null ? "" : email.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            String passwordValue = password.getText() == null ? "" : password.getText().toString();

            if (emailValue.isEmpty()) {
                emailLayout.setError("Please enter your email address");
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                scrollToField(authPanel, emailLayout);
                return;
            }

            if (!isGmail(emailValue)) {
                emailLayout.setError(getString(R.string.gmail_only_warning));
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                scrollToField(authPanel, emailLayout);
                return;
            }

            if (passwordValue.isEmpty()) {
                if (passwordLayout != null) {
                    passwordLayout.setError("Please enter your password");
                    scrollToField(authPanel, passwordLayout);
                }
                return;
            }

            if (!loginEmailKnownRegistered) {
                emailLayout.setErrorEnabled(true);
                emailLayout.setError(getString(R.string.toast_email_not_registered));
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                Toast.makeText(this, R.string.toast_account_not_exist, Toast.LENGTH_SHORT).show();
                scrollToField(authPanel, emailLayout);
                return;
            }

            btnLogin.setEnabled(false);
            signInAndLoadProfile(emailValue, passwordValue, btnLogin);
        });

        forgotPassword.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            String emailValue = email.getText() == null ? "" : email.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
            if (!emailValue.isEmpty()) intent.putExtra("email", emailValue);
            startActivity(intent);
        });

    }

    private void scrollToField(androidx.core.widget.NestedScrollView scroll, View targetView) {
        if (targetView == null) return;
        targetView.requestFocus();
        if (scroll != null) {
            scroll.post(() -> {
                int[] targetLoc = new int[2];
                targetView.getLocationOnScreen(targetLoc);
                int[] scrollLoc = new int[2];
                scroll.getLocationOnScreen(scrollLoc);
                int scrollY = targetLoc[1] - scrollLoc[1] + scroll.getScrollY();
                scroll.smoothScrollTo(0, Math.max(0, scrollY - 80));
            });
        }
    }

    private Runnable loginEmailDebounce;
    private final Handler loginHandler = new Handler(Looper.getMainLooper());
    private boolean loginEmailKnownRegistered = false;

    public static String sanitizeEmailForDb(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(java.util.Locale.ROOT)
                .replace(".", "_")
                .replace("@", "_at_");
    }

    /** Semak sama ada e-mel telah berdaftar untuk paparan tanda semak hijau atau amaran merah. */
    private void checkLoginEmailRegistered(String emailStr, TextInputLayout emailLayout, MaterialButton btnLogin) {
        String query = emailStr.trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty() || !isGmail(query)) {
            return;
        }

        String sanitized = sanitizeEmailForDb(query);
        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("registeredEmails")
                .child(sanitized)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean isRegistered = snapshot != null && snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        loginEmailKnownRegistered = isRegistered;
                        if (isRegistered) {
                            emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                            emailLayout.setEndIconDrawable(R.drawable.ic_check_24);
                            emailLayout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(LoginActivity.this, R.color.success_green)));
                            btnLogin.setEnabled(true);
                        } else {
                            emailLayout.setErrorEnabled(true);
                            emailLayout.setError(getString(R.string.toast_email_not_registered));
                            emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                            emailLayout.setEndIconTintList(null);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        emailLayout.setEndIconDrawable(null);
                        emailLayout.setEndIconTintList(null);
                    }
                });
    }

    /** Semak dan sahkan Gmail. */
    private boolean isGmail(String email) {
        String e = String.valueOf(email == null ? "" : email).trim().toLowerCase();
        return isAllowedEmailDomain(e);
    }

    /** Semak dan sahkan AllowedEmailDomain. */
    private boolean isAllowedEmailDomain(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        return (e.endsWith("@gmail.com") || e.endsWith("@resqtap.com"))
                && (e.length() > "@gmail.com".length() || e.length() > "@resqtap.com".length());
    }

    /** Fungsi untuk forceHeavyText. */
    private void forceHeavyText(TextView textView) {
        if (textView == null) return;
        textView.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        textView.getPaint().setFakeBoldText(true);
        textView.invalidate();
    }

    /** Fungsi untuk openRegisterWithSwipe. */
    private void openRegisterWithSwipe() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /** Fungsi untuk fadeAuthFields. */
    private void fadeAuthFields(float fromAlpha, float toAlpha, Runnable endAction) {
        android.widget.LinearLayout form = findViewById(R.id.auth_form);
        if (form == null) {
            if (endAction != null) endAction.run();
            return;
        }
        int duration = 180;
        int animated = 0;
        for (int i = 1; i < form.getChildCount(); i++) {
            View child = form.getChildAt(i);
            child.animate().cancel();
            child.setAlpha(fromAlpha);
            child.animate().alpha(toAlpha).setDuration(duration).setStartDelay(i * 12L).start();
            animated++;
        }
        if (endAction == null) return;
        long delay = duration + (animated + 1L) * 12L;
        form.postDelayed(endAction, delay);
    }

    /** Kendalikan animasi LoginEntranceAnimations. */
    private void playLoginEntranceAnimations() {

        View header = null;
        View formCard = null;
        try {
            android.widget.LinearLayout root = (android.widget.LinearLayout) findViewById(R.id.main);
            if (root != null && root.getChildCount() > 0) {
                header = root.getChildAt(0);
            }
            if (root != null && root.getChildCount() > 1) {

                View scrollView = root.getChildAt(1);
                if (scrollView instanceof androidx.core.widget.NestedScrollView) {
                    androidx.core.widget.NestedScrollView nsv = (androidx.core.widget.NestedScrollView) scrollView;
                    if (nsv.getChildCount() > 0) {
                        View inner = nsv.getChildAt(0);
                        if (inner instanceof android.widget.LinearLayout) {
                            android.widget.LinearLayout innerLayout = (android.widget.LinearLayout) inner;
                            if (innerLayout.getChildCount() > 0) {
                                formCard = innerLayout.getChildAt(0);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        if (header != null) {
            header.setAlpha(0f);
            header.setTranslationY(-50f);
            header.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(550)
                    .setStartDelay(80)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        if (formCard != null) {
            formCard.setAlpha(0f);
            formCard.setTranslationY(80f);
            formCard.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(250)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }
    }

    /** Setup dan konfigurasi RegisterPrompt. */
    private void setupRegisterPrompt(TextView registerPrompt) {
        registerPrompt.setText(R.string.login_signup_action);
        registerPrompt.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        registerPrompt.setOnClickListener(view -> openRegisterWithSwipe());
        registerPrompt.setHighlightColor(android.graphics.Color.TRANSPARENT);
        if (registerPrompt.getId() == R.id.btn_go_register) {
            return;
        }

        String fullText = getString(R.string.login_signup_prompt);
        String actionText = getString(R.string.login_signup_action);
        int start = fullText.indexOf(actionText);
        if (start < 0) {
            registerPrompt.setOnClickListener(view -> openRegisterWithSwipe());
            return;
        }
        int end = start + actionText.length();
        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new ClickableSpan() {
            /** Handle event klik butang/elemen UI. */
    @Override
            public void onClick(View widget) {
                openRegisterWithSwipe();
            }

            /** Simpan atau hantar data DrawState. */
    @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(LoginActivity.this, R.color.brand_primary));
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerPrompt.setText(spannable);
        registerPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        registerPrompt.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    /** Fungsi untuk signInAndLoadProfile. */
    private void signInAndLoadProfile(String emailValue, String passwordValue, MaterialButton btnLogin) {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(emailValue, passwordValue)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String uid = user.getUid();

                    FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                            .getReference("users")
                            .child(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                if (snapshot == null || !snapshot.exists()) {
                                    // User authenticated in Auth, but DB profile node is missing (incomplete setup or deleted from DB).
                                    // Seamlessly forward them to RegisterActivity to complete profile!
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(this, R.string.toast_login_profile_missing, Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                    intent.putExtra("complete_profile", true);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("email", emailValue);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                                UserPrefs.setUid(this, uid);
                                UserPrefs.setEmail(this, emailValue);
                                applyUserSnapshot(snapshot);

                                ensureNotificationsPermissionBestEffort();

                                // Navigate segera — jangan tunggu operasi DB lain
                                startActivity(new Intent(this, MainActivity.class));
                                finish();

                                // Fire-and-forget: admin check & room fetch di background
                                if (emailValue != null && emailValue.trim().toLowerCase(java.util.Locale.ROOT).endsWith("@resqtap.com")) {
                                    DatabaseReference adminRef = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                                            .getReference("admins")
                                            .child(uid);
                                    adminRef.get().addOnSuccessListener(adminSnap -> {
                                        if (adminSnap == null || !adminSnap.exists()) {
                                            Map<String, Object> adminData = new HashMap<>();
                                            adminData.put("active", true);
                                            adminData.put("email", emailValue.trim().toLowerCase(java.util.Locale.ROOT));
                                            adminData.put("name", UserPrefs.getName(this));
                                            adminData.put("role", "admin");
                                            adminData.put("assignedAt", System.currentTimeMillis());
                                            adminData.put("assignedBy", "system_auto_domain");
                                            adminRef.setValue(adminData);
                                        }
                                    });
                                }
                                FirebaseRoomClient.fetchMostRecentUserRoomCodeQueued(uid, code -> {
                                    String c = String.valueOf(code == null ? "" : code).trim().toUpperCase(java.util.Locale.ROOT);
                                    if (c.isEmpty()) return;
                                    try {
                                        UserPrefs.setActiveRoomCode(LoginActivity.this, c);
                                    } catch (Exception ignored) {
                                    }
                                    try {
                                        SosServiceStarter.start(LoginActivity.this, c);
                                    } catch (Exception ignored) {
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to read user profile from DB. uid=" + uid, e);
                                FirebaseAuth.getInstance().signOut();
                                UserPrefs.clearAccountData(this);
                                btnLogin.setEnabled(true);
                                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                                if (msg.contains("permission_denied")
                                        || (msg.contains("permission") && msg.contains("denied"))
                                        || msg.contains("permission denied")
                                        || msg.contains("denied")) {
                                    Toast.makeText(this, R.string.toast_login_db_denied, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithEmailAndPassword failed. email=" + emailValue, e);
                    showAuthError(e, btnLogin, emailValue);
                });
    }

    /** Paparkan AuthError. */
    private void showAuthError(Exception e, MaterialButton btnLogin, String emailValue) {
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equalsIgnoreCase(code)) {
                btnLogin.setEnabled(true);
                Toast.makeText(this, R.string.toast_too_many_requests, Toast.LENGTH_LONG).show();
                return;
            }
            if ("ERROR_USER_NOT_FOUND".equalsIgnoreCase(code)) {
                btnLogin.setEnabled(true);
                Toast.makeText(this, R.string.toast_account_not_exist, Toast.LENGTH_LONG).show();
                return;
            }
            if ("ERROR_WRONG_PASSWORD".equalsIgnoreCase(code)
                    || "ERROR_INVALID_CREDENTIAL".equalsIgnoreCase(code)
                    || "ERROR_INVALID_LOGIN_CREDENTIALS".equalsIgnoreCase(code)) {
                btnLogin.setEnabled(true);
                checkRegisteredEmail(emailValue, registered -> {
                    if (registered) {
                        Toast.makeText(this, R.string.toast_login_invalid, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, R.string.toast_account_not_exist, Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
        }
        if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
            btnLogin.setEnabled(true);
            Toast.makeText(this, R.string.toast_account_not_exist, Toast.LENGTH_LONG).show();
            return;
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            btnLogin.setEnabled(true);
            checkRegisteredEmail(emailValue, registered -> {
                if (registered) {
                    Toast.makeText(this, R.string.toast_login_invalid, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, R.string.toast_account_not_exist, Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        String detail = e.getMessage() == null ? "" : e.getMessage().trim();
        btnLogin.setEnabled(true);
        if (!detail.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_login_failed) + " (" + detail + ")", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.toast_login_failed, Toast.LENGTH_SHORT).show();
        }
    }

    /** Fungsi untuk ensureNotificationsPermissionBestEffort. */
    private void ensureNotificationsPermissionBestEffort() {
        if (android.os.Build.VERSION.SDK_INT < 33) return;
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) return;
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 9901);
        } catch (Exception ignored) {
        }
    }

    private interface BoolCallback {
        void onResult(boolean value);
    }

    private interface VoidCallback {
        void run();
    }

    /** Semak email dalam registeredEmails node (baca cache dulu). */
    private void checkRegisteredEmail(String email, BoolCallback callback) {
        String sanitized = sanitizeEmailForDb(email);
        if (sanitized.isEmpty()) {
            if (callback != null) callback.onResult(false);
            return;
        }
        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("registeredEmails")
                .child(sanitized)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean exists = snapshot != null && snapshot.exists()
                                && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        if (callback != null) callback.onResult(exists);
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (callback != null) callback.onResult(false);
                    }
                });
    }

    /** Semak dan sahkan EmailExistsInDb. */
    private void checkEmailExistsInDb(String emailLower, BoolCallback onSuccess, VoidCallback onFailure) {
        String emailValue = String.valueOf(emailLower == null ? "" : emailLower).trim().toLowerCase(java.util.Locale.ROOT);
        if (emailValue.isEmpty()) {
            if (onSuccess != null) onSuccess.onResult(false);
            return;
        }
        com.google.firebase.database.Query q = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("users")
                .orderByChild("email")
                .equalTo(emailValue)
                .limitToFirst(1);
        q.get()
                .addOnSuccessListener(snapshot -> {
                    boolean exists = snapshot != null && snapshot.exists();
                    if (onSuccess != null) onSuccess.onResult(exists);
                })
                .addOnFailureListener(err -> {
                    if (onFailure != null) onFailure.run();
                });
    }

    /** Fungsi untuk applyUserSnapshot. */
    private void applyUserSnapshot(DataSnapshot snapshot) {
        if (snapshot == null || !snapshot.exists()) return;
        Object nameObj = snapshot.child("name").getValue();
        if (nameObj != null) UserPrefs.setName(this, String.valueOf(nameObj));
        Object bloodObj = snapshot.child("bloodType").getValue();
        if (bloodObj != null) UserPrefs.setBloodType(this, String.valueOf(bloodObj));
        Object allergiesObj = snapshot.child("allergies").getValue();
        if (allergiesObj != null) UserPrefs.setAllergies(this, String.valueOf(allergiesObj));
        Object weightObj = snapshot.child("weight").getValue();
        UserPrefs.setWeight(this, weightObj == null ? "" : String.valueOf(weightObj));
        Object heightObj = snapshot.child("height").getValue();
        UserPrefs.setHeight(this, heightObj == null ? "" : String.valueOf(heightObj));
        Object genderObj = snapshot.child("gender").getValue();
        if (genderObj != null) UserPrefs.setGender(this, String.valueOf(genderObj));
        Object icObj = snapshot.child("icNumber").getValue();
        if (icObj != null) UserPrefs.setIcNumber(this, String.valueOf(icObj));

        Object medObj = snapshot.child("medications").getValue();
        String medications = medObj == null ? "" : String.valueOf(medObj).trim();
        Object condObj = snapshot.child("existingConditions").getValue();
        String conditions = condObj == null ? "" : String.valueOf(condObj).trim();
        if (conditions.isEmpty() && !medications.isEmpty()) {
            conditions = medications;
        }
        UserPrefs.setExistingConditions(this, conditions);
        UserPrefs.setMedications(this, medications);

        Object organObj = snapshot.child("organDonor").getValue();
        if (organObj != null) UserPrefs.setOrganDonor(this, String.valueOf(organObj));
        Object emailObj = snapshot.child("email").getValue();
        if (emailObj != null) UserPrefs.setEmail(this, String.valueOf(emailObj));
        Object publicIdObj = snapshot.child("publicId").getValue();
        String pubId = publicIdObj == null ? "" : String.valueOf(publicIdObj).trim();
        if (pubId.isEmpty()) {
            pubId = com.example.resqtap.friend.FirebaseFriendClient.format4DigitId(snapshot.getKey(), "");
            snapshot.getRef().child("publicId").setValue(pubId);
        } else {
            pubId = com.example.resqtap.friend.FirebaseFriendClient.format4DigitId(snapshot.getKey(), pubId);
        }
        UserPrefs.setPublicId(this, pubId);

        Object photoUrlObj = snapshot.child("photoUrl").getValue();
        String photoUrl = photoUrlObj == null ? "" : String.valueOf(photoUrlObj);
        if (photoUrl == null) photoUrl = "";
        photoUrl = photoUrl.trim();
        if (photoUrl.isEmpty()) {
            Object photoUriObj = snapshot.child("photoUri").getValue();
            photoUrl = photoUriObj == null ? "" : String.valueOf(photoUriObj);
            if (photoUrl == null) photoUrl = "";
            photoUrl = photoUrl.trim();
        }
        if (!photoUrl.isEmpty()) UserPrefs.setPhotoUrl(this, photoUrl);

        Object photoB64Obj = snapshot.child("photoB64").getValue();
        if (photoB64Obj != null) {
            String b64 = String.valueOf(photoB64Obj);
            if (b64 != null && !b64.trim().isEmpty()) UserPrefs.setPhotoB64(this, b64.trim());
        }

        String pendingPhotoUri = String.valueOf(UserPrefs.getPendingPhotoUri(this) == null ? "" : UserPrefs.getPendingPhotoUri(this)).trim();
        if (!pendingPhotoUri.isEmpty()) {
            retryPendingPhotoSaveToDb(currentUidFromAuth(), pendingPhotoUri);
        }

        Object addrObj = snapshot.child("address").getValue();
        if (addrObj != null) UserPrefs.setAddress(this, String.valueOf(addrObj));
        Object relObj = snapshot.child("religion").getValue();
        if (relObj != null) UserPrefs.setReligion(this, String.valueOf(relObj));
        Object phoneObj = snapshot.child("phoneNumber").getValue();
        if (phoneObj != null) UserPrefs.setPhoneNumber(this, String.valueOf(phoneObj));
        Object dobObj = snapshot.child("dateOfBirth").getValue();
        if (dobObj != null) UserPrefs.setDateOfBirth(this, String.valueOf(dobObj));
        Object ethObj = snapshot.child("ethnicity").getValue();
        if (ethObj != null) UserPrefs.setEthnicity(this, String.valueOf(ethObj));

        try {
            DataSnapshot contactsSnap = snapshot.child("emergencyContacts");
            if (contactsSnap != null && contactsSnap.exists()) {
                java.util.ArrayList<EmergencyContact> contacts = new java.util.ArrayList<>();
                for (DataSnapshot child : contactsSnap.getChildren()) {
                    if (child == null) continue;
                    String id = child.child("id").getValue() == null ? "" : String.valueOf(child.child("id").getValue());
                    if (id.trim().isEmpty()) id = child.getKey() == null ? "" : child.getKey();
                    String name = child.child("name").getValue() == null ? "" : String.valueOf(child.child("name").getValue());
                    String rel = child.child("relationship").getValue() == null ? "" : String.valueOf(child.child("relationship").getValue());
                    String phone = child.child("phone").getValue() == null ? "" : String.valueOf(child.child("phone").getValue());
                    if (id == null) id = "";
                    if (name == null) name = "";
                    if (rel == null) rel = "";
                    if (phone == null) phone = "";
                    if (id.trim().isEmpty()) continue;
                    contacts.add(new EmergencyContact(id.trim(), name, rel, phone));
                }
                UserPrefs.setEmergencyContacts(this, contacts);
            }
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk currentUidFromAuth. */
    private String currentUidFromAuth() {
        try {
            com.google.firebase.auth.FirebaseUser u = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            return u == null ? "" : String.valueOf(u.getUid() == null ? "" : u.getUid()).trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    /** Fungsi untuk retryPendingPhotoSaveToDb. */
    private void retryPendingPhotoSaveToDb(String uid, String localUri) {
        try {
            String u = String.valueOf(uid == null ? "" : uid).trim();
            String uriStr = String.valueOf(localUri == null ? "" : localUri).trim();
            if (u.isEmpty() || uriStr.isEmpty()) return;
            android.net.Uri uri = android.net.Uri.parse(uriStr);
            String b64 = encodeAvatarToBase64(uri, 256, 72);
            if (b64 == null || b64.trim().isEmpty()) return;
            UserPrefs.setPhotoB64(this, b64.trim());
            UserPrefs.setPendingPhotoUri(this, "");
            FirebaseRoomClient.updateUserPhotoB64Queued(u, b64.trim());
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk encodeAvatarToBase64. */
    private String encodeAvatarToBase64(android.net.Uri localUri, int maxDim, int jpegQuality) {
        try {
            android.graphics.BitmapFactory.Options bounds = new android.graphics.BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (java.io.InputStream in = getContentResolver().openInputStream(localUri)) {
                if (in == null) return "";
                android.graphics.BitmapFactory.decodeStream(in, null, bounds);
            }
            int w = Math.max(1, bounds.outWidth);
            int h = Math.max(1, bounds.outHeight);
            int sample = 1;
            int m = Math.max(w, h);
            while (m / sample > maxDim * 2) sample *= 2;

            android.graphics.BitmapFactory.Options opts = new android.graphics.BitmapFactory.Options();
            opts.inSampleSize = sample;
            opts.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
            android.graphics.Bitmap b;
            try (java.io.InputStream in2 = getContentResolver().openInputStream(localUri)) {
                if (in2 == null) return "";
                b = android.graphics.BitmapFactory.decodeStream(in2, null, opts);
            }
            if (b == null) return "";
            int bw = b.getWidth();
            int bh = b.getHeight();
            int mm = Math.max(bw, bh);
            if (mm > maxDim) {
                float s = maxDim / (float) mm;
                int nw = Math.max(1, Math.round(bw * s));
                int nh = Math.max(1, Math.round(bh * s));
                android.graphics.Bitmap scaled = android.graphics.Bitmap.createScaledBitmap(b, nw, nh, true);
                b.recycle();
                b = scaled;
            }
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            b.compress(android.graphics.Bitmap.CompressFormat.JPEG, Math.max(10, Math.min(jpegQuality, 95)), baos);
            b.recycle();
            byte[] bytes = baos.toByteArray();
            return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        } catch (Exception ignored) {
            return "";
        }
    }
}

