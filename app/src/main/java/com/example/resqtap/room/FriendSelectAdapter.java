package com.example.resqtap.room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;
import com.example.resqtap.friend.FirebaseFriendClient.FriendInfo;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * FriendSelectAdapter
 * Adapter dialog pilih kawan untuk invite masuk bilik.
 */
public class FriendSelectAdapter extends RecyclerView.Adapter<FriendSelectAdapter.ViewHolder> {

    private final List<FriendInfo> friends = new ArrayList<>();
    private final Set<String> selectedUids = new HashSet<>();

    /** Fungsi untuk setFriends. */
    public void setFriends(List<FriendInfo> list) {
        friends.clear();
        if (list != null) friends.addAll(list);
        notifyDataSetChanged();
    }

    /** Ambil atau muat data SelectedFriends. */
    public List<FriendInfo> getSelectedFriends() {
        List<FriendInfo> out = new ArrayList<>();
        for (FriendInfo f : friends) {
            if (f != null && selectedUids.contains(f.uid)) {
                out.add(f);
            }
        }
        return out;
    }

    @NonNull
    /** Inflate susun atur XML untuk item ViewHolder. */
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select, parent, false);
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp != null) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            v.setLayoutParams(lp);
        } else {
            v.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        }
        return new ViewHolder(v);
    }

    /** Bind data ke elemen paparan item. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendInfo f = friends.get(position);
        android.content.Context ctx = holder.itemView.getContext();
        holder.name.setText(f.name.isEmpty() ? ctx.getString(R.string.friend_default_name) : f.name);
        holder.tag.setText(f.name + "#" + f.publicId);

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(lp);
        }

        boolean isSelected = selectedUids.contains(f.uid);
        if (isSelected) {
            holder.boxIndicator.setBackgroundResource(R.drawable.bg_select_box_checked);
            holder.ivCheck.setVisibility(View.VISIBLE);
            holder.card.setStrokeColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.white));
            holder.card.setStrokeWidth((int) (2f * ctx.getResources().getDisplayMetrics().density));
        } else {
            holder.boxIndicator.setBackgroundResource(R.drawable.bg_select_box_unchecked);
            holder.ivCheck.setVisibility(View.GONE);
            holder.card.setStrokeColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.brand_primary_dark));
            holder.card.setStrokeWidth((int) (1f * ctx.getResources().getDisplayMetrics().density));
        }

        holder.card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.brand_primary));

        holder.card.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (selectedUids.contains(f.uid)) {
                selectedUids.remove(f.uid);
            } else {
                selectedUids.add(f.uid);
            }
            notifyItemChanged(pos);
        });
    }

    /** Ambil atau muat data ItemCount. */
    @Override
    public int getItemCount() {
        return friends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView name;
        final TextView tag;
        final View boxIndicator;
        final android.widget.ImageView ivCheck;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_friend_item);
            name = itemView.findViewById(R.id.tv_friend_name);
            tag = itemView.findViewById(R.id.tv_friend_tag);
            boxIndicator = itemView.findViewById(R.id.box_select_indicator);
            ivCheck = itemView.findViewById(R.id.iv_select_check);
        }
    }
}
