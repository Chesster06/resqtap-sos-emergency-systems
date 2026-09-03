package com.example.resqtap.report;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * ReportActivity
 * Borang Lapor Insiden: pilih kategori kecemasan, attach lokasi GPS, dan upload gambar/video bukti.
 */
public class ReportActivity extends BaseActivity implements OnMapReadyCallback {
    private static final int MAX_ATTACHMENTS = 5;

    private GoogleMap map;
    private Marker marker;
    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource locationTokenSource;
    private final ExecutorService geocoderExecutor = Executors.newSingleThreadExecutor();

    private ActivityResultLauncher<String[]> locationPerms;
    private ActivityResultLauncher<String[]> pickMedia;
    private ActivityResultLauncher<Void> takeCameraPhoto;

    private TextView addressView;
    private TextView charCountView;
    private TextView attachmentSummaryView;
    private LinearLayout attachmentPreviews;
    private View attachmentPreviewScroll;
    private View successOverlay;
    private View successSheet;
    private View submitContainer;
    private EditText detailsInput;
    private ChipGroup categoryGroup;
    private MaterialButton submitButton;

    private LatLng incidentLatLng;
    private String incidentAddress = "";
    private boolean submitting = false;
    private boolean reportSubmitted = false;
    private final ArrayList<AttachmentInfo> attachments = new ArrayList<>();

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);

        View main = findViewById(R.id.main);
        submitContainer = findViewById(R.id.submit_container);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            if (submitContainer != null) {
                submitContainer.setPadding(
                        submitContainer.getPaddingLeft(),
                        submitContainer.getPaddingTop(),
                        submitContainer.getPaddingRight(),
                        systemBars.bottom + dp(10)
                );
            }
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bindLaunchers();
        bindViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.report_map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    protected void onDestroy() {
        if (locationTokenSource != null) locationTokenSource.cancel();
        geocoderExecutor.shutdownNow();
        super.onDestroy();
    }

    /** Setup dan konfigurasi Launchers. */
    private void bindLaunchers() {
        locationPerms = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse)) {
                        updateLocation();
                    } else {
                        addressView.setText(R.string.report_location_permission_required);
                    }
                }
        );

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.OpenMultipleDocuments(),
                uris -> {
                    if (uris == null || uris.isEmpty()) return;
                    for (Uri uri : uris) {
                        if (attachments.size() >= MAX_ATTACHMENTS) {
                            Toast.makeText(this, R.string.report_attachment_full, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        try {
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } catch (Exception ignored) {
                        }
                        attachments.add(AttachmentInfo.fromUri(
                                uri,
                                queryDisplayName(uri),
                                safe(getContentResolver().getType(uri)),
                                querySize(uri)
                        ));
                    }
                    updateAttachmentSummary();
                }
        );

        takeCameraPhoto = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap == null) return;
                    if (attachments.size() >= MAX_ATTACHMENTS) {
                        Toast.makeText(this, R.string.report_attachment_full, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out);
                    String name = "report_camera_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".jpg";
                    attachments.add(AttachmentInfo.fromBytes(out.toByteArray(), name, "image/jpeg"));
                    updateAttachmentSummary();
                }
        );
    }

    // =========================================================================
    // SEKSYEN: BINDVIEWS
    // =========================================================================
    /** Setup dan konfigurasi Views. */
    private void bindViews() {
        View back = findViewById(R.id.btn_back);
        View updateLocationButton = findViewById(R.id.btn_update_location);
        View pickMediaButton = findViewById(R.id.btn_pick_media);
        View takePhotoButton = findViewById(R.id.btn_take_photo);

        addressView = findViewById(R.id.report_address);
        charCountView = findViewById(R.id.report_char_count);
        attachmentSummaryView = findViewById(R.id.report_attachment_summary);
        attachmentPreviews = findViewById(R.id.report_attachment_previews);
        attachmentPreviewScroll = findViewById(R.id.report_preview_scroll);
        successOverlay = findViewById(R.id.report_success_overlay);
        successSheet = findViewById(R.id.report_success_sheet);
        detailsInput = findViewById(R.id.report_details);
        categoryGroup = findViewById(R.id.report_category_group);
        submitButton = findViewById(R.id.btn_submit_report);
        MaterialButton successOk = findViewById(R.id.btn_success_ok);

        if (back != null) back.setOnClickListener(v -> finish());
        if (updateLocationButton != null) updateLocationButton.setOnClickListener(v -> requestLocation());
        if (pickMediaButton != null) pickMediaButton.setOnClickListener(v -> pickMedia.launch(new String[]{"image/*", "video/*", "audio/*", "application/pdf"}));
        if (takePhotoButton != null) takePhotoButton.setOnClickListener(v -> takeCameraPhoto.launch(null));
        if (categoryGroup != null) categoryGroup.check(R.id.category_crime);
        if (submitButton != null) submitButton.setOnClickListener(v -> submitReport());
        if (successOk != null) {
            successOk.setText(R.string.report_success_ok);
            successOk.setOnClickListener(v -> goToMainPage());
        }

        detailsInput.addTextChangedListener(new TextWatcher() {
            /** Fungsi untuk beforeTextChanged. */
    @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /** Fungsi untuk onTextChanged. */
    @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCountView.setText(getString(R.string.report_char_count, s == null ? 0 : s.length()));
                if (s != null && s.toString().trim().length() > 0) {
                    detailsInput.setError(null);
                }
            }

            /** Fungsi untuk afterTextChanged. */
    @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updateAttachmentSummary();
    }

    /** Fungsi untuk onMapReady. */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        try {
            map.getUiSettings().setMapToolbarEnabled(false);
            map.getUiSettings().setCompassEnabled(false);
            map.getUiSettings().setZoomControlsEnabled(false);
        } catch (Exception ignored) {
        }
        requestLocation();
    }

    /** Fungsi untuk requestLocation. */
    private void requestLocation() {
        if (hasLocationPermission()) {
            updateLocation();
            return;
        }
        locationPerms.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /** Semak dan sahkan LocationPermission. */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    /** Simpan atau hantar data Location. */
    private void updateLocation() {
        if (!hasLocationPermission()) return;
        addressView.setText(R.string.report_location_loading);
        if (locationTokenSource != null) locationTokenSource.cancel();
        locationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, locationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        applyLocation(location);
                    } else {
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(last -> {
                                    if (last != null) applyLocation(last);
                                    else addressView.setText(R.string.report_location_unavailable);
                                })
                                .addOnFailureListener(error -> addressView.setText(R.string.report_location_unavailable));
                    }
                })
                .addOnFailureListener(error -> addressView.setText(R.string.report_location_unavailable));
    }

    /** Fungsi untuk applyLocation. */
    private void applyLocation(Location location) {
        incidentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        incidentAddress = formatLatLng(incidentLatLng);
        addressView.setText(incidentAddress);

        if (map != null) {
            if (marker == null) {
                marker = map.addMarker(new MarkerOptions()
                        .position(incidentLatLng)
                        .title(getString(R.string.report_location_title)));
            } else {
                marker.setPosition(incidentLatLng);
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(incidentLatLng, 16f));
        }

        geocoderExecutor.execute(() -> {
            String resolved = resolveAddress(incidentLatLng);
            if (resolved.isEmpty()) return;
            runOnUiThread(() -> {
                incidentAddress = resolved;
                addressView.setText(resolved);
            });
        });
    }

    /** Fungsi untuk resolveAddress. */
    private String resolveAddress(LatLng latLng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses == null || addresses.isEmpty()) return "";
            Address address = addresses.get(0);
            ArrayList<String> parts = new ArrayList<>();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                String line = safe(address.getAddressLine(i));
                if (!line.isEmpty()) parts.add(line);
            }
            return parts.isEmpty() ? "" : android.text.TextUtils.join(", ", parts);
        } catch (Exception ignored) {
            return "";
        }
    }

    /** Fungsi untuk submitReport. */
    private void submitReport() {
        if (submitting || reportSubmitted) return;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.support_livechat_login_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (incidentLatLng == null) {
            Toast.makeText(this, R.string.report_location_required, Toast.LENGTH_SHORT).show();
            requestLocation();
            return;
        }

        int checkedId = categoryGroup == null ? View.NO_ID : categoryGroup.getCheckedChipId();
        String category = categoryValue(checkedId);
        if (category.isEmpty()) {
            Toast.makeText(this, R.string.report_category_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String details = detailsInput == null ? "" : safe(detailsInput.getText());
        if (details.isEmpty()) {
            if (detailsInput != null) {
                detailsInput.setError(getString(R.string.report_details_required));
                detailsInput.requestFocus();
            }
            Toast.makeText(this, R.string.report_details_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (attachments.isEmpty()) {
            Toast.makeText(this, R.string.report_attachment_required, Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true, getString(R.string.report_attachment_uploading));
        DatabaseReference reportRef = FirebaseDatabase
                .getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("incidentReports")
                .push();
        String reportId = reportRef.getKey();
        if (reportId == null || reportId.trim().isEmpty()) {
            setSubmitting(false, getString(R.string.report_continue));
            Toast.makeText(this, R.string.report_submit_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        uploadAttachments(user.getUid(), reportId, 0, new ArrayList<>(), uploaded -> {
            Map<String, Object> report = buildReportPayload(reportId, user, category, uploaded);
            reportRef.setValue(report).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    markReportSubmitted();
                    showSuccessSheet();
                } else {
                    setSubmitting(false, getString(R.string.report_continue));
                    Toast.makeText(this, R.string.report_submit_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }, error -> {
            setSubmitting(false, getString(R.string.report_continue));
            Toast.makeText(this, R.string.report_submit_failed, Toast.LENGTH_SHORT).show();
        });
    }

    /** Fungsi untuk buildReportPayload. */
    private Map<String, Object> buildReportPayload(String reportId, FirebaseUser user, String category, ArrayList<Map<String, Object>> uploaded) {
        String name = safe(UserPrefs.getName(this));
        if (name.isEmpty()) name = safe(user.getDisplayName());
        if (name.isEmpty()) name = "User";

        Map<String, Object> report = new HashMap<>();
        report.put("reportId", reportId);
        report.put("senderUid", user.getUid());
        report.put("senderName", name);
        report.put("senderEmail", safe(user.getEmail()));
        report.put("publicId", safe(UserPrefs.getPublicId(this)));
        report.put("category", category);
        report.put("categoryLabel", categoryLabel(category));
        report.put("details", safe(detailsInput.getText()));
        report.put("address", safe(incidentAddress));
        report.put("latitude", incidentLatLng.latitude);
        report.put("longitude", incidentLatLng.longitude);
        report.put("status", "new");
        report.put("source", "android_app");
        report.put("createdAt", ServerValue.TIMESTAMP);
        report.put("updatedAt", ServerValue.TIMESTAMP);
        if (!uploaded.isEmpty()) report.put("attachments", uploaded);
        return report;
    }

    private void uploadAttachments(String uid, String reportId, int index, ArrayList<Map<String, Object>> uploaded,
                                   AttachmentUploadDone done, AttachmentUploadFailed failed) {
        if (index >= attachments.size()) {
            done.onDone(uploaded);
            return;
        }

        AttachmentInfo attachment = attachments.get(index);
        if (isImageAttachment(attachment)) {
            Map<String, Object> inline = inlineImageAttachmentMap(attachment);
            if (!inline.isEmpty()) {
                uploaded.add(inline);
                uploadAttachments(uid, reportId, index + 1, uploaded, done, failed);
                return;
            }
        }

        String safeName = sanitizeFileName(attachment.name.isEmpty() ? "attachment" : attachment.name);
        StorageReference target = FirebaseStorage.getInstance()
                .getReference()
                .child("incidentReports")
                .child(uid)
                .child(reportId)
                .child(index + "_" + safeName);

        if (attachment.bytes != null) {
            target.putBytes(attachment.bytes)
                    .addOnSuccessListener(snapshot -> handleUploadedAttachment(target, attachment, uploaded, () ->
                            uploadAttachments(uid, reportId, index + 1, uploaded, done, failed), failed))
                    .addOnFailureListener(failed::onFailed);
            return;
        }

        target.putFile(attachment.uri)
                .addOnSuccessListener(snapshot -> handleUploadedAttachment(target, attachment, uploaded, () ->
                        uploadAttachments(uid, reportId, index + 1, uploaded, done, failed), failed))
                .addOnFailureListener(failed::onFailed);
    }

    private void handleUploadedAttachment(StorageReference target, AttachmentInfo attachment,
                                          ArrayList<Map<String, Object>> uploaded, Runnable next,
                                          AttachmentUploadFailed failed) {
        target.getDownloadUrl()
                .addOnSuccessListener(downloadUri -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", attachment.name);
                    data.put("mimeType", attachment.mimeType);
                    data.put("size", attachment.size);
                    data.put("downloadUrl", downloadUri.toString());
                    data.put("storagePath", target.getPath());
                    uploaded.add(data);
                    next.run();
                })
                .addOnFailureListener(failed::onFailed);
    }

    /** Fungsi untuk categoryValue. */
    private String categoryValue(int checkedId) {
        if (checkedId == R.id.category_fire) return "fire";
        if (checkedId == R.id.category_crime) return "crime";
        if (checkedId == R.id.category_medical) return "medical_aid";
        if (checkedId == R.id.category_humanitarian) return "humanitarian_aid";
        if (checkedId == R.id.category_sea) return "sea_emergency";
        return "";
    }

    /** Fungsi untuk categoryLabel. */
    private String categoryLabel(String category) {
        if ("fire".equals(category)) return getString(R.string.report_category_fire);
        if ("crime".equals(category)) return getString(R.string.report_category_crime);
        if ("medical_aid".equals(category)) return getString(R.string.report_category_medical);
        if ("humanitarian_aid".equals(category)) return getString(R.string.report_category_humanitarian);
        if ("sea_emergency".equals(category)) return getString(R.string.report_category_sea);
        return category;
    }

    /** Fungsi untuk setSubmitting. */
    private void setSubmitting(boolean sending, String label) {
        submitting = sending;
        submitButton.setEnabled(!sending);
        submitButton.setText(label);
    }

    /** Fungsi untuk markReportSubmitted. */
    private void markReportSubmitted() {
        submitting = false;
        reportSubmitted = true;
        if (submitButton != null) {
            submitButton.setEnabled(false);
            submitButton.setText(R.string.report_continue);
        }
    }

    /** Fungsi untuk goToMainPage. */
    private void goToMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /** Paparkan SuccessSheet. */
    private void showSuccessSheet() {
        if (successOverlay == null) {
            Toast.makeText(this, R.string.report_submitted, Toast.LENGTH_SHORT).show();
            goToMainPage();
            return;
        }
        if (submitContainer != null) submitContainer.setVisibility(View.GONE);
        successOverlay.bringToFront();
        successOverlay.setAlpha(0f);
        successOverlay.setVisibility(View.VISIBLE);
        successOverlay.animate().alpha(1f).setDuration(180).start();

        if (successSheet != null) {
            successSheet.post(() -> {
                successSheet.setTranslationY(successSheet.getHeight());
                successSheet.animate()
                        .translationY(0f)
                        .setDuration(280)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            });
        }
    }

    /** Simpan atau hantar data AttachmentSummary. */
    private void updateAttachmentSummary() {
        if (attachmentSummaryView == null) return;
        if (attachments.isEmpty()) {
            attachmentSummaryView.setText(R.string.report_no_attachments);
        } else {
            attachmentSummaryView.setText(getString(R.string.report_attachment_count, attachments.size()));
        }
        renderAttachmentPreviews();
    }

    /** Fungsi untuk renderAttachmentPreviews. */
    private void renderAttachmentPreviews() {
        if (attachmentPreviews == null || attachmentPreviewScroll == null) return;
        attachmentPreviews.removeAllViews();
        attachmentPreviewScroll.setVisibility(attachments.isEmpty() ? View.GONE : View.VISIBLE);
        for (int i = 0; i < attachments.size(); i++) {
            attachmentPreviews.addView(createPreviewTile(attachments.get(i), i));
        }
    }

    /** Fungsi untuk createPreviewTile. */
    private View createPreviewTile(AttachmentInfo attachment, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackgroundResource(R.drawable.bg_report_preview_tile);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(dp(108), ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMarginEnd(dp(10));
        card.setLayoutParams(cardParams);

        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setBackgroundColor(ContextCompat.getColor(this, R.color.report_tile_bg));
        Bitmap bitmap = isImageAttachment(attachment) ? previewBitmap(attachment, 220) : null;
        if (bitmap != null) {
            preview.setImageBitmap(bitmap);
        } else {
            preview.setImageResource(R.drawable.ic_livechat_document);
            preview.setColorFilter(ContextCompat.getColor(this, R.color.report_tile_content));
            preview.setPadding(dp(26), dp(26), dp(26), dp(26));
        }
        card.addView(preview, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(78)
        ));

        TextView name = new TextView(this);
        name.setText(attachment.name.isEmpty() ? "Attachment " + (index + 1) : attachment.name);
        name.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        name.setTextSize(11);
        name.setMaxLines(1);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        nameParams.topMargin = dp(6);
        card.addView(name, nameParams);
        return card;
    }

    /** Fungsi untuk inlineImageAttachmentMap. */
    private Map<String, Object> inlineImageAttachmentMap(AttachmentInfo attachment) {
        Map<String, Object> data = new HashMap<>();
        String mimeType = safe(attachment.mimeType);
        if (mimeType.isEmpty() || !mimeType.startsWith("image/")) mimeType = "image/jpeg";
        String b64 = encodeImageToBase64(attachment, 1024, 74);
        if (b64.isEmpty()) return data;
        data.put("name", attachment.name);
        data.put("mimeType", "image/jpeg");
        data.put("size", attachment.size);
        data.put("downloadUrl", "data:image/jpeg;base64," + b64);
        data.put("inline", true);
        return data;
    }

    /** Semak dan sahkan ImageAttachment. */
    private boolean isImageAttachment(AttachmentInfo attachment) {
        String mimeType = safe(attachment.mimeType).toLowerCase(Locale.ROOT);
        if (mimeType.startsWith("image/")) return true;
        String name = safe(attachment.name).toLowerCase(Locale.ROOT);
        return name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".webp");
    }

    /** Fungsi untuk encodeImageToBase64. */
    private String encodeImageToBase64(AttachmentInfo attachment, int maxDim, int jpegQuality) {
        Bitmap bitmap = decodeAttachmentBitmap(attachment, maxDim);
        if (bitmap == null) return "";
        try {
            int largest = Math.max(bitmap.getWidth(), bitmap.getHeight());
            if (largest > maxDim) {
                float scale = maxDim / (float) largest;
                int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
                int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
                bitmap.recycle();
                bitmap = scaled;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, Math.max(45, Math.min(jpegQuality, 90)), out);
            return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
        } catch (Exception ignored) {
            return "";
        } finally {
            bitmap.recycle();
        }
    }

    /** Fungsi untuk previewBitmap. */
    private Bitmap previewBitmap(AttachmentInfo attachment, int maxDim) {
        Bitmap bitmap = decodeAttachmentBitmap(attachment, maxDim);
        if (bitmap == null) return null;
        int largest = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (largest <= maxDim) return bitmap;
        float scale = maxDim / (float) largest;
        int width = Math.max(1, Math.round(bitmap.getWidth() * scale));
        int height = Math.max(1, Math.round(bitmap.getHeight() * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
        bitmap.recycle();
        return scaled;
    }

    /** Fungsi untuk decodeAttachmentBitmap. */
    private Bitmap decodeAttachmentBitmap(AttachmentInfo attachment, int maxDim) {
        try {
            if (attachment.bytes != null && attachment.bytes.length > 0) {
                BitmapFactory.Options bounds = new BitmapFactory.Options();
                bounds.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(attachment.bytes, 0, attachment.bytes.length, bounds);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxDim);
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeByteArray(attachment.bytes, 0, attachment.bytes.length, opts);
            }
            if (attachment.uri == null) return null;
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (java.io.InputStream in = getContentResolver().openInputStream(attachment.uri)) {
                if (in == null) return null;
                BitmapFactory.decodeStream(in, null, bounds);
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxDim);
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try (java.io.InputStream in = getContentResolver().openInputStream(attachment.uri)) {
                if (in == null) return null;
                return BitmapFactory.decodeStream(in, null, opts);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Fungsi untuk sampleSize. */
    private int sampleSize(int width, int height, int maxDim) {
        int sample = 1;
        int largest = Math.max(Math.max(1, width), Math.max(1, height));
        while (largest / sample > maxDim * 2) sample *= 2;
        return sample;
    }

    /** Fungsi untuk queryDisplayName. */
    private String queryDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return safe(cursor.getString(index));
            }
        } catch (Exception ignored) {
        }
        return safe(uri.getLastPathSegment());
    }

    /** Fungsi untuk querySize. */
    private long querySize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0) return cursor.getLong(index);
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }

    /** Fungsi untuk sanitizeFileName. */
    private String sanitizeFileName(String name) {
        String clean = safe(name).replaceAll("[\\\\/:*?\"<>|#\\[\\]$]", "_");
        if (clean.isEmpty()) clean = "attachment";
        return clean;
    }

    /** Fungsi untuk formatLatLng. */
    private String formatLatLng(LatLng latLng) {
        return String.format(Locale.US, "%.6f, %.6f", latLng.latitude, latLng.longitude);
    }

    /** Fungsi untuk safe. */
    private String safe(Object value) {
        return String.valueOf(value == null ? "" : value).trim();
    }

    /** Fungsi untuk dp. */
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class AttachmentInfo {
        final Uri uri;
        final byte[] bytes;
        final String name;
        final String mimeType;
        final long size;

        private AttachmentInfo(Uri uri, byte[] bytes, String name, String mimeType, long size) {
            this.uri = uri;
            this.bytes = bytes;
            this.name = name == null ? "" : name;
            this.mimeType = mimeType == null || mimeType.trim().isEmpty() ? "application/octet-stream" : mimeType;
            this.size = size;
        }

        static AttachmentInfo fromUri(Uri uri, String name, String mimeType, long size) {
            return new AttachmentInfo(uri, null, name, mimeType, size);
        }

        static AttachmentInfo fromBytes(byte[] bytes, String name, String mimeType) {
            return new AttachmentInfo(null, bytes, name, mimeType, bytes == null ? 0L : bytes.length);
        }
    }

    private interface AttachmentUploadDone {
        void onDone(ArrayList<Map<String, Object>> uploaded);
    }

    private interface AttachmentUploadFailed {
        void onFailed(Exception error);
    }
}
