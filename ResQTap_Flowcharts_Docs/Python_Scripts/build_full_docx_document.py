import os
import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
import generate_docx_report as gdr

DOCS_DIR = os.path.join(os.path.dirname(__file__), "docs_flowcharts")
OUTPUT_DOCX = os.path.join(os.path.dirname(__file__), "ResQTap_Dokumentasi_Carta_Alir_Sistem.docx")

def build_document():
    doc = docx.Document()
    
    # Page setup - A4 with 1 inch (72pt) margins
    for section in doc.sections:
        section.page_width = Inches(8.27)
        section.page_height = Inches(11.69)
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        
    print("Mula membina dokumen Word...")

    # =========================================================================
    # MUKA SURAT KULIT / TAJUK UTAMA (EXECUTIVE COVER HEADER)
    # =========================================================================
    p_badge = doc.add_paragraph()
    gdr.format_paragraph(p_badge, space_before=10, space_after=6)
    r_badge = p_badge.add_run("DOKUMEN SPESIFIKASI KEJURUTERAAN PERISIAN • FINAL YEAR PROJECT (FYP)")
    r_badge.font.name = gdr.FONT_FAMILY
    r_badge.font.size = Pt(9.5)
    r_badge.font.bold = True
    r_badge.font.color.rgb = gdr.COLOR_CRIMSON

    p_title = doc.add_paragraph()
    gdr.format_paragraph(p_title, space_before=0, space_after=6)
    r_title = p_title.add_run("DOKUMENTASI CARTA ALIR SISTEM\n(SYSTEM FLOWCHARTS SPECIFICATION)")
    r_title.font.name = gdr.FONT_FAMILY
    r_title.font.size = Pt(22)
    r_title.font.bold = True
    r_title.font.color.rgb = gdr.COLOR_NAVY

    p_subtitle = doc.add_paragraph()
    gdr.format_paragraph(p_subtitle, space_before=0, space_after=14)
    r_subtitle = p_subtitle.add_run("Sistem Tindak Balas Kecemasan Bersepadu, Amaran Pantas & Penjejakan Masa Nyata ResQTap (OneTapSOS)")
    r_subtitle.font.name = gdr.FONT_FAMILY
    r_subtitle.font.size = Pt(13)
    r_subtitle.font.bold = True
    r_subtitle.font.color.rgb = gdr.COLOR_SLATE

    # Metadata Table
    meta_headers = ["Parameter Spesifikasi", "Butiran Dokumen"]
    meta_rows = [
        ["Nama Projek", "ResQTap (OneTapSOS) Emergency Response System"],
        ["Platform Klien Mudah Alih", "Android Native (Java & Kotlin, Min SDK 24, Target SDK 36)"],
        ["Prasarana Awan & Pangkalan Data", "Firebase Platform (Realtime Database, Authentication, Cloud Storage, Cloud Messaging)"],
        ["Komunikasi Masa Nyata", "Google WebRTC P2P Intercom (Audio & Video Calling) & FusedLocationProvider"],
        ["Portal Pentadbir & Web", "Node.js HTTP Server, REST APIs, & Real-time WebRTC/RTDB Console"],
        ["Piawaian Carta Alir", "ISO 5807:1985 Information Processing - Flowchart Symbols and Conventions"],
        ["Versi Dokumen", "Versi 2.0 (Pengeluaran Rasmi / Stable Release)"],
        ["Tarikh Kemas Kini", "September 2026"]
    ]
    gdr.create_styled_table(doc, meta_headers, meta_rows, col_widths=[2.4, 3.8])

    gdr.add_callout(doc, 
        "Dokumen ini menyediakan spesifikasi carta alir lengkap dan rasmi bagi sistem ResQTap mengikut piawaian ISO 5807. "
        "Setiap rajah carta alir disertakan dengan penerangan logik langkah demi langkah, matrik input/output, pengendalian ralat, "
        "serta rujukan kelas kod sumber projek sebenar.",
        title="PANDUAN DOKUMENTASI", border_color="2563EB", bg_color="EFF6FF")

    doc.add_page_break()

    # =========================================================================
    # JADUAL KANDUNGAN & PENGENALAN
    # =========================================================================
    gdr.add_heading_1(doc, "1. PENGENALAN & RINGKASAN EKOSISTEM RESQTAP")
    
    gdr.add_body_p(doc, 
        "ResQTap (OneTapSOS) adalah sebuah ekosistem keselamatan peribadi dan tindak balas kecemasan pintar yang direka khas untuk "
        "merapatkan jurang masa kritikal (The Golden 5-Minute Window) semasa sesuatu kecemasan berlaku. Sistem ini menggabungkan "
        "aplikasi mudah alih Android Native, perkhidmatan pengkomputeran awan Firebase (Realtime Database, FCM, Cloud Functions), "
        "penjejakan geolokasi FusedLocationProvider, komunikasi suara dan video berasaskan WebRTC, serta sebuah Portal Web Pentadbir.")

    gdr.add_heading_2(doc, "1.1 Struktur Modul Utama Sistem")
    gdr.add_bullet_p(doc, "Mengendalikan pendaftaran, sesi log masuk Firebase Auth, kunci aplikasi berasaskan PIN 4-digit, pengesahan cap jari biometrik, dan mekanisme perlindungan covert Duress PIN.", bold_prefix="Modul 1 - Autentikasi & Keselamatan Aplikasi: ")
    gdr.add_bullet_p(doc, "Pengaktifan OneTap SOS dengan kiraan detik 3 saat, penggera fizikal (siren, strobe, haptik), penyiaran koordinat GPS masa nyata ke Firebase RTDB, pemicu notifikasi FCM, dan penjejakan garis masa bantuan 4 peringkat.", bold_prefix="Modul 2 - Pencetus Kecemasan OneTap SOS: ")
    gdr.add_bullet_p(doc, "Penciptaan dan penyertaan bilik kecemasan melalui Kod 6-Aksara atau imbasan Kod QR, pemantauan status bateri ahli, isyarat Bell/Ping segera, servis penjejakan latar belakang LiveRoomTrackingService, dan pemetaan interaktif Google Maps SDK.", bold_prefix="Modul 3 - Bilik Kecemasan (Room Hub) & Penjejakan GPS: ")
    gdr.add_bullet_p(doc, "Interkom kecemasan suara dan video berkependaman rendah (low-latency) menggunakan WebRTC P2P dengan Firebase RTDB sebagai saluran pensuisan (signaling channel) dan CallService sebagai foreground service.", bold_prefix="Modul 4 - Panggilan Interkom WebRTC: ")
    gdr.add_bullet_p(doc, "Penyerahan aduan insiden bergambar ke Firebase Cloud Storage/RTDB, carian hospital terdekat menggunakan formula Haversine, panggilan terus dialer kecemasan, serta integrasi navigasi Google Maps turn-by-turn.", bold_prefix="Modul 5 - Pelaporan Insiden & Carian Hospital: ")
    gdr.add_bullet_p(doc, "Pusat kawalan kecemasan berasaskan web untuk pemantauan amaran langsung, penempahan kes oleh pentadbir, penyiaran notifikasi awan secara global atau bersasar, dan keselamatan data akaun.", bold_prefix="Modul 6 - Portal Pentadbir & Penyiaran Awan: ")

    gdr.add_heading_2(doc, "1.2 Piawaian Simbol Carta Alir ISO 5807")
    gdr.add_body_p(doc, 
        "Bagi memastikan kejelasan dan kepatuhan kepada amalan kejuruteraan perisian profesional, semua carta alir di dalam dokumen ini "
        "mengikuti piawaian ISO 5807:1985 (Information Processing - Documentation Symbols and Conventions for Data, Program and System Flowcharts):")

    iso_headers = ["Bentuk Simbol", "Nama Simbol (ISO)", "Fungsi Standard", "Aplikasi dalam ResQTap"]
    iso_rows = [
        ["Pill / Terminal", "Terminal (Mula / Tamat)", "Menandakan titik permulaan atau penamatan sesuatu kitaran aliran logik.", "MULA dan TAMAT sesi aktiviti atau proses perkhidmatan."],
        ["Segi Empat Tepat", "Proses (Process)", "Operasi pengkomputeran, pengiraan, penukaran keadaan, atau arahan program.", "Inisialisasi perkhidmatan, kemas kini UI, pengaktifan siren/strobe."],
        ["Rombus (Diamond)", "Keputusan (Decision)", "Titik cabang logik berasaskan penilaian syarat (Boolean / Pelbagai Pilihan).", "Semakan sesi aktif, pengesahan PIN, padanan kod Duress, pilihan pengguna."],
        ["Paralelogram", "Input / Output (I/O)", "Kemasukan data daripada pengguna atau pengeluaran data kepada pengguna/skrin.", "Input pad kekunci PIN, imbasan Kod QR, paparan senarai hospital."],
        ["Segi Empat Bergaris Ganda", "Proses Pratakrif (Subroutine)", "Modul luar, panggilan API, servis latar belakang, atau fungsi awan.", "Panggilan Firebase Cloud Functions, servis LiveRoomTrackingService, WebRTC handshake."],
        ["Garis Alir & Anak Panah", "Aliran Aliran (Flowline)", "Menunjukkan urutan langkah pelaksanaan kawalan daripada satu nod ke nod lain.", "Penyambungan nod secara berturutan berserta label syarat (Ya / Tidak)."]
    ]
    gdr.create_styled_table(doc, iso_headers, iso_rows, col_widths=[1.3, 1.4, 1.7, 1.8])

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 1: CARTA ALIR KESELURUHAN SENI BINA SISTEM
    # =========================================================================
    gdr.add_heading_1(doc, "2. BAHAGIAN 1: CARTA ALIR KESELURUHAN ALIRAN SISTEM RESQTAP")
    
    gdr.add_body_p(doc, 
        "Carta alir ini memberikan gambaran komprehensif mengenai kitaran hayat operasi aplikasi ResQTap bermula daripada fasa "
        "pelancaran SplashActivity, semakan integriti sesi Firebase Auth, pintu keselamatan Kunci Aplikasi (App Lock / Biometrik), "
        "sehinggalah ke penjelajahan modul-modul fungsian di Dashboard Utama (MainActivity).")

    # Embed Flowchart Image 1
    fc1_path = os.path.join(DOCS_DIR, "flowchart_1_overall_architecture.png")
    if os.path.exists(fc1_path):
        doc.add_picture(fc1_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 2.1: Carta Alir Keseluruhan Aliran Navigasi dan Seni Bina Logik Sistem ResQTap")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "2.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Aplikasi dimulakan melalui SplashActivity. Sistem menyemak keserasian tema peranti, memuatkan kebenaran pemberitahuan dan GPS, serta menginisialisasi sambungan Firebase App.", bold_prefix="Langkah 1.0 - Pelancaran Aplikasi: ")
    gdr.add_bullet_p(doc, "Sistem memanggil FirebaseAuth.getInstance().getCurrentUser(). Jika tiada sesi aktif (null), aliran dialihkan ke GetStartedActivity untuk proses onboarding, log masuk, atau pendaftaran akaun. Jika sesi aktif ditemui, sistem meneruskan ke semakan keselamatan.", bold_prefix="Langkah 2.0 - Semakan Sesi Pengguna: ")
    gdr.add_bullet_p(doc, "Sistem menyemak tetapan UserPrefs.isAppLockEnabled() dan UserPrefs.isFingerprintEnabled(). Jika aktif dan sesi belum dinyahkunci, AppLockActivity dipaparkan. Pengguna wajib memasukkan 4-digit PIN atau mengimbas cap jari. Sekiranya kod Duress PIN dimasukkan, protokol Silent SOS diaktifkan serta-merta.", bold_prefix="Langkah 3.0 - Pengesahan Kunci Aplikasi: ")
    gdr.add_bullet_p(doc, "Setelah melepasi keselamatan, pengguna dibawa ke MainActivity. Skrin ini memaparkan profil peribadi, kad rekod perubatan (jenis darah, alahan, keadaan kronik), peratusan bateri peranti, amaran siaran pentadbir, dan suapan berita keselamatan.", bold_prefix="Langkah 4.0 - Papan Pemuka Utama (MainActivity): ")
    gdr.add_bullet_p(doc, "Pengguna boleh memilih antara 5 cabang perkhidmatan utama: (1) Modul OneTap SOS untuk amaran segera, (2) Bilik Kecemasan untuk penjejakan kumpulan, (3) Panggilan WebRTC untuk interkom video/audio, (4) Carian Hospital untuk navigasi rawatan terdekat, atau (5) Pelaporan Insiden & Berita.", bold_prefix="Langkah 5.0 - Pemilihan Modul Fungsian: ")
    gdr.add_bullet_p(doc, "Apabila pengguna menutup aplikasi atau menekan log keluar, servis penjejakan dinyahaktifkan secara selamat, sesi dikunci semula melalui AppLockManager, dan token peranti dikemas kini.", bold_prefix="Langkah 6.0 - Kitaran Hayat & Penamatan Sesi: ")

    gdr.add_heading_2(doc, "2.2 Matriks Entiti, Input, Proses dan Output")
    t1_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t1_rows = [
        ["1.0 Pelancaran", "SplashActivity", "Intent pelancaran aplikasi", "Inisialisasi tema, perkhidmatan & Firebase", "Penyediaan UI dan pemeriksaan persekitaran"],
        ["2.0 Semakan Sesi", "FirebaseAuth", "Token sesi tempatan / Cache Auth", "Semakan getCurrentUser() != null", "Pelepasan ke AppLock atau GetStarted"],
        ["3.0 Kunci Aplikasi", "AppLockManager", "4-digit PIN / Imbasan Biometrik", "Pemadanan PIN biasa, Duress PIN, atau cap jari", "Akses diberi / Silent SOS dicetuskan / Ralat"],
        ["4.0 Dashboard", "MainActivity", "Profil pengguna & pangkalan data", "Paparan kad perubatan, bateri, amaran admin", "Papan pemuka sedia untuk interaksi"],
        ["5.0 Pemilihan Modul", "Intent Dispatcher", "Sentuhan pengguna pada kad menu", "Pelancaran aktiviti khusus mengikut modul", "Navigasi ke modul perkhidmatan yang dipilih"],
        ["6.0 Penamatan", "BaseActivity & Prefs", "Tindakan keluar / onDestroy()", "Pembersihan listener pangkalan data & servis", "Aplikasi selamat dalam mod latar belakang / mati"]
    ]
    gdr.create_styled_table(doc, t1_headers, t1_rows, col_widths=[1.0, 1.2, 1.3, 1.4, 1.3])

    gdr.add_heading_2(doc, "2.3 Pengendalian Ralat dan Mod Luar Talian (Offline Resilience)")
    gdr.add_body_p(doc, 
        "Sekiranya peranti tiada sambungan internet semasa fasa pelancaran, Firebase Realtime Database mengekalkan mod cache luar talian "
        "(Disk Persistence). Pengguna tetap boleh melihat rekod Kad Perubatan tempatan, mengakses nombor kecemasan hospital berdekatan "
        "yang telah disimpan dalam cache, serta menggunakan butang dail terus kecemasan.")

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.app.SplashActivity, com.example.resqtap.home.MainActivity, "
        "com.example.resqtap.app.BaseActivity, com.example.resqtap.security.AppLockManager, com.example.resqtap.utils.UserPrefs.", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 2: CARTA ALIR AUTENTIKASI, KUNCI APLIKASI & DURESS PIN
    # =========================================================================
    gdr.add_heading_1(doc, "3. BAHAGIAN 2: CARTA ALIR AUTENTIKASI, KUNCI APLIKASI & PROTOKOL DURESS PIN")
    
    gdr.add_body_p(doc, 
        "Modul keselamatan ResQTap menyediakan perlindungan berlapis yang merangkumi pengesahan identiti pengguna, perlindungan data peribadi "
        "daripada akses fizikal tanpa kebenaran, serta ciri keselamatan inovatif iaitu Duress PIN (Kod Keselamatan Paksaan) yang direka "
        "khas untuk situasi di mana mangsa dipaksa membuka kunci telefon oleh penjenayah.")

    # Embed Flowchart Image 2
    fc2_path = os.path.join(DOCS_DIR, "flowchart_2_auth_security.png")
    if os.path.exists(fc2_path):
        doc.add_picture(fc2_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 3.1: Carta Alir Pengesahan Pengguna, Kunci Aplikasi dan Protokol Tindak Balas Duress PIN")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "3.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Jika tiada sesi, GetStartedActivity memaparkan pilihan: Log Masuk (memasukkan e-mel dan kata laluan), Daftar Akaun Baharu (memasukkan nama penuh, e-mel, telefon, jenis darah dan maklumat kecemasan), atau Lupa Kata Laluan (menghantar pautan reset kata laluan melalui Firebase Auth).", bold_prefix="Langkah 1.0 - Aliran Onboarding & Autentikasi: ")
    gdr.add_bullet_p(doc, "Selepas pengesahan Firebase Auth berjaya, maklumat profil asas, rekod kad perubatan, dan token peranti (FCM Token) disimpan ke nod /users/{uid} dalam Firebase Realtime Database.", bold_prefix="Langkah 2.0 - Penyimpanan Profil & Kad Perubatan: ")
    gdr.add_bullet_p(doc, "Setiap kali pengguna kembali ke aplikasi daripada keadaan latar belakang (background to foreground), AppLockManager menyemak UserPrefs.isAppLockEnabled() dan UserPrefs.isFingerprintEnabled(). Jika aktif, AppLockActivity dinaikkan ke bahagian hadapan.", bold_prefix="Langkah 3.0 - Pemintasan Kunci Aplikasi (AppLockManager): ")
    gdr.add_bullet_p(doc, "Skrin kunci menyediakan pad angka 4-digit PIN berserta butang imbasan cap jari. Apabila input dimasukkan, sistem melakukan pemadanan tepat mengikut hierarki keselamatan.", bold_prefix="Langkah 4.0 - Pemadanan Input Pengesahan: ")
    gdr.add_bullet_p(doc, "Jika PIN yang dimasukkan sepadan dengan UserPrefs.getAppLockPin() atau imbasan cap jari sah, AppLockManager.setUnlocked(true) dipanggil dan pengguna dibenarkan masuk ke MainActivity.", bold_prefix="Langkah 4.1 - Akses Biasa Dibenarkan: ")
    gdr.add_bullet_p(doc, "Sekiranya PIN sepadan dengan kod khas UserPrefs.getDuressPin(), sistem serta-merta mencetuskan protokol Silent SOS ke Firebase RTDB. Isyarat kecemasan dihantar secara halimunan TANPA sebarang bunyi siren, TANPA lampu suluh, dan TANPA sebarang tanda amaran pada skrin. Antara muka aplikasi dibuka seperti biasa untuk mengelakkan kecurigaan dan bahaya fizikal daripada penyerang.", bold_prefix="Langkah 4.2 - Protokol Keselamatan Rahsia (Duress PIN / Silent SOS): ")
    gdr.add_bullet_p(doc, "Jika PIN tidak sepadan, pembilang ralat dinaikkan dan kesan getaran haptik ralat dipaparkan. Sekiranya cubaan gagal melebihi 5 kali berturut-turut, pad kekunci dilumpuhkan selama 30 saat (Cooldown Timer) bagi menghalang serangan brute-force.", bold_prefix="Langkah 4.3 - Perlindungan Anti Brute-Force: ")

    gdr.add_heading_2(doc, "3.2 Matriks Entiti, Input, Proses dan Output")
    t2_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t2_rows = [
        ["1.0 Autentikasi", "LoginActivity / RegisterActivity", "E-mel, kata laluan, profil perubatan", "signInWithEmailAndPassword / createUser", "Token Firebase Auth dijana"],
        ["2.0 Profil Data", "Firebase RTDB", "Data demografik & kad kecemasan", "Tulis rekod ke /users/{uid}", "Profil diselaraskan ke peranti"],
        ["3.0 Pemintasan", "AppLockManager", "Kitaran hayat aktiviti (onActivityStarted)", "Semak bendera kunci & status sesi", "Lancarkan AppLockActivity jika terkunci"],
        ["4.1 Nyahkunci Sah", "AppLockActivity", "4-digit PIN betul / Biometrik", "Padankan dengan hash PIN tempatan", "isUnlockedForSession = true, masuk Dashboard"],
        ["4.2 Duress PIN", "AppLockActivity & RTDB", "4-digit Kod Duress", "Kesan padanan Duress -> Siar Silent SOS", "Amaran senyap dihantar, apl dibuka menyamar"],
        ["4.3 Cubaan Gagal", "AppLockActivity", "4-digit PIN salah", "Kira percubaan gagal, jika >= 5 kunci 30s", "Pad kekunci disekat, amaran penyejukan"]
    ]
    gdr.create_styled_table(doc, t2_headers, t2_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.auth.LoginActivity, com.example.resqtap.auth.RegisterActivity, "
        "com.example.resqtap.security.AppLockActivity, com.example.resqtap.security.AppLockManager, "
        "com.example.resqtap.security.SetPinActivity, com.example.resqtap.utils.UserPrefs.", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 3: CARTA ALIR PENCETUS & PENGURUSAN ONETAP SOS
    # =========================================================================
    gdr.add_heading_1(doc, "4. BAHAGIAN 3: CARTA ALIR PENCETUS & PENGURUSAN KECEMASAN ONETAP SOS")
    
    gdr.add_body_p(doc, 
        "Modul OneTap SOS merupakan fungsi teras paling kritikal dalam sistem ResQTap. Modul ini direka untuk mengaktifkan amaran pantas "
        "dalam situasi kecemasan sebenar dengan satu sentuhan, sambil menyediakan tingkap masa 3 saat untuk mengelakkan pencetus palsu "
        "(accidental triggers). Setelah disahkan, sistem mengaktifkan penggera fizikal serta menyiarkan koordinat mangsa ke seluruh rangkaian "
        "bilik kecemasan, responden, dan konsol pentadbir.")

    # Embed Flowchart Image 3
    fc3_path = os.path.join(DOCS_DIR, "flowchart_3_onetap_sos.png")
    if os.path.exists(fc3_path):
        doc.add_picture(fc3_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 4.1: Carta Alir Pencetus, Penyiaran Amaran Awan dan Garis Masa Bantuan OneTap SOS")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "4.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Pengguna menekan butang bulat OneTap SOS di MainActivity. Sistem segera menaikkan dialog kecemasan modal (SosBottomSheetController) yang mengandungi teks kiraan detik dan bar gelongsor pembatalan.", bold_prefix="Langkah 1.0 - Pencetus Butang SOS: ")
    gdr.add_bullet_p(doc, "Kiraan detik 3 saat (CountDownTimer) bermula. Sekiranya pengguna tersilap menekan butang, mereka boleh meleret gelongsor (Slide to Cancel) sehingga mencapai nilai >= 95%. Dialog ditutup serta-merta dan tiada amaran dihantar ke pelayan.", bold_prefix="Langkah 2.0 - Kiraan Detik 3 Saat & Pembatalan: ")
    gdr.add_bullet_p(doc, "Jika kiraan detik mencapai 0 saat tanpa dibatalkan, amaran disahkan! Tiga subsistem perkakasan fizikal diaktifkan serentak: (1) Siren desibel tinggi dimainkan melalui SosAudioManager pada kelantangan maksimum, (2) Lampu suluh strob kamera berkelip laju untuk menarik perhatian di lokasi fizikal, dan (3) Corak getaran haptik berterusan dimulakan melalui VibrateManager.", bold_prefix="Langkah 3.0 - Pengaktifan Penggera Fizikal Tempatan: ")
    gdr.add_bullet_p(doc, "Sistem memanggil FusedLocationProviderClient untuk mendapatkan koordinat Latitud dan Longitud mangsa yang paling tepat, tahap bateri semasa, serta membaca senarai bilik aktif mangsa daripada /userRooms/{uid}.", bold_prefix="Langkah 4.0 - Pengambilan Koordinat GPS & Bilik: ")
    gdr.add_bullet_p(doc, "Jika mangsa menyertai sekurang-kurangnya satu bilik kecemasan, rekod amaran disiarkan ke setiap /rooms/{roomCode}/sosAlerts. Sekiranya mangsa belum menyertai sebarang bilik, sistem menggunakan mekanisme sandaran (fallback mechanism) dengan menghantar amaran ke saluran kecemasan terus /rooms/DIRECT/sosAlerts dan nod global /sos_alerts.", bold_prefix="Langkah 5.0 - Penyiaran Isyarat ke Firebase RTDB: ")
    gdr.add_bullet_p(doc, "Penyisipan rekod di RTDB mencetuskan fungsi awan Firebase (Cloud Functions: onRoomSosCreated dan onSosAlertCreated). Fungsi ini mengumpul semua token FCM ahli bilik dan responden berdaftar, lalu menyiarkan Notifikasi Tolak Keutamaan Tinggi (High Priority Push Notification).", bold_prefix="Langkah 6.0 - Pemicu Automasi Cloud Functions: ")
    gdr.add_bullet_p(doc, "Peranti ahli bilik yang menerima notifikasi FCM akan memaparkan SosAlarmActivity di atas skrin kunci (Show on Lock Screen dengan kebenaran USE_FULL_SCREEN_INTENT). Ahli boleh meleret untuk menunda siren (Slide to Snooze), membuka RoomMapActivity untuk menjejaki mangsa secara langsung, atau memulakan panggilan.", bold_prefix="Langkah 7.1 - Tindak Balas Ahli Bilik & Responden: ")
    gdr.add_bullet_p(doc, "Pada masa yang sama, insiden muncul serta-merta dengan amaran merah pada Portal Web Pentadbir. Pentadbir boleh menempah kes (Reserve Incident: menetapkan served=true dan servedBy=adminUid), lalu mengemas kini status garis masa bantuan (Dispatched -> En Route -> On Scene -> Resolved).", bold_prefix="Langkah 7.2 - Pemantauan & Tempahan Pentadbir: ")
    gdr.add_bullet_p(doc, "Aplikasi mangsa yang sedang memantau nod amaran melalui watchSosCaseReservation() akan mengesan tempahan admin secara automatik dan serta-merta melancarkan SosProgressActivity. Mangsa dapat melihat nama pegawai responden, anggaran masa tiba, dan garis masa tindakan secara langsung.", bold_prefix="Langkah 8.0 - Paparan Garis Masa Kemajuan Mangsa: ")
    gdr.add_bullet_p(doc, "Apabila pentadbir atau mangsa menekan butang 'Resolve / Cancel SOS', status di RTDB dikemas kini kepada 'resolved' atau 'cancelled'. Fungsi onRoomSosUpdated menghantar isyarat penamatan ke semua peranti ahli, siren dimatikan, dan perkhidmatan kembali ke mod siap sedia.", bold_prefix="Langkah 9.0 - Penyelesaian & Penamatan Kes: ")

    gdr.add_heading_2(doc, "4.2 Matriks Entiti, Input, Proses dan Output")
    t3_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t3_rows = [
        ["1.0-2.0 Countdown", "SosBottomSheetController", "Tekanan butang SOS / Leretan gelongsor", "CountDownTimer 3s & pengesanan progress >= 95%", "SOS disahkan atau dibatalkan"],
        ["3.0 Penggera Fizikal", "SosAudioManager & VibrateManager", "Pencetus SOS sah", "Mainkan audio siren, strobe kamera, getaran", "Amaran audio-visual di lokasi fizikal"],
        ["4.0 Lokasi & Bilik", "FusedLocationClient", "Sensor GPS & pangkalan data /userRooms", "Ekstrak koordinat (Lat, Lng) & bateri (%)", "Data telemetri kecemasan siap dibungkus"],
        ["5.0 Siaran RTDB", "FirebaseRoomClient", "Payload kecemasan (UID, GPS, devId)", "Tulis ke /rooms/{code}/sosAlerts atau DIRECT", "Rekod amaran aktif wujud dalam awan"],
        ["6.0 Cloud Functions", "onRoomSosCreated", "Event onCreate di RTDB", "Kumpul FCM Tokens & siar notifikasi FCM", "Notifikasi keutamaan tinggi dihantar"],
        ["7.1 Responden", "SosAlarmActivity", "Notifikasi FCM jenis SOS_ALERT", "Paparan penuh skrin atas kunci, bunyi amaran", "Responden sedar insiden, butang peta aktif"],
        ["7.2 & 8.0 Tempahan", "Admin Portal & SosProgressActivity", "Tindakan admin (served = true)", "Kemas kini progressStep 1 hingga 4", "Mangsa lihat status bantuan secara langsung"],
        ["9.0 Penamatan", "onRoomSosUpdated", "Tindakan selesaikan (status: resolved)", "Siaran SOS_CANCELLED, henti siren & servis", "Insiden ditutup secara rasmi"]
    ]
    gdr.create_styled_table(doc, t3_headers, t3_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.sos.SosBottomSheetController, com.example.resqtap.sos.SosAlarmActivity, "
        "com.example.resqtap.sos.SosAudioManager, com.example.resqtap.sos.VibrateManager, com.example.resqtap.sos.SosProgressActivity, "
        "com.example.resqtap.room.FirebaseRoomClient, firebase-functions/index.js (onRoomSosCreated, onRoomSosUpdated).", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 4: CARTA ALIR BILIK KECEMASAN & PENJEJAKAN GPS MASA NYATA
    # =========================================================================
    gdr.add_heading_1(doc, "5. BAHAGIAN 4: CARTA ALIR BILIK KECEMASAN (ROOM HUB) & PENJEJAKAN GPS MASA NYATA")
    
    gdr.add_body_p(doc, 
        "Modul Bilik Kecemasan (Emergency Room Hub) membolehkan pengguna membentuk bulatan keselamatan dipercayai (trusted safety circles) "
        "bersama ahli keluarga, rakan, atau pasukan penyelamat. Modul ini menyediakan perkongsian lokasi geogeospatial langsung secara berterusan "
        "melalui perkhidmatan latar depan (Foreground Service) dan paparan peta interaktif Google Maps.")

    # Embed Flowchart Image 4
    fc4_path = os.path.join(DOCS_DIR, "flowchart_4_emergency_room_map.png")
    if os.path.exists(fc4_path):
        doc.add_picture(fc4_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 5.1: Carta Alir Pengurusan Bilik Kecemasan, Penjejakan Lokasi GPS dan Google Maps")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "5.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Pengguna membuka RoomListActivity. Skrin memaparkan semua bilik kecemasan yang telah disertai berserta butang untuk mencipta bilik baharu atau menyertai bilik sedia ada.", bold_prefix="Langkah 1.0 - Akses Senarai Bilik: ")
    gdr.add_bullet_p(doc, "Jika memilih cipta bilik, pengguna memasukkan nama bilik. Sistem menjana Kod Bilik 6-aksara unik (contoh: RT9482) menggunakan RoomCodeUtils, mendaftarkannya di /rooms/{code}, dan menetapkan pencipta sebagai pemilik bilik (owner).", bold_prefix="Langkah 2.1 - Penciptaan Bilik Baharu: ")
    gdr.add_bullet_p(doc, "Jika memilih sertai bilik, pengguna boleh memasukkan kod 6-aksara secara manual atau mengimbas Kod QR bilik menggunakan ScanQrActivity. Kod disahkan di Firebase RTDB. Jika wujud, UID pengguna dimasukkan ke /rooms/{code}/members/{uid} dan pautan disimpan ke /userRooms/{uid}/{code}.", bold_prefix="Langkah 2.2 - Penyertaan Bilik (Kod / Kod QR): ")
    gdr.add_bullet_p(doc, "Pengguna memasuki RoomHubActivity. Skrin memaparkan avatar semua ahli bilik, status dalam talian (Online / Offline), peratusan bateri peranti setiap ahli, serta waktu kemas kini lokasi terkini.", bold_prefix="Langkah 3.0 - Papan Pemuka Bilik (RoomHubActivity): ")
    gdr.add_bullet_p(doc, "Pengguna boleh menekan butang 'Send Bell' pada profil mana-mana ahli. Ini menulis rekod di /rooms/{code}/bells/{toUid}, memanggil Cloud Function onBellCreated, dan menghantar notifikasi FCM getaran segera kepada ahli tersebut.", bold_prefix="Langkah 4.1 - Isyarat Bell / Ping Segera: ")
    gdr.add_bullet_p(doc, "Pengguna boleh membuka saluran perbualan selamat khusus ahli bilik untuk menghantar mesej teks dan foto bukti kejadian secara langsung melalui RoomChatActivity.", bold_prefix="Langkah 4.2 - Sembang Bilik (Room Chat): ")
    gdr.add_bullet_p(doc, "Apabila RoomMapActivity dibuka, sistem memulakan LiveRoomTrackingService (Foreground Service jenis location|dataSync). Servis ini mengekalkan WakeLock separa dan mengimbas FusedLocationProviderClient secara berkala untuk memuat naik Latitud, Longitud, kelajuan, ketepatan, dan tahap bateri ke nod bilik di RTDB.", bold_prefix="Langkah 5.0 - Servis Penjejakan Latar Belakang: ")
    gdr.add_bullet_p(doc, "Google Maps SDK memaparkan penanda tersuai (Custom Avatar Marker) bagi setiap ahli bilik. Lokasi dikemas kini secara langsung mengikut pergerakan fizikal ahli. Apabila penanda ditekan, kad maklumat ahli dipaparkan berserta jarak relatif (km) dan butang pintasan navigasi.", bold_prefix="Langkah 6.0 - Paparan Interaktif Google Maps: ")

    gdr.add_heading_2(doc, "5.2 Matriks Entiti, Input, Proses dan Output")
    t4_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t4_rows = [
        ["1.0 Senarai Bilik", "RoomListActivity", "Query nod /userRooms/{uid}", "Ambil senarai bilik aktif dari pangkalan data", "Senarai bilik dipaparkan dalam RecyclerView"],
        ["2.1 Cipta Bilik", "RoomCodeUtils & RTDB", "Nama bilik & UID pencipta", "Jana kod rawak 6-aksara, cipta nod di /rooms/{code}", "Bilik baharu didaftarkan dengan kod unik"],
        ["2.2 Sertai Bilik", "ScanQrActivity", "Imbasan kamera QR / Kod 6-aksara", "Semak kewujudan bilik di RTDB & tambah ahli", "Pengguna berjaya didaftarkan sebagai ahli bilik"],
        ["3.0 Hub Bilik", "RoomHubActivity", "Data ahli dari /rooms/{code}/members", "Pantau status online & bateri secara real-time", "Senarai ahli bilik interaktif dipaparkan"],
        ["4.1 Bell Ping", "onBellCreated Cloud Function", "Klik butang 'Bell' pada ahli sasaran", "Pemicu FCM notifikasi jenis bell dengan getaran", "Penerima menerima isyarat ping getaran segera"],
        ["4.2 Sembang", "RoomChatActivity", "Mesej teks / foto kamera", "Tulis ke /rooms/{code}/messages", "Perbualan teks disegerakkan serta-merta"],
        ["5.0 Servis GPS", "LiveRoomTrackingService", "Sensor GPS peranti (LocationRequest)", "Kemas kini Lat/Lng/Bateri ke RTDB berterusan", "Data telemetri masa nyata dimuat naik ke awan"],
        ["6.0 Google Maps", "RoomMapActivity", "Koordinat semua ahli bilik", "Lukis avatar markers & pantau kedudukan", "Peta penjejakan langsung dipaparkan"]
    ]
    gdr.create_styled_table(doc, t4_headers, t4_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.room.RoomListActivity, com.example.resqtap.room.RoomHubActivity, "
        "com.example.resqtap.room.RoomMapActivity, com.example.resqtap.room.LiveRoomTrackingService, "
        "com.example.resqtap.room.RoomChatActivity, com.example.resqtap.room.FirebaseRoomClient, "
        "com.example.resqtap.friend.ScanQrActivity, com.example.resqtap.friend.QrCodeUtils.", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 5: CARTA ALIR PANGGILAN WEBRTC
    # =========================================================================
    gdr.add_heading_1(doc, "6. BAHAGIAN 5: CARTA ALIR PANGGILAN KECEMASAN SUARA & VIDEO WEBRTC")
    
    gdr.add_body_p(doc, 
        "Sistem ResQTap mengintegrasikan teknologi WebRTC (Web Real-Time Communication) untuk membolehkan komunikasi suara dan video "
        "dua hala berkependaman amat rendah (ultra low-latency) antara mangsa, ahli bilik kecemasan, dan responder. Firebase Realtime "
        "Database bertindak sebagai pelayan pensuisan (signaling server) bagi pertukaran Session Description Protocol (SDP) dan calon ICE.")

    # Embed Flowchart Image 5
    fc5_path = os.path.join(DOCS_DIR, "flowchart_5_webrtc_call.png")
    if os.path.exists(fc5_path):
        doc.add_picture(fc5_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 6.1: Carta Alir Protokol Pensuisan dan Sambungan P2P Panggilan Suara & Video WebRTC")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "6.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Pemanggil memulakan panggilan dari SosProgressActivity, RoomHubActivity, atau ChatActivity dengan memilih mod Panggilan Suara (Voice Call) atau Panggilan Video (Video Call). Sistem menjana ID panggilan unik dan mencipta nod /calls/{callId} dengan status 'ringing'.", bold_prefix="Langkah 1.0 - Pemulaan Panggilan (Caller): ")
    gdr.add_bullet_p(doc, "CallService dimulakan sebagai perkhidmatan latar depan (Foreground Service jenis microphone|camera). WebRTCClient menginisialisasi PeerConnectionFactory dan menjana SDP Offer tempatan. Tawaran ini ditulis ke /calls/{callId}/offer.", bold_prefix="Langkah 2.0 - Inisialisasi Signaling & SDP Offer: ")
    gdr.add_bullet_p(doc, "Peranti penerima mengesan panggilan masuk melalui notifikasi tolak FCM atau ValueEventListener di RTDB. IncomingCallManager melancarkan IncomingCallActivity di atas skrin kunci (Show on Lock Screen) dengan nada dering kecemasan dan getaran berterusan.", bold_prefix="Langkah 3.0 - Pengesanan Panggilan Masuk (Receiver): ")
    gdr.add_bullet_p(doc, "Penerima mempunyai 3 pilihan tindak balas: (1) Menekan butang 'Tolak' (Decline) yang mengemas kini status ke 'declined', (2) Membiarkan panggilan tanpa jawapan selama 30 saat yang mengaktifkan status 'timeout', atau (3) Menekan butang 'Terima' (Accept) yang mengemas kini status ke 'connected'.", bold_prefix="Langkah 4.0 - Keputusan Penerima Panggilan: ")
    gdr.add_bullet_p(doc, "Apabila panggilan diterima, penerima menjana SDP Answer dan menulisnya ke /calls/{callId}/answer. Seterusnya, kedua-dua peranti menjana calon ICE (Interactive Connectivity Establishment) dan bertukar maklumat rangkaian melalui nod /calls/{callId}/iceCandidates. Sambungan langsung Peer-to-Peer (P2P) berjaya ditubuhkan.", bold_prefix="Langkah 5.0 - Pertukaran Isyarat & Handshake P2P: ")
    gdr.add_bullet_p(doc, "Aliran audio dan video masa nyata mengalir secara langsung antara kedua-dua peranti tanpa melalui pelayan perantaraan (Direct Media Streaming). Pengguna boleh mengawal fungsi Bisu Mikrofon (Mute), Pembesar Suara (Speakerphone On/Off), dan Menukar Kamera Depan/Belakang.", bold_prefix="Langkah 6.0 - Sesi Panggilan Aktif Berjalan: ")
    gdr.add_bullet_p(doc, "Apabila mana-mana pihak menekan butang 'End Call', status di RTDB dikemas kini kepada 'ended'. CallService dihentikan, kamera dan mikrofon dilepaskan, sambungan PeerConnection ditutup secara teratur, dan rekod panggilan disimpan.", bold_prefix="Langkah 7.0 - Penamatan Panggilan & Pembersihan Sumber: ")

    gdr.add_heading_2(doc, "6.2 Matriks Entiti, Input, Proses dan Output")
    t5_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t5_rows = [
        ["1.0 Pemulaan", "CallSignalingClient", "Pilihan mod panggilan (audio/video)", "Cipta rekod /calls/{callId} status 'ringing'", "Nod panggilan aktif didaftarkan"],
        ["2.0 SDP Offer", "WebRTCClient & CallService", "Stream audio/video tempatan", "Jana SDP Offer & muat naik ke RTDB", "Tawaran sambungan sedia untuk penerima"],
        ["3.0 Panggilan Masuk", "IncomingCallActivity", "Event FCM / Listener RTDB", "Papar skrin panggilan masuk atas lockscreen", "Nada dering kecemasan & getaran aktif"],
        ["4.0 Jawapan", "IncomingCallManager", "Tekanan butang Terima / Tolak", "Kemas kini status panggilan (connected/declined)", "Aliran dialihkan ke sesi panggilan atau ditutup"],
        ["5.0 Handshake", "WebRTC PeerConnection", "SDP Answer & Calon ICE", "P2P Hole Punching & pertukaran calon laluan", "Saluran media P2P berkependaman rendah terbina"],
        ["6.0 Sesi Aktif", "VoiceCall / VideoCallActivity", "Input mikrofon & kamera masa nyata", "Penstriman audio/video dua hala & kawalan UI", "Komunikasi kecemasan langsung berjalan"],
        ["7.0 Penamatan", "CallService & WebRTCClient", "Tekanan butang 'Tamatkan Panggilan'", "Tutup PeerConnection, henti servis foreground", "Sumber perkakasan peranti dilepaskan"]
    ]
    gdr.create_styled_table(doc, t5_headers, t5_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.call.CallSignalingClient, com.example.resqtap.call.WebRTCClient, "
        "com.example.resqtap.call.CallService, com.example.resqtap.call.IncomingCallActivity, "
        "com.example.resqtap.call.IncomingCallManager, com.example.resqtap.call.VoiceCallActivity, "
        "com.example.resqtap.call.VideoCallActivity.", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 6: CARTA ALIR PELAPORAN INSIDEN & CARIAN HOSPITAL
    # =========================================================================
    gdr.add_heading_1(doc, "7. BAHAGIAN 6: CARTA ALIR PELAPORAN INSIDEN & CARIAN HOSPITAL TERDEKAT")
    
    gdr.add_body_p(doc, 
        "Modul ini merangkumi dua perkhidmatan komuniti penting: (1) Pelaporan Insiden Komuniti (Community Incident Reporting) "
        "yang membolehkan orang awam menghantar maklumat kecemasan bergambar berserta geolokasi tepat kepada pihak berkuasa, dan "
        "(2) Carian Hospital Terdekat (Nearby Hospital Finder) yang mengira jarak kemudahan kesihatan berhampiran dan menyediakan navigasi pantas.")

    # Embed Flowchart Image 6
    fc6_path = os.path.join(DOCS_DIR, "flowchart_6_report_hospital.png")
    if os.path.exists(fc6_path):
        doc.add_picture(fc6_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 7.1: Carta Alir Pelaporan Insiden Komuniti dan Carian Fasiliti Hospital Terdekat")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "7.1 Penerangan Aliran Pelaporan Insiden (ReportActivity)")
    gdr.add_bullet_p(doc, "Pengguna membuka ReportActivity. Borang aduan digital dipaparkan berserta semakan keizinan Kamera dan Lokasi.", bold_prefix="Langkah L1.0 - Pembukaan Borang Aduan: ")
    gdr.add_bullet_p(doc, "Pengguna memilih satu kategori insiden daripada senarai pratakrif: Kemalangan Jalan Raya, Jenayah / Keganasan, Kecemasan Perubatan, Kebakaran, Bencana Alam, atau Lain-lain.", bold_prefix="Langkah L2.0 - Pemilihan Kategori Insiden: ")
    gdr.add_bullet_p(doc, "Pengguna memasukkan keterangan bertulis mengenai insiden. Sistem memperoleh koordinat GPS semasa peranti secara automatik untuk disematkan sebagai lokasi kejadian tepat.", bold_prefix="Langkah L3.0 - Keterangan Butiran & Koordinat GPS: ")
    gdr.add_bullet_p(doc, "Pengguna boleh melampirkan foto bukti insiden menggunakan kamera peranti atau memilih daripada galeri melalui FileProvider. Foto dimampatkan dan dimuat naik ke Firebase Cloud Storage di bawah laluan /incident_reports/{reportId}.jpg, lalu pautan muat turun selamat (Download URL) diperoleh.", bold_prefix="Langkah L4.0 - Pemprosesan Lampiran Foto: ")
    gdr.add_bullet_p(doc, "Data laporan lengkap yang mengandungi UID pengirim, cap masa, kategori, keterangan, koordinat GPS, dan pautan foto disimpan ke nod /reports/{reportId} dalam Firebase RTDB. Notifikasi dihantar ke konsol pentadbir dan pengguna menerima nombor rujukan tiket aduan.", bold_prefix="Langkah L5.0 & L6.0 - Penyimpanan Rekod & Penjanaan Tiket: ")

    gdr.add_heading_2(doc, "7.2 Penerangan Aliran Carian Hospital Terdekat (NearbyHospitalActivity)")
    gdr.add_bullet_p(doc, "Pengguna membuka NearbyHospitalActivity. Sistem menyemak status perkhidmatan GPS peranti dan memanggil FusedLocationProviderClient untuk mendapatkan koordinat Latitud dan Longitud pengguna terkini.", bold_prefix="Langkah H1.0 & H2.0 - Pengimbasan GPS Pengguna: ")
    gdr.add_bullet_p(doc, "Sistem menghantar kueri geospatial ke API Kemudahan Kesihatan (Overpass API / Google Places API) untuk mengekstrak senarai hospital, pusat rawatan kecemasan, dan klinik dalam radius perkhidmatan.", bold_prefix="Langkah H3.0 - Kueri API Kemudahan Perubatan: ")
    gdr.add_bullet_p(doc, "Sistem melaksanakan algoritma pengiraan jarak geografi (Formula Haversine) bagi mengira jarak sebenar antara koordinat pengguna dan setiap kemudahan kesihatan. Senarai disusun secara automatik mengikut turutan jarak terdekat.", bold_prefix="Langkah H4.0 - Pengiraan Jarak & Isihan Terdekat: ")
    gdr.add_bullet_p(doc, "Senarai hospital dipaparkan dengan maklumat lengkap: nama fasiliti, alamat, jarak dalam kilometer (km), status waktu operasi, dan nombor talian kecemasan.", bold_prefix="Langkah H5.0 - Paparan Senarai Hospital: ")
    gdr.add_bullet_p(doc, "Pengguna boleh memilih antara dua tindakan utama: (1) Menekan butang 'Panggilan' untuk melancarkan Android Dialer terus ke nombor kecemasan hospital, atau (2) Menekan butang 'Navigasi' untuk melancarkan Google Maps dengan laluan belokan-demi-belokan (turn-by-turn navigation).", bold_prefix="Langkah H6.0 - Panggilan Terus atau Navigasi Google Maps: ")

    gdr.add_heading_2(doc, "7.3 Matriks Entiti, Input, Proses dan Output")
    t6_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t6_rows = [
        ["L1.0-L3.0 Borang", "ReportActivity", "Kategori, teks keterangan, GPS", "Validasi kelengkapan borang & perolehan lokasi", "Data laporan lengkap sedia untuk dihantar"],
        ["L4.0 Storan Imej", "Firebase Cloud Storage", "Fail gambar kamera / galeri", "Mampatan imej & muat naik ke storan awan", "Pautan muat turun selamat (Download URL)"],
        ["L5.0-L6.0 Tiket", "Firebase RTDB (/reports)", "Objek JSON laporan lengkap", "Penyimpanan rekod & notifikasi pentadbir", "Tiket aduan dijana dan disahkan diterima"],
        ["H1.0-H2.0 Lokasi", "FusedLocationProviderClient", "Isyarat satelit GPS / rangkaian", "Ekstrak koordinat (Lat, Lng) terkini pengguna", "Koordinat geolokasi tepat sedia dikueri"],
        ["H3.0-H4.0 Kueri", "Overpass / Places API", "Radius carian & koordinat pengguna", "Pengiraan formula Haversine & susunan jarak", "Senarai fasiliti disusun bermula jarak terdekat"],
        ["H5.0-H6.0 Tindakan", "Dialer & Google Maps", "Pilihan kad hospital oleh pengguna", "Lancarkan ACTION_DIAL atau ACTION_VIEW geo-URI", "Panggilan kecemasan hospital atau navigasi peta"]
    ]
    gdr.create_styled_table(doc, t6_headers, t6_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: com.example.resqtap.report.ReportActivity, com.example.resqtap.report.SupportActivity, "
        "com.example.resqtap.map.NearbyHospitalActivity, com.example.resqtap.map.GpsUtils, "
        "com.example.resqtap.news.MedicalNewsActivity, com.example.resqtap.news.MedicalNewsFetcher.", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 7: CARTA ALIR PORTAL PENTADBIR & PENYIARAN NOTIFIKASI AWAN
    # =========================================================================
    gdr.add_heading_1(doc, "8. BAHAGIAN 7: CARTA ALIR PORTAL PENTADBIR & PENYIARAN NOTIFIKASI AWAN")
    
    gdr.add_body_p(doc, 
        "Portal Web Pentadbir ResQTap (Command & Control Web Dashboard) bertindak sebagai pusat pemantauan bersepadu bagi pihak pengurusan "
        "dan penyelamat kecemasan. Portal ini dihoskan melalui pelayan Node.js (server.js) dan menyokong pemantauan insiden masa nyata, "
        "pengendalian garis masa bantuan, penyiaran notifikasi awan global mahupun bersasar, serta kawalan keselamatan pangkalan data.")

    # Embed Flowchart Image 7
    fc7_path = os.path.join(DOCS_DIR, "flowchart_7_admin_portal_cloud.png")
    if os.path.exists(fc7_path):
        doc.add_picture(fc7_path, width=Inches(6.25))
        p_cap = doc.add_paragraph()
        gdr.format_paragraph(p_cap, space_before=4, space_after=12)
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        r_cap = p_cap.add_run("Rajah 8.1: Carta Alir Pengesahan Pentadbir, Pengendalian Kes SOS dan Penyiaran Notifikasi Awan")
        r_cap.font.name = gdr.FONT_FAMILY
        r_cap.font.size = Pt(9)
        r_cap.font.italic = True
        r_cap.font.color.rgb = gdr.COLOR_MUTED

    gdr.add_heading_2(doc, "8.1 Penerangan Terperinci Langkah Logik")
    gdr.add_bullet_p(doc, "Pentadbir mengakses konsol web melalui pelayar internet (http://localhost:5000/admin atau domain web rasmi). Sistem memaparkan antara muka log masuk selamat berasaskan Firebase Authentication.", bold_prefix="Langkah 1.0 - Akses Portal & Log Masuk: ")
    gdr.add_bullet_p(doc, "Setelah kredensial disahkan oleh Firebase Auth, sistem menyemak peranan pentadbir. Akaun dengan domain rasmi @resqtap.com diberikan peranan pentadbir secara automatik melalui Cloud Function onUserCreated. Sistem juga menyemak nod /admins/{uid} dalam RTDB. Sekiranya tidak sah, ralat 403 Forbidden dipaparkan dan akses dinafikan.", bold_prefix="Langkah 2.0 & 3.0 - Pengesahan Hak Akses Pentadbir: ")
    gdr.add_bullet_p(doc, "Papan pemuka utama (admin.html) memaparkan statistik langsung: bilangan pengguna berdaftar, jumlah bilik kecemasan aktif, bilangan amaran SOS semasa, dan status kesihatan pelayan.", bold_prefix="Langkah 4.0 - Papan Pemuka Pemantauan: ")
    gdr.add_bullet_p(doc, "Konsol mendengar perubahan pada nod /rooms/*/sosAlerts secara langsung. Apabila amaran kecemasan masuk, amaran audio dan visual berkelip merah dipaparkan. Pentadbir boleh menekan butang 'Reserve Kes' (menetapkan served=true dan servedBy=adminUid), lalu mengemas kini langkah kemajuan bantuan (1. Dispatched -> 2. En Route -> 3. On Scene -> 4. Resolved) serta berkomunikasi terus dengan mangsa melalui sembang langsung.", bold_prefix="Langkah 4.1 - Pengendalian Kes SOS Langsung: ")
    gdr.add_bullet_p(doc, "Pentadbir boleh menyiarkan amaran keselamatan rasmi ke peranti pengguna. Pentadbir memilih sasaran audiens ('all' untuk semua pengguna berdaftar, 'live' untuk pengguna yang berada dalam bilik aktif 2 minit terkini, atau 'admins' untuk staf penyelamat), memasukkan tajuk serta mesej, dan menekan butang hantar. Callable Cloud Function sendAdminNotification dipanggil.", bold_prefix="Langkah 4.2 - Penyiaran Notifikasi Pentadbir: ")
    gdr.add_bullet_p(doc, "Fungsi awan mengumpulkan semua token FCM pengguna sasaran, menyiarkan push notification serentak, dan merekodkan statistik penghantaran (bilangan berjaya, bilangan gagal, cap masa) ke nod /admin_notifications/{historyId}.", bold_prefix="Langkah 4.2b - Enjin Penyiaran Awan (FCM Blaster): ")
    gdr.add_bullet_p(doc, "Pentadbir mempunyai kawalan keselamatan penuh untuk memadam akaun pengguna berniat jahat melalui pemicu /admin_user_deletions/{uid} yang memadam rekod Auth dan RTDB secara menyeluruh (onAdminUserDeletionRequest), atau melaksanakan tetapan semula pangkalan data melalui API pembersihan /api/admin/clear-database.", bold_prefix="Langkah 4.3 - Pengurusan Pengguna & Keselamatan Data: ")

    gdr.add_heading_2(doc, "8.2 Matriks Entiti, Input, Proses dan Output")
    t7_headers = ["Langkah Logik", "Entiti / Komponen", "Input Data", "Operasi Pemprosesan", "Output / Hasil"]
    t7_rows = [
        ["1.0-2.0 Akses", "server.js & admin.html", "E-mel & kata laluan pentadbir", "Pengesahan Firebase Auth & semak domain @resqtap.com", "Akses pentadbir diluluskan atau dinafikan (403)"],
        ["3.0 Pemantauan", "Admin Dashboard (app.js)", "Snapshot RTDB /users, /rooms, /sosAlerts", "Kira metrik masa nyata & paparan kad statistik", "Visualisasi status sistem keseluruhan dipaparkan"],
        ["4.1 Tindak Balas SOS", "Firebase RTDB & Console", "Klik 'Reserve Kes' & kemas kini status", "Tulis served=true & kemas kini progressStep (1-4)", "Status bantuan disegerakkan terus ke telefon mangsa"],
        ["4.2 Penyiaran Amaran", "sendAdminNotification", "Sasaran (all/live/admins), tajuk & mesej", "Callable Cloud Function ekstrak FCM tokens & siar", "Notifikasi tolak rasmi diterima pada telefon sasaran"],
        ["4.3 Pembersihan", "onAdminUserDeletionRequest", "Permintaan pemadaman UID bermasalah", "Padam akaun daripada Firebase Auth & RTDB purge", "Akaun dan data dipadam sepenuhnya daripada sistem"],
        ["5.0 Status Sistem", "Database Maintenance API", "Permintaan audit / reset pangkalan data", "cleanup_rtdb.js melaksanakan penyelarasan data", "Pangkalan data kekal optimum, bersih dan selamat"]
    ]
    gdr.create_styled_table(doc, t7_headers, t7_rows, col_widths=[1.1, 1.3, 1.2, 1.4, 1.2])

    gdr.add_body_p(doc, 
        "• Fail Kod Sumber Terlibat: ResQTap-Website/server.js, ResQTap-Website/public/admin.html, "
        "ResQTap-Website/public/app.js, cleanup_rtdb.js, firebase-functions/index.js "
        "(sendAdminNotification, onUserCreated, onAdminUserDeletionRequest, cleanupExpiredAiChats).", 
        bold_prefix="Rujukan Kod Sumber: ")

    doc.add_page_break()

    # =========================================================================
    # BAHAGIAN 8: KESIMPULAN & JADUAL RUMUSAN SENI BINA
    # =========================================================================
    gdr.add_heading_1(doc, "9. KESIMPULAN & JADUAL RUMUSAN SENI BINA SISTEM")
    
    gdr.add_body_p(doc, 
        "Dokumentasi carta alir ini membuktikan bahawa seni bina perisian ResQTap (OneTapSOS) direka bentuk dengan tahap kebolehpercayaan, "
        "kebolehskalaan, dan keselamatan yang tinggi. Penggunaan Firebase Realtime Database digabungkan bersama Cloud Functions "
        "menyediakan asas penyegerakan data yang amat pantas dan bertoleransi terhadap ralat rangkaian. Protokol khas seperti Duress PIN "
        "dan sokongan komunikasi suara/video WebRTC meletakkan sistem ini sebagai sebuah penyelesaian tindak balas kecemasan gred profesional.")

    sum_headers = ["Bahagian Carta Alir", "Fokus Utama Modul", "Komponen Utama Terlibat", "Ciri Keselamatan / Prestasi Khas"]
    sum_rows = [
        ["Bahagian 1: Keseluruhan Aliran", "Kitaran hayat aplikasi & navigasi", "SplashActivity, MainActivity", "Semakan sesi automatik & sandaran luar talian"],
        ["Bahagian 2: Keselamatan & Auth", "Autentikasi, PIN & Biometrik", "AppLockManager, BiometricPrompt", "Silent SOS via Duress PIN & Anti-brute force cooldown"],
        ["Bahagian 3: OneTap SOS", "Pencetus amaran & garis masa bantuan", "SosBottomSheet, Cloud Functions, FCM", "Kiraan detik 3s, penggera fizikal & tempahan admin"],
        ["Bahagian 4: Bilik & Penjejakan", "Bilik kecemasan & pemetaan langsung", "LiveRoomTrackingService, Google Maps", "Foreground Service, perkongsian QR, amaran Bell ping"],
        ["Bahagian 5: Panggilan WebRTC", "Interkom suara dan video P2P", "CallSignalingClient, WebRTCClient", "Kependaman ultra rendah, P2P mesh & integrasi lockscreen"],
        ["Bahagian 6: Laporan & Hospital", "Pelaporan insiden & bantuan perubatan", "ReportActivity, FusedLocationClient", "Storan imej awan, formula Haversine & navigasi terus"],
        ["Bahagian 7: Portal Pentadbir", "Pusat kawalan & penyiaran awan", "server.js, admin.html, FCM Blaster", "Pengesahan domain @resqtap.com & penghapusan pengguna selamat"]
    ]
    gdr.create_styled_table(doc, sum_headers, sum_rows, col_widths=[1.4, 1.6, 1.6, 1.6])

    gdr.add_callout(doc, 
        "Dokumentasi carta alir ini telah disahkan sepadan sepenuhnya dengan kod sumber sebenar di dalam repositori ResQTap. "
        "Semua gambar rajah carta alir beresolusi tinggi dijana secara automatik dan tersemat secara kekal di dalam fail Word ini.",
        title="PENGESAHAN DOKUMEN", border_color="0D9488", bg_color="F0FDFA")

    # Save document
    doc.save(OUTPUT_DOCX)
    print(f"Dokumen berjaya dibina dan disimpan di: {OUTPUT_DOCX}")
    print(f"Saiz fail dokumen: {os.path.getsize(OUTPUT_DOCX) / 1024:.1f} KB")

if __name__ == "__main__":
    build_document()
