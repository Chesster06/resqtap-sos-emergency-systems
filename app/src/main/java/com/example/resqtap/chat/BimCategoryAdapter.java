package com.example.resqtap.chat;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter untuk cip pemilihan kategori Kamus Isyarat BIM.
 */
public class BimCategoryAdapter extends RecyclerView.Adapter<BimCategoryAdapter.CategoryViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    private final Context context;
    private final List<String> categories = new ArrayList<>();
    private final OnCategorySelectedListener listener;
    private String selectedCategory = "Semua";

    public BimCategoryAdapter(@NonNull Context context, @NonNull OnCategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(@NonNull List<String> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public void setSelectedCategory(@NonNull String category) {
        this.selectedCategory = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dictionary_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvCategoryName.setText(category);

        boolean isSelected = category.equalsIgnoreCase(selectedCategory);
        if (isSelected) {
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_bim_chip_selected);
            holder.tvCategoryName.setTextColor(Color.WHITE);
        } else {
            holder.tvCategoryName.setBackgroundResource(R.drawable.bg_bim_chip_unselected);
            holder.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedCategory = category;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onCategorySelected(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        final TextView tvCategoryName;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
