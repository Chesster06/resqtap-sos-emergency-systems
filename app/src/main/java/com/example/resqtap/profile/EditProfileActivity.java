package com.example.resqtap.profile;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.AccountDeletionUtils;
import com.example.resqtap.utils.AvatarUtils;
import com.example.resqtap.utils.DeviceIdUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EditProfileActivity
 * Tampilan moden Edit Profile mengikut reka bentuk kemas:
 * Avatar tengah, Card 1 (Name, Droply benefits), Card 2 (Private Info: Email, Birthdate, Gender, Weight, Height),
 * dan Card 3 (Medical & Contact).
 */
public class EditProfileActivity extends BaseActivity {
    private static final String TAG = "EditProfileActivity";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<String[]> pickImage;

    private ShapeableImageView profilePhoto;
    private TextView tvValueName;
    private TextView tvValueEmail;
    private TextView tvValueDob;
    private TextView tvValueGender;
    private TextView tvValueWeight;
    private TextView tvValueHeight;
    private TextView tvValuePhone;
    private TextView tvValueBloodType;
    private TextView tvValueAllergies;
    private TextView tvValueConditions;
    private TextView tvValueAddress;
    private TextView tvUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        loadProfileData();
        setupListeners();
        setupImagePicker();
    }

    private void initViews() {
        profilePhoto = findViewById(R.id.profile_photo);
        tvValueName = findViewById(R.id.tv_value_name);
        tvValueEmail = findViewById(R.id.tv_value_email);
        tvValueDob = findViewById(R.id.tv_value_dob);
        tvValueGender = findViewById(R.id.tv_value_gender);
        tvValueWeight = findViewById(R.id.tv_value_weight);
        tvValueHeight = findViewById(R.id.tv_value_height);
        tvValuePhone = findViewById(R.id.tv_value_phone);
        tvValueBloodType = findViewById(R.id.tv_value_blood_type);
        tvValueAllergies = findViewById(R.id.tv_value_allergies);
        tvValueConditions = findViewById(R.id.tv_value_conditions);
        tvValueAddress = findViewById(R.id.tv_value_address);
        tvUid = findViewById(R.id.profile_uid);
    }

    private void loadProfileData() {
        String uid = DeviceIdUtils.getStableUid(this);
        String publicId = UserPrefs.getPublicId(this);
        String name = UserPrefs.getName(this);
        String email = UserPrefs.getEmail(this);
        String photoUri = UserPrefs.getPhotoUri(this);
        String photoB64 = UserPrefs.getPhotoB64(this);
        String dob = UserPrefs.getDateOfBirth(this);
        String gender = UserPrefs.getGender(this);
        String weight = UserPrefs.getWeight(this);
        String height = UserPrefs.getHeight(this);
        String phone = UserPrefs.getPhoneNumber(this);
        String bloodType = UserPrefs.getBloodType(this);
        String allergies = UserPrefs.getAllergies(this);
        String conditions = UserPrefs.getExistingConditions(this);
        String address = UserPrefs.getAddress(this);

        // Header Avatar
        AvatarUtils.applyAvatar(profilePhoto, photoB64, photoUri, R.drawable.ic_avatar);

        // UID
        String shownUid = (publicId == null || publicId.trim().isEmpty()) ? uid : publicId.trim().toUpperCase();
        tvUid.setText("UID: " + shownUid);

        // Card 1
        tvValueName.setText(isNotEmpty(name) ? name : getString(R.string.profile_name_default));

        // Card 2: Private Info
        tvValueEmail.setText(isNotEmpty(email) ? email : "Not set");
        tvValueDob.setText(isNotEmpty(dob) ? dob : "Not set");
        tvValueGender.setText(isNotEmpty(gender) ? gender : "Not set");
        tvValueWeight.setText(isNotEmpty(weight) ? weight : "Not set");
        tvValueHeight.setText(isNotEmpty(height) ? height : "Not set");

        // Card 3: Medical & Contact
        tvValuePhone.setText(isNotEmpty(phone) ? phone : "Not set");
        tvValueBloodType.setText(isNotEmpty(bloodType) ? bloodType : "Not set");
        tvValueAllergies.setText(isNotEmpty(allergies) ? allergies : "None");
        String displayedConditions = isNotEmpty(conditions) ? conditions : (isNotEmpty(UserPrefs.getMedications(this)) ? UserPrefs.getMedications(this) : "None");
        tvValueConditions.setText(displayedConditions);
        tvValueAddress.setText(isNotEmpty(address) ? address.toUpperCase(Locale.ROOT) : "Not set");
    }

    private void setupListeners() {
        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Avatar Pick & Preview
        View.OnClickListener photoClickListener = v -> showPhotoOptionsDialog();
        if (profilePhoto != null) profilePhoto.setOnClickListener(photoClickListener);
        View btnPick = findViewById(R.id.btn_pick_photo);
        if (btnPick != null) btnPick.setOnClickListener(v -> launchImagePicker());

        // Card 1
        findViewById(R.id.row_name).setOnClickListener(v -> showEditNameDialog());

        // Card 2: Private Info (Email, DOB, and Gender are uneditable)
        findViewById(R.id.row_weight).setOnClickListener(v -> showEditWeightDialog());
        findViewById(R.id.row_height).setOnClickListener(v -> showEditHeightDialog());

        // Card 3: Medical & Contact
        findViewById(R.id.row_phone).setOnClickListener(v -> showEditPhoneDialog());
        findViewById(R.id.row_blood_type).setOnClickListener(v -> showBloodTypeDialog());
        findViewById(R.id.row_allergies).setOnClickListener(v -> showEditAllergiesDialog());
        findViewById(R.id.row_conditions).setOnClickListener(v -> showEditConditionsDialog());
        findViewById(R.id.row_address).setOnClickListener(v -> showEditAddressDialog());

        // Delete Account
        findViewById(R.id.btn_delete_account).setOnClickListener(v -> AccountDeletionUtils.confirmAndDelete(this, executor));
    }

    private void setupImagePicker() {
        pickImage = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    try {
                        getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}

                    UserPrefs.setPhotoUri(this, uri.toString());
                    UserPrefs.setPendingPhotoUri(this, uri.toString());
                    if (profilePhoto != null) {
                        try {
                            profilePhoto.setImageURI(uri);
                        } catch (Exception ignored) {}
                    }
                    uploadPhotoToStorage(DeviceIdUtils.getStableUid(this), uri);
                }
        );
    }

    private void launchImagePicker() {
        try {
            pickImage.launch(new String[]{"image/*"});
        } catch (Exception e) {
            Toast.makeText(this, R.string.toast_not_implemented, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPhotoOptionsDialog() {
        String photoB64 = UserPrefs.getPhotoB64(this);
        String photoUri = UserPrefs.getPhotoUri(this);
        boolean hasPhoto = isNotEmpty(photoB64) || isNotEmpty(photoUri);

        String[] options;
        if (hasPhoto) {
            options = new String[]{"Choose New Photo", "View Full Photo", "Remove Photo"};
        } else {
            options = new String[]{"Choose Photo"};
        }

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle("Profile Photo")
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchImagePicker();
                    } else if (which == 1) {
                        showPhotoPreviewDialog();
                    } else if (which == 2) {
                        clearProfilePhoto();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showPhotoPreviewDialog() {
        android.app.Dialog d = new android.app.Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        d.setCancelable(true);
        d.setCanceledOnTouchOutside(true);

        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        root.setBackgroundColor(0xB3000000);
        root.setOnClickListener(v -> d.dismiss());

        android.widget.ImageView big = new android.widget.ImageView(this);
        big.setAdjustViewBounds(true);
        big.setScaleType(android.widget.ImageView.ScaleType.FIT_CENTER);
        if (profilePhoto != null && profilePhoto.getDrawable() != null) {
            big.setImageDrawable(profilePhoto.getDrawable());
        } else {
            big.setImageResource(R.drawable.ic_avatar);
        }

        android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
        big.setMaxWidth(Math.round(dm.widthPixels * 0.92f));
        big.setMaxHeight(Math.round(dm.heightPixels * 0.70f));

        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.gravity = android.view.Gravity.CENTER;
        big.setLayoutParams(lp);

        root.addView(big);
        d.setContentView(root);
        d.show();
    }

    private void clearProfilePhoto() {
        String uid = DeviceIdUtils.getStableUid(this);
        UserPrefs.setPhotoB64(this, "");
        UserPrefs.setPhotoUrl(this, "");
        UserPrefs.setPhotoUri(this, "");
        UserPrefs.setPendingPhotoUri(this, "");
        if (isNotEmpty(uid)) FirebaseRoomClient.clearUserPhotoQueued(uid);
        if (profilePhoto != null) profilePhoto.setImageResource(R.drawable.ic_avatar);
        Toast.makeText(this, R.string.toast_profile_photo_removed, Toast.LENGTH_SHORT).show();
    }

    // ──────────────────────────────
    // DIALOGS: EDIT FIELDS
    // ──────────────────────────────

    private void showEditNameDialog() {
        showInputDialog("Edit Name", UserPrefs.getName(this), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME, newText -> {
            if (newText.isEmpty()) return;
            UserPrefs.setName(this, newText);
            tvValueName.setText(newText);
            syncProfileToFirebase();
            showSavedToast("Name updated");
        });
    }

    private void showEditEmailDialog() {
        showInputDialog("Edit Email", UserPrefs.getEmail(this), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, newText -> {
            if (newText.isEmpty()) return;
            UserPrefs.setEmail(this, newText);
            tvValueEmail.setText(newText);
            syncProfileToFirebase();
            showSavedToast("Email updated");
        });
    }

    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        String currentDob = UserPrefs.getDateOfBirth(this);
        try {
            if (isNotEmpty(currentDob)) {
                // Try parse formats: dd-MMMM-yyyy or dd/MM/yyyy
                if (currentDob.contains("-")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
                    Date date = sdf.parse(currentDob);
                    if (date != null) cal.setTime(date);
                } else if (currentDob.contains("/")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                    Date date = sdf.parse(currentDob);
                    if (date != null) cal.setTime(date);
                }
            }
        } catch (Exception ignored) {}

        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH);
        int d = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, R.style.DatePickerTheme, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MMMM-yyyy", Locale.ENGLISH);
            String formattedDate = fmt.format(selected.getTime());

            UserPrefs.setDateOfBirth(this, formattedDate);
            tvValueDob.setText(formattedDate);
            syncProfileToFirebase();
            showSavedToast("Birthdate updated");
        }, y, m, d);

        dpd.show();
    }

    private void showGenderPickerDialog() {
        String[] genders = new String[]{"Male", "Female", "Other"};
        String current = UserPrefs.getGender(this);
        int checkedItem = 0;
        for (int i = 0; i < genders.length; i++) {
            if (genders[i].equalsIgnoreCase(current)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle("Select Gender")
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setSingleChoiceItems(genders, checkedItem, (dialog, which) -> {
                    String selected = genders[which];
                    UserPrefs.setGender(this, selected);
                    tvValueGender.setText(selected);
                    syncProfileToFirebase();
                    showSavedToast("Gender updated");
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditWeightDialog() {
        showInputDialog("Edit Weight", UserPrefs.getWeight(this), InputType.TYPE_CLASS_TEXT, newText -> {
            String val = newText == null ? "" : newText.trim();
            if (val.isEmpty()) {
                UserPrefs.setWeight(this, "");
                tvValueWeight.setText("Not set");
                syncProfileToFirebase();
                showSavedToast("Weight cleared");
                return;
            }
            if (!val.toLowerCase().contains("kg") && !val.toLowerCase().contains("lbs")) {
                val = val + " kg";
            }
            UserPrefs.setWeight(this, val);
            tvValueWeight.setText(val);
            syncProfileToFirebase();
            showSavedToast("Weight updated");
        });
    }

    private void showEditHeightDialog() {
        showInputDialog("Edit Height", UserPrefs.getHeight(this), InputType.TYPE_CLASS_TEXT, newText -> {
            String val = newText == null ? "" : newText.trim();
            if (val.isEmpty()) {
                UserPrefs.setHeight(this, "");
                tvValueHeight.setText("Not set");
                syncProfileToFirebase();
                showSavedToast("Height cleared");
                return;
            }
            UserPrefs.setHeight(this, val);
            tvValueHeight.setText(val);
            syncProfileToFirebase();
            showSavedToast("Height updated");
        });
    }

    private void showEditPhoneDialog() {
        showInputDialog("Edit Phone", UserPrefs.getPhoneNumber(this), InputType.TYPE_CLASS_PHONE, newText -> {
            if (newText.isEmpty()) return;
            UserPrefs.setPhoneNumber(this, newText);
            tvValuePhone.setText(newText);
            syncProfileToFirebase();
            showSavedToast("Phone updated");
        });
    }

    private void showBloodTypeDialog() {
        String[] types = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
        String current = UserPrefs.getBloodType(this);
        int checkedItem = 6; // O+ default
        for (int i = 0; i < types.length; i++) {
            if (types[i].equalsIgnoreCase(current)) {
                checkedItem = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle("Select Blood Type")
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setSingleChoiceItems(types, checkedItem, (dialog, which) -> {
                    String selected = types[which];
                    UserPrefs.setBloodType(this, selected);
                    tvValueBloodType.setText(selected);
                    syncProfileToFirebase();
                    showSavedToast("Blood type updated");
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditAllergiesDialog() {
        String[] tempOptions;
        try {
            tempOptions = getResources().getStringArray(R.array.allergy_options);
        } catch (Exception e) {
            tempOptions = new String[]{"None", "Peanuts / Nuts", "Seafood / Shellfish", "Dairy / Lactose", "Eggs", "Wheat / Gluten", "Penicillin / Antibiotics", "Aspirin / NSAIDs", "Latex", "Dust / Pollen", "Others"};
        }
        final String[] options = tempOptions;
        final String current = UserPrefs.getAllergies(this);
        int matched = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(current)) {
                matched = i;
                break;
            }
        }
        final int checkedItem = matched;

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle("Select Allergy")
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selected = options[which];
                    dialog.dismiss();
                    if (isOtherSelected(selected)) {
                        showInputDialog("Specify Allergy", (checkedItem == -1 && isNotEmpty(current)) ? current : "", InputType.TYPE_CLASS_TEXT, newText -> {
                            String val = newText == null ? "" : newText.trim();
                            String displayVal = val.isEmpty() ? "None" : val;
                            UserPrefs.setAllergies(this, val);
                            tvValueAllergies.setText(displayVal);
                            syncProfileToFirebase();
                            showSavedToast("Allergies updated");
                        });
                    } else {
                        UserPrefs.setAllergies(this, selected);
                        tvValueAllergies.setText(selected);
                        syncProfileToFirebase();
                        showSavedToast("Allergies updated");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditConditionsDialog() {
        String[] tempOptions;
        try {
            tempOptions = getResources().getStringArray(R.array.medication_options);
        } catch (Exception e) {
            tempOptions = new String[]{"None", "Painkillers (Paracetamol / Panadol)", "Antibiotics", "Antihistamines (Allergy)", "Inhaler (Asthma)", "Insulin / Diabetes Meds", "Blood Pressure Meds (Hypertension)", "Heart / Cholesterol Meds", "Gastric / Antacid Meds", "Others"};
        }
        final String[] options = tempOptions;
        String currentConditions = UserPrefs.getExistingConditions(this);
        if (!isNotEmpty(currentConditions)) {
            currentConditions = UserPrefs.getMedications(this);
        }
        int matched = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].equalsIgnoreCase(currentConditions)) {
                matched = i;
                break;
            }
        }
        final int checkedItem = matched;
        final String finalCurrent = currentConditions;

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle("Select Medical Condition / Medication")
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String selected = options[which];
                    dialog.dismiss();
                    if (isOtherSelected(selected)) {
                        showInputDialog("Specify Medical Condition / Medication", (checkedItem == -1 && isNotEmpty(finalCurrent)) ? finalCurrent : "", InputType.TYPE_CLASS_TEXT, newText -> {
                            String val = newText == null ? "" : newText.trim();
                            String displayVal = val.isEmpty() ? "None" : val;
                            UserPrefs.setExistingConditions(this, val);
                            UserPrefs.setMedications(this, val);
                            tvValueConditions.setText(displayVal);
                            syncProfileToFirebase();
                            showSavedToast("Conditions updated");
                        });
                    } else {
                        UserPrefs.setExistingConditions(this, selected);
                        UserPrefs.setMedications(this, selected);
                        tvValueConditions.setText(selected);
                        syncProfileToFirebase();
                        showSavedToast("Conditions updated");
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showEditAddressDialog() {
        showInputDialog("Edit Address", UserPrefs.getAddress(this), InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, new InputFilter[]{new InputFilter.AllCaps()}, newText -> {
            String val = newText == null || newText.trim().isEmpty() ? "" : newText.trim().toUpperCase(Locale.ROOT);
            UserPrefs.setAddress(this, val);
            tvValueAddress.setText(val.isEmpty() ? "Not set" : val);
            syncProfileToFirebase();
            showSavedToast("Address updated");
        });
    }

    // ──────────────────────────────
    // HELPER: DIALOG & FIREBASE SYNC
    // ──────────────────────────────

    private interface OnInputSavedListener {
        void onSaved(String value);
    }

    private void showInputDialog(String title, String prefill, int inputType, OnInputSavedListener listener) {
        showInputDialog(title, prefill, inputType, null, listener);
    }

    private void showInputDialog(String title, String prefill, int inputType, InputFilter[] filters, OnInputSavedListener listener) {
        FrameLayout container = new FrameLayout(this);
        int marginHorizontal = dpToPx(24);
        int marginTop = dpToPx(8);
        int marginBottom = dpToPx(8);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(marginHorizontal, marginTop, marginHorizontal, marginBottom);

        EditText input = new EditText(this);
        input.setLayoutParams(lp);
        input.setInputType(inputType);
        if (filters != null) {
            input.setFilters(filters);
        }
        input.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_primary));
        input.setHintTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary));
        input.setTextSize(16);
        if (isNotEmpty(prefill)) {
            input.setText(prefill);
            input.setSelection(prefill.length());
        }
        input.setSingleLine(inputType != (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE));
        container.addView(input);

        new MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_ResQTap_AlertDialog)
                .setTitle(title)
                .setView(container)
                .setBackground(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_dialog_rounded))
                .setPositiveButton("Save", (dialog, which) -> {
                    String val = input.getText() == null ? "" : input.getText().toString().trim();
                    if (listener != null) listener.onSaved(val);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void syncProfileToFirebase() {
        final String uid = DeviceIdUtils.getStableUid(this);
        final String name = UserPrefs.getName(this);
        final String email = UserPrefs.getEmail(this);
        final String bloodType = UserPrefs.getBloodType(this);
        final String allergies = UserPrefs.getAllergies(this);
        final String existingConditions = UserPrefs.getExistingConditions(this);
        final String photoUrl = UserPrefs.getPhotoUrl(this);
        final String publicId = UserPrefs.getPublicId(this);
        final String address = UserPrefs.getAddress(this);
        final String religion = UserPrefs.getReligion(this);
        final String phone = UserPrefs.getPhoneNumber(this);
        final String dob = UserPrefs.getDateOfBirth(this);
        final String ethnicity = UserPrefs.getEthnicity(this);
        final String gender = UserPrefs.getGender(this);
        final String weight = UserPrefs.getWeight(this);
        final String height = UserPrefs.getHeight(this);

        executor.execute(() -> {
            try {
                FirebaseRoomClient.upsertUserProfile(
                        uid,
                        name,
                        email,
                        bloodType,
                        allergies,
                        existingConditions,
                        photoUrl,
                        publicId,
                        address,
                        religion,
                        phone,
                        dob,
                        ethnicity,
                        gender,
                        weight,
                        height
                );
            } catch (Exception e) {
                Log.e(TAG, "Sync profile failed", e);
            }
        });
    }

    private void uploadPhotoToStorage(String uid, Uri localUri) {
        if (localUri == null || !isNotEmpty(uid)) return;
        executor.execute(() -> {
            try {
                String b64 = encodeAvatarToBase64(localUri, 256, 72);
                if (b64.isEmpty()) throw new RuntimeException("encode_failed");
                UserPrefs.setPhotoB64(EditProfileActivity.this, b64);
                UserPrefs.setPendingPhotoUri(EditProfileActivity.this, "");
                FirebaseRoomClient.updateUserPhotoB64Queued(uid, b64);
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, R.string.toast_photo_uploaded, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e(TAG, "Upload photo failed", e);
                runOnUiThread(() -> Toast.makeText(
                        EditProfileActivity.this,
                        getString(R.string.toast_upload_failed, safeErr(e)),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private String encodeAvatarToBase64(Uri localUri, int maxDim, int jpegQuality) {
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

    private void showSavedToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private static String safeErr(Throwable t) {
        if (t == null) return "unknown";
        String msg = t.getMessage();
        return msg == null || msg.trim().isEmpty() ? t.getClass().getSimpleName() : msg.trim();
    }

    private boolean isOtherSelected(String text) {
        if (text == null) return false;
        String t = text.trim().toLowerCase(java.util.Locale.ROOT);
        return t.equals("others") || t.equals("other") || t.equals("lain-lain") || t.equals("其他") || t.equals("மற்றவை");
    }
}
