package com.example.resqtap.app;

import com.example.resqtap.notification.NotificationHelper;
import com.example.resqtap.notification.NotificationUtils;
import com.example.resqtap.notification.ResQTapMessagingService;
import com.example.resqtap.room.FirebaseRoomClient;
import com.example.resqtap.room.LiveRoomTrackingService;
import com.example.resqtap.utils.LocaleUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;


/**
 * ResQTapApp
 * Application class: setup notification channel, tema gelap/cerah, dan config awal app.
 */
public class ResQTapApp extends Application {
    /** Fungsi untuk attachBaseContext. */
    @Override
    protected void attachBaseContext(Context base) {
        Context wrapped = LocaleUtils.wrap(base);
        super.attachBaseContext(wrapped == null ? base : wrapped);
    }

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    public void onCreate() {
        super.onCreate();
        LocaleUtils.applySavedLocale(this);

        ThemeUtils.applySavedNightMode(this);
        com.example.resqtap.security.AppLockManager.init(this);
        try {


            FirebaseDatabase db = FirebaseDatabase.getInstance(FirebaseRoomClient.DATABASE_URL);
            db.setPersistenceEnabled(true);

            // Pre-sync hot paths supaya data sedia bila diperlukan
            db.getReference("registeredEmails").keepSynced(true);
            FirebaseUser preUser = FirebaseAuth.getInstance().getCurrentUser();
            if (preUser != null) {
                String preUid = preUser.getUid();
                db.getReference("users").child(preUid).keepSynced(true);
                db.getReference("userRooms").child(preUid).keepSynced(true);
                db.getReference("userFriends").child(preUid).keepSynced(true);
            }
        } catch (Exception ignored) {

        }

        try {
            NotificationUtils.ensureBellChannel(this);
        } catch (Exception ignored) {
        }
        try {
            NotificationUtils.ensureAdminChannel(this);
        } catch (Exception ignored) {
        }
        try {
            NotificationHelper.ensureSosChannel(this);
        } catch (Exception ignored) {
        }
        try {
            ResQTapMessagingService.syncCurrentToken(this);
        } catch (Exception ignored) {
        }
        try {
            com.example.resqtap.chat.RateServiceManager.init(this);
        } catch (Exception ignored) {
        }
        try {
            com.example.resqtap.call.IncomingCallManager.init(this);
        } catch (Exception ignored) {
        }

        try {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            if (u != null) {
                String code = String.valueOf(UserPrefs.getActiveRoomCode(this) == null ? "" : UserPrefs.getActiveRoomCode(this)).trim();
                if (!code.isEmpty()) {
                    String uid = u.getUid();
                    String name = UserPrefs.getName(this);
                    if (name == null || name.trim().isEmpty()) name = "User";
                    String photoUrl = String.valueOf(UserPrefs.getPhotoUrl(this) == null ? "" : UserPrefs.getPhotoUrl(this)).trim();
                    String photoB64 = String.valueOf(UserPrefs.getPhotoB64(this) == null ? "" : UserPrefs.getPhotoB64(this)).trim();

                    android.content.Intent i = new android.content.Intent(this, LiveRoomTrackingService.class);
                    i.setAction(LiveRoomTrackingService.ACTION_START);
                    i.putExtra(LiveRoomTrackingService.EXTRA_ROOM_CODE, code);
                    i.putExtra(LiveRoomTrackingService.EXTRA_UID, uid);
                    i.putExtra(LiveRoomTrackingService.EXTRA_NAME, name);
                    i.putExtra(LiveRoomTrackingService.EXTRA_PHOTO_URL, photoUrl);
                    i.putExtra(LiveRoomTrackingService.EXTRA_PHOTO_B64, photoB64);
                    if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(i);
                    else startService(i);
                }
            }
        } catch (Exception ignored) {
        }
    }
}
