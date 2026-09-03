package com.example.resqtap.friend;

import android.content.Context;
import android.util.Log;

import com.example.resqtap.notification.NotificationUtils;
import com.example.resqtap.room.FirebaseRoomClient;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * FirebaseFriendClient
 * Friend Helper: cari friend guna Tag ID (contoh: User#4091), hantar & accept friend request.
 */
public class FirebaseFriendClient {
    private static final String TAG = "FirebaseFriendClient";

    /** Fungsi untuk db. */
    private static DatabaseReference db() {
        return FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL).getReference();
    }

    public static class FriendInfo {
        public String uid = "";
        public String publicId = "";
        public String name = "";
        public String photoUrl = "";
        public long addedAt = 0L;

        /** Fungsi untuk from. */
        public static FriendInfo from(DataSnapshot snap) {
            if (snap == null || !snap.exists()) return null;
            FriendInfo info = new FriendInfo();
            info.uid = snap.getKey() == null ? "" : snap.getKey().trim();
            info.publicId = String.valueOf(snap.child("publicId").getValue() == null ? "" : snap.child("publicId").getValue()).trim();
            info.name = String.valueOf(snap.child("name").getValue() == null ? "" : snap.child("name").getValue()).trim();
            info.photoUrl = String.valueOf(snap.child("photoUrl").getValue() == null ? "" : snap.child("photoUrl").getValue()).trim();
            Object at = snap.child("addedAt").getValue();
            if (at instanceof Number) {
                info.addedAt = ((Number) at).longValue();
            }
            return info;
        }
    }

    public static class FriendRequest {
        public String fromUid = "";
        public String fromName = "";
        public String fromPublicId = "";
        public String targetUid = "";
        public String status = "pending";
        public long createdAt = 0L;

        /** Fungsi untuk from. */
        public static FriendRequest from(DataSnapshot snap) {
            if (snap == null || !snap.exists()) return null;
            FriendRequest req = new FriendRequest();
            req.fromUid = String.valueOf(snap.child("fromUid").getValue() == null ? "" : snap.child("fromUid").getValue()).trim();
            if (req.fromUid.isEmpty()) req.fromUid = snap.getKey() == null ? "" : snap.getKey().trim();
            req.fromName = String.valueOf(snap.child("fromName").getValue() == null ? "" : snap.child("fromName").getValue()).trim();
            req.fromPublicId = String.valueOf(snap.child("fromPublicId").getValue() == null ? "" : snap.child("fromPublicId").getValue()).trim();
            req.targetUid = String.valueOf(snap.child("targetUid").getValue() == null ? "" : snap.child("targetUid").getValue()).trim();
            req.status = String.valueOf(snap.child("status").getValue() == null ? "pending" : snap.child("status").getValue()).trim();
            Object at = snap.child("createdAt").getValue();
            if (at instanceof Number) {
                req.createdAt = ((Number) at).longValue();
            }
            return req;
        }
    }

    /** Fungsi untuk await. */
    private static <T> T await(Task<T> task) throws Exception {
        return Tasks.await(task, 15, TimeUnit.SECONDS);
    }

    /** Fungsi untuk format4DigitId. */
    public static String format4DigitId(String rawUid, String rawPubId) {
        if (rawPubId != null && !rawPubId.trim().isEmpty()) {
            String clean = rawPubId.trim().replaceAll("\\D", "");
            if (clean.length() >= 4) {
                return clean.substring(clean.length() - 4);
            } else if (!clean.isEmpty()) {
                try {
                    return String.format(java.util.Locale.US, "%04d", Integer.parseInt(clean));
                } catch (Exception ignored) {}
            }
        }
        if (rawUid != null && !rawUid.isEmpty()) {
            int hash = Math.abs(rawUid.hashCode()) % 10000;
            if (hash < 1000) hash += 1000;
            return String.format(java.util.Locale.US, "%04d", hash);
        }
        return "1000";
    }

    /** Fungsi untuk findUserByCode. */
    public static FriendInfo findUserByCode(String input) throws Exception {
        if (input == null) return null;
        String raw = input.trim();
        if (raw.isEmpty()) return null;

        String targetName = "";
        String targetTag = "";

        if (raw.contains("#")) {
            String[] parts = raw.split("#", 2);
            targetName = parts[0].trim();
            targetTag = parts[1].trim().replaceAll("\\D", "");
        } else if (raw.matches("^\\d{1,6}$")) {
            targetTag = raw;
        } else {
            targetName = raw;
        }

        DatabaseReference usersRef = db().child("users");
        DataSnapshot allUsersSnap = await(usersRef.get());
        if (allUsersSnap != null && allUsersSnap.exists()) {
            for (DataSnapshot child : allUsersSnap.getChildren()) {
                FriendInfo info = buildFriendInfoFromUserSnap(child);
                if (info == null) continue;

                String uName = info.name.trim();
                String uTag = info.publicId.trim();
                String uUid = info.uid.trim();

                if (!targetName.isEmpty() && !targetTag.isEmpty()) {
                    if (uName.equalsIgnoreCase(targetName) && (uTag.equalsIgnoreCase(targetTag) || uTag.endsWith(targetTag))) {
                        return info;
                    }
                }

                else if (!targetTag.isEmpty()) {
                    if (uTag.equalsIgnoreCase(targetTag) || uTag.endsWith(targetTag) || uUid.equalsIgnoreCase(targetTag)) {
                        return info;
                    }
                }

                else if (!targetName.isEmpty()) {
                    if (uName.equalsIgnoreCase(targetName) || uUid.equalsIgnoreCase(targetName)) {
                        return info;
                    }
                }
            }
        }

        return null;
    }

    /** Fungsi untuk buildFriendInfoFromUserSnap. */
    private static FriendInfo buildFriendInfoFromUserSnap(DataSnapshot snap) {
        if (snap == null || !snap.exists()) return null;
        FriendInfo info = new FriendInfo();
        info.uid = snap.getKey() == null ? "" : snap.getKey().trim();
        info.name = String.valueOf(snap.child("name").getValue() == null ? "" : snap.child("name").getValue()).trim();
        if (info.name.isEmpty()) info.name = String.valueOf(snap.child("fullName").getValue() == null ? "" : snap.child("fullName").getValue()).trim();
        if (info.name.isEmpty()) info.name = "Pengguna";

        String rawPub = String.valueOf(snap.child("publicId").getValue() == null ? "" : snap.child("publicId").getValue()).trim();
        info.publicId = format4DigitId(info.uid, rawPub);
        info.photoUrl = String.valueOf(snap.child("photoUrl").getValue() == null ? "" : snap.child("photoUrl").getValue()).trim();
        return info;
    }

    /** Simpan atau hantar data FriendRequest. */
    public static void sendFriendRequest(Context context, String fromUid, String fromName, String fromPublicId, String targetUid) throws Exception {
        String fUid = String.valueOf(fromUid == null ? "" : fromUid).trim();
        String tUid = String.valueOf(targetUid == null ? "" : targetUid).trim();
        if (fUid.isEmpty()) throw new RuntimeException("invalid_from_uid");
        if (tUid.isEmpty()) throw new RuntimeException("invalid_target_uid");
        if (fUid.equals(tUid)) throw new RuntimeException("cannot_add_self");

        DataSnapshot friendCheck = await(db().child("userFriends").child(fUid).child(tUid).get());
        if (friendCheck != null && friendCheck.exists()) {
            throw new RuntimeException("already_friends");
        }

        DataSnapshot pendingCheck = await(db().child("friendRequests").child(tUid).child(fUid).get());
        if (pendingCheck != null && pendingCheck.exists()) {
            throw new RuntimeException("request_already_pending");
        }

        Map<String, Object> reqData = new HashMap<>();
        reqData.put("fromUid", fUid);
        reqData.put("fromName", String.valueOf(fromName == null ? "" : fromName).trim());
        reqData.put("fromPublicId", String.valueOf(fromPublicId == null ? "" : fromPublicId).trim());
        reqData.put("targetUid", tUid);
        reqData.put("status", "pending");
        reqData.put("createdAt", ServerValue.TIMESTAMP);

        Map<String, Object> updates = new HashMap<>();
        updates.put("friendRequests/" + tUid + "/" + fUid, reqData);
        updates.put("sentRequests/" + fUid + "/" + tUid, reqData);

        String notifId = "friend_req_" + System.currentTimeMillis();
        Map<String, Object> notif = new HashMap<>();
        notif.put("id", notifId);
        notif.put("title", "Permintaan Rakan");
        notif.put("message", (fromName.isEmpty() ? "Seseorang" : fromName) + " telah menghantar permintaan rakan.");
        notif.put("createdAt", ServerValue.TIMESTAMP);
        updates.put("userNotifications/" + tUid + "/" + notifId, notif);

        await(db().updateChildren(updates));
    }

    /** Fungsi untuk acceptFriendRequest. */
    public static void acceptFriendRequest(Context context, String currentUid, String currentName, String currentPublicId, FriendRequest request) throws Exception {
        if (request == null) throw new RuntimeException("invalid_request");
        String cUid = String.valueOf(currentUid == null ? "" : currentUid).trim();
        String fUid = String.valueOf(request.fromUid == null ? "" : request.fromUid).trim();
        if (cUid.isEmpty() || fUid.isEmpty()) throw new RuntimeException("invalid_uids");

        DataSnapshot fromUserSnap = await(db().child("users").child(fUid).get());
        String fName = request.fromName;
        String fPhoto = "";
        String fPublicId = request.fromPublicId;
        if (fromUserSnap != null && fromUserSnap.exists()) {
            if (fName.isEmpty()) fName = String.valueOf(fromUserSnap.child("name").getValue() == null ? "" : fromUserSnap.child("name").getValue()).trim();
            fPhoto = String.valueOf(fromUserSnap.child("photoUrl").getValue() == null ? "" : fromUserSnap.child("photoUrl").getValue()).trim();
            if (fPublicId.isEmpty()) fPublicId = String.valueOf(fromUserSnap.child("publicId").getValue() == null ? "" : fromUserSnap.child("publicId").getValue()).trim();
        }

        DataSnapshot currentUserSnap = await(db().child("users").child(cUid).get());
        String cName = String.valueOf(currentName == null ? "" : currentName).trim();
        String cPhoto = "";
        String cPublicId = String.valueOf(currentPublicId == null ? "" : currentPublicId).trim();
        if (currentUserSnap != null && currentUserSnap.exists()) {
            if (cName.isEmpty()) cName = String.valueOf(currentUserSnap.child("name").getValue() == null ? "" : currentUserSnap.child("name").getValue()).trim();
            cPhoto = String.valueOf(currentUserSnap.child("photoUrl").getValue() == null ? "" : currentUserSnap.child("photoUrl").getValue()).trim();
            if (cPublicId.isEmpty()) cPublicId = String.valueOf(currentUserSnap.child("publicId").getValue() == null ? "" : currentUserSnap.child("publicId").getValue()).trim();
        }

        Map<String, Object> friendA = new HashMap<>();
        friendA.put("uid", fUid);
        friendA.put("name", fName);
        friendA.put("publicId", fPublicId);
        friendA.put("photoUrl", fPhoto);
        friendA.put("addedAt", ServerValue.TIMESTAMP);

        Map<String, Object> friendB = new HashMap<>();
        friendB.put("uid", cUid);
        friendB.put("name", cName);
        friendB.put("publicId", cPublicId);
        friendB.put("photoUrl", cPhoto);
        friendB.put("addedAt", ServerValue.TIMESTAMP);

        Map<String, Object> updates = new HashMap<>();
        updates.put("userFriends/" + cUid + "/" + fUid, friendA);
        updates.put("userFriends/" + fUid + "/" + cUid, friendB);

        updates.put("friendRequests/" + cUid + "/" + fUid, null);
        updates.put("sentRequests/" + fUid + "/" + cUid, null);

        String notifId = "friend_accepted_" + System.currentTimeMillis();
        Map<String, Object> notif = new HashMap<>();
        notif.put("id", notifId);
        notif.put("title", "Permintaan Rakan Diterima");
        notif.put("message", (cName.isEmpty() ? "Pengguna" : cName) + " telah menerima permintaan rakan anda!");
        notif.put("createdAt", ServerValue.TIMESTAMP);
        updates.put("userNotifications/" + fUid + "/" + notifId, notif);

        await(db().updateChildren(updates));

        if (context != null) {
            try {
                NotificationUtils.notifyAdmin(context, "friend_accepted", "Permintaan Rakan Diterima", (cName.isEmpty() ? "Pengguna" : cName) + " telah menerima permintaan rakan anda!");
            } catch (Exception ignored) {}
        }
    }

    /** Fungsi untuk rejectFriendRequest. */
    public static void rejectFriendRequest(String currentUid, String fromUid) throws Exception {
        String cUid = String.valueOf(currentUid == null ? "" : currentUid).trim();
        String fUid = String.valueOf(fromUid == null ? "" : fromUid).trim();
        if (cUid.isEmpty() || fUid.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("friendRequests/" + cUid + "/" + fUid, null);
        updates.put("sentRequests/" + fUid + "/" + cUid, null);
        await(db().updateChildren(updates));
    }

    /** Padam atau bersihkan Friend. */
    public static void removeFriend(String currentUid, String friendUid) throws Exception {
        String cUid = String.valueOf(currentUid == null ? "" : currentUid).trim();
        String fUid = String.valueOf(friendUid == null ? "" : friendUid).trim();
        if (cUid.isEmpty() || fUid.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("userFriends/" + cUid + "/" + fUid, null);
        updates.put("userFriends/" + fUid + "/" + cUid, null);
        await(db().updateChildren(updates));
    }
}

