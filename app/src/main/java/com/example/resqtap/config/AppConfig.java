package com.example.resqtap.config;

/**
 * AppConfig
 * Pusat konfigurasi tunggal bagi semua kunci API, kredensial, dan URL perkhidmatan
 * untuk aplikasi ResQTap (Supabase, Firebase RTDB, Google Maps API).
 */
public final class AppConfig {

    private AppConfig() {
        // Prevent instantiation
    }

    // ==========================================
    // SUPABASE CONFIGURATION
    // ==========================================
    public static final String SUPABASE_URL = "https://umcxapvojtoxqlpnrwdw.supabase.co";
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVtY3hhcHZvanRveHFscG5yd2R3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTEzMTg2OTksImV4cCI6MjEwNjg5NDY5OX0.8qDJYad_rTwlzAqmF0Z1OiRgQ9ocxaluN7MIjz0mL0M";

    // ==========================================
    // FIREBASE REALTIME DATABASE CONFIGURATION
    // ==========================================
    public static final String FIREBASE_DATABASE_URL = "https://resqtap-b9ff5-default-rtdb.firebaseio.com";
    public static final String FIREBASE_PROJECT_ID = "resqtap-b9ff5";
    public static final String FIREBASE_STORAGE_BUCKET = "resqtap-b9ff5.firebasestorage.app";

    // ==========================================
    // GOOGLE CLOUD & MAPS API
    // ==========================================
    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyCr1aSjKTZEdn58dBBWhG1UFsE6iKUwUQc";
}
