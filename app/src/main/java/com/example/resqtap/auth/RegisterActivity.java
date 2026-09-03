package com.example.resqtap.auth;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.friend.QrCodeUtils;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.AvatarUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import com.example.resqtap.contacts.EmergencyContact;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.UUID;
import java.security.SecureRandom;

/**
 * RegisterActivity
 * Skrin Register (4 step wizard): kumpul info peribadi, info perubatan kecemasan, dan auto-generate Tag ID unik (contoh: User#4091).
 */
public class RegisterActivity extends BaseActivity {
    private static final String TAG = "RegisterActivity";
    private volatile boolean emailAvailable = true;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable emailDebounce;
    private boolean completeProfileMode = false;
    private String completeUid = "";
    private boolean authSwipeMode = false;

    private View step1Credentials;
    private View step2PersonalInfo;
    private View step3MedicalEmergency;
    private View step4GetStarted;
    private TextView titleText;
    private TextView subtitleText;

    private String registeredEmail = "";
    private String registeredPassword = "";
    private String registeredUid = "";
    private String photoB64 = "";

    private ActivityResultLauncher<String> requestCameraPermission;
    private ActivityResultLauncher<Intent> takePhotoLauncher;
    private Uri tempCameraUri;
    private ShapeableImageView imgAvatar;

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
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            View step1 = findViewById(R.id.step1_credentials);
            if (step1 != null) {
                int basePaddingBottom = (int) (36 * getResources().getDisplayMetrics().density);
                step1.setPadding(
                        step1.getPaddingLeft(),
                        step1.getPaddingTop(),
                        step1.getPaddingRight(),
                        basePaddingBottom + systemBars.bottom
                );
            }
            View step4 = findViewById(R.id.step4_get_started);
            if (step4 != null) {
                int basePaddingBottom = (int) (24 * getResources().getDisplayMetrics().density);
                step4.setPadding(
                        step4.getPaddingLeft(),
                        step4.getPaddingTop(),
                        step4.getPaddingRight(),
                        basePaddingBottom + systemBars.bottom
                );
            }
            return insets;
        });

        titleText = findViewById(R.id.title);
        subtitleText = findViewById(R.id.subtitle);
        if (titleText != null) titleText.setText(R.string.register_step1_title);
        if (subtitleText != null) {
            subtitleText.setText(R.string.register_step1_subtitle);
            subtitleText.setVisibility(View.VISIBLE);
        }

        step1Credentials = findViewById(R.id.step1_credentials);
        step2PersonalInfo = findViewById(R.id.step2_personal_info);
        step3MedicalEmergency = findViewById(R.id.step3_medical_emergency);
        step4GetStarted = findViewById(R.id.step4_get_started);

        TextInputLayout emailLayout = findViewById(R.id.layout_email);
        TextInputEditText email = findViewById(R.id.input_email);
        TextInputEditText password = findViewById(R.id.input_password);
        TextInputEditText passwordConfirm = findViewById(R.id.input_password_confirm);
        MaterialButton btnCreate = findViewById(R.id.btn_create_account);
        View btnBack = findViewById(R.id.btn_back_login);

        View btnSocialApple = findViewById(R.id.btn_social_apple);
        View btnSocialGoogle = findViewById(R.id.btn_social_google);
        View tvHelp = findViewById(R.id.tv_help);
        if (btnSocialApple != null) {
            btnSocialApple.setOnClickListener(v -> Toast.makeText(this, "Apple Sign-Up is coming soon", Toast.LENGTH_SHORT).show());
        }
        if (btnSocialGoogle != null) {
            btnSocialGoogle.setOnClickListener(v -> Toast.makeText(this, "Google Sign-Up is coming soon", Toast.LENGTH_SHORT).show());
        }
        if (tvHelp != null) {
            tvHelp.setOnClickListener(v -> Toast.makeText(this, "Need assistance? Contact support@resqtap.com", Toast.LENGTH_LONG).show());
        }

        imgAvatar = findViewById(R.id.img_profile_avatar);
        MaterialButton btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        TextInputLayout nameLayout = findViewById(R.id.layout_name);
        TextInputLayout phoneLayout = findViewById(R.id.layout_phone);
        TextInputLayout addressLayout = findViewById(R.id.layout_address);
        TextInputEditText name = findViewById(R.id.input_name);
        TextInputEditText icInput = findViewById(R.id.input_ic);
        MaterialAutoCompleteTextView genderInput = findViewById(R.id.input_gender);
        TextInputEditText phoneInput = findViewById(R.id.input_phone);
        TextInputEditText addressInput = findViewById(R.id.input_address);
        MaterialAutoCompleteTextView religionInput = findViewById(R.id.input_religion);
        TextInputEditText dobInput = findViewById(R.id.input_dob);
        MaterialAutoCompleteTextView ethnicityInput = findViewById(R.id.input_ethnicity);
        MaterialButton btnNextToStep3 = findViewById(R.id.btn_next_to_step3);

        if (name != null) {
            name.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (nameLayout != null) { nameLayout.setError(null); nameLayout.setErrorEnabled(false); }
                }
            });
        }
        if (phoneInput != null) {
            phoneInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (phoneLayout != null) { phoneLayout.setError(null); phoneLayout.setErrorEnabled(false); }
                }
            });
        }
        if (addressInput != null) {
            addressInput.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.AllCaps()});
            addressInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    if (addressLayout != null) { addressLayout.setError(null); addressLayout.setErrorEnabled(false); }
                }
            });
        }

        MaterialAutoCompleteTextView bloodType = findViewById(R.id.input_blood_type);
        MaterialAutoCompleteTextView allergiesInput = findViewById(R.id.input_allergies);
        View layoutAllergiesOther = findViewById(R.id.layout_allergies_other);
        TextInputEditText allergiesOtherInput = findViewById(R.id.input_allergies_other);

        MaterialAutoCompleteTextView medicationsInput = findViewById(R.id.input_medications);
        View layoutMedicationsOther = findViewById(R.id.layout_medications_other);
        TextInputEditText medicationsOtherInput = findViewById(R.id.input_medications_other);

        MaterialAutoCompleteTextView organDonorInput = findViewById(R.id.input_organ_donor);
        TextInputEditText emergencyNameInput = findViewById(R.id.input_emergency_name);
        TextInputEditText emergencyPhoneInput = findViewById(R.id.input_emergency_phone);
        MaterialAutoCompleteTextView emergencyRelationInput = findViewById(R.id.input_emergency_relation);
        MaterialButton btnSaveDetails = findViewById(R.id.btn_save_details);

        MaterialButton btnGetStarted = findViewById(R.id.btn_get_started);

        MaterialButton btnBackStep2 = findViewById(R.id.btn_back_login_step2);
        MaterialButton btnBackStep3 = findViewById(R.id.btn_back_login_step3);

        forceHeavyText(titleText);
        if (btnBack != null) btnBack.setOnClickListener(v -> cancelRegistrationAndGoLogin());
        if (btnBackStep2 != null) btnBackStep2.setOnClickListener(v -> cancelRegistrationAndGoLogin());
        if (btnBackStep3 != null) btnBackStep3.setOnClickListener(v -> cancelRegistrationAndGoLogin());

        authSwipeMode = getIntent() != null && getIntent().getBooleanExtra("auth_swipe", false);
        completeProfileMode = getIntent() != null && getIntent().getBooleanExtra("complete_profile", false);
        completeUid = getIntent() == null ? "" : String.valueOf(getIntent().getStringExtra("uid"));
        String presetEmail = getIntent() == null ? "" : String.valueOf(getIntent().getStringExtra("email"));

        try {
            String[] genders = getResources().getStringArray(R.array.gender_options);
            genderInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, genders));
            genderInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        try {
            String[] religions = getResources().getStringArray(R.array.religion_options);
            religionInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, religions));
            religionInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        try {
            String[] ethnicities = getResources().getStringArray(R.array.ethnicity_options);
            ethnicityInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, ethnicities));
            ethnicityInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        String[] bloodTypes = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "-"};
        bloodType.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, bloodTypes));
        bloodType.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);

        try {
            String[] allergiesList = getResources().getStringArray(R.array.allergy_options);
            allergiesInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, allergiesList));
            allergiesInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        if (allergiesInput != null) {
            allergiesInput.setOnItemClickListener((parent, view, position, id) -> {
                String selected = allergiesInput.getText() == null ? "" : allergiesInput.getText().toString();
                if (isOtherSelected(selected)) {
                    if (layoutAllergiesOther != null) layoutAllergiesOther.setVisibility(View.VISIBLE);
                    if (allergiesOtherInput != null) allergiesOtherInput.requestFocus();
                } else {
                    if (layoutAllergiesOther != null) layoutAllergiesOther.setVisibility(View.GONE);
                    if (allergiesOtherInput != null) allergiesOtherInput.setText("");
                }
            });
            allergiesInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String selected = s == null ? "" : s.toString();
                    if (isOtherSelected(selected)) {
                        if (layoutAllergiesOther != null) layoutAllergiesOther.setVisibility(View.VISIBLE);
                    } else {
                        if (layoutAllergiesOther != null) layoutAllergiesOther.setVisibility(View.GONE);
                    }
                }
            });
        }

        try {
            String[] medicationsList = getResources().getStringArray(R.array.medication_options);
            medicationsInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, medicationsList));
            medicationsInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        if (medicationsInput != null) {
            medicationsInput.setOnItemClickListener((parent, view, position, id) -> {
                String selected = medicationsInput.getText() == null ? "" : medicationsInput.getText().toString();
                if (isOtherSelected(selected)) {
                    if (layoutMedicationsOther != null) layoutMedicationsOther.setVisibility(View.VISIBLE);
                    if (medicationsOtherInput != null) medicationsOtherInput.requestFocus();
                } else {
                    if (layoutMedicationsOther != null) layoutMedicationsOther.setVisibility(View.GONE);
                    if (medicationsOtherInput != null) medicationsOtherInput.setText("");
                }
            });
            medicationsInput.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    String selected = s == null ? "" : s.toString();
                    if (isOtherSelected(selected)) {
                        if (layoutMedicationsOther != null) layoutMedicationsOther.setVisibility(View.VISIBLE);
                    } else {
                        if (layoutMedicationsOther != null) layoutMedicationsOther.setVisibility(View.GONE);
                    }
                }
            });
        }

        try {
            String[] donors = getResources().getStringArray(R.array.organ_donor_options);
            organDonorInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, donors));
            organDonorInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        try {
            String[] relations = getResources().getStringArray(R.array.relationship_options);
            emergencyRelationInput.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown_popup, relations));
            emergencyRelationInput.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        } catch (Exception ignored) {
        }

        if (dobInput != null) {
            dobInput.setOnClickListener(v -> showDobPicker(dobInput));
            dobInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) showDobPicker(dobInput);
            });
        }

        requestCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchFrontCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to take a profile photo", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bitmap bitmap = null;

                        // 1. Cuba muat dari tempCameraUri (FileProvider)
                        if (tempCameraUri != null) {
                            try (java.io.InputStream is = getContentResolver().openInputStream(tempCameraUri)) {
                                if (is != null) {
                                    bitmap = BitmapFactory.decodeStream(is);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Failed to decode from tempCameraUri", e);
                            }
                        }

                        // 2. Fallback kepada intent data extras thumbnail
                        if (bitmap == null && result.getData() != null) {
                            if (result.getData().getExtras() != null) {
                                Object data = result.getData().getExtras().get("data");
                                if (data instanceof Bitmap) {
                                    bitmap = (Bitmap) data;
                                }
                            }
                            if (bitmap == null && result.getData().getData() != null) {
                                try (java.io.InputStream is = getContentResolver().openInputStream(result.getData().getData())) {
                                    if (is != null) {
                                        bitmap = BitmapFactory.decodeStream(is);
                                    }
                                } catch (Exception ignored) {}
                            }
                        }

                        if (bitmap != null) {
                            // Skalakan bitmap jika saiz terlalu besar untuk elakkan masalah memori
                            if (bitmap.getWidth() > 960 || bitmap.getHeight() > 960) {
                                float ratio = Math.min(960f / bitmap.getWidth(), 960f / bitmap.getHeight());
                                int w = Math.round(bitmap.getWidth() * ratio);
                                int h = Math.round(bitmap.getHeight() * ratio);
                                bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                            }

                            photoB64 = bitmapToBase64(bitmap);
                            if (imgAvatar != null) {
                                imgAvatar.setImageBitmap(bitmap);
                                imgAvatar.setImageTintList(null);
                            }
                            Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Photo capture returned empty, please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        if (btnUploadPhoto != null) {
            btnUploadPhoto.setOnClickListener(v -> launchFrontCamera());
        }
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> launchFrontCamera());
        }

        name.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                if (s == null) return;
                String up = s.toString().toUpperCase();
                if (!up.equals(s.toString())) {
                    name.removeTextChangedListener(this);
                    name.setText(up);
                    name.setSelection(up.length());
                    name.addTextChangedListener(this);
                }
            }
        });

        if (completeProfileMode) {
            registeredEmail = presetEmail == null ? "" : presetEmail;
            registeredUid = FirebaseAuth.getInstance().getCurrentUser() == null ? completeUid : FirebaseAuth.getInstance().getCurrentUser().getUid();
            showStep2PersonalInfo();
        }

        emailLayout.setEndIconOnClickListener(v -> {});
        emailLayout.setEndIconCheckable(false);

        email.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                String value = s == null ? "" : s.toString().trim();
                emailAvailable = true;
                btnCreate.setEnabled(true);

                if (emailDebounce != null) handler.removeCallbacks(emailDebounce);
                if (value.isEmpty()) {
                    emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                    emailLayout.setEndIconDrawable(null);
                    emailLayout.setEndIconTintList(null);
                    return;
                }

                if (!isGmail(value)) {
                    emailLayout.setErrorEnabled(true);
                    emailLayout.setError(getString(R.string.gmail_only_warning));
                    emailLayout.setEndIconDrawable(R.drawable.ic_error_24);
                    emailLayout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(RegisterActivity.this, R.color.brand_red)));
                    btnCreate.setEnabled(false);
                    return;
                }

                if (emailLayout.getError() != null) {
                    emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                }
                emailLayout.setEndIconDrawable(null);
                emailLayout.setEndIconTintList(null);
                emailDebounce = () -> checkEmailAvailability(value, emailLayout, btnCreate);
                handler.postDelayed(emailDebounce, 80);
            }
        });

        TextInputLayout passwordLayout = findViewById(R.id.layout_password);
        TextInputLayout passwordConfirmLayout = findViewById(R.id.layout_password_confirm);

        password.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (passwordLayout != null) { passwordLayout.setError(null); passwordLayout.setErrorEnabled(false); }
            }
        });

        passwordConfirm.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (passwordConfirmLayout != null) { passwordConfirmLayout.setError(null); passwordConfirmLayout.setErrorEnabled(false); }
            }
        });

        btnCreate.setOnClickListener(v -> {
            String emailValue = email.getText() == null ? "" : email.getText().toString().trim();
            String passValue = password.getText() == null ? "" : password.getText().toString();
            String passConfirmValue = passwordConfirm.getText() == null ? "" : passwordConfirm.getText().toString();

            if (emailValue.isEmpty()) {
                emailLayout.setError("Please enter your email address");
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                scrollToField(emailLayout);
                return;
            }

            if (!isGmail(emailValue)) {
                emailLayout.setError(getString(R.string.gmail_only_warning));
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                scrollToField(emailLayout);
                return;
            }

            if (passValue.isEmpty()) {
                if (passwordLayout != null) {
                    passwordLayout.setError("Please enter a password");
                    scrollToField(passwordLayout);
                }
                return;
            }

            String sopError = getPasswordSopError(passValue);
            if (sopError != null) {
                if (passwordLayout != null) {
                    passwordLayout.setError(sopError);
                    scrollToField(passwordLayout);
                }
                return;
            }

            if (passConfirmValue.isEmpty()) {
                if (passwordConfirmLayout != null) {
                    passwordConfirmLayout.setError("Please confirm your password");
                    scrollToField(passwordConfirmLayout);
                }
                return;
            }

            if (!passValue.equals(passConfirmValue)) {
                if (passwordConfirmLayout != null) {
                    passwordConfirmLayout.setError("Passwords do not match");
                    scrollToField(passwordConfirmLayout);
                }
                return;
            }

            com.google.android.material.checkbox.MaterialCheckBox cbTerms = findViewById(R.id.cb_terms);
            if (cbTerms != null && !cbTerms.isChecked()) {
                Toast.makeText(this, "Please agree to Safety Terms to continue", Toast.LENGTH_SHORT).show();
                scrollToField(cbTerms);
                return;
            }

            btnCreate.setEnabled(false);

            // Cipta akaun Firebase Auth terus di Langkah 1 untuk mengesahkan e-mel belum berdaftar
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(emailValue, passValue)
                    .addOnSuccessListener(authResult -> {
                        btnCreate.setEnabled(true);
                        if (authResult.getUser() != null) {
                            registeredUid = authResult.getUser().getUid();
                        }
                        registeredEmail = emailValue;
                        registeredPassword = passValue;
                        showStep2PersonalInfo();
                    })
                    .addOnFailureListener(e -> {
                        btnCreate.setEnabled(true);
                        Log.e(TAG, "Step 1 registration check failed", e);
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            emailLayout.setError(getString(R.string.toast_email_already_exists));
                            emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                            emailLayout.setEndIconTintList(null);
                            showAccountExistsDialog(emailValue);
                            scrollToField(emailLayout);
                            return;
                        }
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            if (passwordLayout != null) {
                                passwordLayout.setError(getString(R.string.toast_register_weak_password));
                                scrollToField(passwordLayout);
                            }
                            return;
                        }
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            emailLayout.setError(getString(R.string.toast_register_invalid_email));
                            emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                            emailLayout.setEndIconTintList(null);
                            scrollToField(emailLayout);
                            return;
                        }
                        Toast.makeText(this, e.getMessage() != null ? e.getMessage() : getString(R.string.toast_register_failed), Toast.LENGTH_LONG).show();
                    });
        });

        btnNextToStep3.setOnClickListener(v -> {
            String nameValue = name.getText() == null ? "" : name.getText().toString().trim();
            String phoneValue = phoneInput.getText() == null ? "" : phoneInput.getText().toString().trim();
            String addressValue = addressInput.getText() == null ? "" : addressInput.getText().toString().trim();

            if (nameValue.isEmpty()) {
                if (nameLayout != null) {
                    nameLayout.setError("Please enter your full name");
                    scrollToField(nameLayout);
                }
                return;
            }

            if (phoneValue.isEmpty()) {
                if (phoneLayout != null) {
                    phoneLayout.setError("Please enter your phone number");
                    scrollToField(phoneLayout);
                }
                return;
            }

            if (addressValue.isEmpty()) {
                if (addressLayout != null) {
                    addressLayout.setError("Please enter your home address");
                    scrollToField(addressLayout);
                }
                return;
            }

            showStep3MedicalEmergency();
        });

        btnSaveDetails.setOnClickListener(v -> {
            String nameValue = name.getText() == null ? "" : name.getText().toString().trim();
            String icValue = icInput.getText() == null ? "" : icInput.getText().toString().trim();
            String genderValue = genderInput.getText() == null ? "" : genderInput.getText().toString().trim();
            String phoneValue = phoneInput.getText() == null ? "" : phoneInput.getText().toString().trim();
            String addressValue = addressInput.getText() == null ? "" : addressInput.getText().toString().trim().toUpperCase(java.util.Locale.ROOT);
            String religionValue = religionInput.getText() == null ? "" : religionInput.getText().toString().trim();
            String dobValue = dobInput.getText() == null ? "" : dobInput.getText().toString().trim();
            String ethnicityValue = ethnicityInput.getText() == null ? "" : ethnicityInput.getText().toString().trim();

            String bloodValue = bloodType.getText() == null ? "" : bloodType.getText().toString().trim();

            String allergiesSelection = allergiesInput.getText() == null ? "" : allergiesInput.getText().toString().trim();
            String allergiesOther = allergiesOtherInput == null || allergiesOtherInput.getText() == null ? "" : allergiesOtherInput.getText().toString().trim();
            String allergiesValue = isOtherSelected(allergiesSelection) ? (!allergiesOther.isEmpty() ? allergiesOther : allergiesSelection) : allergiesSelection;

            String medicationsSelection = medicationsInput.getText() == null ? "" : medicationsInput.getText().toString().trim();
            String medicationsOther = medicationsOtherInput == null || medicationsOtherInput.getText() == null ? "" : medicationsOtherInput.getText().toString().trim();
            String medicationsValue = isOtherSelected(medicationsSelection) ? (!medicationsOther.isEmpty() ? medicationsOther : medicationsSelection) : medicationsSelection;

            String organDonorValue = organDonorInput.getText() == null ? "" : organDonorInput.getText().toString().trim();

            String emNameValue = emergencyNameInput.getText() == null ? "" : emergencyNameInput.getText().toString().trim();
            String emPhoneValue = emergencyPhoneInput.getText() == null ? "" : emergencyPhoneInput.getText().toString().trim();
            String emRelationValue = emergencyRelationInput.getText() == null ? "" : emergencyRelationInput.getText().toString().trim();

            btnSaveDetails.setEnabled(false);

            // Jikalau pengguna sudah ada sesi login (cth: complete profile mode)
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                registeredUid = currentUser.getUid();
                saveProfileToDatabase(registeredUid, registeredEmail, nameValue, icValue, genderValue, phoneValue, addressValue, religionValue, dobValue, ethnicityValue, bloodValue, allergiesValue, medicationsValue, organDonorValue, emNameValue, emPhoneValue, emRelationValue, btnSaveDetails);
                return;
            }

            // Cipta akaun Firebase Auth dan simpan ke database sekaligus
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(registeredEmail, registeredPassword)
                    .addOnSuccessListener(result -> {
                        if (result.getUser() == null) {
                            btnSaveDetails.setEnabled(true);
                            Toast.makeText(this, R.string.toast_register_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        registeredUid = result.getUser().getUid();
                        saveProfileToDatabase(registeredUid, registeredEmail, nameValue, icValue, genderValue, phoneValue, addressValue, religionValue, dobValue, ethnicityValue, bloodValue, allergiesValue, medicationsValue, organDonorValue, emNameValue, emPhoneValue, emRelationValue, btnSaveDetails);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "createUserWithEmailAndPassword failed at final save", e);
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            // Jika e-mel sudah ada dalam Auth, cuba log masuk dengan kata laluan yang dimasukkan
                            FirebaseAuth.getInstance()
                                    .signInWithEmailAndPassword(registeredEmail, registeredPassword)
                                    .addOnSuccessListener(authRes -> {
                                        if (authRes.getUser() != null) {
                                            registeredUid = authRes.getUser().getUid();
                                            saveProfileToDatabase(registeredUid, registeredEmail, nameValue, icValue, genderValue, phoneValue, addressValue, religionValue, dobValue, ethnicityValue, bloodValue, allergiesValue, medicationsValue, organDonorValue, emNameValue, emPhoneValue, emRelationValue, btnSaveDetails);
                                        } else {
                                            btnSaveDetails.setEnabled(true);
                                            showAccountExistsDialog(registeredEmail);
                                        }
                                    })
                                    .addOnFailureListener(authErr -> {
                                        btnSaveDetails.setEnabled(true);
                                        showAccountExistsDialog(registeredEmail);
                                    });
                            return;
                        }
                        btnSaveDetails.setEnabled(true);
                        if (e instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(this, R.string.toast_register_weak_password, Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(this, R.string.toast_register_invalid_email, Toast.LENGTH_LONG).show();
                            return;
                        }
                        String detail = e.getMessage() == null ? "" : e.getMessage().trim();
                        if (!detail.isEmpty()) {
                            Toast.makeText(this, getString(R.string.toast_register_failed) + " " + detail, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, R.string.toast_register_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        btnGetStarted.setOnClickListener(v -> showTermsOfServiceDialog());

        btnBack.setOnClickListener(v -> cancelRegistrationAndGoLogin());
    }

    /**
     * Paparkan dialog Terms of Service selepas pendaftaran selesai.
     * Pengguna wajib skrol kandungan hingga ke bawah sebelum butang ACCEPT diaktifkan.
     */
    private void showTermsOfServiceDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_terms_of_service, null);
        androidx.core.widget.NestedScrollView scrollTos = dialogView.findViewById(R.id.scroll_tos);
        com.google.android.material.button.MaterialButton btnAccept = dialogView.findViewById(R.id.btn_accept_tos);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.Theme_ResQTap_CustomDialog)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            int dialogWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.88);
            dialog.getWindow().setLayout(dialogWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }

        if (scrollTos != null && btnAccept != null) {
            scrollTos.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                android.view.View child = v.getChildAt(0);
                if (child != null) {
                    int diff = (child.getBottom() - (v.getHeight() + scrollY));
                    if (diff <= 35) {
                        if (!btnAccept.isEnabled()) {
                            btnAccept.setEnabled(true);
                            btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK));
                            btnAccept.setTextColor(android.graphics.Color.WHITE);
                        }
                    }
                }
            });

            // Semak sekiranya teks muat sepenuhnya pada skrin tanpa perlu skrol
            scrollTos.post(() -> {
                android.view.View child = scrollTos.getChildAt(0);
                if (child != null && child.getHeight() <= scrollTos.getHeight()) {
                    btnAccept.setEnabled(true);
                    btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.BLACK));
                    btnAccept.setTextColor(android.graphics.Color.WHITE);
                }
            });

            btnAccept.setOnClickListener(v -> {
                UserPrefs.setTosAccepted(RegisterActivity.this, true);
                dialog.dismiss();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
    }

    /** Simpan profil penuh pengguna ke Firebase Realtime Database dan selesaikan pendaftaran. */
    private void saveProfileToDatabase(String uid, String emailVal, String nameValue, String icValue, String genderValue,
                                       String phoneValue, String addressValue, String religionValue, String dobValue,
                                       String ethnicityValue, String bloodValue, String allergiesValue,
                                       String medicationsValue, String organDonorValue, String emNameValue,
                                       String emPhoneValue, String emRelationValue, MaterialButton btnSaveDetails) {
        Map<String, Object> user = new HashMap<>();
        user.put("name", nameValue);
        user.put("email", String.valueOf(emailVal == null ? "" : emailVal).trim().toLowerCase(java.util.Locale.ROOT));
        user.put("icNumber", icValue);
        user.put("gender", genderValue);
        user.put("phone", phoneValue);
        user.put("address", addressValue);
        user.put("religion", religionValue);
        user.put("dob", dobValue);
        user.put("ethnicity", ethnicityValue);

        user.put("bloodType", bloodValue);
        user.put("allergies", allergiesValue);
        user.put("existingConditions", medicationsValue);
        user.put("medications", medicationsValue);
        user.put("organDonor", organDonorValue);

        if (!photoB64.isEmpty()) {
            user.put("photoB64", photoB64);
        }
        user.put("createdAt", System.currentTimeMillis());

        ArrayList<EmergencyContact> contactList = new ArrayList<>();
        if (!emNameValue.isEmpty() || !emPhoneValue.isEmpty()) {
            EmergencyContact contact = new EmergencyContact(
                    UUID.randomUUID().toString(),
                    emNameValue,
                    emRelationValue,
                    emPhoneValue
            );
            contactList.add(contact);
            user.put("emergencyContacts", contactList);
        }

        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("users")
                .child(uid)
                .setValue(user)
                .addOnSuccessListener(vv -> {
                    // Simpan UserPrefs segera (tanpa tunggu DB ops lain)
                    UserPrefs.setUid(this, uid);
                    UserPrefs.setEmail(this, emailVal);
                    UserPrefs.setName(this, nameValue);
                    UserPrefs.setIcNumber(this, icValue);
                    UserPrefs.setGender(this, genderValue);
                    UserPrefs.setPhoneNumber(this, phoneValue);
                    UserPrefs.setAddress(this, addressValue);
                    UserPrefs.setReligion(this, religionValue);
                    UserPrefs.setDateOfBirth(this, dobValue);
                    UserPrefs.setEthnicity(this, ethnicityValue);
                    UserPrefs.setBloodType(this, bloodValue);
                    UserPrefs.setAllergies(this, allergiesValue);
                    UserPrefs.setExistingConditions(this, medicationsValue);
                    UserPrefs.setMedications(this, medicationsValue);
                    UserPrefs.setOrganDonor(this, organDonorValue);
                    if (!contactList.isEmpty()) {
                        UserPrefs.setEmergencyContacts(this, contactList);
                    }
                    if (!photoB64.isEmpty()) {
                        UserPrefs.setPhotoB64(this, photoB64);
                    }

                    // Terus tunjuk step 4 — jangan tunggu DB ops
                    showStep4GetStarted();

                    // Fire-and-forget: registeredEmails, admin, publicId di background
                    String sanitizedEmail = sanitizeEmailForDb(emailVal);
                    if (!sanitizedEmail.isEmpty()) {
                        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                                .getReference("registeredEmails")
                                .child(sanitizedEmail)
                                .setValue(true);
                    }

                    if (emailVal != null && emailVal.trim().toLowerCase(java.util.Locale.ROOT).endsWith("@resqtap.com")) {
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("active", true);
                        adminData.put("email", emailVal.trim().toLowerCase(java.util.Locale.ROOT));
                        adminData.put("name", nameValue != null ? nameValue : "");
                        adminData.put("role", "admin");
                        adminData.put("assignedAt", System.currentTimeMillis());
                        adminData.put("assignedBy", "system_auto_domain");

                        FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                                .getReference("admins")
                                .child(uid)
                                .setValue(adminData);
                    }

                    assignPublicId(uid, publicId -> {
                        UserPrefs.setPublicId(RegisterActivity.this, publicId);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "save profile failed. uid=" + uid, e);
                    if (btnSaveDetails != null) btnSaveDetails.setEnabled(true);
                    Toast.makeText(this, R.string.toast_register_failed_db, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Transitions UI to Step 2 (Personal Information).
     * Validates Step 1 credentials, creates the initial Firebase Authentication user,
     * and displays profile inputs (Name, IC, Phone, DOB, Gender, Avatar upload).
     */
    private void showStep2PersonalInfo() {
        if (titleText != null) titleText.setText(R.string.register_step2_title);
        if (subtitleText != null) {
            subtitleText.setText(R.string.register_step2_subtitle);
            subtitleText.setVisibility(View.VISIBLE);
        }

        if (step1Credentials != null) step1Credentials.setVisibility(View.GONE);
        if (step3MedicalEmergency != null) step3MedicalEmergency.setVisibility(View.GONE);
        if (step4GetStarted != null) step4GetStarted.setVisibility(View.GONE);

        if (step2PersonalInfo != null) {
            step2PersonalInfo.setVisibility(View.VISIBLE);
            step2PersonalInfo.setAlpha(0f);
            step2PersonalInfo.animate().alpha(1f).setDuration(220).start();
        }
    }

    /**
     * Transitions UI to Step 3 (Medical & Emergency Information).
     * Validates Step 2 fields and collects critical first-responder data (Blood type, Allergies, Emergency contact).
     */
    private void showStep3MedicalEmergency() {
        if (titleText != null) titleText.setText(R.string.register_step3_title);
        if (subtitleText != null) {
            subtitleText.setText(R.string.register_step3_subtitle);
            subtitleText.setVisibility(View.VISIBLE);
        }

        if (step1Credentials != null) step1Credentials.setVisibility(View.GONE);
        if (step2PersonalInfo != null) step2PersonalInfo.setVisibility(View.GONE);
        if (step4GetStarted != null) step4GetStarted.setVisibility(View.GONE);

        if (step3MedicalEmergency != null) {
            step3MedicalEmergency.setVisibility(View.VISIBLE);
            step3MedicalEmergency.setAlpha(0f);
            step3MedicalEmergency.animate().alpha(1f).setDuration(220).start();
        }
    }

    /**
     * Finalizes registration (Step 4).
     * Generates a unique user Public Tag via atomic transaction, persists full profile to /users/{uid},
     * stores user session flags, and displays the onboarding completion screen.
     */
    private void showStep4GetStarted() {
        if (titleText != null) titleText.setText(R.string.register_step4_title);
        if (subtitleText != null) {
            subtitleText.setText(R.string.register_step4_subtitle);
            subtitleText.setVisibility(View.VISIBLE);
        }

        if (step1Credentials != null) step1Credentials.setVisibility(View.GONE);
        if (step2PersonalInfo != null) step2PersonalInfo.setVisibility(View.GONE);
        if (step3MedicalEmergency != null) step3MedicalEmergency.setVisibility(View.GONE);

        if (step4GetStarted != null) {
            step4GetStarted.setVisibility(View.VISIBLE);
            step4GetStarted.setAlpha(0f);
            step4GetStarted.setTranslationY(320f);

            // 1. Animasi kad meluncur naik dari bawah (slide up from bottom)
            step4GetStarted.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(450)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator(1.8f))
                    .start();

            // 2. Animasi bulatan hijau pudar mengembang
            View circleBg = findViewById(R.id.view_circle_bg);
            if (circleBg != null) {
                circleBg.setScaleX(0.2f);
                circleBg.setScaleY(0.2f);
                circleBg.setAlpha(0f);
                circleBg.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setStartDelay(100)
                        .setDuration(380)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(1.3f))
                        .start();
            }

            // 3. Animasi tanda semak hijau muncul dengan lantunan spring (animated tick pop-in)
            View successTick = findViewById(R.id.img_success_tick);
            if (successTick != null) {
                successTick.setScaleX(0f);
                successTick.setScaleY(0f);
                successTick.setAlpha(0f);
                successTick.setRotation(-40f);
                successTick.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .rotation(0f)
                        .alpha(1f)
                        .setStartDelay(220)
                        .setDuration(480)
                        .setInterpolator(new android.view.animation.OvershootInterpolator(2.2f))
                        .start();
            }
        }
    }

    /** Paparkan DobPicker. */
    private void showDobPicker(TextInputEditText dobInput) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR) - 20;
        int month = cal.get(java.util.Calendar.MONTH);
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

        String current = dobInput.getText() == null ? "" : dobInput.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                String[] parts = current.split("/");
                if (parts.length == 3) {
                    day = Integer.parseInt(parts[0]);
                    month = Integer.parseInt(parts[1]) - 1;
                    year = Integer.parseInt(parts[2]);
                }
            } catch (Exception ignored) {}
        }

        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this, R.style.DatePickerTheme, (view, y, m, d) -> {
            String formatted = String.format(java.util.Locale.US, "%02d/%02d/%04d", d, m + 1, y);
            dobInput.setText(formatted);
        }, year, month, day);

        dialog.setOnShowListener(d -> {
            try {
                android.widget.Button pos = dialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE);
                android.widget.Button neg = dialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE);
                if (pos != null) {
                    pos.setTextColor(android.graphics.Color.parseColor("#D81B60"));
                    pos.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);
                    pos.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
                }
                if (neg != null) {
                    neg.setTextColor(android.graphics.Color.parseColor("#475569"));
                    neg.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);
                    neg.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
                }
                android.view.Window window = dialog.getWindow();
                if (window != null) {
                    window.setGravity(android.view.Gravity.CENTER);
                    window.setLayout(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            } catch (Exception ignored) {}
        });

        dialog.show();
    }

    private void scrollToField(View targetView) {
        if (targetView == null) return;
        targetView.requestFocus();
        androidx.core.widget.NestedScrollView authPanel = findViewById(R.id.auth_panel);
        if (authPanel != null) {
            authPanel.post(() -> {
                int[] targetLoc = new int[2];
                targetView.getLocationOnScreen(targetLoc);
                int[] scrollLoc = new int[2];
                authPanel.getLocationOnScreen(scrollLoc);
                int scrollY = targetLoc[1] - scrollLoc[1] + authPanel.getScrollY();
                authPanel.smoothScrollTo(0, Math.max(0, scrollY - 80));
            });
        }
    }

    public static String sanitizeEmailForDb(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(java.util.Locale.ROOT)
                .replace(".", "_")
                .replace("@", "_at_");
    }

    /** Semak dan sahkan EmailAvailability secara langsung melalui Firebase RTDB registeredEmails dan Auth. */
    private void checkEmailAvailability(String email, TextInputLayout emailLayout, MaterialButton btnCreate) {
        String query = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (query.isEmpty() || !isGmail(query)) return;

        String sanitized = sanitizeEmailForDb(query);
        DatabaseReference ref = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("registeredEmails")
                .child(sanitized);

        ref.get().addOnSuccessListener(snapshot -> {
            boolean dbExists = snapshot != null && snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
            if (dbExists) {
                emailLayout.setError(getString(R.string.toast_email_already_exists));
                emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                emailLayout.setEndIconTintList(null);
                btnCreate.setEnabled(false);
            } else {
                emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                emailLayout.setEndIconDrawable(R.drawable.ic_check_24);
                emailLayout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green)));
                btnCreate.setEnabled(true);
            }
        }).addOnFailureListener(e -> {
            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(query).addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    java.util.List<String> methods = task.getResult().getSignInMethods();
                    if (methods != null && !methods.isEmpty()) {
                        emailLayout.setError(getString(R.string.toast_email_already_exists));
                        emailLayout.setEndIconDrawable(R.drawable.ic_error_circle_24);
                        emailLayout.setEndIconTintList(null);
                        btnCreate.setEnabled(false);
                        return;
                    }
                }
                emailLayout.setError(null); emailLayout.setErrorEnabled(false);
                emailLayout.setEndIconDrawable(R.drawable.ic_check_24);
                emailLayout.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_green)));
                btnCreate.setEnabled(true);
            });
        });
    }

    private interface PublicIdCallback {
        void onAssigned(String publicId);
    }

    /**
     * Generates and assigns a unique public identifier tag (e.g. Username#1234).
     * Uses a SecureRandom 4-digit code and checks availability to prevent duplicate identity tags.
     *
     * @param uid Target Firebase user UID
     * @param callback Callback invoked once tag is assigned
     */
    private void assignPublicId(String uid, PublicIdCallback callback) {
        DatabaseReference counterRef = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("counters")
                .child("user_public_id");

        counterRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                long current = 0L;
                if (currentData.getValue() != null) {
                    try {
                        current = ((Number) currentData.getValue()).longValue();
                    } catch (Exception ignored) {
                        current = 0L;
                    }
                }
                currentData.setValue(current + 1);
                return Transaction.success(currentData);
            }

            /** Fungsi untuk onComplete. */
    @Override
            public void onComplete(com.google.firebase.database.DatabaseError error, boolean committed, com.google.firebase.database.DataSnapshot currentData) {
                String publicId;
                if (committed && currentData != null && currentData.getValue() != null) {
                    long seq = ((Number) currentData.getValue()).longValue();
                    publicId = String.format(java.util.Locale.US, "%04d", (seq % 9000) + 1000);
                } else {
                    SecureRandom random = new SecureRandom();
                    publicId = String.format(java.util.Locale.US, "%04d", random.nextInt(9000) + 1000);
                }

                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("publicId", publicId);
                updateMap.put("publicIdSeq", System.currentTimeMillis());

                FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                        .getReference("users")
                        .child(uid)
                        .updateChildren(updateMap)
                        .addOnCompleteListener(task -> {
                            if (callback != null) callback.onAssigned(publicId);
                        });
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

    /** Fungsi untuk uriToBase64. */
    private String uriToBase64(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return "";
            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    /** Fungsi untuk forceHeavyText. */
    private void forceHeavyText(TextView textView) {
        if (textView == null) return;
        textView.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        textView.getPaint().setFakeBoldText(true);
        textView.invalidate();
    }

    /** Fungsi untuk cancelRegistrationAndGoLogin. */
    private void cancelRegistrationAndGoLogin() {
        String uid = registeredUid.isEmpty()
                ? (FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : completeUid)
                : registeredUid;
        if (uid == null || uid.trim().isEmpty()) {
            uid = UserPrefs.getUid(this);
        }

        final String targetUid = String.valueOf(uid == null ? "" : uid).trim();

        if (!targetUid.isEmpty()) {
            try {
                FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL)
                        .getReference("users")
                        .child(targetUid)
                        .removeValue();
            } catch (Exception e) {
                Log.e(TAG, "failed to remove user node on cancel", e);
            }
        }

        try {
            com.google.firebase.auth.FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) {
                current.delete().addOnCompleteListener(task -> {
                    FirebaseAuth.getInstance().signOut();
                    UserPrefs.clearAccountData(this);
                    finishToLogin();
                });
                return;
            }
        } catch (Exception ignored) {}
        FirebaseAuth.getInstance().signOut();
        UserPrefs.clearAccountData(this);
        finishToLogin();
    }

    /** Paparkan dialog jika akaun sudah berdaftar dan beri pilihan Login atau Reset Password. */
    private void showAccountExistsDialog(String emailValue) {
        if (isFinishing() || isDestroyed()) return;
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.account_exists_dialog_title)
                .setMessage(getString(R.string.account_exists_dialog_msg, emailValue))
                .setPositiveButton(R.string.go_to_login, (d, w) -> finishToLogin())
                .setNegativeButton(R.string.reset_password, (d, w) -> {
                    Intent intent = new Intent(this, ForgotPasswordActivity.class);
                    intent.putExtra("email", emailValue);
                    startActivity(intent);
                })
                .setNeutralButton(R.string.cancel, (d, w) -> d.dismiss())
                .show();
    }

    /** Fungsi untuk finishToLogin. */
    private void finishToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /** Buka kamera hadapan sahaja secara langsung untuk swafoto profil. */
    private void launchFrontCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
            return;
        }

        try {
            java.io.File cacheDir = new java.io.File(getCacheDir(), "camera");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            java.io.File photoFile = new java.io.File(cacheDir, "selfie_" + System.currentTimeMillis() + ".jpg");
            tempCameraUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempCameraUri);
            // Meminta kamera depan secara eksplisit merentasi pelbagai pengeluar peranti Android
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1); // 1 = Front Camera
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            intent.putExtra("camerafacing", "front");
            intent.putExtra("previous_mode", "front");
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoLauncher.launch(intent);
        } catch (Exception e) {
            Log.w(TAG, "FileProvider camera launch failed, fallback to basic intent", e);
            Intent fallbackIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fallbackIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            fallbackIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            fallbackIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
            fallbackIntent.putExtra("camerafacing", "front");
            fallbackIntent.putExtra("previous_mode", "front");
            try {
                takePhotoLauncher.launch(fallbackIntent);
            } catch (Exception ex) {
                Toast.makeText(this, "Could not launch camera: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Tukar Bitmap foto kamera kepada format Base64. */
    private String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return "";
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "failed to convert bitmap to base64", e);
            return "";
        }
    }

    /** Semak ketetapan SOP Kata Laluan (Min 8 aksara, huruf besar, huruf kecil, nombor, & simbol khas). */
    public static String getPasswordSopError(String password) {
        if (password == null || password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter (A-Z).";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter (a-z).";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one digit (0-9).";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
            return "Password must contain at least one special symbol (!@#$%^&*...).";
        }
        return null;
    }

    private boolean isOtherSelected(String text) {
        if (text == null) return false;
        String t = text.trim().toLowerCase(java.util.Locale.ROOT);
        return t.equals("others") || t.equals("other") || t.equals("lain-lain") || t.equals("其他") || t.equals("மற்றவை");
    }
}

