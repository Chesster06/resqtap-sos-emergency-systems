package com.example.resqtap.map;
import com.example.resqtap.R;

import com.example.resqtap.app.BaseActivity;
import com.example.resqtap.utils.PermissionUtils;
import com.example.resqtap.utils.ThemeUtils;
import com.example.resqtap.utils.UserPrefs;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * NearbyHospitalActivity
 * Cari hospital terdekat guna Google Places API: tunjuk jarak, nombor telefon, dan link direct ke Google Maps.
 */
public class NearbyHospitalActivity extends BaseActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPerms;
    private CancellationTokenSource cancellationTokenSource;

    private LatLng currentLatLng;
    private Marker myLocationMarker;
    private final ArrayList<Marker> hospitalMarkers = new ArrayList<>();

    private BottomSheetBehavior<?> sheetBehavior;
    private boolean pendingSearch = false;
    private boolean gpsDialogShown = false;
    private boolean hasAutoCentered = false;
    private View mapControls;

    private String myPhotoUri = "";
    private String myPhotoB64 = "";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ArrayList<HospitalItem> hospitals = new ArrayList<>();
    private HospitalAdapter listAdapter;
    private TextView status;

    private static final int SEARCH_RADIUS_METERS = 4000;

    private static final class HospitalItem {
        final String name;
        final String address;
        final double lat;
        final double lng;
        double distanceMeters;

        HospitalItem(String name, String address, double lat, double lng) {
            this.name = name;
            this.address = address;
            this.lat = lat;
            this.lng = lng;
            this.distanceMeters = -1d;
        }
    }

    private final class HospitalAdapter extends android.widget.BaseAdapter {
        /** Ambil atau muat data Count. */
    @Override
        public int getCount() {
            return hospitals.size();
        }

        /** Ambil atau muat data Item. */
    @Override
        public Object getItem(int position) {
            return hospitals.get(position);
        }

        /** Ambil atau muat data ItemId. */
    @Override
        public long getItemId(int position) {
            return position;
        }

        /** Ambil atau muat data View. */
    @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hospital, parent, false);
            }

            HospitalItem item = hospitals.get(position);
            TextView name = row.findViewById(R.id.hospital_name);
            TextView distance = row.findViewById(R.id.hospital_distance);
            TextView address = row.findViewById(R.id.hospital_address);
            MaterialButton direction = row.findViewById(R.id.btn_direction);

            if (name != null) name.setText(item.name);
            if (distance != null) distance.setText(formatDistance(item.distanceMeters));
            if (address != null) address.setText(item.address == null ? "" : item.address);
            if (direction != null) direction.setOnClickListener(v -> openDirections(item));

            return row;
        }
    }

    // =========================================================================
    // SEKSYEN: ONCREATE
    // =========================================================================
    /** Inisialisasi aktiviti, bind view UI, dan setup listener. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applySavedNightMode(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nearby_hospital);

        View main = findViewById(R.id.main);
        View bottomSheet = findViewById(R.id.bottom_sheet);

        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        if (bottomSheet != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomSheet, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
                return insets;
            });
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationPerms = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::onLocationPermissionResult
        );

        myPhotoUri = String.valueOf(UserPrefs.getPhotoUri(this) == null ? "" : UserPrefs.getPhotoUri(this)).trim();
        myPhotoB64 = String.valueOf(UserPrefs.getPhotoB64(this) == null ? "" : UserPrefs.getPhotoB64(this)).trim();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        MaterialButton btnZoomIn = findViewById(R.id.btn_map_zoom_in);
        MaterialButton btnZoomOut = findViewById(R.id.btn_map_zoom_out);
        MaterialButton btnMyLocation = findViewById(R.id.btn_map_my_location);
        MaterialButton btnMapType = findViewById(R.id.btn_map_type);
        MaterialButton btnBack = findViewById(R.id.btn_back);
        mapControls = findViewById(R.id.map_controls);

        if (btnZoomIn != null) btnZoomIn.setOnClickListener(v -> {
            if (map == null) return;
            map.animateCamera(CameraUpdateFactory.zoomIn());
        });
        if (btnZoomOut != null) btnZoomOut.setOnClickListener(v -> {
            if (map == null) return;
            map.animateCamera(CameraUpdateFactory.zoomOut());
        });
        if (btnMyLocation != null) btnMyLocation.setOnClickListener(v -> requestLocationIfNeeded(true));
        if (btnMapType != null) btnMapType.setOnClickListener(v -> toggleMapType());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        MaterialButton btnSearch = findViewById(R.id.btn_search_hospital);
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> {
                pendingSearch = true;
                fetchNearbyHospitals();
            });
        }

        if (bottomSheet != null) {
            sheetBehavior = BottomSheetBehavior.from(bottomSheet);
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        applyMapControlsBottomOffset();

        status = findViewById(R.id.status);
        ListView list = findViewById(R.id.hospital_list);
        listAdapter = new HospitalAdapter();
        if (list != null) {
            list.setAdapter(listAdapter);
            list.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
                if (position < 0 || position >= hospitals.size()) return;
                openDirections(hospitals.get(position));
            });
        }

        maybePromptEnableGps();
        requestLocationIfNeeded(false);
    }

    /** Aktiviti aktif dan sedia untuk interaksi pengguna. */
    @Override
    protected void onResume() {
        super.onResume();
        if (GpsUtils.isGpsEnabled(this)) gpsDialogShown = false;
        maybePromptEnableGps();
        requestLocationIfNeeded(false);
        if (pendingSearch && PermissionUtils.hasAnyLocation(this) && GpsUtils.isGpsEnabled(this)) {
            fetchNearbyHospitals();
        }
    }

    /** Aktiviti dijeda sementara. */
    @Override
    protected void onPause() {
        super.onPause();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
            cancellationTokenSource = null;
        }
    }

    // =========================================================================
    // SEKSYEN: ONDESTROY
    // =========================================================================
    /** Pembersihan memori, buang listener, dan tutup sambungan. */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    /** Fungsi untuk onMapReady. */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        applySavedMapType();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(true);

        LatLng fallback = new LatLng(1.3521, 103.8198);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(fallback, 12f));

        requestLocationIfNeeded(false);
    }

    /** Fungsi untuk applySavedMapType. */
    private void applySavedMapType() {
        if (map == null) return;
        try {
            boolean satellite = UserPrefs.isMapTypeSatellite(this);
            map.setMapType(satellite ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
            applyResQTapMapStyle(!satellite);
            map.setTrafficEnabled(UserPrefs.isMapTrafficEnabled(this));
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk applyResQTapMapStyle. */
    private void applyResQTapMapStyle(boolean enabled) {
        if (map == null) return;
        try {
            boolean night = ThemeUtils.isNightMode(this);
            map.setMapStyle(enabled && night
                    ? MapStyleOptions.loadRawResourceStyle(this, R.raw.resqtap_map_style)
                    : null);
        } catch (Exception ignored) {
        }
    }

    /** Fungsi untuk applyMapControlsBottomOffset. */
    private void applyMapControlsBottomOffset() {
        if (mapControls == null) return;
        ViewCompat.setOnApplyWindowInsetsListener(mapControls, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int peek = 0;
            try {
                if (sheetBehavior != null) peek = sheetBehavior.getPeekHeight();
            } catch (Exception ignored) {
            }
            if (peek <= 0) peek = dp(this, 200);

            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams clp = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) lp;

                clp.bottomMargin = dp(this, 16) + peek;
                v.setLayoutParams(clp);
            }
            return insets;
        });
        ViewCompat.requestApplyInsets(mapControls);
    }

    /** Fungsi untuk dp. */
    private static int dp(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /** Fungsi untuk toggleMapType. */
    private void toggleMapType() {
        MapTypeBottomSheet.show(this, map);
    }

    /** Fungsi untuk requestLocationIfNeeded. */
    private void requestLocationIfNeeded(boolean userInitiated) {
        if (map == null) return;
        if (!GpsUtils.isGpsEnabled(this)) {
            if (userInitiated || pendingSearch) maybePromptEnableGps();
            return;
        }
        if (PermissionUtils.hasAnyLocation(this)) {
            enableMyLocationAndCenter(userInitiated);
        } else {
            locationPerms.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /** Fungsi untuk onLocationPermissionResult. */
    private void onLocationPermissionResult(Map<String, Boolean> result) {
        Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
        Boolean coarse = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);
        boolean granted = Boolean.TRUE.equals(fine) || Boolean.TRUE.equals(coarse);
        if (!granted) {
            Toast.makeText(this, R.string.nearby_hospital_need_location, Toast.LENGTH_SHORT).show();
            return;
        }
        enableMyLocationAndCenter(true);
        if (pendingSearch) fetchNearbyHospitals();
    }

    /** Fungsi untuk enableMyLocationAndCenter. */
    private void enableMyLocationAndCenter(boolean userInitiated) {
        if (map == null) return;
        if (!PermissionUtils.hasAnyLocation(this)) return;
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (map == null) return;
                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (userInitiated || !hasAutoCentered) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                        hasAutoCentered = true;
                    }
                    upsertMyLocationMarker(currentLatLng);
                    return;
                }
                cancellationTokenSource = new CancellationTokenSource();
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellationTokenSource.getToken())
                        .addOnSuccessListener(current -> {
                            if (current == null || map == null) return;
                            currentLatLng = new LatLng(current.getLatitude(), current.getLongitude());
                            if (userInitiated || !hasAutoCentered) {
                                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                                hasAutoCentered = true;
                            }
                            upsertMyLocationMarker(currentLatLng);
                        });
            });
        } catch (SecurityException ignored) {
        }
    }

    /** Ambil atau muat data NearbyHospitals. */
    private void fetchNearbyHospitals() {
        if (!PermissionUtils.hasAnyLocation(this)) {
            Toast.makeText(this, R.string.nearby_hospital_need_location, Toast.LENGTH_SHORT).show();
            requestLocationIfNeeded(true);
            return;
        }
        if (!GpsUtils.isGpsEnabled(this)) {
            maybePromptEnableGps();
            return;
        }
        if (sheetBehavior != null) sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        if (status != null) {
            status.setText(R.string.nearby_hospital_loading);
            status.setVisibility(View.VISIBLE);
        }

        hospitals.clear();
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        clearHospitalMarkers();

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    upsertMyLocationMarker(currentLatLng);
                    doNearbySearch(currentLatLng);
                    return;
                }
                cancellationTokenSource = new CancellationTokenSource();
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                        .addOnSuccessListener(current -> {
                            if (current == null) {
                                if (status != null) status.setText(R.string.nearby_hospital_need_location);
                                return;
                            }
                            currentLatLng = new LatLng(current.getLatitude(), current.getLongitude());
                            upsertMyLocationMarker(currentLatLng);
                            doNearbySearch(currentLatLng);
                        });
            });
        } catch (SecurityException e) {
            if (status != null) status.setText(R.string.nearby_hospital_need_location);
        }
    }

    /** Fungsi untuk doNearbySearch. */
    private void doNearbySearch(LatLng center) {
        String apiKey = getString(R.string.google_maps_key);
        if (apiKey == null || apiKey.contains("YOUR_GOOGLE_MAPS_API_KEY")) {
            runOnUiThread(() -> {
                if (status != null) status.setText(R.string.nearby_hospital_error_missing_api_key);
            });
            return;
        }

        executor.execute(() -> {
            try {
                PlacesResponse first = fetchPlacesNearby(center, apiKey, "hospital", null);
                if (first.isDenied()) {
                    runOnUiThread(() -> showPlacesDenied(first));
                    return;
                }
                if (first.isOkWithResults()) {
                    runOnUiThread(() -> applyHospitals(first.items));
                    return;
                }

                PlacesResponse fallback = fetchPlacesNearby(center, apiKey, "health", "hospital");
                if (fallback.isDenied()) {
                    runOnUiThread(() -> showPlacesDenied(fallback));
                    return;
                }
                runOnUiThread(() -> applyHospitals(fallback.items));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (status != null) status.setText(R.string.nearby_hospital_error_failed_load);
                });
                pendingSearch = false;
            }
        });
    }

    private static final class PlacesResponse {
        final String status;
        final String errorMessage;
        final ArrayList<HospitalItem> items;

        PlacesResponse(String status, String errorMessage, ArrayList<HospitalItem> items) {
            this.status = status == null ? "" : status;
            this.errorMessage = errorMessage == null ? "" : errorMessage;
            this.items = items == null ? new ArrayList<>() : items;
        }

        boolean isDenied() {
            return "REQUEST_DENIED".equalsIgnoreCase(status) || "INVALID_REQUEST".equalsIgnoreCase(status);
        }

        boolean isOkWithResults() {
            return "OK".equalsIgnoreCase(status) && !items.isEmpty();
        }
    }

    /** Ambil atau muat data PlacesNearby. */
    private PlacesResponse fetchPlacesNearby(LatLng center, String apiKey, String type, String keyword) throws Exception {
        String location = center.latitude + "," + center.longitude;
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=");
        url.append(URLEncoder.encode(location, StandardCharsets.UTF_8.name()));
        url.append("&radius=").append(SEARCH_RADIUS_METERS);
        if (type != null && !type.trim().isEmpty()) {
            url.append("&type=").append(URLEncoder.encode(type, StandardCharsets.UTF_8.name()));
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            url.append("&keyword=").append(URLEncoder.encode(keyword, StandardCharsets.UTF_8.name()));
        }
        url.append("&language=").append(URLEncoder.encode(Locale.getDefault().getLanguage(), StandardCharsets.UTF_8.name()));
        url.append("&key=").append(URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name()));

        HttpURLConnection conn = (HttpURLConnection) new URL(url.toString()).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8
        ));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        JSONObject root = new JSONObject(sb.toString());
        String status = root.optString("status", "");
        String error = root.optString("error_message", "");

        JSONArray results = root.optJSONArray("results");
        ArrayList<HospitalItem> found = new ArrayList<>();
        if (results != null) {
            for (int i = 0; i < results.length() && i < 15; i++) {
                JSONObject r = results.getJSONObject(i);
                String name = r.optString("name", "Hospital");
                String address = r.optString("vicinity", "");
                JSONObject geometry = r.optJSONObject("geometry");
                JSONObject loc = geometry == null ? null : geometry.optJSONObject("location");
                if (loc == null) continue;
                double lat = loc.optDouble("lat", 0);
                double lng = loc.optDouble("lng", 0);
                found.add(new HospitalItem(name, address, lat, lng));
            }
        }
        return new PlacesResponse(status, error, found);
    }

    /** Paparkan PlacesDenied. */
    private void showPlacesDenied(PlacesResponse response) {
        if (status != null) status.setVisibility(View.VISIBLE);
        String msg = "Places API request denied.";
        if (!response.errorMessage.trim().isEmpty()) {
            msg += "\n" + response.errorMessage.trim();
        } else if (!response.status.trim().isEmpty()) {
            msg += "\nStatus: " + response.status.trim();
        }
        msg += "\n\nFix: Enable \"Places API\" in Google Cloud + use a Web Service key (not Android-restricted).";
        if (status != null) status.setText(msg);
        hospitals.clear();
        if (listAdapter != null) listAdapter.notifyDataSetChanged();
        clearHospitalMarkers();
        pendingSearch = false;
    }

    /** Fungsi untuk applyHospitals. */
    private void applyHospitals(ArrayList<HospitalItem> found) {
        hospitals.clear();
        if (currentLatLng != null && found != null) {
            for (HospitalItem h : found) {
                h.distanceMeters = distanceMeters(currentLatLng, h.lat, h.lng);
            }
            Collections.sort(found, Comparator.comparingDouble(a -> a.distanceMeters < 0 ? Double.MAX_VALUE : a.distanceMeters));
        }
        if (found != null) hospitals.addAll(found);
        if (listAdapter != null) listAdapter.notifyDataSetChanged();

        if (hospitals.isEmpty()) {
            if (status != null) {
                status.setText(R.string.nearby_hospital_no_results);
                status.setVisibility(View.VISIBLE);
            }
        } else {
            if (status != null) status.setVisibility(View.GONE);
        }

        if (map != null) {
            clearHospitalMarkers();
            if (currentLatLng != null) upsertMyLocationMarker(currentLatLng);
            for (HospitalItem h : hospitals) {
                Marker m = map.addMarker(new MarkerOptions().position(new LatLng(h.lat, h.lng)).title(h.name));
                if (m != null) hospitalMarkers.add(m);
            }
            if (currentLatLng != null) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f));
            }
        }
        pendingSearch = false;
    }

    /** Fungsi untuk openDirections. */
    private void openDirections(HospitalItem item) {
        if (item == null) return;
        Uri uri = Uri.parse("google.navigation:q=" + item.lat + "," + item.lng + "&mode=d");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    /** Padam atau bersihkan HospitalMarkers. */
    private void clearHospitalMarkers() {
        for (Marker m : hospitalMarkers) {
            try {
                if (m != null) m.remove();
            } catch (Exception ignored) {
            }
        }
        hospitalMarkers.clear();
    }

    /** Fungsi untuk upsertMyLocationMarker. */
    private void upsertMyLocationMarker(LatLng pos) {
        if (map == null || pos == null) return;
        Bitmap icon = buildSelfMarkerBitmap();
        if (myLocationMarker == null) {
            myLocationMarker = map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title("You")
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .anchor(0.5f, 0.5f));
        } else {
            myLocationMarker.setPosition(pos);
            myLocationMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
            myLocationMarker.setAnchor(0.5f, 0.5f);
        }
    }

    /** Fungsi untuk maybePromptEnableGps. */
    private void maybePromptEnableGps() {
        if (gpsDialogShown) return;
        if (GpsUtils.isGpsEnabled(this)) return;
        gpsDialogShown = true;
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.nearby_hospital_enable_location_title)
                .setMessage(R.string.nearby_hospital_enable_location_msg)
                .setPositiveButton(R.string.nearby_hospital_enable_location_action, (d, which) -> {
                    try {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } catch (Exception ignored) {
                    }
                })
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .show();
    }

    /** Fungsi untuk distanceMeters. */
    private static double distanceMeters(LatLng from, double toLat, double toLng) {
        if (from == null) return -1d;
        try {
            float[] out = new float[1];
            Location.distanceBetween(from.latitude, from.longitude, toLat, toLng, out);
            return out[0];
        } catch (Exception ignored) {
            return -1d;
        }
    }

    /** Fungsi untuk formatDistance. */
    private String formatDistance(double meters) {
        if (meters < 0) return "";
        if (meters < 1000) return String.format(Locale.getDefault(), "%.0f m", meters);
        return String.format(Locale.getDefault(), "%.1f km", (meters / 1000d));
    }

    /** Fungsi untuk buildSelfMarkerBitmap. */
    private Bitmap buildSelfMarkerBitmap() {
        Bitmap b = decodeBase64Avatar(myPhotoB64);
        if (b != null) return buildProfileMarkerBitmapFromPhoto(this, b);
        return buildProfileMarkerBitmap(this, myPhotoUri);
    }

    /** Fungsi untuk decodeBase64Avatar. */
    private static Bitmap decodeBase64Avatar(String b64) {
        String s = String.valueOf(b64 == null ? "" : b64).trim();
        if (s.isEmpty()) return null;
        try {
            byte[] bytes = android.util.Base64.decode(s, android.util.Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Fungsi untuk buildProfileMarkerBitmap. */
    private static Bitmap buildProfileMarkerBitmap(Context ctx, String photoUri) {
        final int sizePx = dp(ctx, 44);
        final int borderPx = dp(ctx, 3);
        final int shadowPx = dp(ctx, 4);

        Bitmap src = loadProfileBitmap(ctx, photoUri);
        if (src == null) {
            return defaultAvatarMarkerBitmap(ctx, sizePx, borderPx, shadowPx);
        }
        Bitmap scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true);
        return circularWithBorderAndShadow(scaled, borderPx, shadowPx);
    }

    /** Fungsi untuk buildProfileMarkerBitmapFromPhoto. */
    private static Bitmap buildProfileMarkerBitmapFromPhoto(Context ctx, Bitmap src) {
        final int sizePx = dp(ctx, 44);
        final int borderPx = dp(ctx, 3);
        final int shadowPx = dp(ctx, 4);
        if (src == null) return defaultAvatarMarkerBitmap(ctx, sizePx, borderPx, shadowPx);
        Bitmap scaled = Bitmap.createScaledBitmap(src, sizePx, sizePx, true);
        return circularWithBorderAndShadow(scaled, borderPx, shadowPx);
    }

    /** Ambil atau muat data ProfileBitmap. */
    private static Bitmap loadProfileBitmap(Context ctx, String photoUri) {
        String u = String.valueOf(photoUri == null ? "" : photoUri).trim();
        if (u.isEmpty()) return null;
        try {
            Uri uri = Uri.parse(u);
            try (java.io.InputStream in = ctx.getContentResolver().openInputStream(uri)) {
                if (in == null) return null;
                return BitmapFactory.decodeStream(in);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Fungsi untuk bitmapFromDrawable. */
    private static Bitmap bitmapFromDrawable(Context ctx, int resId, int w, int h) {
        try {
            android.graphics.drawable.Drawable d = ContextCompat.getDrawable(ctx, resId);
            if (d == null) return null;
            try {
                d = d.mutate();
            } catch (Exception ignored) {
            }
            Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b);
            d.setBounds(0, 0, w, h);
            d.draw(c);
            return b;
        } catch (Exception ignored) {
            return null;
        }
    }

    /** Fungsi untuk defaultAvatarMarkerBitmap. */
    private static Bitmap defaultAvatarMarkerBitmap(Context ctx, int sizePx, int borderPx, int shadowPx) {
        int outSize = sizePx + (shadowPx * 2);
        Bitmap out = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        float cx = outSize / 2f;
        float cy = outSize / 2f;
        float radius = sizePx / 2f;

        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(0xFFFFFFFF);
        shadow.setStyle(Paint.Style.FILL);
        shadow.setShadowLayer(shadowPx, 0f, shadowPx * 0.75f, 0x55000000);
        canvas.drawCircle(cx, cy, radius, shadow);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(0xFF0B1220);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius - (shadowPx * 0.5f), fill);

        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(0xFFFFFFFF);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(borderPx);
        canvas.drawCircle(cx, cy, radius - (shadowPx * 0.5f) - (borderPx / 2f), border);

        int iconSize = Math.round(sizePx * 0.72f);
        Bitmap icon = bitmapFromDrawable(ctx, R.drawable.ic_avatar, iconSize, iconSize);
        if (icon != null) {
            float left = cx - (icon.getWidth() / 2f);
            float top = cy - (icon.getHeight() / 2f);
            canvas.drawBitmap(icon, left, top, new Paint(Paint.ANTI_ALIAS_FLAG));
            icon.recycle();
        }
        return out;
    }

    /** Fungsi untuk circularWithBorderAndShadow. */
    private static Bitmap circularWithBorderAndShadow(Bitmap src, int borderPx, int shadowPx) {
        int size = Math.min(src.getWidth(), src.getHeight());
        int outSize = size + (shadowPx * 2);
        Bitmap out = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);

        float cx = outSize / 2f;
        float cy = outSize / 2f;
        float radius = (size / 2f);
        float contentRadius = radius - borderPx;

        Paint shadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadow.setColor(0xFFFFFFFF);
        shadow.setStyle(Paint.Style.FILL);
        shadow.setShadowLayer(shadowPx, 0f, shadowPx * 0.75f, 0x55000000);
        canvas.drawCircle(cx, cy, radius, shadow);

        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(0xFFFFFFFF);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx, cy, radius - shadowPx * 0.5f, fill);

        Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap circle = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888);
        Canvas c2 = new Canvas(circle);

        Paint mask = new Paint(Paint.ANTI_ALIAS_FLAG);
        mask.setColor(0xFF000000);
        c2.drawCircle(cx, cy, contentRadius, mask);
        mask.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        RectF dst = new RectF(cx - contentRadius, cy - contentRadius, cx + contentRadius, cy + contentRadius);
        c2.drawBitmap(src, null, dst, mask);
        canvas.drawBitmap(circle, 0, 0, imagePaint);
        circle.recycle();
        return out;
    }

}
