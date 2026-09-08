package com.example.resqtap.news;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.resqtap.R;
import com.example.resqtap.app.BaseActivity;

import java.util.List;

public class MedicalNewsActivity extends BaseActivity {

    private RecyclerView rvAllNews;
    private MedicalNewsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_news);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvAllNews = findViewById(R.id.rv_all_news);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressLoading = findViewById(R.id.progress_loading);

        if (rvAllNews != null) {
            rvAllNews.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MedicalNewsAdapter(this);
            rvAllNews.setAdapter(adapter);
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeResources(R.color.brand_primary);
            swipeRefresh.setOnRefreshListener(this::loadNews);
        }

        loadNews();
    }

    private void loadNews() {
        if (progressLoading != null && (swipeRefresh == null || !swipeRefresh.isRefreshing())) {
            progressLoading.setVisibility(View.VISIBLE);
        }

        MedicalNewsFetcher.fetchMedicalNews(25, new MedicalNewsFetcher.NewsCallback() {
            @Override
            public void onSuccess(List<MedicalNewsItem> newsList) {
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                if (adapter != null) {
                    adapter.submitList(newsList);
                }
            }

            @Override
            public void onError(Exception e) {
                if (progressLoading != null) progressLoading.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                Toast.makeText(MedicalNewsActivity.this, R.string.medical_news_load_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
