package com.example.resqtap.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;


/**
 * FriendsAdapter
 * Adapter RecyclerView untuk list friend.
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    public interface OnFriendActionListener {
        void onRemoveFriend(FirebaseFriendClient.FriendInfo friend);
    }

    private final List<FirebaseFriendClient.FriendInfo> allFriends = new ArrayList<>();
    private final List<FirebaseFriendClient.FriendInfo> displayFriends = new ArrayList<>();
    private final OnFriendActionListener listener;
    private String currentQuery = "";

    public FriendsAdapter(OnFriendActionListener listener) {
        this.listener = listener;
    }

    /** Fungsi untuk setFriends. */
    public void setFriends(List<FirebaseFriendClient.FriendInfo> newFriends) {
        allFriends.clear();
        if (newFriends != null) allFriends.addAll(newFriends);
        applyFilter(currentQuery);
    }

    /** Fungsi untuk filter. */
    public void filter(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase();
        applyFilter(currentQuery);
    }

    /** Fungsi untuk applyFilter. */
    private void applyFilter(String query) {
        displayFriends.clear();
        if (query.isEmpty()) {
            displayFriends.addAll(allFriends);
        } else {
            for (FirebaseFriendClient.FriendInfo friend : allFriends) {
                String name = friend.name != null ? friend.name.toLowerCase() : "";
                String pubId = friend.publicId != null ? friend.publicId.toLowerCase() : "";
                if (name.contains(query) || pubId.contains(query)) {
                    displayFriends.add(friend);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    /** Inflate susun atur XML untuk item ViewHolder. */
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(v);
    }

    /** Bind data ke elemen paparan item. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseFriendClient.FriendInfo friend = displayFriends.get(position);
        android.content.Context ctx = holder.itemView.getContext();
        holder.name.setText(friend.name.isEmpty() ? ctx.getString(R.string.friend_default_name) : friend.name);
        if (friend.publicId.isEmpty()) {
            holder.publicId.setText(ctx.getString(R.string.friend_resqtap_default));
        } else {
            holder.publicId.setText(ctx.getString(R.string.friend_tag_format, friend.publicId));
        }
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveFriend(friend);
        });
    }

    /** Ambil atau muat data ItemCount. */
    @Override
    public int getItemCount() {
        return displayFriends.size();
    }

    /** Ambil atau muat data TotalCount. */
    public int getTotalCount() {
        return allFriends.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView publicId;
        final MaterialButton btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.friend_name);
            publicId = itemView.findViewById(R.id.friend_public_id);
            btnRemove = itemView.findViewById(R.id.btn_remove_friend);
        }
    }
}

