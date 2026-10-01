import os
from PIL import Image, ImageDraw
import generate_flowcharts as gf

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts")
os.makedirs(OUTPUT_DIR, exist_ok=True)

# ==============================================================================
# CARTA ALIR 1: KESELURUHAN SENI BINA SISTEM RESQTAP
# ==============================================================================
def draw_flowchart_1():
    W, H = 1600, 1220
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 1: KESELURUHAN ALIRAN SISTEM RESQTAP", 
        "Gambaran Keseluruhan Navigasi Pengguna, Keselamatan Aplikasi & Modul-Modul Utama Ekosistem ResQTap")
    
    # Nodes
    gf.draw_pill(d, [700, 110, 900, 155], "MULA", is_start=True)
    gf.draw_process(d, [630, 185, 970, 260], "1.0 Pelancaran Aplikasi", [
        "Pengguna membuka ResQTap",
        "SplashActivity menyemak status peranti & kebenaran"
    ])
    gf.draw_decision(d, 800, 335, 290, 85, "Adakah Pengguna", ["Mempunyai Sesi Aktif?"])
    
    # Sesi Tidak Aktif -> Autentikasi
    gf.draw_io_box(d, [150, 295, 490, 375], "2.0 Onboarding & Autentikasi", [
        "Pilihan: Log Masuk / Daftar / Lupa Kata Laluan",
        "Pengesahan melalui Firebase Authentication"
    ])
    
    # Sesi Aktif -> Kunci Aplikasi
    gf.draw_decision(d, 800, 465, 300, 85, "Adakah Kunci Aplikasi /", ["Biometrik Diaktifkan?"])
    
    # App Lock Diaktifkan
    gf.draw_process(d, [1140, 430, 1480, 500], "3.0 Skrin Kunci (AppLock)", [
        "Masukkan PIN 4-Digit atau Imbas Cap Jari",
        "Duress PIN: Cetus Silent SOS jika dipaksa"
    ])
    
    # Menu Utama
    gf.draw_process(d, [620, 570, 980, 650], "4.0 Dashboard Utama (MainActivity)", [
        "Paparan Kad Perubatan, Status Bateri & Amaran Semasa",
        "Pengguna memilih perkhidmatan yang diperlukan"
    ])
    
    gf.draw_decision(d, 800, 725, 280, 80, "Pilihan Perkhidmatan", ["Pengguna di Dashboard?"])
    
    # 5 Sub-sistem Modul
    gf.draw_process(d, [60, 820, 320, 915], "MODUL SOS", [
        "Pencetus OneTap SOS",
        "Countdown 3 saat & getaran",
        "Siaran amaran ke Firebase RTDB"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_process(d, [350, 820, 620, 915], "BILIK KECEMASAN", [
        "Cipta / Sertai Bilik (Kod / QR)",
        "LiveRoomTrackingService aktif",
        "Peta Google Maps & Chat Bilik"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_process(d, [650, 820, 950, 915], "PANGGILAN WEBRTC", [
        "Panggilan Suara / Video",
        "WebRTC Signaling via RTDB",
        "Sambungan P2P Audio/Video"
    ], border_color=(99, 102, 241), accent_color=(238, 242, 255))
    
    gf.draw_process(d, [980, 820, 1260, 915], "HOSPITAL TERDEKAT", [
        "Imbas GPS pengguna",
        "Kueri kemudahan perubatan",
        "Panggilan kecemasan & navigasi"
    ], border_color=(14, 165, 233), accent_color=(240, 249, 255))
    
    gf.draw_process(d, [1290, 820, 1550, 915], "LAPORAN & BERITA", [
        "Lapor insiden bergambar",
        "Muat naik ke Cloud Storage",
        "Berita perubatan & advisori"
    ], border_color=(168, 85, 247), accent_color=(250, 245, 255))
    
    # Pengurusan Selesai
    gf.draw_process(d, [640, 990, 960, 1060], "5.0 Kitaran Hayat & Log Keluar", [
        "Pengguna selesai menggunakan perkhidmatan / keluar aplikasi",
        "Data sesi dan token FCM diselaraskan"
    ])
    gf.draw_pill(d, [710, 1110, 890, 1155], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (800, 155), (800, 185))
    gf.draw_arrow(d, (800, 260), (800, 292))
    
    # Decision 2.0 (Sesi Aktif?)
    gf.draw_arrow(d, (655, 335), (490, 335), label="Tidak", label_side="top")
    gf.draw_poly_arrow(d, [(320, 295), (320, 222), (630, 222)], label="Selesai Log Masuk", label_side="top")
    gf.draw_arrow(d, (800, 377), (800, 422), label="Ya", label_side="right")
    
    # Decision 3.0 (Kunci Aplikasi?)
    gf.draw_arrow(d, (950, 465), (1140, 465), label="Ya", label_side="top")
    gf.draw_poly_arrow(d, [(1310, 500), (1310, 610), (980, 610)], label="Disahkan", label_side="bottom")
    gf.draw_arrow(d, (800, 507), (800, 570), label="Tidak", label_side="right")
    
    gf.draw_arrow(d, (800, 650), (800, 685))
    
    # Decision 5.0 to 5 sub-modules
    gf.draw_poly_arrow(d, [(660, 725), (190, 725), (190, 820)])
    gf.draw_poly_arrow(d, [(710, 750), (485, 750), (485, 820)])
    gf.draw_arrow(d, (800, 765), (800, 820))
    gf.draw_poly_arrow(d, [(890, 750), (1120, 750), (1120, 820)])
    gf.draw_poly_arrow(d, [(940, 725), (1420, 725), (1420, 820)])
    
    # From 5 sub-modules to Lifecycle
    gf.draw_poly_arrow(d, [(190, 915), (190, 960), (700, 960), (700, 990)])
    gf.draw_poly_arrow(d, [(485, 915), (485, 960), (750, 960), (750, 990)])
    gf.draw_arrow(d, (800, 915), (800, 990))
    gf.draw_poly_arrow(d, [(1120, 915), (1120, 960), (850, 960), (850, 990)])
    gf.draw_poly_arrow(d, [(1420, 915), (1420, 960), (900, 960), (900, 990)])
    
    gf.draw_arrow(d, (800, 1060), (800, 1110))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_1_overall_architecture.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 2: AUTENTIKASI, KUNCI APLIKASI & DURESS PIN
# ==============================================================================
def draw_flowchart_2():
    W, H = 1500, 1260
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 2: PENGESAHAN PENGGUNA, KUNCI APLIKASI & PROTOKOL DURESS PIN", 
        "Logik Pengesahan Sesi, Pengesahan Biometrik/PIN 4-Digit, dan Tindak Balas Keselamatan Senyap Duress")
        
    gf.draw_pill(d, [650, 110, 850, 155], "MULA", is_start=True)
    gf.draw_process(d, [580, 185, 920, 260], "1.0 Lancarkan SplashActivity", [
        "Inisialisasi Firebase App & Semak Keadaan Sesi",
        "FirebaseAuth.getInstance().getCurrentUser()"
    ])
    gf.draw_decision(d, 750, 335, 280, 85, "Adakah Sesi Pengguna", ["Wujud & Sah?"])
    
    # Onboarding & Auth Flow
    gf.draw_io_box(d, [120, 295, 460, 375], "2.0 GetStarted & Autentikasi", [
        "Pengguna pilih Log Masuk / Daftar / Reset",
        "Pengesahan E-mel & Kata Laluan melalui Firebase"
    ])
    gf.draw_process(d, [120, 420, 460, 495], "2.1 Simpan Data Pengguna", [
        "Simpan Profil & Kad Perubatan ke RTDB",
        "Jana Token Peranti (FCM Token) ke profil"
    ])
    
    # Sesi Wujud -> Semak AppLock
    gf.draw_decision(d, 750, 465, 300, 85, "Adakah Kunci Aplikasi /", ["Biometrik Diaktifkan?"])
    
    gf.draw_process(d, [580, 570, 920, 645], "3.0 Papar Skrin AppLockActivity", [
        "Skrin kunci paparkan pad kekunci 4-digit PIN",
        "Pilihan Imbasan Cap Jari (BiometricPrompt)"
    ])
    
    gf.draw_decision(d, 750, 735, 290, 90, "Jenis Input Sah?", ["Padanan Pengesahan"])
    
    # 4 Kemungkinan Keputusan Input
    # 1. PIN Sah / Biometrik Sah (Tengah)
    gf.draw_process(d, [610, 850, 890, 925], "4.1 Akses Biasa Dibenarkan", [
        "AppLockManager.setUnlocked(true)",
        "Buka kunci sesi aplikasi"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    
    # 2. DURESS PIN (Kod Paksaan) (Kanan)
    gf.draw_subroutine(d, [990, 695, 1420, 795], "4.2 PROTOKOL DURESS PIN (SILENT SOS)", [
        "Padan dengan UserPrefs.getDuressPin()",
        "Hantar Silent SOS Alert ke Firebase RTDB serta-merta",
        "TIADA siren, TIADA strobe, TIADA tanda amaran skrin",
        "Buka paparan apl seperti biasa untuk elak ancaman fizikal"
    ])
    
    # 3. PIN Tidak Sah / Salah (Kiri)
    gf.draw_process(d, [80, 700, 440, 770], "4.3 PIN Tidak Sah", [
        "Kira cubaan gagal (Failed Attempts++)",
        "Paparkan getaran ralat pada bar PIN"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_decision(d, 260, 840, 240, 75, "Cubaan Gagal", ["> 5 Kali Berturut?"])
    gf.draw_process(d, [80, 930, 440, 995], "4.4 Kunci Peranti Sementara", [
        "Kunci aplikasi selama 30 saat (Cooldown Timer)",
        "Lumpuhkan input pad kekunci"
    ], border_color=gf.C_STOP_BD)
    
    # Akses Berjaya -> MainActivity
    gf.draw_process(d, [580, 1020, 920, 1095], "5.0 Navigasi ke MainActivity", [
        "Muat papan pemuka utama ResQTap",
        "Servis penjejakan bersiap sedia"
    ])
    gf.draw_pill(d, [660, 1150, 840, 1195], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (750, 155), (750, 185))
    gf.draw_arrow(d, (750, 260), (750, 292))
    
    # Dec 2.0 (Sesi Wujud?)
    gf.draw_arrow(d, (610, 335), (460, 335), label="Tidak", label_side="top")
    gf.draw_arrow(d, (290, 375), (290, 420))
    gf.draw_poly_arrow(d, [(460, 460), (510, 460), (510, 222), (580, 222)], label="Log Masuk Berjaya", label_side="top")
    gf.draw_arrow(d, (750, 377), (750, 422), label="Ya", label_side="right")
    
    # Dec 3.0 (Kunci Aktif?)
    gf.draw_poly_arrow(d, [(900, 465), (960, 465), (960, 1060), (920, 1060)], label="Tidak Aktif", label_side="top")
    gf.draw_arrow(d, (750, 507), (750, 570), label="Ya", label_side="right")
    gf.draw_arrow(d, (750, 645), (750, 690))
    
    # Dec 4.0 (Jenis Input)
    # Ke PIN Sah
    gf.draw_arrow(d, (750, 780), (750, 850), label="PIN / Biometrik Sah", label_side="right")
    gf.draw_arrow(d, (750, 925), (750, 1020))
    
    # Ke Duress PIN
    gf.draw_arrow(d, (895, 735), (990, 735), label="Duress PIN Sah", label_side="top")
    gf.draw_poly_arrow(d, [(1205, 795), (1205, 1040), (920, 1040)], label="Buka Mod Samar", label_side="right")
    
    # Ke PIN Salah
    gf.draw_arrow(d, (605, 735), (440, 735), label="PIN Salah", label_side="top")
    gf.draw_arrow(d, (260, 770), (260, 802))
    gf.draw_arrow(d, (260, 877), (260, 930), label="Ya", label_side="right")
    gf.draw_poly_arrow(d, [(80, 965), (40, 965), (40, 610), (580, 610)], label="Tamat Masa", label_side="left")
    gf.draw_poly_arrow(d, [(380, 840), (520, 840), (520, 630), (580, 630)], label="Tidak (>0 baki)", label_side="top")
    
    gf.draw_arrow(d, (750, 1095), (750, 1150))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_2_auth_security.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 3: PENCETUS & PENGURUSAN KECEMASAN ONETAP SOS
# ==============================================================================
def draw_flowchart_3():
    W, H = 1650, 1500
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 3: PENCETUSAN & PENGURUSAN KECEMASAN ONETAP SOS", 
        "Aliran Tindak Balas Pantas: Kiraan Detik, Penggera Fizikal, Penyiaran Firebase RTDB/FCM, dan Garis Masa Bantuan")
        
    gf.draw_pill(d, [720, 110, 920, 155], "MULA", is_start=True)
    gf.draw_process(d, [630, 185, 1010, 260], "1.0 Tekan Butang OneTap SOS", [
        "Pengguna menekan butang bulat SOS di MainActivity",
        "Sistem mencetuskan SosBottomSheetController"
    ])
    gf.draw_decision(d, 820, 340, 320, 85, "Kiraan Detik 3 Saat", ["(CountDownTimer Berjalan)"])
    
    # Slide to Cancel
    gf.draw_process(d, [1200, 305, 1530, 380], "2.0 Leret Batal (Slide to Cancel)", [
        "Pengguna leret gelongsor (Progress >= 95%)",
        "Kiraan detik dimatikan & tiada amaran dihantar"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    gf.draw_pill(d, [1310, 420, 1420, 460], "TAMAT", is_start=False)
    
    # Kiraan Tamat -> Cetus Penggera
    gf.draw_process(d, [620, 440, 1020, 535], "3.0 Pengaktifan Penggera Tempatan", [
        "Bunyikan Siren Desibel Tinggi (SosAudioManager)",
        "Nyalakan Lampu Suluh Strob Berkelip Berterusan",
        "Corak Getaran Haptik Berirama (VibrateManager)"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_io_box(d, [620, 570, 1020, 650], "4.0 Dapatkan Lokasi & Bilik Pengguna", [
        "fusedLocationClient.getLastLocation() -> Lat, Lng",
        "Kueri senarai bilik aktif dari /userRooms/{uid}"
    ])
    
    gf.draw_decision(d, 820, 725, 290, 80, "Adakah Pengguna", ["Mempunyai Bilik Aktif?"])
    
    # Cabang Siaran
    gf.draw_subroutine(d, [350, 810, 740, 890], "5.1 Siaran Bilik Kumpulan", [
        "Hantar ke setiap /rooms/{code}/sosAlerts",
        "Sertakan koordinat GPS, bateri & timestamp"
    ])
    gf.draw_subroutine(d, [900, 810, 1290, 890], "5.2 Siaran Saluran Terus (DIRECT)", [
        "Hantar ke saluran sandaran /rooms/DIRECT/sosAlerts",
        "Daftar amaran ke nod global /sos_alerts"
    ])
    
    # Firebase Cloud Functions Trigger
    gf.draw_subroutine(d, [590, 930, 1050, 1025], "6.0 Firebase Cloud Functions Automasi", [
        "Pemicu onRoomSosCreated & onSosAlertCreated",
        "Kumpul Token Peranti (FCM Tokens) ahli & responden",
        "Hantar Notifikasi Tolak Keutamaan Tinggi (High Priority FCM)"
    ], border_color=gf.C_SUB_BD, bg_color=gf.C_SUB_BG)
    
    # 2 Tindak Balas Serentak: Penerima & Pentadbir
    gf.draw_process(d, [120, 1070, 560, 1165], "7.1 Responden / Ahli Bilik", [
        "Terima notifikasi FCM -> Lancarkan SosAlarmActivity",
        "Skrin amaran penuh di atas skrin kunci (Lock Screen)",
        "Pilihan: Tunda Siren (Snooze), Buka Peta Live, Hubungi"
    ], border_color=(217, 119, 6), accent_color=(255, 251, 235))
    
    gf.draw_process(d, [1080, 1070, 1520, 1165], "7.2 Pentadbir Web Portal", [
        "Kes kecemasan muncul merah di Dashboard Admin",
        "Pentadbir klik 'Reserve Kes' (served = true, servedBy)",
        "Kemas kini status: Dispatched -> En Route -> On Scene"
    ], border_color=gf.C_PROC_BD, accent_color=gf.C_PROC_ACCENT)
    
    # Mangsa mengesan tempahan admin
    gf.draw_process(d, [600, 1205, 1040, 1300], "8.0 Garis Masa Bantuan (SosProgressActivity)", [
        "watchSosCaseReservation() mengesan admin tempah kes",
        "Paparan kemajuan interaktif 4 peringkat langsung:",
        "1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved"
    ], border_color=(99, 102, 241), accent_color=(238, 242, 255))
    
    gf.draw_decision(d, 820, 1375, 290, 75, "Kes Diselesaikan /", ["Dibatalkan?"])
    gf.draw_pill(d, [730, 1450, 910, 1490], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 297))
    
    # Dec 2.0
    gf.draw_arrow(d, (980, 340), (1200, 340), label="Leret Batal", label_side="top")
    gf.draw_arrow(d, (1365, 380), (1365, 420))
    gf.draw_arrow(d, (820, 382), (820, 440), label="Kiraan Tamat (0s)", label_side="right")
    
    gf.draw_arrow(d, (820, 535), (820, 570))
    gf.draw_arrow(d, (820, 650), (820, 685))
    
    # Dec 5.0
    gf.draw_poly_arrow(d, [(675, 725), (545, 725), (545, 810)], label="Ya (Ada Bilik)", label_side="top")
    gf.draw_poly_arrow(d, [(965, 725), (1095, 725), (1095, 810)], label="Tidak (Tiada Bilik)", label_side="top")
    
    gf.draw_poly_arrow(d, [(545, 890), (545, 910), (740, 910), (740, 930)])
    gf.draw_poly_arrow(d, [(1095, 890), (1095, 910), (900, 910), (900, 930)])
    
    # From Cloud Functions to Receiver & Admin
    gf.draw_poly_arrow(d, [(660, 1025), (340, 1025), (340, 1070)], label="FCM ke Ahli", label_side="top")
    gf.draw_poly_arrow(d, [(980, 1025), (1300, 1025), (1300, 1070)], label="RTDB Sync ke Admin", label_side="top")
    
    # Both lead to Progress
    gf.draw_poly_arrow(d, [(340, 1165), (340, 1250), (600, 1250)])
    gf.draw_poly_arrow(d, [(1300, 1165), (1300, 1250), (1040, 1250)], label="Admin Reserve", label_side="top")
    
    gf.draw_arrow(d, (820, 1300), (820, 1337))
    gf.draw_arrow(d, (820, 1412), (820, 1450), label="Ya (Resolved)", label_side="right")
    
    path = os.path.join(OUTPUT_DIR, "flowchart_3_onetap_sos.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 4: BILIK KECEMASAN & PENJEJAKAN GPS MASA NYATA
# ==============================================================================
def draw_flowchart_4():
    W, H = 1600, 1300
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 4: BILIK KECEMASAN (ROOM HUB) & PENJEJAKAN GPS MASA NYATA", 
        "Pengurusan Bilik Kecemasan, Perkongsian Kod/QR, Perkhidmatan Penjejakan Latar Belakang & Paparan Google Maps")
        
    gf.draw_pill(d, [700, 110, 900, 155], "MULA", is_start=True)
    gf.draw_process(d, [610, 185, 990, 260], "1.0 Akses Menu Bilik Kecemasan", [
        "Pengguna membuka RoomListActivity",
        "Memaparkan senarai bilik kecemasan aktif pengguna"
    ])
    gf.draw_decision(d, 800, 335, 270, 80, "Pilihan Bilik", ["Pengguna?"])
    
    # Cipta Bilik (Kiri)
    gf.draw_process(d, [180, 415, 540, 495], "2.1 Cipta Bilik Baharu", [
        "Pengguna masukkan Nama Bilik",
        "Sistem jana kod rawak unik 6 aksara (cth: RT9482)",
        "Daftarkan pengguna sebagai Pemilik (Owner)"
    ])
    
    # Sertai Bilik (Kanan)
    gf.draw_process(d, [1060, 415, 1420, 495], "2.2 Sertai Bilik Sedia Ada", [
        "Pilihan 1: Masukkan 6 aksara Kod Bilik secara manual",
        "Pilihan 2: Imbas Kod QR Bilik (ScanQrActivity)"
    ])
    gf.draw_decision(d, 1240, 570, 260, 80, "Pengesahan Kod Bilik", ["di Firebase RTDB?"])
    
    # RoomHub
    gf.draw_process(d, [620, 600, 980, 680], "3.0 Akses RoomHubActivity", [
        "Papar senarai ahli, status dalam talian & peratusan bateri",
        "Pilihan: Hantar Bell, Buka Sembang, atau Buka Peta"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_decision(d, 800, 755, 270, 80, "Operasi Ahli Bilik", ["Pilihan Interaksi?"])
    
    # 3 Pilihan Interaksi
    # Pilihan 1: Bell / Ping
    gf.draw_subroutine(d, [80, 840, 440, 925], "4.1 Hantar Bell / Ping Kecemasan", [
        "Tulis rekod ke /rooms/{code}/bells/{toUid}",
        "Cloud Function onBellCreated mencetuskan FCM getaran",
        "Ahli menerima isyarat ping kecemasan segera"
    ])
    
    # Pilihan 2: Room Chat
    gf.draw_process(d, [480, 840, 820, 925], "4.2 Sembang Bilik (RoomChatActivity)", [
        "Saluran pemesejan selamat khusus ahli bilik",
        "Hantar mesej teks dan foto insiden masa nyata",
        "Penyegerakan berterusan melalui Firebase RTDB"
    ])
    
    # Pilihan 3: Room Map
    gf.draw_process(d, [860, 840, 1240, 925], "4.3 Peta Lokasi (RoomMapActivity)", [
        "Pengguna membuka antara muka Google Maps SDK",
        "Memerlukan kebenaran ACCESS_FINE_LOCATION"
    ], border_color=gf.C_PROC_BD)
    
    # Penjejakan Latar Belakang
    gf.draw_subroutine(d, [840, 980, 1260, 1075], "5.0 LiveRoomTrackingService (Foreground)", [
        "Servis Latar Belakang jenis 'location | dataSync'",
        "FusedLocationProviderClient mengimbas GPS berterusan",
        "Muat naik koordinat Lat/Lng, ketepatan & bateri ke RTDB"
    ])
    
    gf.draw_process(d, [840, 1120, 1260, 1205], "6.0 Pemetaan Google Maps Interaktif", [
        "Papar avatar penanda (custom marker) setiap ahli bilik",
        "Penyegerakan pergerakan secara langsung atas peta",
        "Klik ahli untuk kiraan jarak & navigasi terus"
    ], border_color=(14, 165, 233), accent_color=(240, 249, 255))
    
    gf.draw_pill(d, [710, 1220, 890, 1260], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (800, 155), (800, 185))
    gf.draw_arrow(d, (800, 260), (800, 295))
    
    # Dec 2.0
    gf.draw_poly_arrow(d, [(665, 335), (360, 335), (360, 415)], label="Cipta Bilik", label_side="top")
    gf.draw_poly_arrow(d, [(935, 335), (1240, 335), (1240, 415)], label="Sertai Bilik", label_side="top")
    
    gf.draw_poly_arrow(d, [(360, 495), (360, 640), (620, 640)], label="Bilik Berjaya Dicipta", label_side="bottom")
    
    gf.draw_arrow(d, (1240, 495), (1240, 530))
    gf.draw_arrow(d, (1110, 570), (980, 640), label="Sah (Ditemui)", label_side="top")
    gf.draw_poly_arrow(d, [(1370, 570), (1480, 570), (1480, 455), (1420, 455)], label="Tidak Sah (Ralat)", label_side="right")
    
    gf.draw_arrow(d, (800, 680), (800, 715))
    
    # Dec 4.0 Pilihan Interaksi
    gf.draw_poly_arrow(d, [(665, 755), (260, 755), (260, 840)], label="Bell Ping", label_side="top")
    gf.draw_poly_arrow(d, [(730, 785), (650, 785), (650, 840)], label="Sembang", label_side="right")
    gf.draw_poly_arrow(d, [(870, 785), (1050, 785), (1050, 840)], label="Peta Live", label_side="left")
    
    # Map leads to Service and Maps rendering
    gf.draw_arrow(d, (1050, 925), (1050, 980))
    gf.draw_arrow(d, (1050, 1075), (1050, 1120))
    
    # Endings
    gf.draw_poly_arrow(d, [(260, 925), (260, 1240), (710, 1240)])
    gf.draw_poly_arrow(d, [(650, 925), (650, 1240), (710, 1240)])
    gf.draw_poly_arrow(d, [(840, 1160), (800, 1160), (800, 1220)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_4_emergency_room_map.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 5: PANGGILAN KECEMASAN SUARA & VIDEO WEBRTC
# ==============================================================================
def draw_flowchart_5():
    W, H = 1650, 1380
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 5: PANGGILAN KECEMASAN SUARA & VIDEO WEBRTC", 
        "Protokol Pensuisan Firebase RTDB, Sambungan Peer-to-Peer (P2P), dan Pengurusan Media Dua Hala")
        
    gf.draw_pill(d, [720, 110, 920, 155], "MULA", is_start=True)
    gf.draw_process(d, [620, 185, 1020, 260], "1.0 Pemanggil Mulakan Panggilan", [
        "Pilih mod: Panggilan Suara (Voice) atau Panggilan Video",
        "Jana ID Panggilan unik (/calls/{callId}) dengan status 'ringing'"
    ])
    gf.draw_subroutine(d, [610, 295, 1030, 385], "2.0 Inisialisasi CallSignalingClient & SDP Offer", [
        "Mulakan CallService (Foreground: microphone/camera)",
        "WebRTCClient menjana SDP Offer tempatan",
        "Muat naik SDP Offer ke nod /calls/{callId}/offer"
    ])
    gf.draw_process(d, [610, 420, 1030, 505], "3.0 Peranti Penerima Kesan Panggilan", [
        "IncomingCallManager mengesan panggilan melalui FCM / RTDB",
        "Lancarkan IncomingCallActivity (Penuh Skrin atas Kunci Peranti)",
        "Mainkan nada dering panggilan kecemasan & getaran"
    ], border_color=(217, 119, 6), accent_color=(255, 251, 235))
    
    gf.draw_decision(d, 820, 595, 290, 85, "Tindak Balas Penerima?", ["Pilihan Tindakan"])
    
    # 3 Kemungkinan: Tolak, Tamat Masa, Terima
    gf.draw_process(d, [150, 560, 480, 630], "4.1 Panggilan Ditolak", [
        "Penerima menekan 'Tolak' (Decline)",
        "Kemas kini status ke 'declined' di RTDB"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    gf.draw_process(d, [1170, 560, 1500, 630], "4.2 Tamat Masa (Timeout 30s)", [
        "Tiada jawapan selepas 30 saat berdering",
        "Kemas kini status ke 'timeout' di RTDB"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    
    gf.draw_process(d, [630, 700, 1010, 775], "4.3 Panggilan Diterima (Accept)", [
        "Penerima menekan 'Terima' (Accept)",
        "Kemas kini status panggilan ke 'connected' di RTDB"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    
    # Handshake WebRTC
    gf.draw_subroutine(d, [590, 815, 1050, 915], "5.0 Pertukaran Isyarat WebRTC (SDP Answer & ICE)", [
        "Penerima menjana SDP Answer & tulis ke /calls/{callId}/answer",
        "Kedua-dua pihak bertukar Calon ICE melalui /calls/{callId}/iceCandidates",
        "Penubuhan sambungan langsung Peer-to-Peer (P2P Mesh)"
    ], border_color=(99, 102, 241), bg_color=(238, 242, 255))
    
    gf.draw_process(d, [610, 950, 1030, 1050], "6.0 Sesi Panggilan Aktif Berjalan", [
        "Penstriman Audio & Video real-time dengan kependaman rendah",
        "Kawalan Pengguna: Bisu Mikrofon (Mute), Pembesar Suara (Speaker),",
        "dan Tukar Kamera Depan/Belakang (jika panggilan video)"
    ], border_color=gf.C_PROC_BD)
    
    gf.draw_decision(d, 820, 1140, 290, 75, "Panggilan Ditamatkan?", ["(Mana-mana Pihak)"])
    
    gf.draw_process(d, [620, 1225, 1020, 1300], "7.0 Penamatan & Pembersihan Sumber", [
        "Kemas kini status ke 'ended' di Firebase RTDB",
        "Hentikan CallService & bebaskan kamera dan mikrofon"
    ])
    gf.draw_pill(d, [730, 1335, 910, 1370], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 295))
    gf.draw_arrow(d, (820, 385), (820, 420))
    gf.draw_arrow(d, (820, 505), (820, 552))
    
    # Dec 4.0
    gf.draw_arrow(d, (675, 595), (480, 595), label="Tolak", label_side="top")
    gf.draw_arrow(d, (965, 595), (1170, 595), label="Timeout", label_side="top")
    gf.draw_arrow(d, (820, 637), (820, 700), label="Terima", label_side="right")
    
    gf.draw_arrow(d, (820, 775), (820, 815))
    gf.draw_arrow(d, (820, 915), (820, 950))
    gf.draw_arrow(d, (820, 1050), (820, 1102))
    
    gf.draw_arrow(d, (820, 1177), (820, 1225), label="Ya (Tamat)", label_side="right")
    gf.draw_arrow(d, (820, 1300), (820, 1335))
    
    # Decline / Timeout to cleanup
    gf.draw_poly_arrow(d, [(315, 630), (315, 1262), (620, 1262)])
    gf.draw_poly_arrow(d, [(1335, 630), (1335, 1262), (1020, 1262)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_5_webrtc_call.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 6: PELAPORAN INSIDEN & CARIAN HOSPITAL TERDEKAT
# ==============================================================================
def draw_flowchart_6():
    W, H = 1650, 1250
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 6: PELAPORAN INSIDEN KOMUNITI & CARIAN HOSPITAL TERDEKAT", 
        "Aliran Penghantaran Laporan Bergambar ke Firebase Storage/RTDB dan Algoritma Carian Navigasi Hospital Terdekat")
        
    # Divider line
    d.line([(825, 110), (825, 1200)], fill=(226, 232, 240), width=2)
    
    # ================= KIRI: PELAPORAN INSIDEN =================
    gf.draw_pill(d, [260, 110, 460, 155], "MULA (LAPORAN)", is_start=True)
    gf.draw_process(d, [160, 185, 560, 260], "L1.0 Buka Borang ReportActivity", [
        "Pengguna mengakses borang aduan awam / kecemasan",
        "Kebenaran Kamera & Lokasi disemak"
    ])
    gf.draw_io_box(d, [160, 290, 560, 365], "L2.0 Pilih Kategori Insiden", [
        "Kemalangan Jalan Raya / Jenayah / Kecemasan Perubatan",
        "Kebakaran / Bencana Alam / Lain-lain"
    ])
    gf.draw_io_box(d, [160, 395, 560, 470], "L3.0 Butiran & Lokasi Kejadian", [
        "Pengguna memasukkan deskripsi terperinci",
        "GPS semasa diperoleh secara automatik"
    ])
    gf.draw_decision(d, 360, 550, 270, 75, "Adakah Lampiran", ["Foto Disertakan?"])
    
    # Muat Naik Foto
    gf.draw_subroutine(d, [140, 630, 580, 715], "L4.1 Muat Naik ke Firebase Storage", [
        "Kompres imej & muat naik ke /incident_reports/{id}.jpg",
        "Dapatkan pautan muat turun selamat (Download URL)"
    ])
    
    gf.draw_subroutine(d, [140, 755, 580, 840], "L5.0 Simpan Rekod ke Firebase RTDB", [
        "Simpan nod data lengkap ke /reports/{reportId}",
        "Sertakan UID, timestamp, kategori, GPS & URL Foto"
    ])
    gf.draw_process(d, [160, 880, 560, 955], "L6.0 Pengesahan & Penjanaan Tiket", [
        "Paparkan mesej status aduan diterima",
        "Notifikasi dihantar kepada pihak pentadbir"
    ], border_color=gf.C_START_END_BD, accent_color=(236, 253, 245))
    gf.draw_pill(d, [270, 1010, 450, 1050], "TAMAT", is_start=False)
    
    # Arrows Kiri
    gf.draw_arrow(d, (360, 155), (360, 185))
    gf.draw_arrow(d, (360, 260), (360, 290))
    gf.draw_arrow(d, (360, 365), (360, 395))
    gf.draw_arrow(d, (360, 470), (360, 512))
    
    gf.draw_arrow(d, (360, 587), (360, 630), label="Ya (Ada Foto)", label_side="right")
    gf.draw_poly_arrow(d, [(495, 550), (620, 550), (620, 795), (580, 795)], label="Tidak", label_side="top")
    
    gf.draw_arrow(d, (360, 715), (360, 755))
    gf.draw_arrow(d, (360, 840), (360, 880))
    gf.draw_arrow(d, (360, 955), (360, 1010))
    
    # ================= KANAN: CARIAN HOSPITAL =================
    gf.draw_pill(d, [1180, 110, 1380, 155], "MULA (HOSPITAL)", is_start=True)
    gf.draw_process(d, [1080, 185, 1480, 260], "H1.0 Buka NearbyHospitalActivity", [
        "Pengguna memilih menu 'Hospital & Klinik Terdekat'",
        "Semakan keizinan GPS peranti dijalankan"
    ])
    gf.draw_subroutine(d, [1080, 295, 1480, 375], "H2.0 Dapatkan Koordinat GPS Pengguna", [
        "fusedLocationClient.getCurrentLocation()",
        "Peroleh Latitud & Longitud semasa pengguna"
    ])
    gf.draw_subroutine(d, [1080, 410, 1480, 495], "H3.0 Kueri API Kemudahan Perubatan", [
        "Hantar permintaan geolokasi ke API Perubatan",
        "Dapatkan data hospital, pusat perubatan & klinik"
    ])
    gf.draw_process(d, [1080, 530, 1480, 615], "H4.0 Kira Jarak & Susun Terdekat", [
        "Kira jarak relatif menggunakan formula Haversine",
        "Susun senarai hospital bermula dari jarak paling dekat"
    ])
    gf.draw_io_box(d, [1080, 650, 1480, 735], "H5.0 Paparan Senarai Hospital", [
        "Papar nama hospital, alamat, jarak (km),",
        "waktu operasi dan nombor telefon kecemasan"
    ])
    gf.draw_decision(d, 1280, 810, 270, 75, "Pilihan Tindakan", ["Pengguna?"])
    
    gf.draw_process(d, [960, 895, 1240, 975], "H6.1 Panggilan Telefon", [
        "Buka Android Dialer terus",
        "ke talian kecemasan hospital"
    ], border_color=(217, 119, 6))
    
    gf.draw_process(d, [1310, 895, 1590, 975], "H6.2 Navigasi Google Maps", [
        "Lancarkan Google Maps Intent",
        "Panduan laluan belokan-demi-belokan"
    ], border_color=gf.C_PROC_BD)
    
    gf.draw_pill(d, [1190, 1020, 1370, 1060], "TAMAT", is_start=False)
    
    # Arrows Kanan
    gf.draw_arrow(d, (1280, 155), (1280, 185))
    gf.draw_arrow(d, (1280, 260), (1280, 295))
    gf.draw_arrow(d, (1280, 375), (1280, 410))
    gf.draw_arrow(d, (1280, 495), (1280, 530))
    gf.draw_arrow(d, (1280, 615), (1280, 650))
    gf.draw_arrow(d, (1280, 735), (1280, 772))
    
    gf.draw_poly_arrow(d, [(1145, 810), (1100, 810), (1100, 895)], label="Panggilan", label_side="left")
    gf.draw_poly_arrow(d, [(1415, 810), (1450, 810), (1450, 895)], label="Navigasi", label_side="right")
    
    gf.draw_poly_arrow(d, [(1100, 975), (1100, 1040), (1190, 1040)])
    gf.draw_poly_arrow(d, [(1450, 975), (1450, 1040), (1370, 1040)])
    
    path = os.path.join(OUTPUT_DIR, "flowchart_6_report_hospital.png")
    img.save(path)
    print("Saved:", path)

# ==============================================================================
# CARTA ALIR 7: PORTAL PENTADBIR & PENYIARAN NOTIFIKASI AWAN
# ==============================================================================
def draw_flowchart_7():
    W, H = 1650, 1260
    img = Image.new("RGB", (W, H), color=gf.BG_COLOR)
    d = ImageDraw.Draw(img)
    
    gf.draw_header_banner(d, W, 
        "CARTA ALIR 7: PORTAL PENTADBIR & PENYIARAN NOTIFIKASI AWAN", 
        "Pengesahan Akses Admin, Pengendalian Kes SOS Langsung, Penyiaran Notifikasi Pentadbir & Penyelenggaraan Sistem")
        
    gf.draw_pill(d, [720, 110, 920, 155], "MULA", is_start=True)
    gf.draw_process(d, [610, 185, 1030, 260], "1.0 Akses Web Portal Pentadbir", [
        "Akses melalui pelayar web (server.js /admin)",
        "Pengesahan melalui Firebase Authentication"
    ])
    gf.draw_decision(d, 820, 335, 310, 85, "Adakah Pengguna", ["Mempunyai Akses Admin?"])
    
    # Akses Ditolak
    gf.draw_process(d, [150, 295, 480, 375], "2.0 Akses Ditolak (403)", [
        "Domain e-mel bukan @resqtap.com dan tiada rekod /admins/{uid}",
        "Sistem menafikan akses ke papan pemuka"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    gf.draw_pill(d, [260, 420, 370, 460], "TAMAT", is_start=False)
    
    # Akses Dibenarkan -> Dashboard
    gf.draw_process(d, [610, 450, 1030, 535], "3.0 Papan Pemuka Pentadbir (admin.html)", [
        "Pemantauan status masa nyata: Bilangan Pengguna, Bilik & Amaran",
        "Pengendali memilih tindakan operasi yang diinginkan"
    ], border_color=gf.C_PROC_BD, accent_color=gf.C_PROC_ACCENT)
    
    gf.draw_decision(d, 820, 625, 300, 80, "Pilihan Operasi", ["Tindakan Pentadbir?"])
    
    # 3 Operasi Pentadbir
    # Operasi 1: Kes SOS Langsung
    gf.draw_process(d, [80, 720, 480, 825], "4.1 Kawalan Insiden SOS", [
        "Dengar amaran masuk dari /rooms/*/sosAlerts",
        "Klik 'Reserve Kes' (served = true, servedBy = adminUid)",
        "Kemas kini status kemajuan (Dispatched -> Resolved)",
        "Sembang langsung dengan mangsa kecemasan"
    ], border_color=gf.C_STOP_BD, accent_color=(255, 241, 242))
    
    # Operasi 2: Penyiaran Notifikasi
    gf.draw_process(d, [580, 720, 1060, 825], "4.2 Penyiaran Notifikasi Pentadbir", [
        "Pilih sasaran audiens: Semua / Bilik Live / Pentadbir",
        "Masukkan Tajuk dan Kandungan Mesej Amaran",
        "Panggil Cloud Function: sendAdminNotification"
    ], border_color=gf.C_SUB_BD, accent_color=gf.C_SUB_BG)
    
    gf.draw_subroutine(d, [580, 870, 1060, 965], "4.2b Cloud Function FCM Blaster", [
        "Kumpul FCM Token pengguna sasaran",
        "Hantar push notification keutamaan tinggi serentak",
        "Simpan rekod ke /admin_notifications/{historyId}"
    ])
    
    # Operasi 3: Penyelenggaraan & Pemadaman Pengguna
    gf.draw_process(d, [1140, 720, 1560, 825], "4.3 Keselamatan & Penyelenggaraan", [
        "Padam akaun bermasalah via /admin_user_deletions/{uid}",
        "Cloud Function memadam rekod Auth & RTDB menyeluruh",
        "Pembersihan pangkalan data via /api/admin/clear-database"
    ], border_color=(100, 116, 139), accent_color=(241, 245, 249))
    
    gf.draw_process(d, [630, 1040, 1010, 1115], "5.0 Pangkalan Data Dikemas Kini", [
        "Operasi selesai dan disegerakkan ke Firebase",
        "Papan pemuka memaparkan statistik terkini"
    ])
    gf.draw_pill(d, [730, 1160, 910, 1200], "TAMAT", is_start=False)
    
    # Arrows
    gf.draw_arrow(d, (820, 155), (820, 185))
    gf.draw_arrow(d, (820, 260), (820, 292))
    
    # Dec 2.0 (Admin Auth)
    gf.draw_arrow(d, (665, 335), (480, 335), label="Tidak (Bukan Admin)", label_side="top")
    gf.draw_arrow(d, (315, 375), (315, 420))
    gf.draw_arrow(d, (820, 377), (820, 450), label="Ya (Admin Sah)", label_side="right")
    
    gf.draw_arrow(d, (820, 535), (820, 585))
    
    # Dec 3.0 (Operasi)
    gf.draw_poly_arrow(d, [(670, 625), (280, 625), (280, 720)], label="Urus SOS", label_side="top")
    gf.draw_arrow(d, (820, 665), (820, 720), label="Notifikasi", label_side="right")
    gf.draw_poly_arrow(d, [(970, 625), (1350, 625), (1350, 720)], label="Penyelenggaraan", label_side="top")
    
    # Notification sub-flow
    gf.draw_arrow(d, (820, 825), (820, 870))
    gf.draw_arrow(d, (820, 965), (820, 1040))
    
    # From 4.1 & 4.3 to 5.0
    gf.draw_poly_arrow(d, [(280, 825), (280, 1077), (630, 1077)])
    gf.draw_poly_arrow(d, [(1350, 825), (1350, 1077), (1010, 1077)])
    
    gf.draw_arrow(d, (820, 1115), (820, 1160))
    
    path = os.path.join(OUTPUT_DIR, "flowchart_7_admin_portal_cloud.png")
    img.save(path)
    print("Saved:", path)

if __name__ == "__main__":
    print("Mula menjana 7 gambar rajah carta alir...")
    draw_flowchart_1()
    draw_flowchart_2()
    draw_flowchart_3()
    draw_flowchart_4()
    draw_flowchart_5()
    draw_flowchart_6()
    draw_flowchart_7()
    print("Semua 7 carta alir berjaya dijana dalam folder docs_flowcharts!")
