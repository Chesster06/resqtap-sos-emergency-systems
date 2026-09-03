package com.example.resqtap.home;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.auth.LoginActivity;
import com.example.resqtap.contacts.EmergencyContactsListActivity;
import com.example.resqtap.map.NearbyHospitalActivity;
import com.example.resqtap.notification.NotificationHelper;
import com.example.resqtap.notification.NotificationsActivity;
import com.example.resqtap.notification.NotificationUtils;
import com.example.resqtap.report.ReportActivity;
import com.example.resqtap.report.SupportActivity;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.room.LiveRoomTrackingService;
import com.example.resqtap.sos.SosBottomSheetController;
import com.example.resqtap.sos.SosServiceStarter;
import com.example.resqtap.highlight.HighlightAdapter;
import com.example.resqtap.highlight.HighlightItem;
import com.example.resqtap.news.MedicalNewsActivity;
import com.example.resqtap.news.MedicalNewsAdapter;
import com.example.resqtap.news.MedicalNewsFetcher;
import com.example.resqtap.news.MedicalNewsItem;
import com.example.resqtap.utils.AvatarUtils;
import com.example.resqtap.utils.BatteryOptimizationHelper;
import com.example.resqtap.utils.BottomNavUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;
import com.example.resqtap.wear.WearSosBridge;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;


/**
 * MainActivity
 * Main Dashboard: Butang OneTap SOS, grid menu pantas, status bateri, dan sambungan ke smartwatch.
 */
public class MainActivity extends BaseActivity implements MessageClient.OnMessageReceivedListener, DataClient.OnDataChangedListener {
    private TextView subtitle;
    private String phone;
    private ObjectAnimator sosPulseAnimator;
    private Animator sosRingOuterAnimator;
    private Animator sosRingMidAnimator;
    private ObjectAnimator sosGlowAnimator;
    private boolean entranceAnimationPlayed = false;
    private TextView title;
    private ShapeableImageView headerPhoto;
    private MaterialButton btnInbox;
    private android.view.View inboxUnreadDot;
    private ActivityResultLauncher<String> notifPerm;
    private android.view.View medicalCard;
    private TextView medicalBlood;
    private TextView medicalAllergies;
    private TextView medicalConditions;
    private SosBottomSheetController sosSheet;
    private String activeRoomCode = "";
    private DatabaseReference adminNotificationRef;
    private ValueEventListener adminNotificationListener;
    private String pendingAdminNotificationId = "";
    private String pendingAdminNotificationTitle = "";
    private String pendingAdminNotificationMessage = "";
    private boolean adminNotificationBaselineLoaded = false;
    private volatile boolean restoringActiveRoom = false;
    private volatile String myActiveSosId = "";
    private volatile boolean cancelMySosWhenIdArrives = false;
    private boolean pendingWatchSosTrigger = false;
    private long lastWatchSosTriggerAt = 0L;
    private RecyclerView rvMedicalNews;
    private MedicalNewsAdapter medicalNewsAdapter;
    private android.view.View newsProgress;
    private RecyclerView rvHighlights;
    private HighlightAdapter highlightAdapter;

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        android.view.View root = findViewById(R.id.main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        BottomNavUtils.setup(bottomNav, this, R.id.nav_bottom_home);
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

        notifPerm = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted && !pendingAdminNotificationId.isEmpty()) {
                        postAdminNotification(
                                pendingAdminNotificationId,
                                pendingAdminNotificationTitle,
                                pendingAdminNotificationMessage
                        );
                        pendingAdminNotificationId = "";
                        pendingAdminNotificationTitle = "";
                        pendingAdminNotificationMessage = "";
                    }
                }
        );

        headerPhoto = findViewById(R.id.header_profile_photo);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        medicalCard = findViewById(R.id.medical_card);
        medicalBlood = findViewById(R.id.tv_medical_blood);
        medicalAllergies = findViewById(R.id.tv_medical_allergies);
        medicalConditions = findViewById(R.id.tv_medical_conditions);
        phone = getIntent().getStringExtra("phone");

        MaterialButton btnSos = findViewById(R.id.btn_sos);
        btnInbox = findViewById(R.id.btn_inbox);
        inboxUnreadDot = findViewById(R.id.inbox_unread_dot);
        sosSheet = new SosBottomSheetController(this, new SosBottomSheetController.SosCallbacks() {
            /** Fungsi untuk onSosStarted. */
    @Override
            public void onSosStarted() {
                String code = String.valueOf(UserPrefs.getActiveRoomCode(MainActivity.this) == null ? "" : UserPrefs.getActiveRoomCode(MainActivity.this)).trim();
                if (code.isEmpty()) {
                    showJoinOrCreateRoomDialog();
                    return;
                }
                FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
                if (u == null) return;
                String uid = u.getUid();
                String dev = UserPrefs.getOrCreateDeviceId(MainActivity.this);
                String name = UserPrefs.getName(MainActivity.this);
                if (name == null || name.trim().isEmpty()) name = "User";
                FirebaseRoomClient.sendRoomSosQueued(code, uid, dev, name, id -> {
                    myActiveSosId = String.valueOf(id == null ? "" : id).trim();
                    if (cancelMySosWhenIdArrives && !myActiveSosId.isEmpty()) {
                        FirebaseRoomClient.cancelRoomSosQueued(code, myActiveSosId, dev);
                        myActiveSosId = "";
                        cancelMySosWhenIdArrives = false;
                    }
                });
            }

            /** Fungsi untuk onSosCancelled. */
    @Override
            public void onSosCancelled() {
                String code = String.valueOf(UserPrefs.getActiveRoomCode(MainActivity.this) == null ? "" : UserPrefs.getActiveRoomCode(MainActivity.this)).trim();
                String id = String.valueOf(myActiveSosId == null ? "" : myActiveSosId).trim();
                if (!code.isEmpty() && !id.isEmpty()) {
                    String dev = UserPrefs.getOrCreateDeviceId(MainActivity.this);
                    FirebaseRoomClient.cancelRoomSosQueued(code, id, dev);
                } else if (!code.isEmpty() && id.isEmpty()) {
                    cancelMySosWhenIdArrives = true;
                }
                myActiveSosId = "";
            }
        });
        handleWatchSosIntent(getIntent());

        android.view.View featureHospital = findViewById(R.id.feature_hospital);
        android.view.View featureContacts = findViewById(R.id.feature_contacts);
        android.view.View featureSupport = findViewById(R.id.feature_support);
        android.view.View featureLanguage = findViewById(R.id.feature_language);

        if (featureHospital != null) featureHospital.setOnClickListener(v -> startActivity(new Intent(this, NearbyHospitalActivity.class)));
        if (featureContacts != null) featureContacts.setOnClickListener(v -> startActivity(new Intent(this, EmergencyContactsListActivity.class)));
        if (featureSupport != null) featureSupport.setOnClickListener(v -> startActivity(new Intent(this, SupportActivity.class)));
        if (featureLanguage != null) featureLanguage.setOnClickListener(v -> startActivity(new Intent(this, ReportActivity.class)));

        btnSos.setOnClickListener(v -> {
            triggerSosFlow();
        });

        if (btnInbox != null) btnInbox.setOnClickListener(v -> startActivity(new Intent(this, NotificationsActivity.class)));

        ensureNotificationsPermission();
        try { NotificationHelper.createNotificationChannel(this); } catch (Exception ignored) {}
        try { NotificationHelper.ensureSosChannel(this); } catch (Exception ignored) {}
        try { NotificationUtils.ensureAdminChannel(this); } catch (Exception ignored) {}
        try { BatteryOptimizationHelper.promptOnce(this); } catch (Exception ignored) {}
        startAdminNotificationListener();

        refreshProfileUi();
        refreshInboxBadge();
        setupHighlights();
        setupMedicalNews();

        playEntranceAnimations();
    }

    private void setupHighlights() {
        rvHighlights = findViewById(R.id.rv_highlights);
        if (rvHighlights == null) return;

        rvHighlights.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        highlightAdapter = new HighlightAdapter(this);
        rvHighlights.setAdapter(highlightAdapter);

        // Enable ultra smooth horizontal scrolling within vertical NestedScrollView
        rvHighlights.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            private float startX = 0f;
            private float startY = 0f;

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, android.view.MotionEvent e) {
                switch (e.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        startX = e.getX();
                        startY = e.getY();
                        break;
                    case android.view.MotionEvent.ACTION_MOVE:
                        float dx = Math.abs(e.getX() - startX);
                        float dy = Math.abs(e.getY() - startY);
                        if (dx > dy && dx > 8f) {
                            if (rv.getParent() != null) {
                                rv.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        } else if (dy > dx && dy > 8f) {
                            if (rv.getParent() != null) {
                                rv.getParent().requestDisallowInterceptTouchEvent(false);
                            }
                        }
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        if (rv.getParent() != null) {
                            rv.getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, android.view.MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        // Listen to Firebase Realtime Database
        DatabaseReference hlRef = FirebaseDatabase.getInstance().getReference("highlights");
        hlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                java.util.List<HighlightItem> list = new java.util.ArrayList<>();
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        HighlightItem item = child.getValue(HighlightItem.class);
                        if (item != null) {
                            if (item.getId() == null || item.getId().isEmpty()) item.setId(child.getKey());
                            if (item.isActive()) {
                                list.add(item);
                            }
                        }
                    }
                    // Sort by order
                    java.util.Collections.sort(list, (a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
                }

                // If empty in database, seed real initial banners to Firebase RTDB
                if (list.isEmpty()) {
                    seedHighlightsToFirebase(hlRef);
                    list.add(new HighlightItem("hl_people_first", "People First Abilities Always", R.drawable.img_highlight_1, "https://www.jkm.gov.my"));
                    list.add(new HighlightItem("hl_ability_limits", "Ability Has No Limits - Inclusion Is Everyone's Mission", R.drawable.img_highlight_2, "https://www.moh.gov.my"));
                    list.add(new HighlightItem("hl_different_abilities", "Different Abilities One Community - Inclusion Today", R.drawable.img_highlight_3, "https://www.malaysia.gov.my"));
                }

                if (highlightAdapter != null) {
                    highlightAdapter.submitList(list);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Fallback defaults if offline
                java.util.List<HighlightItem> list = new java.util.ArrayList<>();
                list.add(new HighlightItem("hl_people_first", "People First Abilities Always", R.drawable.img_highlight_1, "https://www.jkm.gov.my"));
                list.add(new HighlightItem("hl_ability_limits", "Ability Has No Limits - Inclusion Is Everyone's Mission", R.drawable.img_highlight_2, "https://www.moh.gov.my"));
                list.add(new HighlightItem("hl_different_abilities", "Different Abilities One Community - Inclusion Today", R.drawable.img_highlight_3, "https://www.malaysia.gov.my"));
                if (highlightAdapter != null) {
                    highlightAdapter.submitList(list);
                }
            }
        });
    }

    private void seedHighlightsToFirebase(DatabaseReference hlRef) {
        try {
            java.util.Map<String, Object> map1 = new java.util.HashMap<>();
            map1.put("id", "hl_people_first");
            map1.put("title", "People First Abilities Always");
            map1.put("imageUrl", "assets/highlight_1.jpg");
            map1.put("actionUrl", "https://www.jkm.gov.my");
            map1.put("order", 1);
            map1.put("active", true);
            map1.put("createdAt", System.currentTimeMillis());

            java.util.Map<String, Object> map2 = new java.util.HashMap<>();
            map2.put("id", "hl_ability_limits");
            map2.put("title", "Ability Has No Limits - Inclusion Is Everyone's Mission");
            map2.put("imageUrl", "assets/highlight_2.jpg");
            map2.put("actionUrl", "https://www.moh.gov.my");
            map2.put("order", 2);
            map2.put("active", true);
            map2.put("createdAt", System.currentTimeMillis());

            java.util.Map<String, Object> map3 = new java.util.HashMap<>();
            map3.put("id", "hl_different_abilities");
            map3.put("title", "Different Abilities One Community - Inclusion Today");
            map3.put("imageUrl", "assets/highlight_3.jpg");
            map3.put("actionUrl", "https://www.malaysia.gov.my");
            map3.put("order", 3);
            map3.put("active", true);
            map3.put("createdAt", System.currentTimeMillis());

            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("hl_people_first", map1);
            updates.put("hl_ability_limits", map2);
            updates.put("hl_different_abilities", map3);

            hlRef.updateChildren(updates);
        } catch (Exception ignored) {
        }
    }

    private void setupMedicalNews() {
        rvMedicalNews = findViewById(R.id.rv_medical_news);
        newsProgress = findViewById(R.id.news_progress);
        android.view.View btnSeeAll = findViewById(R.id.btn_see_all_news);

        if (rvMedicalNews != null) {
            rvMedicalNews.setLayoutManager(new LinearLayoutManager(this));
            medicalNewsAdapter = new MedicalNewsAdapter(this);
            rvMedicalNews.setAdapter(medicalNewsAdapter);
        }

        if (btnSeeAll != null) {
            btnSeeAll.setOnClickListener(v -> {
                Intent intent = new Intent(this, MedicalNewsActivity.class);
                startActivity(intent);
            });
        }

        loadMedicalNews();
    }

    private void loadMedicalNews() {
        if (newsProgress != null) newsProgress.setVisibility(android.view.View.VISIBLE);
        MedicalNewsFetcher.fetchMedicalNews(5, new MedicalNewsFetcher.NewsCallback() {
            @Override
            public void onSuccess(java.util.List<MedicalNewsItem> newsList) {
                if (newsProgress != null) newsProgress.setVisibility(android.view.View.GONE);
                if (medicalNewsAdapter != null) {
                    medicalNewsAdapter.submitList(newsList);
                }
            }

            @Override
            public void onError(Exception e) {
                if (newsProgress != null) newsProgress.setVisibility(android.view.View.GONE);
            }
        });
    }

    /** Handle intent baharu yang dihantar ke aktiviti. */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleWatchSosIntent(intent);
    }

    // =========================================================================
    // 1. ENTRANCE ANIMATIONS
    // =========================================================================
    /** Mainkan animasi slaid dan pudar semasa kali pertama membuka dashboard. */
    private void playEntranceAnimations() {
        if (entranceAnimationPlayed) return;
        entranceAnimationPlayed = true;

        android.view.View headerCard = findViewById(R.id.header_card);
        if (headerCard != null) {
            Animation headerAnim = AnimationUtils.loadAnimation(this, R.anim.item_fade_slide_up);
            headerAnim.setStartOffset(80);
            headerCard.startAnimation(headerAnim);
        }

        android.view.View sosHint = findViewById(R.id.sos_hint);
        MaterialButton btnSos2 = findViewById(R.id.btn_sos);
        if (btnSos2 != null) {
            btnSos2.setAlpha(0f);
            btnSos2.setScaleX(0.7f);
            btnSos2.setScaleY(0.7f);
            btnSos2.animate()
                    .alpha(1f).scaleX(1f).scaleY(1f)
                    .setDuration(600)
                    .setStartDelay(220)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
                    .start();
        }
        if (sosHint != null) {
            sosHint.setAlpha(0f);
            sosHint.animate().alpha(1f).setDuration(500).setStartDelay(600).start();
        }

        int[] cardIds = {R.id.feature_hospital, R.id.feature_contacts, R.id.feature_support, R.id.feature_language};
        long[] delays = {350, 430, 510, 590};
        for (int i = 0; i < cardIds.length; i++) {
            android.view.View card = findViewById(cardIds[i]);
            if (card == null) continue;
            card.setAlpha(0f);
            card.setTranslationY(70f);
            card.setScaleX(0.93f);
            card.setScaleY(0.93f);
            final int idx = i;
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(450)
                    .setStartDelay(delays[idx])
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        android.view.View featuresTitle = findViewById(R.id.features_title);
        if (featuresTitle != null) {
            featuresTitle.setAlpha(0f);
            featuresTitle.animate().alpha(1f).setDuration(400).setStartDelay(300).start();
        }
    }

    /** Aktiviti aktif dan sedia untuk interaksi pengguna. */
    @Override
    protected void onResume() {
        super.onResume();
        try {
            Wearable.getMessageClient(this).addListener(this);
            Wearable.getDataClient(this).addListener(this);
        } catch (Exception ignored) {
        }
        refreshProfileUi();
        refreshInboxBadge();
        try { BatteryOptimizationHelper.promptOnce(this); } catch (Exception ignored) {}
        ensureActiveRoomTracking();
        startSosRingPulse();
        if (WearSosBridge.consumePending(this)) {
            scheduleWatchSosTrigger();
        }
        if (pendingWatchSosTrigger) {
            scheduleWatchSosTrigger();
        }
    }

    /** Fungsi untuk onMessageReceived. */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent == null || !WearSosBridge.SOS_MESSAGE_PATH.equals(messageEvent.getPath())) return;
        runOnUiThread(this::scheduleWatchSosTrigger);
    }

    /** Fungsi untuk onDataChanged. */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (dataEvents == null) return;
        for (DataEvent event : dataEvents) {
            if (event == null || event.getType() != DataEvent.TYPE_CHANGED || event.getDataItem() == null) continue;
            android.net.Uri uri = event.getDataItem().getUri();
            if (uri != null && WearSosBridge.SOS_DATA_PATH.equals(uri.getPath())) {
                runOnUiThread(this::scheduleWatchSosTrigger);
                return;
            }
        }
    }

    /** Fungsi untuk handleWatchSosIntent. */
    private void handleWatchSosIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(WearSosBridge.triggerExtra(), false)) return;
        intent.removeExtra(WearSosBridge.triggerExtra());
        WearSosBridge.consumePending(this);
        scheduleWatchSosTrigger();
    }

    /** Fungsi untuk scheduleWatchSosTrigger. */
    private void scheduleWatchSosTrigger() {
        pendingWatchSosTrigger = true;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            pendingWatchSosTrigger = false;
            long now = android.os.SystemClock.elapsedRealtime();
            if (now - lastWatchSosTriggerAt < 1200L) return;
            lastWatchSosTriggerAt = now;
            triggerSosFlow();
        }, 350L);
    }

    /** Fungsi untuk triggerSosFlow. */
    private void triggerSosFlow() {
        if (sosSheet == null) {
            pendingWatchSosTrigger = true;
            return;
        }
        String code = String.valueOf(UserPrefs.getActiveRoomCode(MainActivity.this) == null ? "" : UserPrefs.getActiveRoomCode(MainActivity.this)).trim();
        if (code.isEmpty()) {
            showJoinOrCreateRoomDialog();
            return;
        }
        sosSheet.show();
    }

    /** Fungsi untuk ensureNotificationsPermission. */
    private void ensureNotificationsPermission() {
        if (android.os.Build.VERSION.SDK_INT < 33) return;
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) return;
            notifPerm.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk startAdminNotificationListener. */
    private void startAdminNotificationListener() {
        if (adminNotificationListener != null) return;
        adminNotificationRef = FirebaseDatabase
                .getInstance(FirebaseRoomClient.DATABASE_URL)
                .getReference("broadcastNotifications/current");
        adminNotificationListener = new ValueEventListener() {
            /** Callback apabila data Firebase Realtime Database berubah. */
    @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                Boolean activeValue = snapshot.child("active").getValue(Boolean.class);
                if (activeValue != null && !activeValue) return;

                String id = value(snapshot.child("id"));
                String message = value(snapshot.child("message"));
                if (id.isEmpty() || message.isEmpty()) return;

                if (!adminNotificationBaselineLoaded) {
                    adminNotificationBaselineLoaded = true;
                    String lastId = UserPrefs.getLastAdminNotificationId(MainActivity.this);
                    if (!id.equals(lastId)) {
                        UserPrefs.setLastAdminNotificationId(MainActivity.this, id);
                        UserPrefs.incrementUnreadNotifications(MainActivity.this);
                        refreshInboxBadge();
                    }
                    return;
                }

                String lastId = UserPrefs.getLastAdminNotificationId(MainActivity.this);
                if (id.equals(lastId)) {
                    refreshInboxBadge();
                    return;
                }

                String title = value(snapshot.child("title"));
                if (title.isEmpty()) title = "ResQTap";
                UserPrefs.setLastAdminNotificationId(MainActivity.this, id);
                UserPrefs.incrementUnreadNotifications(MainActivity.this);
                refreshInboxBadge();
                if (!hasNotificationPermission()) {
                    pendingAdminNotificationId = id;
                    pendingAdminNotificationTitle = title;
                    pendingAdminNotificationMessage = message;
                    ensureNotificationsPermission();
                    return;
                }

                postAdminNotification(id, title, message);
            }

            /** Callback sekiranya operasi Firebase dibatalkan atau gagal. */
    @Override
            public void onCancelled(DatabaseError error) {
                try {
                    android.util.Log.d("SOS_DEBUG", "Admin notification listener cancelled: " + error.getMessage());
                } catch (Exception ignored) {
                }
            }
        };
        adminNotificationRef.addValueEventListener(adminNotificationListener);
    }

    /** Fungsi untuk stopAdminNotificationListener. */
    private void stopAdminNotificationListener() {
        if (adminNotificationRef != null && adminNotificationListener != null) {
            adminNotificationRef.removeEventListener(adminNotificationListener);
        }
        adminNotificationRef = null;
        adminNotificationListener = null;
    }

    /** Fungsi untuk value. */
    private static String value(DataSnapshot snapshot) {
        Object raw = snapshot == null ? null : snapshot.getValue();
        return String.valueOf(raw == null ? "" : raw).trim();
    }

    /** Semak dan sahkan NotificationPermission. */
    private boolean hasNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT < 33) return true;
        try {
            return androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Fungsi untuk postAdminNotification. */
    private void postAdminNotification(String id, String title, String message) {
        if (id == null || id.trim().isEmpty() || message == null || message.trim().isEmpty()) return;
        NotificationHelper.showLocalNotification(this, title, message);
    }

    /** Fungsi untuk refreshInboxBadge. */
    private void refreshInboxBadge() {
        if (inboxUnreadDot != null) {
            boolean hasUnread = UserPrefs.getUnreadNotifications(this) > 0;
            inboxUnreadDot.setVisibility(hasUnread ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

    /** Paparkan JoinOrCreateRoomDialog. */
    private void showJoinOrCreateRoomDialog() {
        try {
            int pad = (int) (20 * getResources().getDisplayMetrics().density);

            android.widget.LinearLayout root = new android.widget.LinearLayout(this);
            root.setOrientation(android.widget.LinearLayout.VERTICAL);
            root.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            root.setPadding(pad, pad, pad, pad);

            android.widget.TextView tv = new android.widget.TextView(this);
            tv.setText(R.string.dialog_room_required_message);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextAlignment(android.view.View.TEXT_ALIGNMENT_CENTER);
            tv.setTextSize(14);
            tv.setTextColor(getColor(R.color.text_primary));
            root.addView(tv, new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            com.google.android.material.button.MaterialButton ok = new com.google.android.material.button.MaterialButton(this);
            ok.setText(android.R.string.ok);
            ok.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
            ok.setTextSize(15);
            ok.setTypeface(null, android.graphics.Typeface.BOLD);
            ok.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(this, R.color.filled_button_color)));
            ok.setCornerRadius((int) (22 * getResources().getDisplayMetrics().density));
            android.widget.LinearLayout.LayoutParams okLp = new android.widget.LinearLayout.LayoutParams(
                    (int) (130 * getResources().getDisplayMetrics().density),
                    (int) (48 * getResources().getDisplayMetrics().density)
            );
            okLp.topMargin = (int) (16 * getResources().getDisplayMetrics().density);
            okLp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            root.addView(ok, okLp);

            final androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                    .setView(root)
                    .create();
            ok.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk refreshProfileUi. */
    private void refreshProfileUi() {
        String name = UserPrefs.getName(this);
        String photoUri = UserPrefs.getPhotoUri(this);
        String photoB64 = UserPrefs.getPhotoB64(this);
        String bloodType = UserPrefs.getBloodType(this);
        String allergies = UserPrefs.getAllergies(this);
        String existingConditions = UserPrefs.getExistingConditions(this);
        if (existingConditions == null || existingConditions.trim().isEmpty()) {
            existingConditions = UserPrefs.getMedications(this);
        }
        boolean hideMedical = UserPrefs.isHideMedicalInfo(this);

        String shownName = (name == null || name.trim().isEmpty()) ? getString(R.string.profile_name_default) : name.trim();

        if (title != null) {
            title.setText(getString(R.string.home_greeting, shownName));
        }
        if (subtitle != null) {
            subtitle.setText(getString(R.string.home_welcome_back));
        }
        if (headerPhoto != null) {
            AvatarUtils.applyAvatar(headerPhoto, photoB64, photoUri, R.drawable.ic_avatar);
        }

        if (medicalCard != null) {
            medicalCard.setVisibility(hideMedical ? android.view.View.GONE : android.view.View.VISIBLE);
        }
        if (!hideMedical) {
            String bt = bloodType == null ? "" : bloodType.trim();
            if (bt.isEmpty()) bt = "-";
            if (medicalBlood != null) medicalBlood.setText(bt);

            String al = allergies == null ? "" : allergies.trim();
            if (medicalAllergies != null) {
                medicalAllergies.setText(al.isEmpty() ? getString(R.string.medical_allergies_none) : al);
            }

            String ex = existingConditions == null ? "" : existingConditions.trim();
            if (medicalConditions != null) {
                medicalConditions.setText(ex.isEmpty() ? getString(R.string.medical_conditions_none) : ex);
            }
        }
    }

    /** Aktiviti dijeda sementara. */
    @Override
    protected void onPause() {
        super.onPause();
        try {
            Wearable.getMessageClient(this).removeListener(this);
            Wearable.getDataClient(this).removeListener(this);
        } catch (Exception ignored) {
        }
        MaterialButton btnSos = findViewById(R.id.btn_sos);
        if (btnSos != null) stopSosPulse(btnSos);
        stopSosRingPulse();
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    protected void onDestroy() {
        stopAdminNotificationListener();
        super.onDestroy();
    }

    /** Fungsi untuk ensureActiveRoomTracking. */
    private void ensureActiveRoomTracking() {
        try {
            activeRoomCode = String.valueOf(UserPrefs.getActiveRoomCode(this) == null ? "" : UserPrefs.getActiveRoomCode(this)).trim();
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u == null) return;
            String uid = u.getUid();

            if (activeRoomCode.isEmpty()) {
                restoreActiveRoomIfMissing(uid);
                return;
            }
            try {
                android.util.Log.d("SOS_DEBUG", "Saved currentRoomId: " + activeRoomCode);
            } catch (Exception ignored) {
            }
            SosServiceStarter.start(this, activeRoomCode);
            try {
                android.util.Log.d("SOS_DEBUG", "LiveRoomTrackingService started");
            } catch (Exception ignored) {
            }
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk restoreActiveRoomIfMissing. */
    private void restoreActiveRoomIfMissing(String uid) {
        if (restoringActiveRoom) return;
        restoringActiveRoom = true;
        FirebaseRoomClient.fetchMostRecentUserRoomCodeQueued(uid, code -> {
            restoringActiveRoom = false;
            String c = String.valueOf(code == null ? "" : code).trim();
            if (c.isEmpty()) return;
            try {
                UserPrefs.setActiveRoomCode(MainActivity.this, c);
            } catch (Exception ignored) {
            }
            runOnUiThread(this::ensureActiveRoomTracking);
        });
    }

    /** Fungsi untuk startSosPulse. */
    private void startSosPulse(MaterialButton btn) {
        stopSosPulse(btn);
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.08f);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.08f);
        sosPulseAnimator = ObjectAnimator.ofPropertyValuesHolder(btn, sx, sy);
        sosPulseAnimator.setDuration(450);
        sosPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sosPulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        sosPulseAnimator.start();
    }

    /** Fungsi untuk stopSosPulse. */
    private void stopSosPulse(MaterialButton btn) {
        if (sosPulseAnimator != null) {
            sosPulseAnimator.cancel();
            sosPulseAnimator = null;
        }
        btn.setScaleX(1f);
        btn.setScaleY(1f);
    }

    /** Fungsi untuk startSosRingPulse. */
    private void startSosRingPulse() {
        android.view.View ringOuter = findViewById(R.id.sos_ring_outer);
        android.view.View ringMid = findViewById(R.id.sos_ring_mid);
        android.view.View ringGlow = findViewById(R.id.sos_ring_glow);

        if (ringOuter != null && sosRingOuterAnimator == null) {
            try {
                sosRingOuterAnimator = AnimatorInflater.loadAnimator(this, R.animator.sos_ring_pulse_outer);
                sosRingOuterAnimator.setTarget(ringOuter);
                sosRingOuterAnimator.start();
            } catch (Exception ignored) {

                ObjectAnimator scaleX = ObjectAnimator.ofFloat(ringOuter, "scaleX", 1f, 1.22f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(ringOuter, "scaleY", 1f, 1.22f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ringOuter, "alpha", 0.75f, 0.35f);
                for (ObjectAnimator a : new ObjectAnimator[]{scaleX, scaleY, alpha}) {
                    a.setDuration(1600);
                    a.setRepeatCount(ValueAnimator.INFINITE);
                    a.setRepeatMode(ValueAnimator.REVERSE);
                }
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY, alpha);
                set.start();
                sosRingOuterAnimator = set;
            }
        }

        if (ringMid != null && sosRingMidAnimator == null) {
            try {
                sosRingMidAnimator = AnimatorInflater.loadAnimator(this, R.animator.sos_ring_pulse_mid);
                sosRingMidAnimator.setTarget(ringMid);
                sosRingMidAnimator.start();
            } catch (Exception ignored) {

                ObjectAnimator scaleX = ObjectAnimator.ofFloat(ringMid, "scaleX", 1f, 1.12f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(ringMid, "scaleY", 1f, 1.12f);
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ringMid, "alpha", 0.85f, 0.45f);
                for (ObjectAnimator a : new ObjectAnimator[]{scaleX, scaleY, alpha}) {
                    a.setDuration(1600);
                    a.setRepeatCount(ValueAnimator.INFINITE);
                    a.setRepeatMode(ValueAnimator.REVERSE);
                    a.setStartDelay(200);
                }
                AnimatorSet set = new AnimatorSet();
                set.playTogether(scaleX, scaleY, alpha);
                set.start();
                sosRingMidAnimator = set;
            }
        }

        if (ringGlow != null && sosGlowAnimator == null) {
            PropertyValuesHolder gsX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.08f);
            PropertyValuesHolder gsY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.08f);
            PropertyValuesHolder gAlpha = PropertyValuesHolder.ofFloat("alpha", 0.35f, 0.18f);
            sosGlowAnimator = ObjectAnimator.ofPropertyValuesHolder(ringGlow, gsX, gsY, gAlpha);
            sosGlowAnimator.setDuration(2000);
            sosGlowAnimator.setRepeatCount(ValueAnimator.INFINITE);
            sosGlowAnimator.setRepeatMode(ValueAnimator.REVERSE);
            sosGlowAnimator.start();
        }
    }

    /** Fungsi untuk stopSosRingPulse. */
    private void stopSosRingPulse() {
        if (sosRingOuterAnimator != null) {
            sosRingOuterAnimator.cancel();
            sosRingOuterAnimator = null;
        }
        if (sosRingMidAnimator != null) {
            sosRingMidAnimator.cancel();
            sosRingMidAnimator = null;
        }
        if (sosGlowAnimator != null) {
            sosGlowAnimator.cancel();
            sosGlowAnimator = null;
        }

        android.view.View ringOuter = findViewById(R.id.sos_ring_outer);
        android.view.View ringMid = findViewById(R.id.sos_ring_mid);
        android.view.View ringGlow = findViewById(R.id.sos_ring_glow);
        if (ringOuter != null) { ringOuter.setScaleX(1f); ringOuter.setScaleY(1f); }
        if (ringMid != null) { ringMid.setScaleX(1f); ringMid.setScaleY(1f); }
        if (ringGlow != null) { ringGlow.setScaleX(1f); ringGlow.setScaleY(1f); }
    }
}

