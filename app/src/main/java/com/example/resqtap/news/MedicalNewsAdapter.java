package com.example.resqtap.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MedicalNewsAdapter extends RecyclerView.Adapter<MedicalNewsAdapter.NewsViewHolder> {

    private final Context context;
    private final List<MedicalNewsItem> items = new ArrayList<>();
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(3);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final LruCache<String, Bitmap> imageCache = new LruCache<>(30);

    public MedicalNewsAdapter(Context context) {
        this.context = context;
    }

    public void submitList(List<MedicalNewsItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_news, parent, false);
        return new NewsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        MedicalNewsItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());

        String snippet = item.getSnippet();
        if (snippet.isEmpty()) {
            holder.tvSnippet.setVisibility(View.GONE);
        } else {
            holder.tvSnippet.setVisibility(View.VISIBLE);
            holder.tvSnippet.setText(snippet);
        }

        String meta = (item.getSource().isEmpty() ? "Kesihatan" : item.getSource()) + " • " + item.getPublishedAt();
        holder.tvMeta.setText(meta);

        // Thumbnail loading
        holder.imgThumb.setImageResource(position % 2 == 0 ? R.drawable.img_news_hospital_kkm : R.drawable.img_news_cpr);
        String imgUrl = item.getImageUrl();
        if (imgUrl != null && !imgUrl.isEmpty()) {
            loadImageAsync(imgUrl, holder.imgThumb);
        }

        holder.itemView.setOnClickListener(v -> {
            String url = item.getUrl();
            if (url != null && !url.isEmpty()) {
                openNewsUrl(url);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void openNewsUrl(String urlStr) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setShowTitle(true);
            builder.setToolbarColor(ContextCompat.getColor(context, R.color.brand_primary));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(urlStr));
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlStr));
                context.startActivity(intent);
            } catch (Exception ignored) {
            }
        }
    }

    private void loadImageAsync(String urlStr, ImageView targetView) {
        Bitmap cached = imageCache.get(urlStr);
        if (cached != null) {
            targetView.setImageBitmap(cached);
            return;
        }

        imageExecutor.execute(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "ResQTap-App/1.0");
                if (conn.getResponseCode() == 200) {
                    InputStream is = conn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                    conn.disconnect();
                    if (bitmap != null) {
                        imageCache.put(urlStr, bitmap);
                        mainHandler.post(() -> targetView.setImageBitmap(bitmap));
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSnippet, tvMeta;
        ImageView imgThumb;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_news_title);
            tvSnippet = itemView.findViewById(R.id.tv_news_snippet);
            tvMeta = itemView.findViewById(R.id.tv_news_meta);
            imgThumb = itemView.findViewById(R.id.img_news_thumb);
        }
    }
}
