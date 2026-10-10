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
 * Adapter RecyclerView bagi paparan kad Isyarat BIM SignBank (Ekspresi)
 */
public class BimSignAdapter extends RecyclerView.Adapter<BimSignAdapter.BimSignViewHolder> {

    public interface OnBimSignClickListener {
        void onSignClick(@NonNull BimSignItem item);
        void onSignDetailClick(@NonNull BimSignItem item);
    }

    private final Context context;
    private final List<BimSignItem> items = new ArrayList<>();
    private final OnBimSignClickListener listener;
    private final BimSignRepository repository;

    public BimSignAdapter(@NonNull Context context, @NonNull OnBimSignClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.repository = BimSignRepository.getInstance();
    }

    public void updateList(@NonNull List<BimSignItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BimSignViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bim_sign_card, parent, false);
        return new BimSignViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BimSignViewHolder holder, int position) {
        BimSignItem item = items.get(position);
        holder.tvPerkataan.setText(item.getPerkataan());
        holder.tvWord.setText(item.getWord());

        // Muat imej isyarat terus daripada res/drawable (atau fallback ke assets)
        if (item.getDrawableResId() != 0) {
            holder.ivThumbnail.setImageResource(item.getDrawableResId());
        } else {
            repository.loadImage(context, item, holder.ivThumbnail);
        }

        // Klik kad -> tambah ke ayat transkrip kamera
        holder.itemView.setOnClickListener(v -> listener.onSignClick(item));

        // Klik butang video/butiran
        if (holder.btnPlayVideo != null) {
            holder.btnPlayVideo.setOnClickListener(v -> listener.onSignDetailClick(item));
        }

        // Long click -> buka butiran
        holder.itemView.setOnLongClickListener(v -> {
            listener.onSignDetailClick(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BimSignViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivThumbnail;
        final TextView tvPerkataan;
        final TextView tvWord;
        final FrameLayout btnPlayVideo;

        BimSignViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.iv_sign_thumbnail);
            tvPerkataan = itemView.findViewById(R.id.tv_sign_perkataan);
            tvWord = itemView.findViewById(R.id.tv_sign_word);
            btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
        }
    }
}
