package com.example.resqtap.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Repositori Pengurusan Data Isyarat BIM SignBank (Ekspresi)
 * Menggunakan imej terus daripada res/drawable tanpa memerlukan fail JSON/assets.
 */
public class BimSignRepository {
    private static final String TAG = "BimSignRepository";

    private static volatile BimSignRepository instance;
    private final List<BimSignItem> allSigns = new ArrayList<>();
    private boolean isLoaded = false;

    private BimSignRepository() {}

    public static BimSignRepository getInstance() {
        if (instance == null) {
            synchronized (BimSignRepository.class) {
                if (instance == null) {
                    instance = new BimSignRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Memuatkan semua 112 data isyarat ekspresi terus daripada katalog drawable natif.
     */
    public synchronized List<BimSignItem> getSigns(@NonNull Context context) {
        if (!isLoaded) {
            allSigns.clear();
            allSigns.addAll(BimSignCatalog.getSigns());
            isLoaded = true;
            Log.d(TAG, "Berjaya memuatkan " + allSigns.size() + " isyarat BIM terus daripada res/drawable.");
        }
        return new ArrayList<>(allSigns);
    }

    /**
     * Memuat imej ke dalam target ImageView menggunakan res/drawable natif.
     */
    public void loadImage(@NonNull Context context, @NonNull BimSignItem item, @NonNull ImageView targetView) {
        if (item.getDrawableResId() != 0) {
            targetView.setImageResource(item.getDrawableResId());
        }
    }

    /**
     * Dapatkan Bitmap isyarat secara langsung daripada resource.
     */
    @Nullable
    public Bitmap getBitmapSync(@NonNull Context context, @NonNull BimSignItem item) {
        if (item.getDrawableResId() != 0) {
            try {
                return BitmapFactory.decodeResource(context.getResources(), item.getDrawableResId());
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * Mencari isyarat berdasarkan kata kunci carian (BM atau BI).
     */
    public List<BimSignItem> searchSigns(@NonNull Context context, @Nullable String query) {
        return filterSigns(context, null, query);
    }

    /**
     * Mengambil senarai kategori unik yang disusun rapi berserta kiraan item.
     */
    public List<String> getCategories(@NonNull Context context) {
        List<BimSignItem> list = getSigns(context);
        java.util.LinkedHashSet<String> cats = new java.util.LinkedHashSet<>();
        cats.add("Semua");
        for (BimSignItem item : list) {
            if (item.getCategory() != null && !item.getCategory().isEmpty()) {
                cats.add(item.getCategory());
            }
        }
        return new ArrayList<>(cats);
    }

    /**
     * Menapis isyarat mengikut kategori dan kata kunci carian secara serentak.
     */
    public List<BimSignItem> filterSigns(@NonNull Context context, @Nullable String category, @Nullable String query) {
        List<BimSignItem> list = getSigns(context);
        boolean filterCat = category != null && !category.isEmpty() && !category.equalsIgnoreCase("Semua");
        boolean filterQuery = query != null && !query.trim().isEmpty();

        if (!filterCat && !filterQuery) {
            return list;
        }

        List<BimSignItem> result = new ArrayList<>();
        for (BimSignItem item : list) {
            boolean catMatch = !filterCat || item.getCategory().equalsIgnoreCase(category);
            boolean queryMatch = !filterQuery || item.matchesQuery(query);
            if (catMatch && queryMatch) {
                result.add(item);
            }
        }
        return result;
    }
}
