package com.example.resqtap.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter untuk grid paparan kad Kamus Isyarat BIM SignBank.
 */
public class BimDictionarySignAdapter extends RecyclerView.Adapter<BimDictionarySignAdapter.SignViewHolder> {

    public interface OnDictionarySignClickListener {
        void onSignClick(@NonNull BimSignItem item);
        void onSignDetailClick(@NonNull BimSignItem item);
    }

    private final Context context;
    private final List<BimSignItem> items = new ArrayList<>();
    private final OnDictionarySignClickListener listener;
    private final boolean isPickMode;

    public BimDictionarySignAdapter(@NonNull Context context, boolean isPickMode, @NonNull OnDictionarySignClickListener listener) {
        this.context = context;
        this.isPickMode = isPickMode;
        this.listener = listener;
    }

    public void updateList(@NonNull List<BimSignItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bim_dictionary_card, parent, false);
        return new SignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SignViewHolder holder, int position) {
        BimSignItem item = items.get(position);
        holder.tvPerkataan.setText(item.getPerkataan());
        holder.tvWord.setText(item.getWord());
        holder.tvCategoryTag.setText(item.getCategory() != null ? item.getCategory() : "Umum");

        if (item.getDrawableResId() != 0) {
            holder.ivThumbnail.setImageResource(item.getDrawableResId());
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.img_sign_ily);
        }

        if (isPickMode) {
            holder.tvActionLabel.setText("Ketik untuk guna");
        } else {
            holder.tvActionLabel.setText("Lihat butiran");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSignClick(item);
            }
        });

        holder.btnPlayVideo.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSignDetailClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SignViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvPerkataan;
        final TextView tvWord;
        final TextView tvCategoryTag;
        final TextView tvActionLabel;
        final FrameLayout btnPlayVideo;

        SignViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_sign_thumbnail);
            tvPerkataan = itemView.findViewById(R.id.tv_sign_perkataan);
            tvWord = itemView.findViewById(R.id.tv_sign_word);
            tvCategoryTag = itemView.findViewById(R.id.tv_sign_category_tag);
            tvActionLabel = itemView.findViewById(R.id.tv_action_label);
            btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
        }
    }
}
