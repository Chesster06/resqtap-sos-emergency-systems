package com.example.resqtap.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Model data bagi Isyarat Bahasa Isyarat Malaysia (BIM) SignBank
 * Sumber: https://bimsignbank.org/groups/daily-life/expressive (MFD & Guidewire)
 */
public class BimSignItem implements Serializable {
    private final int id;
    private final String documentId;
    private final String perkataan;
    private final String word;
    private final String videoUrl;
    private final String contohAyat;
    private final String exampleSentence;
    private final String assetPath;
    private final String webpUrl;
    private final String category;
    private int drawableResId = 0;

    public BimSignItem(int id,
                       @NonNull String perkataan,
                       @NonNull String word,
                       int drawableResId,
                       @Nullable String category,
                       @Nullable String videoUrl,
                       @Nullable String contohAyat,
                       @Nullable String exampleSentence) {
        this.id = id;
        this.documentId = "";
        this.perkataan = perkataan.trim();
        this.word = word.trim();
        this.drawableResId = drawableResId;
        this.category = (category != null && !category.trim().isEmpty()) ? category.trim() : "Umum";
        this.videoUrl = videoUrl != null ? videoUrl : "";
        this.contohAyat = contohAyat != null ? contohAyat.trim() : "";
        this.exampleSentence = exampleSentence != null ? exampleSentence.trim() : "";
        this.assetPath = "";
        this.webpUrl = "";
    }

    public BimSignItem(int id,
                       @NonNull String perkataan,
                       @NonNull String word,
                       int drawableResId,
                       @Nullable String videoUrl,
                       @Nullable String contohAyat,
                       @Nullable String exampleSentence) {
        this(id, perkataan, word, drawableResId, "Umum", videoUrl, contohAyat, exampleSentence);
    }

    public BimSignItem(int id,
                       @Nullable String documentId,
                       @NonNull String perkataan,
                       @NonNull String word,
                       @Nullable String videoUrl,
                       @Nullable String contohAyat,
                       @Nullable String exampleSentence,
                       @Nullable String assetPath,
                       @Nullable String webpUrl,
                       @Nullable String category) {
        this.id = id;
        this.documentId = documentId != null ? documentId : "";
        this.perkataan = perkataan.trim();
        this.word = word.trim();
        this.videoUrl = videoUrl != null ? videoUrl : "";
        this.contohAyat = contohAyat != null ? contohAyat.trim() : "";
        this.exampleSentence = exampleSentence != null ? exampleSentence.trim() : "";
        this.assetPath = assetPath != null ? assetPath : ("bim_signs/bim_exp_" + id + ".webp");
        this.webpUrl = webpUrl != null ? webpUrl : "";
        this.category = category != null ? category : "Ekspresi";
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public void setDrawableResId(int drawableResId) {
        this.drawableResId = drawableResId;
    }

    public static String toValidResourceName(String text) {
        if (text == null) return "";
        String clean = text.toLowerCase().replaceAll("[^a-z0-9]", "_");
        clean = clean.replaceAll("_+", "_");
        if (clean.startsWith("_")) clean = clean.substring(1);
        if (clean.endsWith("_")) clean = clean.substring(0, clean.length() - 1);
        return clean;
    }

    public int getId() {
        return id;
    }

    public String getDocumentId() {
        return documentId;
    }

    @NonNull
    public String getPerkataan() {
        return perkataan;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getContohAyat() {
        return contohAyat;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public String getAssetPath() {
        return assetPath;
    }

    public String getWebpUrl() {
        return webpUrl;
    }

    public String getCategory() {
        return category;
    }

    /**
     * Memeriksa sama ada item ini sepadan dengan carian pengguna (BM atau BI)
     */
    public boolean matchesQuery(@Nullable String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.trim().toLowerCase();
        return perkataan.toLowerCase().contains(q) ||
                word.toLowerCase().contains(q) ||
                (contohAyat != null && contohAyat.toLowerCase().contains(q)) ||
                (exampleSentence != null && exampleSentence.toLowerCase().contains(q));
    }
}
