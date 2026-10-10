package com.example.resqtap.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Aktiviti Kamus Bahasa Isyarat Malaysia (BIM) SignBank (465 Isyarat Rasmi)
 * Dilengkapi dengan carian, penapisan kategori penuh, pratonton video rasmi,
 * serta sokongan memilih isyarat terus ke dalam transkrip kamera.
 */
public class BimSignDictionaryActivity extends BaseActivity {

    private BimSignRepository repository;
    private BimCategoryAdapter categoryAdapter;
    private BimDictionarySignAdapter signAdapter;

    private String currentCategory = "Semua";
    private String currentQuery = "";

    private EditText etSearch;
    private ImageButton btnClearSearch;
    private TextView tvHeaderSubtitle;
    private View layoutEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bim_sign_dictionary);

        repository = BimSignRepository.getInstance();

        initViews();
        setupCategories();
        setupSignsGrid();
        loadSigns();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvHeaderSubtitle = findViewById(R.id.tv_header_subtitle);
        layoutEmpty = findViewById(R.id.layout_empty_dictionary);

        etSearch = findViewById(R.id.et_dictionary_search);
        btnClearSearch = findViewById(R.id.btn_clear_dictionary_search);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s != null ? s.toString().trim() : "";
                    if (btnClearSearch != null) {
                        btnClearSearch.setVisibility(currentQuery.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                    filterSigns();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                if (etSearch != null) {
                    etSearch.setText("");
                }
            });
        }
    }

    private void setupCategories() {
        RecyclerView rvCategories = findViewById(R.id.rv_dictionary_categories);
        if (rvCategories == null) return;

        categoryAdapter = new BimCategoryAdapter(this, category -> {
            currentCategory = category;
            filterSigns();
        });

        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        List<String> categories = repository.getCategories(this);
        categoryAdapter.setCategories(categories);
    }

    private void setupSignsGrid() {
        RecyclerView rvSigns = findViewById(R.id.rv_dictionary_signs);
        if (rvSigns == null) return;

        signAdapter = new BimDictionarySignAdapter(this, item -> showBimSignDetailDialog(item));

        rvSigns.setLayoutManager(new GridLayoutManager(this, 2));
        rvSigns.setAdapter(signAdapter);
    }

    private void loadSigns() {
        filterSigns();
    }

    private void filterSigns() {
        if (repository == null || signAdapter == null) return;

        List<BimSignItem> filtered = repository.filterSigns(this, currentCategory, currentQuery);
        signAdapter.updateList(filtered);

        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (tvHeaderSubtitle != null) {
            if ("Semua".equalsIgnoreCase(currentCategory)) {
                tvHeaderSubtitle.setText(filtered.size() + " Isyarat Rasmi SignBank • Berkategori");
            } else {
                tvHeaderSubtitle.setText("Kategori: " + currentCategory + " (" + filtered.size() + " isyarat)");
            }
        }
    }

    /**
     * Memaparkan dialog butiran penuh isyarat BIM termasuk contoh ayat, makna, dan pautan video demonstrasi rasmi.
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
        TextView tvCategoryBadge = dialogView.findViewById(R.id.tv_detail_category_badge);
        View layoutExamples = dialogView.findViewById(R.id.layout_example_sentences);
        TextView tvContohAyat = dialogView.findViewById(R.id.tv_detail_contoh_ayat);
        TextView tvExampleSentence = dialogView.findViewById(R.id.tv_detail_example_sentence);
        View btnClose = dialogView.findViewById(R.id.btn_close_detail);
        View btnWatchVideo = dialogView.findViewById(R.id.btn_watch_bim_video);

        tvPerkataan.setText(item.getPerkataan());
        tvWord.setText(item.getWord());

        if (tvCategoryBadge != null) {
            String cat = (item.getCategory() != null && !item.getCategory().isEmpty()) ? item.getCategory() : "Umum";
            tvCategoryBadge.setText("Kategori: " + cat);
        }

        if (item.getDrawableResId() != 0) {
            ivThumbnail.setImageResource(item.getDrawableResId());
        } else {
            ivThumbnail.setImageResource(R.drawable.img_sign_ily);
        }

        boolean hasExamples = (!item.getContohAyat().isEmpty()) || (!item.getExampleSentence().isEmpty());
        if (hasExamples) {
            layoutExamples.setVisibility(View.VISIBLE);
            tvContohAyat.setText(item.getContohAyat().isEmpty() ? "-" : item.getContohAyat());
            tvExampleSentence.setText(item.getExampleSentence().isEmpty() ? "-" : item.getExampleSentence());
        } else {
            layoutExamples.setVisibility(View.GONE);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnWatchVideo != null) {
            final String videoUrl = item.getVideoUrl();
            if (videoUrl != null && !videoUrl.trim().isEmpty()) {
                btnWatchVideo.setVisibility(View.VISIBLE);
                btnWatchVideo.setOnClickListener(v -> openVideoUrl(videoUrl));
            } else {
                btnWatchVideo.setVisibility(View.GONE);
            }
        }

        dialog.show();
    }

    private void openVideoUrl(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().isEmpty()) return;
        try {
            CustomTabsIntent customTabs = new CustomTabsIntent.Builder()
                    .setToolbarColor(ContextCompat.getColor(this, R.color.brand_primary))
                    .setShowTitle(true)
                    .build();
            customTabs.launchUrl(this, Uri.parse(videoUrl.trim()));
        } catch (Exception ex) {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl.trim()));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.bim_video_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
