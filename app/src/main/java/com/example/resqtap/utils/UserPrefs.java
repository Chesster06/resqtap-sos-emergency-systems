package com.example.resqtap.utils;
import com.example.resqtap.R;

import com.example.resqtap.contacts.EmergencyContact;

import android.content.Context;
import android.content.SharedPreferences;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;


/**
 * UserPrefs
 * Helper SharedPreferences untuk simpan data session user.
 */
public final class UserPrefs {
    private static final String PREFS = "ResQTap_prefs";
    private static final String KEY_UID = "uid";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PUBLIC_ID = "public_id";
    private static final String KEY_PHOTO_URI = "photo_uri";
    private static final String KEY_PHOTO_URL = "photo_url";
    private static final String KEY_PHOTO_B64 = "photo_b64";
    private static final String KEY_NIGHT_MODE = "night_mode";
    private static final String KEY_BLOOD_TYPE = "blood_type";
    private static final String KEY_ALLERGIES = "allergies";
    private static final String KEY_EXISTING_CONDITIONS = "existing_conditions";
    private static final String KEY_HIDE_MEDICAL_INFO = "hide_medical_info";
    private static final String KEY_VIBRATION_STRENGTH = "vibration_strength";
    private static final String KEY_TTS_ENABLED = "tts_enabled";
    private static final String KEY_FULLSCREEN_QUICK_MESSAGE = "fullscreen_quick_message";
    private static final String KEY_APP_LOCK_ENABLED = "app_lock_enabled";
    private static final String KEY_APP_LOCK_PIN = "app_lock_pin";
    private static final String KEY_FINGERPRINT_ENABLED = "fingerprint_enabled";
    private static final String KEY_FACE_ID_ENABLED = "face_id_enabled";
    private static final String KEY_BIOMETRIC_PIN = "biometric_pin";
    private static final String KEY_DURESS_SAFEGUARD_ENABLED = "duress_safeguard_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_RELIGION = "religion";
    private static final String KEY_PHONE = "phone_number";
    private static final String KEY_DOB = "date_of_birth";
    private static final String KEY_ETHNICITY = "ethnicity";
    private static final String KEY_IC_NUMBER = "ic_number";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_WEIGHT = "weight";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_MEDICATIONS = "medications";
    private static final String KEY_ORGAN_DONOR = "organ_donor";
    private static final String KEY_EMERGENCY_CONTACTS_JSON = "emergency_contacts_json";
    private static final String KEY_LOW_BATTERY_ALERT = "low_battery_alert";
    private static final String KEY_BATTERY_SAVER_MODE = "battery_saver_mode";
    private static final String KEY_NOTIF_UNREAD = "notif_unread";
    private static final String KEY_LAST_ADMIN_NOTIFICATION_ID = "last_admin_notification_id";
    private static final String KEY_FCM_TOKEN = "fcm_token";
    private static final String KEY_PENDING_PHOTO_URI = "pending_photo_uri";
    private static final String KEY_ACTIVE_ROOM_CODE = "active_room_code";
    private static final String KEY_TOS_ACCEPTED = "tos_accepted";
    private static final String KEY_SOS_LAST_SEEN_AT_PREFIX = "sos_last_seen_at_";
    private static final String KEY_SOS_LAST_HANDLED_ID_PREFIX = "sos_last_handled_id_";
    private static final String KEY_SOS_LISTENER_BASELINE_PREFIX = "sos_listener_baseline_";
    private static final String KEY_ROOM_MAP_VISIBLE = "room_map_visible";
    private static final String KEY_SOS_FOCUS_ROOM = "sos_focus_room";
    private static final String KEY_SOS_FOCUS_SENDER_UID = "sos_focus_sender_uid";
    private static final String KEY_SOS_FOCUS_ALERT_ID = "sos_focus_alert_id";
    private static final String KEY_SOS_FOCUS_CREATED_AT = "sos_focus_created_at";
    private static final String KEY_SOS_FOCUS_CONSUMED = "sos_focus_consumed";

    private static final String KEY_SOS_UI_ROOM = "sos_ui_room";
    private static final String KEY_SOS_UI_SENDER_UID = "sos_ui_sender_uid";
    private static final String KEY_SOS_UI_ALERT_ID = "sos_ui_alert_id";
    private static final String KEY_SOS_UI_UNTIL_MS = "sos_ui_until_ms";
    private static final String KEY_BATTERY_OPT_PROMPTED = "battery_opt_prompted";
    private static final String KEY_MAP_TYPE_SATELLITE = "map_type_satellite";
    private static final String KEY_MAP_TRAFFIC_ENABLED = "map_traffic_enabled";

    private UserPrefs() {
    }

    /** Fungsi untuk prefs. */
    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Ambil atau muat data OrCreateUid. */
    public static String getOrCreateUid(Context context) {
        SharedPreferences p = prefs(context);
        String uid = p.getString(KEY_UID, null);
        uid = uid == null ? "" : uid.trim().toUpperCase();
        if (!uid.matches("^ONE-[A-Z0-9]{4}$")) {
            uid = generateShortUid();
            p.edit().putString(KEY_UID, uid).apply();
        }
        return uid;
    }

    /** Ambil atau muat data OrCreateDeviceId. */
    public static String getOrCreateDeviceId(Context context) {
        SharedPreferences p = prefs(context);
        String id = p.getString(KEY_DEVICE_ID, null);
        id = id == null ? "" : id.trim();
        if (id.length() < 8) {
            id = generateDeviceId();
            p.edit().putString(KEY_DEVICE_ID, id).apply();
        }
        return id;
    }

    /** Fungsi untuk setUid. */
    public static void setUid(Context context, String uid) {
        prefs(context).edit().putString(KEY_UID, uid == null ? "" : uid.trim()).apply();
    }

    /** Ambil atau muat data Uid. */
    public static String getUid(Context context) {
        return prefs(context).getString(KEY_UID, "");
    }

    /** Ambil atau muat data Email. */
    public static String getEmail(Context context) {
        return prefs(context).getString(KEY_EMAIL, "");
    }

    /** Fungsi untuk setEmail. */
    public static void setEmail(Context context, String email) {
        prefs(context).edit().putString(KEY_EMAIL, email == null ? "" : email.trim()).apply();
    }

    /** Ambil atau muat data PublicId. */
    public static String getPublicId(Context context) {
        return prefs(context).getString(KEY_PUBLIC_ID, "");
    }

    /** Fungsi untuk setPublicId. */
    public static void setPublicId(Context context, String publicId) {
        prefs(context).edit().putString(KEY_PUBLIC_ID, publicId == null ? "" : publicId.trim().toUpperCase()).apply();
    }

    /** Ambil atau muat data PhotoUri. */
    public static String getPhotoUri(Context context) {
        return prefs(context).getString(KEY_PHOTO_URI, "");
    }

    /** Fungsi untuk setPhotoUri. */
    public static void setPhotoUri(Context context, String uri) {
        prefs(context).edit().putString(KEY_PHOTO_URI, uri == null ? "" : uri.trim()).apply();
    }

    /** Ambil atau muat data PhotoUrl. */
    public static String getPhotoUrl(Context context) {
        return prefs(context).getString(KEY_PHOTO_URL, "");
    }

    /** Fungsi untuk setPhotoUrl. */
    public static void setPhotoUrl(Context context, String url) {
        prefs(context).edit().putString(KEY_PHOTO_URL, url == null ? "" : url.trim()).apply();
    }

    /** Ambil atau muat data PhotoB64. */
    public static String getPhotoB64(Context context) {
        return prefs(context).getString(KEY_PHOTO_B64, "");
    }

    /** Fungsi untuk setPhotoB64. */
    public static void setPhotoB64(Context context, String b64) {
        prefs(context).edit().putString(KEY_PHOTO_B64, b64 == null ? "" : b64.trim()).apply();
    }

    /** Fungsi untuk setUidFromPhone. */
    public static void setUidFromPhone(Context context, String phone) {
        String uid = deriveUidFromPhone(phone);
        prefs(context).edit().putString(KEY_UID, uid).apply();
    }

    /** Fungsi untuk deriveUidFromPhone. */
    private static String deriveUidFromPhone(String phone) {
        String normalized = normalizePhone(phone);
        if (normalized.isEmpty()) return generateShortUid();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(normalized.getBytes(StandardCharsets.UTF_8));
            final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder("ONE-");

            for (int i = 0; i < 4; i++) {
                int v = hash[i] & 0xFF;
                sb.append(alphabet.charAt(v % alphabet.length()));
            }
            return sb.toString();
        } catch (Exception e) {
            return generateShortUid();
        }
    }

    /** Fungsi untuk normalizePhone. */
    private static String normalizePhone(String phone) {
        String p = String.valueOf(phone == null ? "" : phone).trim();
        if (p.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
        }
        return sb.toString();
    }

    /** Fungsi untuk generateShortUid. */
    private static String generateShortUid() {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder("ONE-");
        for (int i = 0; i < 4; i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }

    /** Fungsi untuk generateDeviceId. */
    private static String generateDeviceId() {
        final String hex = "0123456789abcdef";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 16; i++) sb.append(hex.charAt(r.nextInt(hex.length())));
        return sb.toString();
    }

    /** Ambil atau muat data Name. */
    public static String getName(Context context) {
        return prefs(context).getString(KEY_NAME, "");
    }

    /** Fungsi untuk setName. */
    public static void setName(Context context, String name) {
        prefs(context).edit().putString(KEY_NAME, name == null ? "" : name.trim()).apply();
    }

    /** Ambil atau muat data LastAdminNotificationId. */
    public static String getLastAdminNotificationId(Context context) {
        return prefs(context).getString(KEY_LAST_ADMIN_NOTIFICATION_ID, "");
    }

    /** Fungsi untuk setLastAdminNotificationId. */
    public static void setLastAdminNotificationId(Context context, String notificationId) {
        prefs(context).edit().putString(KEY_LAST_ADMIN_NOTIFICATION_ID, notificationId == null ? "" : notificationId.trim()).apply();
    }

    /** Padam atau bersihkan AccountData. */
    public static void clearAccountData(Context context) {

        prefs(context).edit()
                .remove(KEY_UID)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL)
                .remove(KEY_PUBLIC_ID)
                .remove(KEY_PHOTO_URI)
                .remove(KEY_PHOTO_URL)
                .remove(KEY_PHOTO_B64)
                .remove(KEY_PENDING_PHOTO_URI)
                .remove(KEY_BLOOD_TYPE)
                .remove(KEY_ALLERGIES)
                .remove(KEY_EXISTING_CONDITIONS)
                .remove(KEY_ADDRESS)
                .remove(KEY_RELIGION)
                .remove(KEY_PHONE)
                .remove(KEY_DOB)
                .remove(KEY_ETHNICITY)
                .remove(KEY_GENDER)
                .remove(KEY_WEIGHT)
                .remove(KEY_HEIGHT)
                .remove(KEY_HIDE_MEDICAL_INFO)
                .remove(KEY_EMERGENCY_CONTACTS_JSON)
                .remove(KEY_APP_LOCK_ENABLED)
                .remove(KEY_APP_LOCK_PIN)
                .remove(KEY_FINGERPRINT_ENABLED)
                .remove(KEY_FACE_ID_ENABLED)
                .remove(KEY_BIOMETRIC_PIN)
                .apply();
    }

    /** Ambil atau muat data PendingPhotoUri. */
    public static String getPendingPhotoUri(Context context) {
        return prefs(context).getString(KEY_PENDING_PHOTO_URI, "");
    }

    /** Fungsi untuk setPendingPhotoUri. */
    public static void setPendingPhotoUri(Context context, String uri) {
        prefs(context).edit().putString(KEY_PENDING_PHOTO_URI, uri == null ? "" : uri.trim()).apply();
    }

    /** Ambil atau muat data BloodType. */
    public static String getBloodType(Context context) {
        return prefs(context).getString(KEY_BLOOD_TYPE, "");
    }

    /** Fungsi untuk setBloodType. */
    public static void setBloodType(Context context, String bloodType) {
        prefs(context).edit().putString(KEY_BLOOD_TYPE, bloodType == null ? "" : bloodType.trim()).apply();
    }

    /** Ambil atau muat data Allergies. */
    public static String getAllergies(Context context) {
        return prefs(context).getString(KEY_ALLERGIES, "");
    }

    /** Fungsi untuk setAllergies. */
    public static void setAllergies(Context context, String allergies) {
        prefs(context).edit().putString(KEY_ALLERGIES, allergies == null ? "" : allergies.trim()).apply();
    }

    /** Ambil atau muat data ExistingConditions. */
    public static String getExistingConditions(Context context) {
        return prefs(context).getString(KEY_EXISTING_CONDITIONS, "");
    }

    /** Fungsi untuk setExistingConditions. */
    public static void setExistingConditions(Context context, String conditions) {
        prefs(context).edit().putString(KEY_EXISTING_CONDITIONS, conditions == null ? "" : conditions.trim()).apply();
    }

    /** Semak dan sahkan HideMedicalInfo. */
    public static boolean isHideMedicalInfo(Context context) {
        return prefs(context).getBoolean(KEY_HIDE_MEDICAL_INFO, false);
    }

    /** Fungsi untuk setHideMedicalInfo. */
    public static void setHideMedicalInfo(Context context, boolean hide) {
        prefs(context).edit().putBoolean(KEY_HIDE_MEDICAL_INFO, hide).apply();
    }

    /** Ambil atau muat data VibrationStrength. */
    public static String getVibrationStrength(Context context) {
        return prefs(context).getString(KEY_VIBRATION_STRENGTH, "medium");
    }

    /** Fungsi untuk setVibrationStrength. */
    public static void setVibrationStrength(Context context, String strength) {
        prefs(context).edit().putString(KEY_VIBRATION_STRENGTH, strength == null ? "medium" : strength).apply();
    }

    /** Semak dan sahkan TtsEnabled. */
    public static boolean isTtsEnabled(Context context) {
        return prefs(context).getBoolean(KEY_TTS_ENABLED, false);
    }

    /** Fungsi untuk setTtsEnabled. */
    public static void setTtsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_TTS_ENABLED, enabled).apply();
    }

    /** Semak dan sahkan FullscreenQuickMessage. */
    public static boolean isFullscreenQuickMessage(Context context) {
        return prefs(context).getBoolean(KEY_FULLSCREEN_QUICK_MESSAGE, false);
    }

    /** Fungsi untuk setFullscreenQuickMessage. */
    public static void setFullscreenQuickMessage(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_FULLSCREEN_QUICK_MESSAGE, enabled).apply();
    }

    /** Semak dan sahkan AppLockEnabled. */
    public static boolean isAppLockEnabled(Context context) {
        return prefs(context).getBoolean(KEY_APP_LOCK_ENABLED, false);
    }

    /** Fungsi untuk setAppLockEnabled. */
    public static void setAppLockEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply();
    }

    /** Ambil atau muat data AppLockPin. */
    public static String getAppLockPin(Context context) {
        return prefs(context).getString(KEY_APP_LOCK_PIN, "");
    }

    /** Fungsi untuk setAppLockPin. */
    public static void setAppLockPin(Context context, String pin) {
        prefs(context).edit().putString(KEY_APP_LOCK_PIN, pin == null ? "" : pin).apply();
    }

    /** Semak dan sahkan FingerprintEnabled. */
    public static boolean isFingerprintEnabled(Context context) {
        return prefs(context).getBoolean(KEY_FINGERPRINT_ENABLED, false);
    }

    /** Fungsi untuk setFingerprintEnabled. */
    public static void setFingerprintEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply();
    }

    /** Semak dan sahkan FaceIdEnabled. */
    public static boolean isFaceIdEnabled(Context context) {
        return prefs(context).getBoolean(KEY_FACE_ID_ENABLED, false);
    }

    /** Fungsi untuk setFaceIdEnabled. */
    public static void setFaceIdEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_FACE_ID_ENABLED, enabled).apply();
    }

    /** Ambil atau muat data BiometricPin. */
    public static String getBiometricPin(Context context) {
        return prefs(context).getString(KEY_BIOMETRIC_PIN, "");
    }

    /** Fungsi untuk setBiometricPin. */
    public static void setBiometricPin(Context context, String pin) {
        prefs(context).edit().putString(KEY_BIOMETRIC_PIN, pin == null ? "" : pin).apply();
    }

    /** Semak dan sahkan DuressSafeguardEnabled. */
    public static boolean isDuressSafeguardEnabled(Context context) {
        return prefs(context).getBoolean(KEY_DURESS_SAFEGUARD_ENABLED, true);
    }

    /** Fungsi untuk setDuressSafeguardEnabled. */
    public static void setDuressSafeguardEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DURESS_SAFEGUARD_ENABLED, enabled).apply();
    }

    /** Ambil atau muat data Language. */
    public static String getLanguage(Context context) {
        return prefs(context).getString(KEY_LANGUAGE, "en");
    }

    /** Fungsi untuk setLanguage. */
    public static void setLanguage(Context context, String lang) {
        String normalized = String.valueOf(lang == null ? "" : lang).trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("ms")) normalized = "ms";
        else if (normalized.startsWith("zh")) normalized = "zh";
        else if (normalized.startsWith("ta")) normalized = "ta";
        else if (normalized.startsWith("en")) normalized = "en";
        else normalized = "en";
        prefs(context).edit().putString(KEY_LANGUAGE, normalized).apply();
    }

    /** Ambil atau muat data Address. */
    public static String getAddress(Context context) {
        return prefs(context).getString(KEY_ADDRESS, "");
    }

    /** Fungsi untuk setAddress. */
    public static void setAddress(Context context, String address) {
        prefs(context).edit().putString(KEY_ADDRESS, address == null ? "" : address).apply();
    }

    /** Ambil atau muat data Religion. */
    public static String getReligion(Context context) {
        return prefs(context).getString(KEY_RELIGION, "");
    }

    /** Fungsi untuk setReligion. */
    public static void setReligion(Context context, String religion) {
        prefs(context).edit().putString(KEY_RELIGION, religion == null ? "" : religion).apply();
    }

    /** Ambil atau muat data PhoneNumber. */
    public static String getPhoneNumber(Context context) {
        return prefs(context).getString(KEY_PHONE, "");
    }

    /** Fungsi untuk setPhoneNumber. */
    public static void setPhoneNumber(Context context, String phone) {
        prefs(context).edit().putString(KEY_PHONE, phone == null ? "" : phone).apply();
    }

    /** Ambil atau muat data DateOfBirth. */
    public static String getDateOfBirth(Context context) {
        return prefs(context).getString(KEY_DOB, "");
    }

    /** Fungsi untuk setDateOfBirth. */
    public static void setDateOfBirth(Context context, String dob) {
        prefs(context).edit().putString(KEY_DOB, dob == null ? "" : dob).apply();
    }

    /** Ambil atau muat data Ethnicity. */
    public static String getEthnicity(Context context) {
        return prefs(context).getString(KEY_ETHNICITY, "");
    }

    /** Fungsi untuk setEthnicity. */
    public static void setEthnicity(Context context, String ethnicity) {
        prefs(context).edit().putString(KEY_ETHNICITY, ethnicity == null ? "" : ethnicity).apply();
    }

    /** Ambil atau muat data IcNumber. */
    public static String getIcNumber(Context context) {
        return prefs(context).getString(KEY_IC_NUMBER, "");
    }

    /** Fungsi untuk setIcNumber. */
    public static void setIcNumber(Context context, String ic) {
        prefs(context).edit().putString(KEY_IC_NUMBER, ic == null ? "" : ic).apply();
    }

    /** Ambil atau muat data Gender. */
    public static String getGender(Context context) {
        return prefs(context).getString(KEY_GENDER, "");
    }

    /** Fungsi untuk setGender. */
    public static void setGender(Context context, String gender) {
        prefs(context).edit().putString(KEY_GENDER, gender == null ? "" : gender).apply();
    }

    /** Ambil atau muat data Weight. */
    public static String getWeight(Context context) {
        return prefs(context).getString(KEY_WEIGHT, "");
    }

    /** Fungsi untuk setWeight. */
    public static void setWeight(Context context, String weight) {
        prefs(context).edit().putString(KEY_WEIGHT, weight == null ? "" : weight.trim()).apply();
    }

    /** Ambil atau muat data Height. */
    public static String getHeight(Context context) {
        return prefs(context).getString(KEY_HEIGHT, "");
    }

    /** Fungsi untuk setHeight. */
    public static void setHeight(Context context, String height) {
        prefs(context).edit().putString(KEY_HEIGHT, height == null ? "" : height.trim()).apply();
    }

    /** Ambil atau muat data Medications. */
    public static String getMedications(Context context) {
        return prefs(context).getString(KEY_MEDICATIONS, "");
    }

    /** Fungsi untuk setMedications. */
    public static void setMedications(Context context, String medications) {
        prefs(context).edit().putString(KEY_MEDICATIONS, medications == null ? "" : medications).apply();
    }

    /** Ambil atau muat data OrganDonor. */
    public static String getOrganDonor(Context context) {
        return prefs(context).getString(KEY_ORGAN_DONOR, "");
    }

    /** Fungsi untuk setOrganDonor. */
    public static void setOrganDonor(Context context, String donor) {
        prefs(context).edit().putString(KEY_ORGAN_DONOR, donor == null ? "" : donor).apply();
    }

    /** Semak dan sahkan PersonalInfoComplete. */
    public static boolean isPersonalInfoComplete(Context context) {
        if (isResqTapEmail(getEmail(context))) {
            return true;
        }
        return !getAddress(context).trim().isEmpty()
                && !getReligion(context).trim().isEmpty()
                && !getPhoneNumber(context).trim().isEmpty()
                && !getDateOfBirth(context).trim().isEmpty()
                && !getEthnicity(context).trim().isEmpty();
    }

    /** Semak dan sahkan ResqTapEmail. */
    private static boolean isResqTapEmail(String email) {
        if (email == null) return false;
        String value = email.trim().toLowerCase(java.util.Locale.ROOT);
        return value.endsWith("@resqtap.com");
    }

    public static java.util.ArrayList<EmergencyContact> getEmergencyContacts(Context context) {
        String raw = prefs(context).getString(KEY_EMERGENCY_CONTACTS_JSON, "");
        java.util.ArrayList<EmergencyContact> out = new java.util.ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;
        boolean dirty = false;
        try {
            org.json.JSONArray arr = new org.json.JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String id = o.optString("id", "").trim();
                String name = o.optString("name", "").trim();
                String rel = o.optString("relationship", "").trim();
                String phone = o.optString("phone", "").trim();
                String notes = o.optString("notes", "").trim();
                if (name.isEmpty() && phone.isEmpty()) continue;
                if (id.isEmpty()) {
                    id = java.util.UUID.randomUUID().toString();
                    dirty = true;
                }
                out.add(new EmergencyContact(id, name, rel, phone, notes));
            }
        } catch (Exception ignored) {
        }
        if (dirty) saveEmergencyContacts(context, out);
        return out;
    }

    /** Fungsi untuk addEmergencyContact. */
    public static void addEmergencyContact(Context context, EmergencyContact c) {
        if (c == null) return;
        String id = c.id == null ? "" : c.id.trim();
        String name = c.name == null ? "" : c.name.trim();
        String rel = c.relationship == null ? "" : c.relationship.trim();
        String phone = c.phone == null ? "" : c.phone.trim();
        String notes = c.notes == null ? "" : c.notes.trim();
        if (name.isEmpty() || phone.isEmpty()) return;

        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        if (hasEmergencyContactDuplicate(list, "", name, phone)) return;
        String finalId = id.isEmpty() ? java.util.UUID.randomUUID().toString() : id;
        list.add(new EmergencyContact(finalId, name, rel, phone, notes));
        saveEmergencyContacts(context, list);
    }

    /** Fungsi untuk setEmergencyContacts. */
    public static void setEmergencyContacts(Context context, java.util.ArrayList<EmergencyContact> contacts) {
        java.util.ArrayList<EmergencyContact> list = new java.util.ArrayList<>();
        if (contacts != null) list.addAll(contacts);
        saveEmergencyContacts(context, list);
    }

    /** Simpan atau hantar data EmergencyContact. */
    public static void updateEmergencyContact(Context context, String id, String name, String relationship, String phone) {
        updateEmergencyContact(context, id, name, relationship, phone, "");
    }

    public static void updateEmergencyContact(Context context, String id, String name, String relationship, String phone, String notes) {
        String safeId = String.valueOf(id == null ? "" : id).trim();
        if (safeId.isEmpty()) return;
        String n = String.valueOf(name == null ? "" : name).trim();
        String r = String.valueOf(relationship == null ? "" : relationship).trim();
        String p = String.valueOf(phone == null ? "" : phone).trim();
        String not = String.valueOf(notes == null ? "" : notes).trim();
        if (n.isEmpty() || p.isEmpty()) return;

        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        if (hasEmergencyContactDuplicate(list, safeId, n, p)) return;
        for (int i = 0; i < list.size(); i++) {
            EmergencyContact c = list.get(i);
            if (c == null) continue;
            if (safeId.equals(c.id)) {
                list.set(i, new EmergencyContact(safeId, n, r, p, not));
                saveEmergencyContacts(context, list);
                return;
            }
        }
    }

    /** Semak dan sahkan EmergencyContactDuplicate. */
    public static boolean hasEmergencyContactDuplicate(Context context, String excludeId, String name, String phone) {
        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        return hasEmergencyContactDuplicate(list, excludeId, name, phone);
    }

    /** Semak dan sahkan EmergencyContactPhoneDuplicate. */
    public static boolean hasEmergencyContactPhoneDuplicate(Context context, String excludeId, String phone) {
        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        if (list == null) return false;
        String ex = String.valueOf(excludeId == null ? "" : excludeId).trim();
        String p = normalizePhoneNumber(phone);
        if (p.isEmpty()) return false;
        for (EmergencyContact c : list) {
            if (c == null) continue;
            if (!ex.isEmpty() && ex.equals(String.valueOf(c.id == null ? "" : c.id).trim())) continue;
            String cp = normalizePhoneNumber(c.phone);
            if (!cp.isEmpty() && cp.equals(p)) return true;
        }
        return false;
    }

    /** Semak dan sahkan EmergencyContactNameDuplicate. */
    public static boolean hasEmergencyContactNameDuplicate(Context context, String excludeId, String name) {
        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        if (list == null) return false;
        String ex = String.valueOf(excludeId == null ? "" : excludeId).trim();
        String n = normalizeName(name);
        if (n.isEmpty()) return false;
        for (EmergencyContact c : list) {
            if (c == null) continue;
            if (!ex.isEmpty() && ex.equals(String.valueOf(c.id == null ? "" : c.id).trim())) continue;
            String cn = normalizeName(c.name);
            if (!cn.isEmpty() && cn.equals(n)) return true;
        }
        return false;
    }

    /** Semak dan sahkan EmergencyContactDuplicate. */
    private static boolean hasEmergencyContactDuplicate(java.util.ArrayList<EmergencyContact> list, String excludeId, String name, String phone) {
        if (list == null) return false;
        String ex = String.valueOf(excludeId == null ? "" : excludeId).trim();
        String n = normalizeName(name);
        String p = normalizePhoneNumber(phone);
        if (n.isEmpty() || p.isEmpty()) return false;
        for (EmergencyContact c : list) {
            if (c == null) continue;
            if (!ex.isEmpty() && ex.equals(String.valueOf(c.id == null ? "" : c.id).trim())) continue;
            String cn = normalizeName(c.name);
            String cp = normalizePhoneNumber(c.phone);
            if (!cn.isEmpty() && !cp.isEmpty() && cn.equals(n) && cp.equals(p)) return true;
        }
        return false;
    }

    /** Fungsi untuk normalizeName. */
    private static String normalizeName(String name) {
        return String.valueOf(name == null ? "" : name).trim().toUpperCase(java.util.Locale.ROOT);
    }

    /** Fungsi untuk normalizePhoneNumber. */
    private static String normalizePhoneNumber(String phone) {
        String p = String.valueOf(phone == null ? "" : phone).trim();
        if (p.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < p.length(); i++) {
            char c = p.charAt(i);
            if (c >= '0' && c <= '9') sb.append(c);
        }
        return sb.toString();
    }

    /** Padam atau bersihkan EmergencyContactById. */
    public static void deleteEmergencyContactById(Context context, String id) {
        String safeId = String.valueOf(id == null ? "" : id).trim();
        if (safeId.isEmpty()) return;
        java.util.ArrayList<EmergencyContact> list = getEmergencyContacts(context);
        for (int i = 0; i < list.size(); i++) {
            EmergencyContact c = list.get(i);
            if (c == null) continue;
            if (safeId.equals(c.id)) {
                list.remove(i);
                break;
            }
        }
        saveEmergencyContacts(context, list);
    }

    /** Simpan atau hantar data EmergencyContacts. */
    private static void saveEmergencyContacts(Context context, java.util.ArrayList<EmergencyContact> list) {
        org.json.JSONArray arr = new org.json.JSONArray();
        if (list != null) {
            for (EmergencyContact c : list) {
                if (c == null) continue;
                String id = c.id == null ? "" : c.id.trim();
                String name = c.name == null ? "" : c.name.trim();
                String rel = c.relationship == null ? "" : c.relationship.trim();
                String phone = c.phone == null ? "" : c.phone.trim();
                String notes = c.notes == null ? "" : c.notes.trim();
                if (name.isEmpty() || phone.isEmpty()) continue;
                org.json.JSONObject o = new org.json.JSONObject();
                try {
                    o.put("id", id.isEmpty() ? java.util.UUID.randomUUID().toString() : id);
                    o.put("name", name);
                    o.put("relationship", rel);
                    o.put("phone", phone);
                    o.put("notes", notes);
                    arr.put(o);
                } catch (Exception ignored) {
                }
            }
        }
        prefs(context).edit().putString(KEY_EMERGENCY_CONTACTS_JSON, arr.toString()).apply();
    }

    /** Semak dan sahkan LowBatteryAlert. */
    public static boolean isLowBatteryAlert(Context context) {
        return prefs(context).getBoolean(KEY_LOW_BATTERY_ALERT, false);
    }

    /** Fungsi untuk setLowBatteryAlert. */
    public static void setLowBatteryAlert(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_LOW_BATTERY_ALERT, enabled).apply();
    }

    /** Semak dan sahkan BatterySaverMode. */
    public static boolean isBatterySaverMode(Context context) {
        return prefs(context).getBoolean(KEY_BATTERY_SAVER_MODE, false);
    }

    /** Fungsi untuk setBatterySaverMode. */
    public static void setBatterySaverMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_BATTERY_SAVER_MODE, enabled).apply();
    }

    /** Ambil atau muat data UnreadNotifications. */
    public static int getUnreadNotifications(Context context) {
        return prefs(context).getInt(KEY_NOTIF_UNREAD, 0);
    }

    /** Fungsi untuk setUnreadNotifications. */
    public static void setUnreadNotifications(Context context, int count) {
        int safe = Math.max(0, count);
        prefs(context).edit().putInt(KEY_NOTIF_UNREAD, safe).apply();
    }

    /** Fungsi untuk incrementUnreadNotifications. */
    public static void incrementUnreadNotifications(Context context) {
        int next = getUnreadNotifications(context) + 1;
        setUnreadNotifications(context, next);
    }

    /** Ambil atau muat data FcmToken. */
    public static String getFcmToken(Context context) {
        return prefs(context).getString(KEY_FCM_TOKEN, "");
    }

    /** Fungsi untuk setFcmToken. */
    public static void setFcmToken(Context context, String token) {
        prefs(context).edit().putString(KEY_FCM_TOKEN, token == null ? "" : token.trim()).apply();
    }

    /** Ambil atau muat data ActiveRoomCode. */
    public static String getActiveRoomCode(Context context) {
        return prefs(context).getString(KEY_ACTIVE_ROOM_CODE, "");
    }

    /** Fungsi untuk setActiveRoomCode. */
    public static void setActiveRoomCode(Context context, String roomCode) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        prefs(context).edit().putString(KEY_ACTIVE_ROOM_CODE, code).apply();
    }

    /** Ambil atau muat data SosLastSeenAt. */
    public static long getSosLastSeenAt(Context context, String roomCode) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return 0L;
        return prefs(context).getLong(KEY_SOS_LAST_SEEN_AT_PREFIX + code, 0L);
    }

    /** Fungsi untuk setSosLastSeenAt. */
    public static void setSosLastSeenAt(Context context, String roomCode, long createdAtMs) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return;
        prefs(context).edit().putLong(KEY_SOS_LAST_SEEN_AT_PREFIX + code, Math.max(0L, createdAtMs)).apply();
    }

    /** Ambil atau muat data SosLastHandledId. */
    public static String getSosLastHandledId(Context context, String roomCode) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return "";
        return prefs(context).getString(KEY_SOS_LAST_HANDLED_ID_PREFIX + code, "");
    }

    /** Fungsi untuk setSosLastHandledId. */
    public static void setSosLastHandledId(Context context, String roomCode, String alertId) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return;
        String id = String.valueOf(alertId == null ? "" : alertId).trim();
        prefs(context).edit().putString(KEY_SOS_LAST_HANDLED_ID_PREFIX + code, id).apply();
    }

    /** Ambil atau muat data SosListenerBaselineAt. */
    public static long getSosListenerBaselineAt(Context context, String roomCode) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return 0L;
        return prefs(context).getLong(KEY_SOS_LISTENER_BASELINE_PREFIX + code, 0L);
    }

    /** Fungsi untuk setSosListenerBaselineAt. */
    public static void setSosListenerBaselineAt(Context context, String roomCode, long baselineMs) {
        String code = String.valueOf(roomCode == null ? "" : roomCode).trim().toUpperCase(java.util.Locale.ROOT);
        if (code.isEmpty()) return;
        prefs(context).edit().putLong(KEY_SOS_LISTENER_BASELINE_PREFIX + code, Math.max(0L, baselineMs)).apply();
    }

    /** Semak dan sahkan RoomMapVisible. */
    public static boolean isRoomMapVisible(Context context) {
        return prefs(context).getBoolean(KEY_ROOM_MAP_VISIBLE, false);
    }

    /** Fungsi untuk setRoomMapVisible. */
    public static void setRoomMapVisible(Context context, boolean visible) {
        prefs(context).edit().putBoolean(KEY_ROOM_MAP_VISIBLE, visible).apply();
    }

    /** Semak dan sahkan BatteryOptPrompted. */
    public static boolean isBatteryOptPrompted(Context context) {
        return prefs(context).getBoolean(KEY_BATTERY_OPT_PROMPTED, false);
    }

    /** Fungsi untuk setBatteryOptPrompted. */
    public static void setBatteryOptPrompted(Context context, boolean prompted) {
        prefs(context).edit().putBoolean(KEY_BATTERY_OPT_PROMPTED, prompted).apply();
    }

    /** Fungsi untuk setPendingSosFocus. */
    public static void setPendingSosFocus(Context context, String roomId, String senderUid, String alertId, long createdAtMs) {
        if (context == null) return;
        String room = String.valueOf(roomId == null ? "" : roomId).trim().toUpperCase(java.util.Locale.ROOT);
        String sender = String.valueOf(senderUid == null ? "" : senderUid).trim();
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        long at = Math.max(0L, createdAtMs);
        prefs(context).edit()
                .putString(KEY_SOS_FOCUS_ROOM, room)
                .putString(KEY_SOS_FOCUS_SENDER_UID, sender)
                .putString(KEY_SOS_FOCUS_ALERT_ID, aId)
                .putLong(KEY_SOS_FOCUS_CREATED_AT, at)
                .putBoolean(KEY_SOS_FOCUS_CONSUMED, false)
                .apply();
    }

    /** Ambil atau muat data PendingSosFocusRoom. */
    public static String getPendingSosFocusRoom(Context context) {
        return prefs(context).getString(KEY_SOS_FOCUS_ROOM, "");
    }

    /** Ambil atau muat data PendingSosFocusSenderUid. */
    public static String getPendingSosFocusSenderUid(Context context) {
        return prefs(context).getString(KEY_SOS_FOCUS_SENDER_UID, "");
    }

    /** Ambil atau muat data PendingSosFocusAlertId. */
    public static String getPendingSosFocusAlertId(Context context) {
        return prefs(context).getString(KEY_SOS_FOCUS_ALERT_ID, "");
    }

    /** Ambil atau muat data PendingSosFocusCreatedAt. */
    public static long getPendingSosFocusCreatedAt(Context context) {
        return prefs(context).getLong(KEY_SOS_FOCUS_CREATED_AT, 0L);
    }

    /** Semak dan sahkan PendingSosFocusConsumed. */
    public static boolean isPendingSosFocusConsumed(Context context) {
        return prefs(context).getBoolean(KEY_SOS_FOCUS_CONSUMED, true);
    }

    /** Fungsi untuk markPendingSosFocusConsumed. */
    public static void markPendingSosFocusConsumed(Context context) {
        prefs(context).edit().putBoolean(KEY_SOS_FOCUS_CONSUMED, true).apply();
    }

    /** Fungsi untuk setSosUiBlink. */
    public static void setSosUiBlink(Context context, String roomId, String senderUid, String alertId, long untilMs) {
        if (context == null) return;
        String room = String.valueOf(roomId == null ? "" : roomId).trim().toUpperCase(java.util.Locale.ROOT);
        String sender = String.valueOf(senderUid == null ? "" : senderUid).trim();
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        long until = Math.max(0L, untilMs);
        prefs(context).edit()
                .putString(KEY_SOS_UI_ROOM, room)
                .putString(KEY_SOS_UI_SENDER_UID, sender)
                .putString(KEY_SOS_UI_ALERT_ID, aId)
                .putLong(KEY_SOS_UI_UNTIL_MS, until)
                .apply();
    }

    /** Ambil atau muat data SosUiRoom. */
    public static String getSosUiRoom(Context context) {
        return prefs(context).getString(KEY_SOS_UI_ROOM, "");
    }

    /** Ambil atau muat data SosUiSenderUid. */
    public static String getSosUiSenderUid(Context context) {
        return prefs(context).getString(KEY_SOS_UI_SENDER_UID, "");
    }

    /** Ambil atau muat data SosUiAlertId. */
    public static String getSosUiAlertId(Context context) {
        return prefs(context).getString(KEY_SOS_UI_ALERT_ID, "");
    }

    /** Ambil atau muat data SosUiUntilMs. */
    public static long getSosUiUntilMs(Context context) {
        return prefs(context).getLong(KEY_SOS_UI_UNTIL_MS, 0L);
    }

    /** Padam atau bersihkan SosUiBlink. */
    public static void clearSosUiBlink(Context context) {
        if (context == null) return;
        prefs(context).edit()
                .remove(KEY_SOS_UI_ROOM)
                .remove(KEY_SOS_UI_SENDER_UID)
                .remove(KEY_SOS_UI_ALERT_ID)
                .remove(KEY_SOS_UI_UNTIL_MS)
                .apply();
    }

    /** Padam atau bersihkan SosUiBlinkIfAlert. */
    public static void clearSosUiBlinkIfAlert(Context context, String roomId, String alertId) {
        if (context == null) return;
        String room = String.valueOf(roomId == null ? "" : roomId).trim().toUpperCase(java.util.Locale.ROOT);
        String aId = String.valueOf(alertId == null ? "" : alertId).trim();
        if (room.isEmpty() || aId.isEmpty()) return;
        String storedRoom = String.valueOf(getSosUiRoom(context) == null ? "" : getSosUiRoom(context)).trim().toUpperCase(java.util.Locale.ROOT);
        String storedAlert = String.valueOf(getSosUiAlertId(context) == null ? "" : getSosUiAlertId(context)).trim();
        if (room.equals(storedRoom) && aId.equals(storedAlert)) clearSosUiBlink(context);
    }

    /** Ambil atau muat data NightMode. */
    public static int getNightMode(Context context) {
        if (!prefs(context).getBoolean("theme_v2_light_default_applied", false)) {
            prefs(context).edit()
                    .putBoolean("theme_v2_light_default_applied", true)
                    .putInt(KEY_NIGHT_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
                    .apply();
            return androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        }
        int mode = prefs(context).getInt(KEY_NIGHT_MODE, androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        if (mode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            return androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        }
        return mode;
    }

    /** Fungsi untuk setNightMode. */
    public static void setNightMode(Context context, int mode) {
        prefs(context).edit().putInt(KEY_NIGHT_MODE, mode).apply();
    }

    /** Semak dan sahkan MapTypeSatellite. */
    public static boolean isMapTypeSatellite(Context context) {
        return prefs(context).getBoolean(KEY_MAP_TYPE_SATELLITE, false);
    }

    /** Fungsi untuk setMapTypeSatellite. */
    public static void setMapTypeSatellite(Context context, boolean satellite) {
        prefs(context).edit().putBoolean(KEY_MAP_TYPE_SATELLITE, satellite).apply();
    }

    /** Semak dan sahkan MapTrafficEnabled. */
    public static boolean isMapTrafficEnabled(Context context) {
        return prefs(context).getBoolean(KEY_MAP_TRAFFIC_ENABLED, false);
    }

    /** Fungsi untuk setMapTrafficEnabled. */
    public static void setMapTrafficEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_MAP_TRAFFIC_ENABLED, enabled).apply();
    }

    /** Semak sama ada pengguna telah menerima Terms of Service. */
    public static boolean isTosAccepted(Context context) {
        return prefs(context).getBoolean(KEY_TOS_ACCEPTED, false);
    }

    /** Simpan status penerimaan Terms of Service. */
    public static void setTosAccepted(Context context, boolean accepted) {
        prefs(context).edit().putBoolean(KEY_TOS_ACCEPTED, accepted).apply();
    }
}

