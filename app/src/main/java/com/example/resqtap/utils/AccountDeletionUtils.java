package com.example.resqtap.utils;

import com.example.resqtap.R;

import com.example.resqtap.auth.LoginActivity;
import com.example.resqtap.room.FirebaseRoomClient;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AccountDeletionUtils
 * Utility delete account: re-auth -> RTDB wipe -> Auth delete -> redirect.
 */
public final class AccountDeletionUtils {
    private static final String TAG = "AccountDeletionUtils";

    private AccountDeletionUtils() {
    }

    /** Dialog pengesahan awal sebelum padam akaun. */
    public static void confirmAndDelete(Activity activity, ExecutorService executor) {
        if (activity == null || executor == null) return;
        new MaterialAlertDialogBuilder(activity)
                .setMessage(R.string.delete_account_confirm)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(android.R.string.ok, (d, w) -> showReauthDialog(activity, executor))
                .show();
    }

    /** Dialog minta password untuk re-authenticate sebelum padam. */
    private static void showReauthDialog(Activity activity, ExecutorService executor) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            Toast.makeText(activity, R.string.delete_account_requires_relogin, Toast.LENGTH_LONG).show();
            return;
        }

        TextInputLayout passwordLayout = new TextInputLayout(activity);
        passwordLayout.setHint(activity.getString(R.string.delete_account_password_hint));
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

        TextInputEditText passwordInput = new TextInputEditText(passwordLayout.getContext());
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordLayout.addView(passwordInput);

        FrameLayout container = new FrameLayout(activity);
        int pad = (int) (20 * activity.getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad, pad, 0);
        container.addView(passwordLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.delete_account_reauth_title)
                .setMessage(R.string.delete_account_reauth_desc)
                .setView(container)
                .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.delete_account_reauth_action, (d, w) -> {
                    String password = passwordInput.getText() != null
                            ? passwordInput.getText().toString().trim() : "";
                    if (password.isEmpty()) {
                        Toast.makeText(activity, R.string.delete_account_password_hint, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(activity, R.string.toast_processing, Toast.LENGTH_SHORT).show();
                    executor.execute(() -> reauthAndDelete(activity, executor, user, password));
                })
                .show();
    }

    /** Re-authenticate -> RTDB wipe -> Auth delete -> stop services + redirect. */
    private static void reauthAndDelete(Activity activity, ExecutorService executor,
                                         FirebaseUser user, String password) {
        try {
            // 1. Re-authenticate pengguna
            Tasks.await(user.reauthenticate(
                    EmailAuthProvider.getCredential(user.getEmail(), password)),
                    15, TimeUnit.SECONDS);
            Log.i(TAG, "Re-authentication successful");

            String uid = user.getUid() != null ? user.getUid().trim() : "";
            if (uid.isEmpty()) uid = UserPrefs.getUid(activity);
            final String finalUid = uid;

            // 2. RTDB wipe dahulu (auth token masih valid, belum signOut)
            if (finalUid != null && !finalUid.isEmpty()) {
                try {
                    FirebaseRoomClient.deleteAccountData(finalUid);
                    Log.i(TAG, "RTDB data wiped for uid=" + finalUid);
                } catch (Exception e) {
                    Log.e(TAG, "RTDB deleteAccountData failed uid=" + finalUid, e);
                }
            }

            // 3. Auth delete (token masih sah selepas re-auth)
            try {
                Tasks.await(user.delete(), 15, TimeUnit.SECONDS);
                Log.i(TAG, "Firebase Auth account deleted");
            } catch (Exception e) {
                Log.e(TAG, "Auth user.delete() failed", e);
            }

            // 4. Baru stop services + clear local + redirect ke login
            try {
                com.example.resqtap.sos.SosServiceStarter.stop(activity);
            } catch (Exception ignored) {}
            UserPrefs.clearAccountData(activity);
            activity.runOnUiThread(() -> goToLogin(activity));

        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                Log.e(TAG, "Wrong password for re-auth", e);
            } else {
                Log.e(TAG, "reauthAndDelete failed", e);
            }
            activity.runOnUiThread(() ->
                    Toast.makeText(activity, R.string.delete_account_failed, Toast.LENGTH_LONG).show());
        }
    }

    /** Redirect ke LoginActivity selepas akaun dipadam. */
    private static void goToLogin(Activity activity) {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception ignored) {}
        Toast.makeText(activity, R.string.delete_account_success, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
