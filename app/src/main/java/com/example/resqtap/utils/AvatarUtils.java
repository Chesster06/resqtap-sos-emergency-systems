package com.example.resqtap.utils;

import android.net.Uri;
import android.widget.ImageView;


/**
 * AvatarUtils
 * Utility load avatar user dari URL, Storage gs://, atau Base64.
 */
public final class AvatarUtils {
    private AvatarUtils() {}

    /** Fungsi untuk applyAvatar. */
    public static void applyAvatar(ImageView view, String photoB64, String photoUri, int fallbackRes) {
        if (view == null) return;
        String b64 = String.valueOf(photoB64 == null ? "" : photoB64).trim();
        if (!b64.isEmpty()) {
            try {
                byte[] bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT);
                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bmp != null) {
                    view.setImageBitmap(bmp);
                    return;
                }
            } catch (Exception ignored) {
            }
        }

        String uri = String.valueOf(photoUri == null ? "" : photoUri).trim();
        if (!uri.isEmpty()) {
            try {
                view.setImageURI(Uri.parse(uri));
                return;
            } catch (Exception ignored) {
            }
        }

        view.setImageResource(fallbackRes);
    }
}

