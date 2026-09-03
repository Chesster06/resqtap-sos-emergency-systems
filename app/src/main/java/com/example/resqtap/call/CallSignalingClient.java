package com.example.resqtap.call;

import com.example.resqtap.room.FirebaseRoomClient;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


/**
 * CallSignalingClient
 * Signaling Client (Firebase RTDB): tukar SDP Offer/Answer dan ICE Candidate antara caller & callee.
 */
public class CallSignalingClient {
    private static CallSignalingClient instance;
    private final DatabaseReference rootRef;

    private ValueEventListener incomingCallListener;
    private DatabaseReference incomingCallRef;

    public interface IncomingCallHandler {
        void onIncomingCall(String callId, String callerName, String callerPhotoUrl, String callType);
        void onCallCancelled(String callId);
    }

    private CallSignalingClient() {
        rootRef = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL).getReference();
    }

    /** Ambil atau muat data Instance. */
    public static synchronized CallSignalingClient getInstance() {
        if (instance == null) {
            instance = new CallSignalingClient();
        }
        return instance;
    }

    /** Fungsi untuk listenForIncomingCalls. */
    public void listenForIncomingCalls(String myUid, IncomingCallHandler handler) {
        if (myUid == null || myUid.trim().isEmpty() || handler == null) return;

        stopListeningForIncomingCalls();

        incomingCallRef = rootRef.child("userCalls").child(myUid).child("currentCall");
        incomingCallListener = new ValueEventListener() {
            /** Callback apabila data Firebase Realtime Database berubah. */
    @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot == null || !snapshot.exists()) {

                    handler.onCallCancelled("");
                    return;
                }

                String status = getString(snapshot, "status");
                String callId = getString(snapshot, "callId");

                if ("ringing".equals(status) && !callId.isEmpty()) {
                    String callerName = getString(snapshot, "callerName");
                    String callerPhotoUrl = getString(snapshot, "callerPhotoUrl");
                    String callType = getString(snapshot, "callType");
                    handler.onIncomingCall(callId, callerName, callerPhotoUrl, callType.isEmpty() ? "video" : callType);
                } else if ("cancelled".equals(status) || "ended".equals(status)) {
                    handler.onCallCancelled(callId);
                }
            }

            /** Callback sekiranya operasi Firebase dibatalkan atau gagal. */
    @Override
            public void onCancelled(DatabaseError error) {
            }
        };
        incomingCallRef.addValueEventListener(incomingCallListener);
    }

    /** Fungsi untuk stopListeningForIncomingCalls. */
    public void stopListeningForIncomingCalls() {
        if (incomingCallRef != null && incomingCallListener != null) {
            incomingCallRef.removeEventListener(incomingCallListener);
        }
        incomingCallListener = null;
        incomingCallRef = null;
    }

    /** Fungsi untuk acceptCall. */
    public void acceptCall(String myUid, String callId) {
        if (myUid == null || myUid.isEmpty() || callId == null || callId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "accepted");
        updates.put("acceptedAt", com.google.firebase.database.ServerValue.TIMESTAMP);

        rootRef.child("userCalls").child(myUid).child("currentCall").updateChildren(updates);
    }

    /** Fungsi untuk rejectCall. */
    public void rejectCall(String myUid, String callId) {
        if (myUid == null || myUid.isEmpty() || callId == null || callId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "rejected");
        updates.put("rejectedAt", com.google.firebase.database.ServerValue.TIMESTAMP);

        rootRef.child("userCalls").child(myUid).child("currentCall").updateChildren(updates);
    }

    /** Fungsi untuk endCall. */
    public void endCall(String myUid, String callId) {
        if (myUid == null || myUid.isEmpty() || callId == null || callId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "ended");
        updates.put("endedAt", com.google.firebase.database.ServerValue.TIMESTAMP);

        rootRef.child("userCalls").child(myUid).child("currentCall").updateChildren(updates);
    }

    /** Simpan atau hantar data Answer. */
    public void sendAnswer(String callId, String sdp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "answer");
        payload.put("sdp", sdp);
        rootRef.child("calls").child(callId).child("answer").setValue(payload);
    }

    /** Simpan atau hantar data IceCandidate. */
    public void sendIceCandidate(String callId, String role, String sdpMid, int sdpMLineIndex, String sdp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("sdpMid", sdpMid);
        payload.put("sdpMLineIndex", sdpMLineIndex);
        payload.put("candidate", sdp);

        rootRef.child("calls").child(callId).child(role).push().setValue(payload);
    }

    /** Dengar tawaran SDP Offer dari pemanggil melalui RTDB. */
    public void listenForOffer(String callId, ValueEventListener listener) {
        rootRef.child("calls").child(callId).child("offer").addValueEventListener(listener);
    }

    /** Dengar maklumat ICE Candidates untuk bina sambungan P2P. */
    public void listenForIceCandidates(String callId, String role, ChildEventListener listener) {
        rootRef.child("calls").child(callId).child(role).addChildEventListener(listener);
    }

    /** Fungsi untuk setCameraState. */
    public void setCameraState(String callId, String role, boolean isEnabled) {
        if (callId == null || callId.isEmpty()) return;
        rootRef.child("calls").child(callId).child("cameraState").child(role).setValue(isEnabled);
    }

    /** Fungsi untuk listenForCameraState. */
    public void listenForCameraState(String callId, String role, ValueEventListener listener) {
        if (callId == null || callId.isEmpty()) return;
        rootRef.child("calls").child(callId).child("cameraState").child(role).addValueEventListener(listener);
    }

    /** Padam atau bersihkan CameraStateListener. */
    public void removeCameraStateListener(String callId, String role, ValueEventListener listener) {
        if (callId == null || listener == null) return;
        rootRef.child("calls").child(callId).child("cameraState").child(role).removeEventListener(listener);
    }

    /** Fungsi untuk setMicState. */
    public void setMicState(String callId, String role, boolean isEnabled) {
        if (callId == null || callId.isEmpty()) return;
        rootRef.child("calls").child(callId).child("micState").child(role).setValue(isEnabled);
    }

    /** Fungsi untuk listenForMicState. */
    public void listenForMicState(String callId, String role, ValueEventListener listener) {
        if (callId == null || callId.isEmpty()) return;
        rootRef.child("calls").child(callId).child("micState").child(role).addValueEventListener(listener);
    }

    /** Padam atau bersihkan MicStateListener. */
    public void removeMicStateListener(String callId, String role, ValueEventListener listener) {
        if (callId == null || listener == null) return;
        rootRef.child("calls").child(callId).child("micState").child(role).removeEventListener(listener);
    }

    /** Padam atau bersihkan OfferListener. */
    public void removeOfferListener(String callId, ValueEventListener listener) {
        if (callId == null || listener == null) return;
        rootRef.child("calls").child(callId).child("offer").removeEventListener(listener);
    }

    /** Padam atau bersihkan IceCandidateListener. */
    public void removeIceCandidateListener(String callId, String role, ChildEventListener listener) {
        if (callId == null || listener == null) return;
        rootRef.child("calls").child(callId).child(role).removeEventListener(listener);
    }

    /** Ambil atau muat data String. */
    private String getString(DataSnapshot snapshot, String key) {
        if (snapshot.hasChild(key)) {
            Object val = snapshot.child(key).getValue();
            return val != null ? String.valueOf(val) : "";
        }
        return "";
    }
}

