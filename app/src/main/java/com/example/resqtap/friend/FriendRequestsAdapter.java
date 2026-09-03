package com.example.resqtap.friend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqtap.R;

import java.util.ArrayList;
import java.util.List;


/**
 * FriendRequestsAdapter
 * Adapter untuk card friend request.
 */
public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.ViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(FirebaseFriendClient.FriendRequest request);
        void onReject(FirebaseFriendClient.FriendRequest request);
    }

    private final List<FirebaseFriendClient.FriendRequest> requests = new ArrayList<>();
    private final OnRequestActionListener listener;

    public FriendRequestsAdapter(OnRequestActionListener listener) {
        this.listener = listener;
    }

    /** Fungsi untuk setRequests. */
    public void setRequests(List<FirebaseFriendClient.FriendRequest> newRequests) {
        requests.clear();
        if (newRequests != null) requests.addAll(newRequests);
        notifyDataSetChanged();
    }

    @NonNull
    /** Inflate susun atur XML untuk item ViewHolder. */
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(v);
    }

    /** Bind data ke elemen paparan item. */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseFriendClient.FriendRequest request = requests.get(position);
        android.content.Context ctx = holder.itemView.getContext();
        holder.name.setText(request.fromName.isEmpty() ? ctx.getString(R.string.friend_user_default) : request.fromName);
        holder.subtext.setText(request.fromPublicId.isEmpty()
                ? ctx.getString(R.string.friend_incoming_request_subtext)
                : ctx.getString(R.string.friend_incoming_request_user_subtext, request.fromPublicId));

        holder.btnAccept.setOnClickListener(v -> {
            if (listener != null) listener.onAccept(request);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(request);
        });
    }

    /** Ambil atau muat data ItemCount. */
    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView subtext;
        final Button btnAccept;
        final Button btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.req_name);
            subtext = itemView.findViewById(R.id.req_subtext);
            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnReject = itemView.findViewById(R.id.btn_reject_request);
        }
    }
}
