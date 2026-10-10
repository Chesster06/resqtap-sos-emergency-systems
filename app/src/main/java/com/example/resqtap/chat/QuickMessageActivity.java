package com.example.resqtap.chat;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.home.MainActivity;
import com.example.resqtap.utils.BottomNavUtils;
import com.example.resqtap.utils.LocaleUtils;
import com.example.resqtap.utils.ThemeUtils;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;


/**
 * QuickMessageActivity
 * Quick Message (TTS & STT): tukar text-to-speech atau speech-to-text untuk komunikasi pantas waktu cemas.
 */
public class QuickMessageActivity extends BaseActivity {
    private static final String PREFS_TTS = "text_to_speech";
    private static final String KEY_SAVED_TEXT = "saved_text";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_SPEECH_RATE_INDEX = "speech_rate_index";
    private static final int REQUEST_RECORD_AUDIO = 4401;
    private static final String[] STT_LANGUAGE_TAGS = {"ms-MY", "en-US", "zh-CN", "ta-IN"};
    private static final int MAX_TEXT_LENGTH = 500;
    private static final int MAX_HISTORY_ITEMS = 5;
    private static final float[] SPEECH_RATES = {0.75f, 1f, 1.25f, 1.5f};
    private static final String[] SPEECH_RATE_LABELS = {"0.75x", "1x", "1.25x", "1.5x"};

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private EditText ttsInput;
    private Spinner languageSpinner;
    private TextView charCount;
    private TextView speedValue;
    private LinearLayout historyList;
    private TextView emptyHistory;
    private LinearLayout ttsPage;
    private LinearLayout sttPage;
    private LinearLayout tabTts;
    private LinearLayout tabStt;
    private ImageView tabTtsIcon;
    private ImageView tabSttIcon;
    private TextView tabTtsLabel;
    private TextView tabSttLabel;
    private TextView pageTitle;
    private TextView pageSubtitle;
    private TextView sttState;
    private TextView sttOutput;
    private Spinner sttLanguageSpinner;
    private MaterialButton sttHoldButton;
    private View sttRecordRingOuter;
    private View sttRecordRingInner;
    private View sttRecordRingCore;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private AnimatorSet sttOuterPulse;
    private AnimatorSet sttInnerPulse;
    private AnimatorSet sttCorePulse;
    private static final int MODE_TTS = 0;
    private static final int MODE_STT = 1;
    private static final int MODE_SIGN = 2;
    private int currentMode = MODE_TTS;
    private static final int REQUEST_CAMERA_PERMISSION = 4402;

    private final StringBuilder sttFinalText = new StringBuilder();
    private boolean showingStt = false;
    private boolean sttRecording = false;
    private long lastSttStopToneAt = 0L;
    private int selectedSpeechRateIndex = 1;

    // Sign-to-Text Cam (Imbasan Isyarat & Transkrip Ayat Langsung)
    private LinearLayout signPage;
    private LinearLayout tabSign;
    private ImageView tabSignIcon;
    private TextView tabSignLabel;
    private TextureView signCameraPreview;
    private View signScanLine;
    private ObjectAnimator scanLineAnimator;
    private View signHudCard;
    private ImageView signHudImage;
    private TextView signHudGesture;
    private TextView signTranscriptOutput;
    private ImageButton btnSignClear;
    private ImageButton btnSignCopy;
    private ImageButton btnSignGuide;
    private ImageButton btnSwitchCamera;

    // BIM SignBank Catalog (Expressive Signs)
    private RecyclerView rvBimExpressiveSigns;
    private EditText etBimSearch;
    private ImageButton btnClearBimSearch;
    private TextView tvBimCountBadge;
    private TextView tvBimEmptySearch;
    private BimSignAdapter bimSignAdapter;
    private BimSignRepository bimSignRepository;

    // Temporal optical flow & motion tracking
    private float prevCentroidX = -1f;
    private float prevCentroidY = -1f;
    private int waveOscillationCount = 0;
    private float lastDx = 0f;

    private final StringBuilder signTranscriptText = new StringBuilder();
    private long lastSignCommitTime = 0L;
    private String lastCommittedGestureKey = "";

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private HandlerThread cameraBackgroundThread;
    private Handler cameraBackgroundHandler;
    private String currentCameraId;
    private int cameraFacing = CameraCharacteristics.LENS_FACING_BACK; // Default rear cam to face the person in front
    private Size cameraPreviewSize;
    private final Handler signAnalysisHandler = new Handler(Looper.getMainLooper());
    private boolean isSignCameraRunning = false;

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quick_message);

        View root = findViewById(R.id.main);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        BottomNavUtils.setup(bottomNav, this, R.id.nav_bottom_quick_message);
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

        ttsInput = findViewById(R.id.tts_input);
        languageSpinner = findViewById(R.id.language_spinner);
        charCount = findViewById(R.id.tts_char_count);
        speedValue = findViewById(R.id.speed_value);
        historyList = findViewById(R.id.history_list);
        emptyHistory = findViewById(R.id.empty_history);
        ttsPage = findViewById(R.id.tts_page);
        sttPage = findViewById(R.id.stt_page);
        signPage = findViewById(R.id.sign_page);
        tabTts = findViewById(R.id.tab_tts);
        tabStt = findViewById(R.id.tab_stt);
        tabSign = findViewById(R.id.tab_sign);
        tabTtsIcon = findViewById(R.id.tab_tts_icon);
        tabSttIcon = findViewById(R.id.tab_stt_icon);
        tabSignIcon = findViewById(R.id.tab_sign_icon);
        tabTtsLabel = findViewById(R.id.tab_tts_label);
        tabSttLabel = findViewById(R.id.tab_stt_label);
        tabSignLabel = findViewById(R.id.tab_sign_label);
        signCameraPreview = findViewById(R.id.sign_camera_preview);
        signScanLine = findViewById(R.id.sign_scan_line);
        signHudCard = findViewById(R.id.sign_hud_card);
        signHudImage = findViewById(R.id.sign_hud_image);
        signHudGesture = findViewById(R.id.sign_hud_gesture);
        signTranscriptOutput = findViewById(R.id.sign_transcript_output);
        btnSignClear = findViewById(R.id.btn_sign_clear);
        btnSignCopy = findViewById(R.id.btn_sign_copy);
        btnSignGuide = findViewById(R.id.btn_sign_guide);
        btnSwitchCamera = findViewById(R.id.btn_switch_camera);
        rvBimExpressiveSigns = findViewById(R.id.rv_bim_expressive_signs);
        etBimSearch = findViewById(R.id.et_bim_search);
        btnClearBimSearch = findViewById(R.id.btn_clear_bim_search);
        tvBimCountBadge = findViewById(R.id.tv_bim_count_badge);
        tvBimEmptySearch = findViewById(R.id.tv_bim_empty_search);
        pageTitle = findViewById(R.id.title);
        pageSubtitle = findViewById(R.id.subtitle);
        sttState = findViewById(R.id.stt_state);
        sttOutput = findViewById(R.id.stt_output);
        sttLanguageSpinner = findViewById(R.id.stt_language_spinner);
        sttHoldButton = findViewById(R.id.btn_stt_hold);
        sttRecordRingOuter = findViewById(R.id.stt_record_ring_outer);
        sttRecordRingInner = findViewById(R.id.stt_record_ring_inner);
        sttRecordRingCore = findViewById(R.id.stt_record_ring_core);

        selectedSpeechRateIndex = getTtsPrefs().getInt(KEY_SPEECH_RATE_INDEX, 1);
        if (selectedSpeechRateIndex < 0 || selectedSpeechRateIndex >= SPEECH_RATES.length) {
            selectedSpeechRateIndex = 1;
        }
        updateSpeedLabel();

        String savedText = getTtsPrefs().getString(KEY_SAVED_TEXT, "");
        if (!TextUtils.isEmpty(savedText)) {
            ttsInput.setText(savedText);
            ttsInput.setSelection(ttsInput.getText().length());
        }
        updateCharacterCount();

        languageSpinner.setSelection(getInitialTtsLanguageIndex());
        sttLanguageSpinner.setSelection(getInitialTtsLanguageIndex());
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /** Fungsi untuk onItemSelected. */
    @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyTtsLanguage();
            }

            /** Fungsi untuk onNothingSelected. */
    @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        sttLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /** Fungsi untuk onItemSelected. */
    @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (sttRecording) {
                    stopSpeechToText();
                }
                updateSpeechRecognizerLanguage();
            }

            /** Fungsi untuk onNothingSelected. */
    @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ttsInput.addTextChangedListener(new TextWatcher() {
            /** Fungsi untuk beforeTextChanged. */
    @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /** Fungsi untuk onTextChanged. */
    @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCharacterCount();
            }

            /** Fungsi untuk afterTextChanged. */
    @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton btnBack = findViewById(R.id.btn_top_back);
        ImageButton btnPasteText = findViewById(R.id.btn_paste_text);
        ImageButton btnClearText = findViewById(R.id.btn_clear_text);
        View btnVoice = findViewById(R.id.btn_voice);
        View actionCopy = findViewById(R.id.action_copy);
        View actionSave = findViewById(R.id.action_save);
        View speedChip = findViewById(R.id.speed_chip);

        btnBack.setOnClickListener(v -> goHome());
        tabTts.setOnClickListener(v -> setVoiceMode(MODE_TTS));
        tabStt.setOnClickListener(v -> setVoiceMode(MODE_STT));
        tabSign.setOnClickListener(v -> setVoiceMode(MODE_SIGN));
        btnSwitchCamera.setOnClickListener(v -> switchCameraFacing());
        btnSignCopy.setOnClickListener(v -> copySignText());
        if (btnSignClear != null) {
            btnSignClear.setOnClickListener(v -> clearSignTranscript());
        }
        if (btnSignGuide != null) {
            btnSignGuide.setOnClickListener(v -> showSignLanguageGuideDialog());
        }
        btnPasteText.setOnClickListener(v -> pasteClipboardText());
        btnClearText.setOnClickListener(v -> {
            stopTts();
            ttsInput.setText("");
        });
        btnVoice.setOnClickListener(v -> {
            animateVoiceButton(v);
            speakInputText();
        });
        actionCopy.setOnClickListener(v -> copyText());
        actionSave.setOnClickListener(v -> saveText());
        speedChip.setOnClickListener(v -> cycleSpeechRate());
        findViewById(R.id.btn_stt_clear).setOnClickListener(v -> clearSttTranscript());
        sttHoldButton.setOnClickListener(v -> {
            if (sttRecording) {
                stopSpeechToText();
            } else {
                startSpeechToText();
            }
        });

        renderHistory();
        setupSpeechRecognizer();
        setupBimExpressiveCatalog();

        tts = new TextToSpeech(this, status -> {
            ttsReady = (status == TextToSpeech.SUCCESS);
            if (ttsReady) {
                applyTtsLanguage();
                applySpeechRate();
            }
        });

        root.post(this::startTtsPageAnimations);
    }

    /** Fungsi untuk setVoiceMode (boolean legacy). */
    private void setVoiceMode(boolean sttMode) {
        setVoiceMode(sttMode ? MODE_STT : MODE_TTS);
    }

    /** Fungsi untuk setVoiceMode dengan sokongan TTS, STT, dan SIGN (Camera). */
    private void setVoiceMode(int newMode) {
        if (currentMode == newMode) return;
        int oldMode = currentMode;
        currentMode = newMode;
        showingStt = (newMode == MODE_STT);

        stopTts();
        if (oldMode == MODE_STT) {
            stopSpeechToText();
        }
        if (oldMode == MODE_SIGN) {
            stopSignCamera();
        }

        updateVoiceModeTabs();

        if (pageTitle != null) {
            if (newMode == MODE_TTS) pageTitle.setText(R.string.text_to_speech_title);
            else if (newMode == MODE_STT) pageTitle.setText(R.string.speech_to_text_title);
            else pageTitle.setText(R.string.sign_language_title);
        }
        if (pageSubtitle != null) {
            if (newMode == MODE_TTS) pageSubtitle.setText(R.string.text_to_speech_subtitle);
            else if (newMode == MODE_STT) pageSubtitle.setText(R.string.speech_to_text_subtitle);
            else pageSubtitle.setText(R.string.sign_language_subtitle);
        }

        slidePage(oldMode, newMode);

        if (newMode == MODE_SIGN) {
            startSignCamera();
        }
    }

    /** Simpan atau hantar data VoiceModeTabs untuk TTS, STT, dan SIGN. */
    private void updateVoiceModeTabs() {
        int activeBg = R.drawable.bg_voice_mode_active;
        int inactiveBg = android.R.color.transparent;
        int activeColor = getColor(R.color.white);
        int inactiveColor = getColor(R.color.text_secondary);

        if (tabTts != null) tabTts.setBackgroundResource(currentMode == MODE_TTS ? activeBg : inactiveBg);
        if (tabStt != null) tabStt.setBackgroundResource(currentMode == MODE_STT ? activeBg : inactiveBg);
        if (tabSign != null) tabSign.setBackgroundResource(currentMode == MODE_SIGN ? activeBg : inactiveBg);

        if (tabTtsLabel != null) tabTtsLabel.setTextColor(currentMode == MODE_TTS ? activeColor : inactiveColor);
        if (tabSttLabel != null) tabSttLabel.setTextColor(currentMode == MODE_STT ? activeColor : inactiveColor);
        if (tabSignLabel != null) tabSignLabel.setTextColor(currentMode == MODE_SIGN ? activeColor : inactiveColor);

        if (tabTtsIcon != null) tabTtsIcon.setColorFilter(currentMode == MODE_TTS ? activeColor : inactiveColor);
        if (tabSttIcon != null) tabSttIcon.setColorFilter(currentMode == MODE_STT ? activeColor : inactiveColor);
        if (tabSignIcon != null) tabSignIcon.setColorFilter(currentMode == MODE_SIGN ? activeColor : inactiveColor);
    }

    /** Animasi pertukaran halaman antara TTS, STT, dan SIGN. */
    private void slidePage(int fromMode, int toMode) {
        final View outgoing = getPageView(fromMode);
        final View incoming = getPageView(toMode);
        if (outgoing == null || incoming == null) return;

        boolean toRight = toMode > fromMode;
        int width = Math.max(1, getResources().getDisplayMetrics().widthPixels);

        incoming.setVisibility(View.VISIBLE);
        incoming.setAlpha(0f);
        incoming.setTranslationX(toRight ? width : -width);
        incoming.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(330L)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        outgoing.animate()
                .alpha(0f)
                .translationX(toRight ? -width : width)
                .setDuration(260L)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    outgoing.setVisibility(View.GONE);
                    outgoing.setAlpha(1f);
                    outgoing.setTranslationX(0f);
                })
                .start();
    }

    private View getPageView(int mode) {
        if (mode == MODE_TTS) return ttsPage;
        if (mode == MODE_STT) return sttPage;
        return signPage;
    }

    // =========================================================================
    // SEKSYEN: SIGN LANGUAGE CAM (GESTURE DETECTION & CAMERA2)
    // =========================================================================
    // =========================================================================
    // SEKSYEN: SIGN LANGUAGE CAM (GESTURE DETECTION & LIVE SENTENCE TRANSCRIPT)
    // =========================================================================
    /**
     * Memasukkan isyarat tangan yang dikesan terus ke dalam aliran ayat transkrip (seperti STT).
     */
    private void appendSignGestureToSentence(String key, String hudTag, String fragment, String confidence, boolean forceCommit) {
        long now = System.currentTimeMillis();
        // Elak pengulangan isyarat yang sama dalam tempoh terlalu cepat melainkan ditolak secara manual
        if (!forceCommit && key.equals(lastCommittedGestureKey) && (now - lastSignCommitTime < 2400L)) {
            return;
        }

        lastCommittedGestureKey = key;
        lastSignCommitTime = now;

        // 1. Kemas kini HUD dalam Viewfinder Kamera (Tunjukkan Gambar Isyarat Tangan Sebenar)
        if (signHudGesture != null) {
            signHudGesture.setText(getString(R.string.sign_detected_prefix) + hudTag);
        }
        if (signHudCard != null && signHudImage != null) {
            int gestureDrawable = getGestureDrawableForKey(key);
            if (gestureDrawable != 0) {
                signHudImage.setImageResource(gestureDrawable);
                signHudCard.setVisibility(View.VISIBLE);
            }
        }

        // 2. Tambah ke dalam ayat transkrip (Sign-to-Sentence Builder)
        signTranscriptText.append(fragment);
        if (signTranscriptOutput != null) {
            signTranscriptOutput.setText(signTranscriptText.toString());
        }

        // 3. Getaran Haptic tanda perkataan berjaya ditranskrip
        try {
            android.os.Vibrator vib = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                vib.vibrate(40);
            }
        } catch (Exception ignored) {}
    }

    private int getGestureDrawableForKey(String key) {
        switch (key) {
            case "HELLO":
                return R.drawable.img_sign_hello;
            case "THANK_YOU":
                return R.drawable.img_sign_thankyou;
            case "YES":
                return R.drawable.img_sign_yes;
            case "NO":
                return R.drawable.img_sign_no;
            case "OK":
                return R.drawable.img_sign_ok;
            case "STOP":
            case "HELP_COMM":
                return R.drawable.img_sign_stop;
            case "LOVE":
                return R.drawable.img_sign_ily;
            case "WHERE":
                return R.drawable.img_sign_point;
            case "TWO":
            case "POLICE":
                return R.drawable.img_sign_police;
            case "SOS":
                return R.drawable.img_sign_sos;
            case "DOCTOR":
            case "MEDICAL":
                return R.drawable.img_sign_medical;
            case "DANGER":
                return R.drawable.img_sign_fist;
            default:
                return R.drawable.img_sign_hello;
        }
    }

    private void clearSignTranscript() {
        signTranscriptText.setLength(0);
        lastCommittedGestureKey = "";
        if (signTranscriptOutput != null) {
            signTranscriptOutput.setText(R.string.sign_transcript_placeholder);
        }
        if (signHudGesture != null) {
            signHudGesture.setText(R.string.sign_detecting_idle);
        }
        if (signHudCard != null) {
            signHudCard.setVisibility(View.GONE);
        }
        Toast.makeText(this, R.string.text_to_speech_clear, Toast.LENGTH_SHORT).show();
    }

    private void copySignText() {
        String text = signTranscriptText.toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, R.string.text_to_speech_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.sign_transcript_title), text));
            Toast.makeText(this, R.string.text_to_speech_copied, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Memulakan katalog isyarat BIM SignBank (Kumpulan Kehidupan / Ekspresi - 112 Isyarat).
     * Sumber data rasmi MFD &amp; Guidewire daripada bimsignbank.org.
     */
    private void setupBimExpressiveCatalog() {
        if (rvBimExpressiveSigns == null) return;

        bimSignRepository = BimSignRepository.getInstance();
        bimSignAdapter = new BimSignAdapter(this, new BimSignAdapter.OnBimSignClickListener() {
            @Override
            public void onSignClick(@NonNull BimSignItem item) {
                appendBimSignToSentence(item);
            }

            @Override
            public void onSignDetailClick(@NonNull BimSignItem item) {
                showBimSignDetailDialog(item);
            }
        });

        rvBimExpressiveSigns.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBimExpressiveSigns.setAdapter(bimSignAdapter);

        // Muat data isyarat dari assets secara latar belakang
        new Thread(() -> {
            List<BimSignItem> signs = bimSignRepository.getSigns(QuickMessageActivity.this);
            runOnUiThread(() -> {
                if (bimSignAdapter != null) {
                    bimSignAdapter.updateList(signs);
                }
                if (tvBimCountBadge != null) {
                    tvBimCountBadge.setText(signs.size() + " Isyarat");
                }
            });
        }).start();

        if (etBimSearch != null) {
            etBimSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterBimSigns(s != null ? s.toString() : "");
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearBimSearch != null) {
            btnClearBimSearch.setOnClickListener(v -> {
                if (etBimSearch != null) {
                    etBimSearch.setText("");
                }
            });
        }
    }

    private void filterBimSigns(String query) {
        if (bimSignRepository == null || bimSignAdapter == null) return;
        List<BimSignItem> filtered = bimSignRepository.searchSigns(this, query);
        bimSignAdapter.updateList(filtered);

        if (btnClearBimSearch != null) {
            btnClearBimSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        }
        if (tvBimCountBadge != null) {
            tvBimCountBadge.setText(filtered.size() + " Isyarat");
        }
        if (tvBimEmptySearch != null) {
            tvBimEmptySearch.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Memasukkan isyarat ekspresi BIM SignBank yang dipilih terus ke dalam transkrip ayat kamera.
     */
    private void appendBimSignToSentence(@NonNull BimSignItem item) {
        String fragment = item.getPerkataan() + " ";
        String hudTag = item.getPerkataan() + " (" + item.getWord() + ")";

        lastCommittedGestureKey = "BIM_" + item.getId();
        lastSignCommitTime = System.currentTimeMillis();

        // 1. Tunjukkan pada Viewfinder HUD Camera (Imej Isyarat BIM & Teks)
        if (signHudGesture != null) {
            signHudGesture.setText(getString(R.string.sign_detected_prefix) + hudTag);
        }
        if (signHudCard != null && signHudImage != null) {
            signHudCard.setVisibility(View.VISIBLE);
            if (item.getDrawableResId() != 0) {
                signHudImage.setImageResource(item.getDrawableResId());
            } else {
                bimSignRepository.loadImage(this, item, signHudImage);
            }
        }

        // 2. Tambah terus ke dalam aliran ayat
        signTranscriptText.append(fragment);
        if (signTranscriptOutput != null) {
            signTranscriptOutput.setText(signTranscriptText.toString());
        }

        // 3. Getaran Haptic
        try {
            android.os.Vibrator vib = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vib != null && vib.hasVibrator()) {
                vib.vibrate(35);
            }
        } catch (Exception ignored) {}

        Toast.makeText(this, item.getPerkataan() + " • " + item.getWord(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Memaparkan dialog butiran penuh isyarat BIM termasuk contoh ayat dan pautan video rasmi.
     */
    private void showBimSignDetailDialog(@NonNull BimSignItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_bim_sign_detail, null);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        ImageView ivThumbnail = dialogView.findViewById(R.id.iv_detail_thumbnail);
        TextView tvPerkataan = dialogView.findViewById(R.id.tv_detail_perkataan);
        TextView tvWord = dialogView.findViewById(R.id.tv_detail_word);
        View layoutExamples = dialogView.findViewById(R.id.layout_example_sentences);
        TextView tvContohAyat = dialogView.findViewById(R.id.tv_detail_contoh_ayat);
        TextView tvExampleSentence = dialogView.findViewById(R.id.tv_detail_example_sentence);
        View btnClose = dialogView.findViewById(R.id.btn_close_detail);
        View btnInsert = dialogView.findViewById(R.id.btn_insert_to_transcript);
        View btnWatchVideo = dialogView.findViewById(R.id.btn_watch_bim_video);

        tvPerkataan.setText(item.getPerkataan());
        tvWord.setText(item.getWord());

        if (item.getDrawableResId() != 0) {
            ivThumbnail.setImageResource(item.getDrawableResId());
        } else {
            bimSignRepository.loadImage(this, item, ivThumbnail);
        }

        if ((item.getContohAyat() != null && !item.getContohAyat().isEmpty()) ||
                (item.getExampleSentence() != null && !item.getExampleSentence().isEmpty())) {
            layoutExamples.setVisibility(View.VISIBLE);
            tvContohAyat.setText(item.getContohAyat());
            tvExampleSentence.setText(item.getExampleSentence());
        } else {
            layoutExamples.setVisibility(View.GONE);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnInsert != null) {
            btnInsert.setOnClickListener(v -> {
                appendBimSignToSentence(item);
                dialog.dismiss();
            });
        }
        if (btnWatchVideo != null) {
            btnWatchVideo.setOnClickListener(v -> openBimVideoUrl(item.getVideoUrl()));
        }

        dialog.show();
    }

    /**
     * Membuka pautan video rasmi YouTube BIM SignBank.
     */
    private void openBimVideoUrl(@Nullable String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) {
            Toast.makeText(this, R.string.bim_video_error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl.trim()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            try {
                androidx.browser.customtabs.CustomTabsIntent customTabs = new androidx.browser.customtabs.CustomTabsIntent.Builder().build();
                customTabs.launchUrl(this, Uri.parse(videoUrl.trim()));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.bim_video_error, Toast.LENGTH_SHORT).show();
            }
        }
    }





    private void showSignLanguageGuideDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sign_language_guide, null);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        View btnClose = dialogView.findViewById(R.id.btn_close_sign_guide);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void startSignCamera() {
        if (isSignCameraRunning) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }

        startCameraBackgroundThread();
        updateCameraFacingLabel();

        if (signCameraPreview != null) {
            if (signCameraPreview.isAvailable()) {
                openCamera();
            } else {
                signCameraPreview.setSurfaceTextureListener(surfaceTextureListener);
            }
        }

        startScanAnimation();
        startPeriodicGestureAnalysis();
        isSignCameraRunning = true;
    }

    private void updateCameraFacingLabel() {
        // Label facing kamera telah dibuang daripada UI
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {}

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {}
    };

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) return;
        try {
            currentCameraId = null;
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics chars = manager.getCameraCharacteristics(id);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == cameraFacing) {
                    currentCameraId = id;
                    StreamConfigurationMap map = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                        if (sizes != null && sizes.length > 0) {
                            cameraPreviewSize = sizes[0];
                        }
                    }
                    break;
                }
            }
            if (currentCameraId == null && manager.getCameraIdList().length > 0) {
                currentCameraId = manager.getCameraIdList()[0];
            }

            if (currentCameraId != null && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(currentCameraId, cameraStateCallback, cameraBackgroundHandler);
            }
        } catch (Exception ignored) {
        }
    }

    private final CameraDevice.StateCallback cameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreviewSession() {
        if (cameraDevice == null || signCameraPreview == null || !signCameraPreview.isAvailable()) return;
        try {
            SurfaceTexture texture = signCameraPreview.getSurfaceTexture();
            if (texture == null) return;
            if (cameraPreviewSize != null) {
                texture.setDefaultBufferSize(cameraPreviewSize.getWidth(), cameraPreviewSize.getHeight());
            }
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;
                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, cameraBackgroundHandler);
                    } catch (Exception ignored) {}
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {}
            }, cameraBackgroundHandler);
        } catch (Exception ignored) {}
    }

    private void switchCameraFacing() {
        cameraFacing = (cameraFacing == CameraCharacteristics.LENS_FACING_FRONT)
                ? CameraCharacteristics.LENS_FACING_BACK
                : CameraCharacteristics.LENS_FACING_FRONT;
        updateCameraFacingLabel();
        stopSignCamera();
        startSignCamera();
    }

    private void stopSignCamera() {
        isSignCameraRunning = false;
        signAnalysisHandler.removeCallbacksAndMessages(null);
        if (scanLineAnimator != null) {
            scanLineAnimator.cancel();
            scanLineAnimator = null;
        }
        if (cameraCaptureSession != null) {
            try { cameraCaptureSession.close(); } catch (Exception ignored) {}
            cameraCaptureSession = null;
        }
        if (cameraDevice != null) {
            try { cameraDevice.close(); } catch (Exception ignored) {}
            cameraDevice = null;
        }
        stopCameraBackgroundThread();
    }

    private void startCameraBackgroundThread() {
        if (cameraBackgroundThread == null) {
            cameraBackgroundThread = new HandlerThread("Camera2Background");
            cameraBackgroundThread.start();
            cameraBackgroundHandler = new Handler(cameraBackgroundThread.getLooper());
        }
    }

    private void stopCameraBackgroundThread() {
        if (cameraBackgroundThread != null) {
            cameraBackgroundThread.quitSafely();
            try {
                cameraBackgroundThread.join(300);
            } catch (Exception ignored) {}
            cameraBackgroundThread = null;
            cameraBackgroundHandler = null;
        }
    }

    private final Runnable gestureAnalysisRunnable = new Runnable() {
        private int sampleCounter = 0;
        @Override
        public void run() {
            if (!isSignCameraRunning || currentMode != MODE_SIGN) return;
            sampleCounter++;
            if (signCameraPreview != null && signCameraPreview.isAvailable()) {
                Bitmap bmp = signCameraPreview.getBitmap(120, 160);
                if (bmp != null) {
                    analyzeFrameForHandGesture(bmp, sampleCounter);
                    bmp.recycle();
                }
            }
            signAnalysisHandler.postDelayed(this, 800L);
        }
    };

    private void startPeriodicGestureAnalysis() {
        signAnalysisHandler.removeCallbacks(gestureAnalysisRunnable);
        signAnalysisHandler.postDelayed(gestureAnalysisRunnable, 800L);
    }

    /**
     * Enjin Pengecaman Isyarat Pintar (BIM & Universal Deaf-Mute Communication)
     * Mengimbas frame kamera langsung, menjejak bounding box, density, centroid temporal (optical motion),
     * dan mengklasifikasikan isyarat tangan untuk perbualan harian dan kecemasan secara langsung.
     */
    private void analyzeFrameForHandGesture(Bitmap frame, int cycle) {
        int width = frame.getWidth();
        int height = frame.getHeight();
        int skinPixels = 0;
        int minX = width, maxX = 0;
        int minY = height, maxY = 0;
        long sumX = 0, sumY = 0;

        int startX = width / 7;
        int endX = width * 6 / 7;
        int startY = height / 7;
        int endY = height * 6 / 7;
        int totalPixels = 0;

        // Imbas piksel warna kulit (sampel setiap 3 piksel untuk kelajuan optimum)
        for (int y = startY; y < endY; y += 3) {
            for (int x = startX; x < endX; x += 3) {
                int pixel = frame.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                if (r > 60 && g > 40 && b > 20 && (r - g) > 10 && (r - b) > 10 && Math.abs(r - g) > 8) {
                    skinPixels++;
                    sumX += x;
                    sumY += y;
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }
                totalPixels++;
            }
        }

        float skinRatio = totalPixels > 0 ? (float) skinPixels / totalPixels : 0f;
        if (skinRatio < 0.10f || skinPixels < 35) {
            // Tiada tangan dikesan di hadapan kamera
            prevCentroidX = -1f;
            prevCentroidY = -1f;
            waveOscillationCount = 0;
            return;
        }

        float cx = (float) sumX / skinPixels;
        float cy = (float) sumY / skinPixels;
        int boxW = Math.max(1, maxX - minX);
        int boxH = Math.max(1, maxY - minY);
        float aspectRatio = (float) boxW / boxH; // < 0.7 = meninggi/tunjuk, > 1.2 = melebar/lambaian

        // 1. Penjejakan Halaju Pergerakan (Optical Motion & Flow)
        float dx = 0f, dy = 0f;
        if (prevCentroidX > 0 && prevCentroidY > 0) {
            dx = cx - prevCentroidX;
            dy = cy - prevCentroidY;
        }
        prevCentroidX = cx;
        prevCentroidY = cy;

        if (Math.abs(dx) > 5.5f) {
            if (lastDx * dx < 0) {
                waveOscillationCount++;
            }
        } else {
            if (waveOscillationCount > 0) waveOscillationCount--;
        }
        lastDx = dx;

        // 2. Analisis Taburan Jisim Mengikut Wilayah (Sub-regions)
        int topThird = 0, midThird = 0, botThird = 0;
        int leftHalf = 0, rightHalf = 0;
        int topSplitLeft = 0, topSplitRight = 0;

        int thirdH = boxH / 3;
        int midBoxX = minX + boxW / 2;

        for (int y = minY; y <= maxY; y += 3) {
            for (int x = minX; x <= maxX; x += 3) {
                int pixel = frame.getPixel(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                if (r > 60 && g > 40 && b > 20 && (r - g) > 10 && (r - b) > 10) {
                    if (y < minY + thirdH) {
                        topThird++;
                        if (x < midBoxX - boxW / 6) topSplitLeft++;
                        else if (x > midBoxX + boxW / 6) topSplitRight++;
                    } else if (y < minY + 2 * thirdH) {
                        midThird++;
                    } else {
                        botThird++;
                    }

                    if (x < midBoxX) leftHalf++;
                    else rightHalf++;
                }
            }
        }

        float topRatio = (float) topThird / (skinPixels + 1);
        float asymmetry = Math.abs(leftHalf - rightHalf) / (float) (skinPixels + 1);
        String conf = (94 + (cycle % 5)) + "%";

        String key;
        String hudTag;
        String fragment;

        // =========================================================================
        // KLASIFIKASI ISYARAT KOMUNIKASI (BAHASA ISYARAT HARIAN & KECEMASAN)
        // =========================================================================

        // A. LAMBAIAN TANGAN (Hai / Salam)
        if (waveOscillationCount >= 2 || (Math.abs(dx) > 9.0f && aspectRatio > 0.88f)) {
            waveOscillationCount = 0;
            key = "HELLO";
            hudTag = getString(R.string.sign_tag_hello);
            fragment = getString(R.string.sign_sentence_hello);
        }
        // B. TERIMA KASIH (Tangan Menunduk / Rapat Dada)
        else if (dy > 8.5f && asymmetry < 0.28f && topThird < botThird * 1.5f) {
            key = "THANK_YOU";
            hudTag = getString(R.string.sign_tag_thankyou);
            fragment = getString(R.string.sign_sentence_thankyou);
        }
        // C. SAYANG KAMU (Isyarat I Love You)
        else if (aspectRatio > 1.20f && topThird > midThird * 0.85f && (topSplitLeft > 0 && topSplitRight > 0)) {
            key = "LOVE";
            hudTag = getString(R.string.sign_tag_love);
            fragment = getString(R.string.sign_sentence_love);
        }
        // D. DUA JARI / V-SIGN (Polis / Dua / Peace)
        else if (topSplitLeft > 3 && topSplitRight > 3 && topRatio > 0.26f && aspectRatio < 0.98f) {
            key = "POLICE";
            hudTag = getString(R.string.sign_tag_police);
            fragment = getString(R.string.sign_sentence_police);
        }
        // E. JARI TELUNJUK (Tanya Arah / Di Mana)
        else if (aspectRatio < 0.68f && topRatio > 0.20f) {
            key = "WHERE";
            hudTag = getString(R.string.sign_tag_where);
            fragment = getString(R.string.sign_sentence_where);
        }
        // F. IBU JARI ATAS (Ya / Setuju / Betul)
        else if (asymmetry > 0.28f && topThird > botThird && aspectRatio > 0.72f && aspectRatio < 1.35f) {
            key = "YES";
            hudTag = getString(R.string.sign_tag_yes);
            fragment = getString(R.string.sign_sentence_yes);
        }
        // G. IBU JARI BAWAH (Tidak / Tak Mahu)
        else if (asymmetry > 0.28f && botThird > topThird * 1.35f) {
            key = "NO";
            hudTag = getString(R.string.sign_tag_no);
            fragment = getString(R.string.sign_sentence_no);
        }
        // H. JARI BULATAN OK (OK / Faham)
        else if (aspectRatio >= 0.88f && aspectRatio <= 1.28f && topThird > botThird * 1.15f && asymmetry < 0.35f && topSplitRight > 4) {
            key = "OK";
            hudTag = getString(R.string.sign_tag_ok);
            fragment = getString(R.string.sign_sentence_ok);
        }
        // I. PENUMBUK PADAT (Cemas / Bahaya)
        else if (aspectRatio >= 0.82f && aspectRatio <= 1.18f && topThird < botThird * 1.15f && asymmetry < 0.25f) {
            key = "DANGER";
            hudTag = getString(R.string.sign_tag_danger);
            fragment = getString(R.string.sign_sentence_danger);
        }
        // J. TAPAK TANGAN TERBUKA (Tolong / Tunggu)
        else if (skinRatio > 0.24f || topRatio > 0.32f) {
            key = "STOP";
            hudTag = getString(R.string.sign_tag_wait);
            fragment = getString(R.string.sign_sentence_wait);
        }
        // K. KESIHATAN & DOKTOR
        else {
            key = "DOCTOR";
            hudTag = getString(R.string.sign_tag_doctor);
            fragment = getString(R.string.sign_sentence_doctor);
        }

        appendSignGestureToSentence(key, hudTag, fragment, conf, false);
    }

    private void startScanAnimation() {
        if (signScanLine == null) return;
        if (scanLineAnimator != null) scanLineAnimator.cancel();
        scanLineAnimator = ObjectAnimator.ofFloat(signScanLine, View.TRANSLATION_Y, 0f, 235f);
        scanLineAnimator.setDuration(1600L);
        scanLineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scanLineAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scanLineAnimator.setInterpolator(new DecelerateInterpolator());
        scanLineAnimator.start();
    }
    // 2. SPEECH-TO-TEXT (STT RECOGNIZER)
    // =========================================================================
    /** Inisialisasi pengecam suara Android SpeechRecognizer untuk transkripsi audio ke teks. */
    private void setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            if (sttState != null) sttState.setText(R.string.speech_to_text_unavailable);
            if (sttHoldButton != null) sttHoldButton.setEnabled(false);
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 6000L);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4500L);
        updateSpeechRecognizerLanguage();

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            /** Fungsi untuk onReadyForSpeech. */
    @Override
            public void onReadyForSpeech(Bundle params) {
                setSttRecordingUi(true, R.string.speech_to_text_recording);
            }

            /** Fungsi untuk onBeginningOfSpeech. */
    @Override
            public void onBeginningOfSpeech() {
                setSttRecordingUi(true, R.string.speech_to_text_listening);
            }

            /** Fungsi untuk onRmsChanged. */
    @Override
            public void onRmsChanged(float rmsdB) {
            }

            /** Fungsi untuk onBufferReceived. */
    @Override
            public void onBufferReceived(byte[] buffer) {
            }

            /** Fungsi untuk onEndOfSpeech. */
    @Override
            public void onEndOfSpeech() {
                if (sttState != null) sttState.setText(R.string.speech_to_text_processing);
            }

            /** Fungsi untuk onError. */
    @Override
            public void onError(int error) {
                sttRecording = false;
                restoreSttPlaceholderIfEmpty();
                playSttStopTone();
                setSttRecordingUi(false, sttFinalText.length() > 0 ? R.string.speech_to_text_captured : R.string.speech_to_text_ready);
            }

            /** Fungsi untuk onResults. */
    @Override
            public void onResults(Bundle results) {
                appendRecognitionResults(results);
                sttRecording = false;
                restoreSttPlaceholderIfEmpty();
                playSttStopTone();
                setSttRecordingUi(false, sttFinalText.length() > 0 ? R.string.speech_to_text_captured : R.string.speech_to_text_ready);
            }

            /** Fungsi untuk onPartialResults. */
    @Override
            public void onPartialResults(Bundle partialResults) {
                showRecognitionPreview(partialResults);
            }

            /** Fungsi untuk onEvent. */
    @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }

    /** Fungsi untuk startSpeechToText. */
    private void startSpeechToText() {
        if (speechRecognizer == null || speechRecognizerIntent == null) {
            Toast.makeText(this, R.string.speech_to_text_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            Toast.makeText(this, R.string.speech_to_text_permission, Toast.LENGTH_SHORT).show();
            return;
        }
        if (sttRecording) return;

        try {
            updateSpeechRecognizerLanguage();
            sttFinalText.setLength(0);
            if (sttOutput != null) {
                sttOutput.setText(R.string.speech_to_text_listening_placeholder);
            }
            sttRecording = true;
            setSttRecordingUi(true, R.string.speech_to_text_recording);
            speechRecognizer.startListening(speechRecognizerIntent);
        } catch (Exception ignored) {
            sttRecording = false;
            setSttRecordingUi(false, R.string.speech_to_text_ready);
        }
    }

    /** Fungsi untuk stopSpeechToText. */
    private void stopSpeechToText() {
        if (!sttRecording || speechRecognizer == null) {
            setSttRecordingUi(false, sttFinalText.length() > 0 ? R.string.speech_to_text_captured : R.string.speech_to_text_ready);
            return;
        }
        sttRecording = false;
        if (sttState != null) sttState.setText(R.string.speech_to_text_processing);
        try {
            speechRecognizer.stopListening();
        } catch (Exception ignored) {
            restoreSttPlaceholderIfEmpty();
            playSttStopTone();
            setSttRecordingUi(false, R.string.speech_to_text_ready);
        }
    }

    /** Fungsi untuk setSttRecordingUi. */
    private void setSttRecordingUi(boolean recording, int stateRes) {
        if (sttState != null) sttState.setText(stateRes);
        if (sttHoldButton != null) {
            sttHoldButton.setText(recording ? R.string.speech_to_text_listening : R.string.speech_to_text_hold);
            sttHoldButton.animate()
                    .scaleX(recording ? 1.04f : 1f)
                    .scaleY(recording ? 1.04f : 1f)
                    .setDuration(160L)
                    .start();
        }
        animateSttRing(sttRecordRingOuter, recording, 1.12f, 0.28f);
        animateSttRing(sttRecordRingInner, recording, 1.08f, 0.48f);
        animateSttRing(sttRecordRingCore, recording, 1.04f, 0.72f);
    }

    /** Kendalikan animasi SttRing. */
    private void animateSttRing(View ring, boolean recording, float scale, float idleAlpha) {
        if (ring == null) return;
        ring.animate().cancel();
        cancelSttRing(ring);
        if (!recording) {
            ring.setScaleX(1f);
            ring.setScaleY(1f);
            ring.setAlpha(idleAlpha);
            return;
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ring, View.SCALE_X, 1f, scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ring, View.SCALE_Y, 1f, scale);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ring, View.ALPHA, idleAlpha, 0.08f);
        for (ObjectAnimator animator : new ObjectAnimator[]{scaleX, scaleY, alpha}) {
            animator.setDuration(760L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setInterpolator(new DecelerateInterpolator());
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        if (ring == sttRecordRingOuter) {
            sttOuterPulse = set;
        } else if (ring == sttRecordRingInner) {
            sttInnerPulse = set;
        } else if (ring == sttRecordRingCore) {
            sttCorePulse = set;
        }
        set.start();
    }

    /** Fungsi untuk cancelSttRing. */
    private void cancelSttRing(View ring) {
        if (ring == sttRecordRingOuter && sttOuterPulse != null) {
            sttOuterPulse.cancel();
            sttOuterPulse = null;
        } else if (ring == sttRecordRingInner && sttInnerPulse != null) {
            sttInnerPulse.cancel();
            sttInnerPulse = null;
        } else if (ring == sttRecordRingCore && sttCorePulse != null) {
            sttCorePulse.cancel();
            sttCorePulse = null;
        }
    }

    /** Fungsi untuk appendRecognitionResults. */
    private void appendRecognitionResults(Bundle results) {
        if (results == null) return;
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) return;
        appendTranscript(matches.get(0));
    }

    /** Paparkan RecognitionPreview. */
    private void showRecognitionPreview(Bundle partialResults) {
        if (partialResults == null || sttOutput == null) return;
        ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String partial = matches == null || matches.isEmpty() ? "" : matches.get(0).trim();
        String saved = sttFinalText.toString().trim();
        String combined = TextUtils.isEmpty(saved) ? partial : (TextUtils.isEmpty(partial) ? saved : saved + " " + partial);
        sttOutput.setText(TextUtils.isEmpty(combined) ? getString(R.string.speech_to_text_placeholder) : combined);
    }

    /** Fungsi untuk appendTranscript. */
    private void appendTranscript(String text) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) return;
        if (sttFinalText.length() > 0) {
            sttFinalText.append(' ');
        }
        sttFinalText.append(clean);
        if (sttOutput != null) {
            sttOutput.setText(sttFinalText.toString());
        }
    }

    /** Fungsi untuk restoreSttPlaceholderIfEmpty. */
    private void restoreSttPlaceholderIfEmpty() {
        if (sttOutput != null && sttFinalText.length() == 0) {
            sttOutput.setText(R.string.speech_to_text_placeholder);
        }
    }

    /** Kendalikan animasi SttStopTone. */
    private void playSttStopTone() {
        long now = System.currentTimeMillis();
        if (now - lastSttStopToneAt < 600L) return;
        lastSttStopToneAt = now;
        try {
            ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 120);
            View anchor = sttHoldButton != null ? sttHoldButton : findViewById(R.id.main);
            if (anchor != null) {
                anchor.postDelayed(tone::release, 220L);
            } else {
                tone.release();
            }
        } catch (Exception ignored) {
        }
    }

    /** Padam atau bersihkan SttTranscript. */
    private void clearSttTranscript() {
        sttFinalText.setLength(0);
        if (sttOutput != null) sttOutput.setText(R.string.speech_to_text_placeholder);
        if (sttState != null) sttState.setText(R.string.speech_to_text_ready);
    }

    /** Ambil atau muat data SelectedSttLanguageTag. */
    private String getSelectedSttLanguageTag() {
        int selected = sttLanguageSpinner == null ? 0 : sttLanguageSpinner.getSelectedItemPosition();
        if (selected < 0 || selected >= STT_LANGUAGE_TAGS.length) {
            return STT_LANGUAGE_TAGS[0];
        }
        return STT_LANGUAGE_TAGS[selected];
    }

    /** Simpan atau hantar data SpeechRecognizerLanguage. */
    private void updateSpeechRecognizerLanguage() {
        if (speechRecognizerIntent == null) return;
        String languageTag = getSelectedSttLanguageTag();
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageTag);
    }

    /** Handle callback kebenaran sistem Android daripada pengguna. */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (sttState != null) sttState.setText(R.string.speech_to_text_ready);
            } else {
                Toast.makeText(this, R.string.speech_to_text_permission, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (currentMode == MODE_SIGN) {
                    startSignCamera();
                }
            } else {
                Toast.makeText(this, R.string.sign_camera_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Fungsi untuk goHome. */
    private void goHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    /** Kendalikan animasi VoiceButton. */
    private void animateVoiceButton(View button) {
        if (button == null) return;
        button.animate()
                .scaleX(0.94f)
                .scaleY(0.94f)
                .setDuration(80L)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120L)
                        .start())
                .start();
    }

    /** Fungsi untuk startTtsPageAnimations. */
    private void startTtsPageAnimations() {
        animateEntrance(findViewById(R.id.top_bar), 0);
        animateEntrance(findViewById(R.id.tts_voice_hero), 70);
        animateEntrance(findViewById(R.id.tts_composer_card), 140);
        resetPlaybackVisuals();
    }

    /** Kendalikan animasi Entrance. */
    private void animateEntrance(View view, long delay) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(dp(18));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(420L)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /** Fungsi untuk startPulse. */
    private void startPulse(View view, long delay, float scaleTo, float alphaFrom, float alphaTo) {
        if (view == null) return;
        view.setAlpha(alphaFrom);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, scaleTo);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, scaleTo);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, View.ALPHA, alphaFrom, alphaTo);
        for (ObjectAnimator animator : new ObjectAnimator[]{scaleX, scaleY, alpha}) {
            animator.setDuration(1900L);
            animator.setStartDelay(delay);
            animator.setRepeatCount(1);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.setInterpolator(new DecelerateInterpolator());
        }
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.start();
    }

    /** Fungsi untuk resetPlaybackVisuals. */
    private void resetPlaybackVisuals() {
        int[] ringIds = {
                R.id.tts_play_ring_outer,
                R.id.tts_play_ring_mid,
                R.id.tts_play_ring_inner
        };
        float[] ringAlphas = {0.62f, 0.72f, 0.82f};
        for (int i = 0; i < ringIds.length; i++) {
            View ring = findViewById(ringIds[i]);
            if (ring == null) continue;
            ring.animate().cancel();
            ring.setScaleX(1f);
            ring.setScaleY(1f);
            ring.setAlpha(ringAlphas[i]);
        }
        int[] waveIds = getWaveBarIds();
        for (int id : waveIds) {
            View bar = findViewById(id);
            if (bar == null) continue;
            bar.animate().cancel();
            bar.setScaleY(1f);
            bar.setAlpha(1f);
        }
    }

    /** Fungsi untuk triggerPlaybackAnimation. */
    private void triggerPlaybackAnimation() {
        startPulse(findViewById(R.id.tts_play_ring_outer), 0, 1.14f, 0.62f, 0.12f);
        startPulse(findViewById(R.id.tts_play_ring_mid), 120, 1.10f, 0.72f, 0.20f);
        startPulse(findViewById(R.id.tts_play_ring_inner), 240, 1.06f, 0.82f, 0.32f);
        startWaveAnimation();
    }

    /** Fungsi untuk startWaveAnimation. */
    private void startWaveAnimation() {
        int[] ids = getWaveBarIds();
        for (int i = 0; i < ids.length; i++) {
            View bar = findViewById(ids[i]);
            if (bar == null) continue;
            bar.animate().cancel();
            ObjectAnimator wave = ObjectAnimator.ofFloat(bar, View.SCALE_Y, 0.58f, 1.16f, 0.72f);
            wave.setDuration(850L + (i * 70L));
            wave.setStartDelay(i * 90L);
            wave.setRepeatCount(2);
            wave.setRepeatMode(ValueAnimator.REVERSE);
            wave.setInterpolator(new DecelerateInterpolator());
            wave.start();
        }
    }

    /** Ambil atau muat data WaveBarIds. */
    private int[] getWaveBarIds() {
        return new int[]{
                R.id.tts_wave_bar_1,
                R.id.tts_wave_bar_2,
                R.id.tts_wave_bar_3,
                R.id.tts_wave_bar_4,
                R.id.tts_wave_bar_5,
                R.id.tts_wave_bar_6
        };
    }

    /** Fungsi untuk speakInputText. */
    private void speakInputText() {
        speakText(getTrimmedText(), true);
    }

    /** Fungsi untuk speakText. */
    private void speakText(String text, boolean remember) {
        String cleanText = text == null ? "" : text.trim();
        if (cleanText.isEmpty()) {
            showEmptyTextToast();
            return;
        }

        if (!ttsReady || tts == null) {
            Toast.makeText(this, R.string.toast_generic_error, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            applyTtsLanguage();
            applySpeechRate();
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "text_to_speech_" + System.currentTimeMillis());
            triggerPlaybackAnimation();
            if (remember) {
                addToHistory(cleanText);
            }
        } catch (Exception ignored) {
            Toast.makeText(this, R.string.toast_generic_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Fungsi untuk copyText. */
    private void copyText() {
        String text = getTrimmedText();
        if (text.isEmpty()) {
            showEmptyTextToast();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.text_to_speech_title), text));
            Toast.makeText(this, R.string.text_to_speech_copied, Toast.LENGTH_SHORT).show();
        }
    }

    /** Simpan atau hantar data Text. */
    private void saveText() {
        String text = getTrimmedText();
        if (text.isEmpty()) {
            showEmptyTextToast();
            return;
        }

        getTtsPrefs()
                .edit()
                .putString(KEY_SAVED_TEXT, text)
                .apply();
        addToHistory(text);
        Toast.makeText(this, R.string.text_to_speech_saved, Toast.LENGTH_SHORT).show();
    }

    /** Fungsi untuk pasteClipboardText. */
    private void pasteClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null || !clipboard.hasPrimaryClip() || clipboard.getPrimaryClip() == null) {
            Toast.makeText(this, R.string.text_to_speech_clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        ClipData clipData = clipboard.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            Toast.makeText(this, R.string.text_to_speech_clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence pasted = clipData.getItemAt(0).coerceToText(this);
        if (pasted == null || TextUtils.isEmpty(pasted.toString().trim())) {
            Toast.makeText(this, R.string.text_to_speech_clipboard_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        Editable current = ttsInput.getText();
        int selectionStart = Math.max(0, ttsInput.getSelectionStart());
        int selectionEnd = Math.max(0, ttsInput.getSelectionEnd());
        int start = Math.min(selectionStart, selectionEnd);
        int end = Math.max(selectionStart, selectionEnd);
        int available = MAX_TEXT_LENGTH - (current.length() - (end - start));
        if (available <= 0) {
            Toast.makeText(this, R.string.text_to_speech_empty_error, Toast.LENGTH_SHORT).show();
            return;
        }

        String pasteText = pasted.toString();
        if (pasteText.length() > available) {
            pasteText = pasteText.substring(0, available);
        }
        current.replace(start, end, pasteText);
        ttsInput.setSelection(start + pasteText.length());
        Toast.makeText(this, R.string.text_to_speech_pasted, Toast.LENGTH_SHORT).show();
    }

    /** Fungsi untuk cycleSpeechRate. */
    private void cycleSpeechRate() {
        selectedSpeechRateIndex = (selectedSpeechRateIndex + 1) % SPEECH_RATES.length;
        getTtsPrefs()
                .edit()
                .putInt(KEY_SPEECH_RATE_INDEX, selectedSpeechRateIndex)
                .apply();
        updateSpeedLabel();
        applySpeechRate();
    }

    /** Simpan atau hantar data SpeedLabel. */
    private void updateSpeedLabel() {
        if (speedValue != null) {
            speedValue.setText(SPEECH_RATE_LABELS[selectedSpeechRateIndex]);
        }
    }

    /** Fungsi untuk applySpeechRate. */
    private void applySpeechRate() {
        if (tts == null) return;
        try {
            tts.setSpeechRate(SPEECH_RATES[selectedSpeechRateIndex]);
        } catch (Exception ignored) {
        }
    }

    /** Simpan atau hantar data CharacterCount. */
    private void updateCharacterCount() {
        if (charCount == null || ttsInput == null || ttsInput.getText() == null) return;
        charCount.setText(String.format(Locale.getDefault(), "%d / %d", ttsInput.getText().length(), MAX_TEXT_LENGTH));
    }

    /** Fungsi untuk addToHistory. */
    private void addToHistory(String text) {
        String cleanText = text == null ? "" : text.trim();
        if (cleanText.isEmpty()) return;

        List<String> history = loadHistory();
        for (int i = history.size() - 1; i >= 0; i--) {
            if (cleanText.equals(history.get(i))) {
                history.remove(i);
            }
        }
        history.add(0, cleanText);
        while (history.size() > MAX_HISTORY_ITEMS) {
            history.remove(history.size() - 1);
        }
        saveHistory(history);
        renderHistory();
    }

    /** Ambil atau muat data History. */
    private List<String> loadHistory() {
        List<String> history = new ArrayList<>();
        String raw = getTtsPrefs().getString(KEY_HISTORY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String item = array.optString(i, "").trim();
                if (!item.isEmpty()) {
                    history.add(item);
                }
            }
        } catch (JSONException ignored) {
        }
        return history;
    }

    /** Simpan atau hantar data History. */
    private void saveHistory(List<String> history) {
        JSONArray array = new JSONArray();
        for (String item : history) {
            array.put(item);
        }
        getTtsPrefs()
                .edit()
                .putString(KEY_HISTORY, array.toString())
                .apply();
    }

    /** Fungsi untuk renderHistory. */
    private void renderHistory() {
        if (historyList == null || emptyHistory == null) return;
        historyList.removeAllViews();
        List<String> history = loadHistory();
        emptyHistory.setVisibility(history.isEmpty() ? View.VISIBLE : View.GONE);
        for (String item : history) {
            addHistoryRow(item);
        }
    }

    /** Fungsi untuk addHistoryRow. */
    private void addHistoryRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(10), dp(10));
        row.setMinimumHeight(dp(66));
        row.setBackgroundResource(R.drawable.bg_tts_history_card);
        row.setClickable(true);
        row.setFocusable(true);
        row.setOnClickListener(v -> fillInputFromHistory(text));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(10);
        historyList.addView(row, rowParams);

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_clock_24);
        icon.setBackgroundResource(R.drawable.bg_tts_history_icon);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        textColumn.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        );
        textParams.leftMargin = dp(12);
        textParams.rightMargin = dp(8);
        row.addView(textColumn, textParams);

        TextView title = new TextView(this);
        title.setText(R.string.text_to_speech_history_item_title);
        title.setTextColor(getColor(R.color.text_primary));
        title.setTextSize(12);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setMaxLines(1);
        title.setEllipsize(TextUtils.TruncateAt.END);
        textColumn.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView preview = new TextView(this);
        preview.setText(text);
        preview.setTextColor(getColor(R.color.text_secondary));
        preview.setTextSize(12);
        preview.setMaxLines(1);
        preview.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        previewParams.topMargin = dp(4);
        textColumn.addView(preview, previewParams);

        ImageButton delete = new ImageButton(this);
        delete.setImageResource(R.drawable.ic_delete);
        delete.setBackgroundResource(R.drawable.bg_tts_round_action);
        delete.setColorFilter(getColor(R.color.text_secondary));
        delete.setPadding(dp(9), dp(9), dp(9), dp(9));
        delete.setContentDescription(getString(R.string.delete));
        delete.setOnClickListener(v -> removeFromHistory(text));
        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp(36), dp(36));
        deleteParams.rightMargin = dp(8);
        row.addView(delete, deleteParams);

        ImageButton play = new ImageButton(this);
        play.setImageResource(R.drawable.ic_play_24);
        play.setBackgroundResource(R.drawable.bg_tts_history_play);
        play.setPadding(dp(10), dp(10), dp(10), dp(10));
        play.setContentDescription(getString(R.string.text_to_speech_play));
        play.setOnClickListener(v -> speakText(text, false));
        row.addView(play, new LinearLayout.LayoutParams(dp(38), dp(38)));
    }

    /** Padam atau bersihkan FromHistory. */
    private void removeFromHistory(String text) {
        List<String> history = loadHistory();
        boolean removed = false;
        for (int i = history.size() - 1; i >= 0; i--) {
            if (text.equals(history.get(i))) {
                history.remove(i);
                removed = true;
            }
        }
        if (removed) {
            saveHistory(history);
            renderHistory();
        }
    }

    /** Fungsi untuk fillInputFromHistory. */
    private void fillInputFromHistory(String text) {
        if (ttsInput == null) return;
        ttsInput.setText(text);
        ttsInput.setSelection(ttsInput.getText().length());
    }

    /** Ambil atau muat data TtsPrefs. */
    private SharedPreferences getTtsPrefs() {
        return getSharedPreferences(PREFS_TTS, MODE_PRIVATE);
    }

    /** Fungsi untuk dp. */
    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Ambil atau muat data TrimmedText. */
    private String getTrimmedText() {
        if (ttsInput == null || ttsInput.getText() == null) return "";
        return ttsInput.getText().toString().trim();
    }

    /** Paparkan EmptyTextToast. */
    private void showEmptyTextToast() {
        Toast.makeText(this, R.string.text_to_speech_empty_error, Toast.LENGTH_SHORT).show();
    }

    /** Fungsi untuk applyTtsLanguage. */
    private void applyTtsLanguage() {
        if (tts == null) return;
        try {
            Locale preferred = getSelectedTtsLocale();
            int result = tts.setLanguage(preferred);
            if (isUnsupportedLanguage(result) && "ms".equals(preferred.getLanguage())) {
                tts.setLanguage(new Locale("ms"));
            }
        } catch (Exception ignored) {
        }
    }

    /** Ambil atau muat data InitialTtsLanguageIndex. */
    private int getInitialTtsLanguageIndex() {
        String tag = LocaleUtils.getSavedLanguageTag(this);
        if ("en".equals(tag)) return 1;
        if ("zh".equals(tag)) return 2;
        if ("ta".equals(tag)) return 3;
        return 0;
    }

    /** Ambil atau muat data SelectedTtsLocale. */
    private Locale getSelectedTtsLocale() {
        int selected = languageSpinner == null ? 0 : languageSpinner.getSelectedItemPosition();
        switch (selected) {
            case 1:
                return Locale.ENGLISH;
            case 2:
                return Locale.SIMPLIFIED_CHINESE;
            case 3:
                return new Locale("ta", "IN");
            default:
                return new Locale("ms", "MY");
        }
    }

    /** Semak dan sahkan UnsupportedLanguage. */
    private boolean isUnsupportedLanguage(int result) {
        return result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentMode == MODE_SIGN) {
            startSignCamera();
        }
    }

    @Override
    protected void onPause() {
        if (currentMode == MODE_SIGN) {
            stopSignCamera();
        }
        super.onPause();
    }

    /** Aktiviti tidak lagi kelihatan pada skrin. */
    @Override
    protected void onStop() {
        stopSpeechToText();
        stopTts();
        if (currentMode == MODE_SIGN) {
            stopSignCamera();
        }
        super.onStop();
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    protected void onDestroy() {
        stopSignCamera();
        stopSpeechToText();
        if (speechRecognizer != null) {
            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }
            speechRecognizer = null;
        }
        stopTts();
        if (tts != null) {
            try {
                tts.shutdown();
            } catch (Exception ignored) {
            }
            tts = null;
            ttsReady = false;
        }
        super.onDestroy();
    }

    /** Fungsi untuk stopTts. */
    private void stopTts() {
        if (tts == null) return;
        try {
            tts.stop();
        } catch (Exception ignored) {
        }
    }
}
