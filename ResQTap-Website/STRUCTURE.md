# 📐 ResQTap Website - Struktur Senibina MVC & Panduan Kategori Fail

Panduan lengkap organisasi folder dan fail bagi **ResQTap Website & Admin Dashboard**. Sistem ini kini distrukturkan mengikut corak **Model-View-Controller (MVC)** dengan pengkategorian yang jelas bagi memudahkan pencarian dan penyelenggaraan kod.

---

## 📂 Struktur Direktori Keseluruhan

```text
ResQTap-Website/
├── 📄 server.js                       # Entry point pelayan (CJS) - menyambung ke MVC server
├── 📄 auto_deploy_watcher.js          # Pemantau auto-deploy ke Firebase Hosting
├── 📄 package.json                    # Konfigurasi npm scripts & metadata
├── 📄 start_auto_deploy.bat           # Skrip pelancaran pemantau latar depan
├── 📄 start_auto_deploy_background.vbs# Skrip pelancaran pemantau latar belakang
├── 📄 run_watcher_background.vbs      # Skrip pelancaran pelayan latar belakang
├── 📄 STRUCTURE.md                    # Dokumentasi struktur fail ini
│
├── 📁 server/                         # 🟢 [BACKEND MVC]
│   ├── 📄 server.js                   # Pelayan HTTP teras & penghala statik / SPA
│   ├── 📁 controllers/                # [CONTROLLERS - BACKEND]
│   │   └── 📄 adminApiController.js   # Pengendali API Admin (reset-db & delete-user)
│   ├── 📁 services/                   # [SERVICES & WORKERS]
│   │   └── 📄 rtdbWatcherService.js   # Pemantau giliran tugasan Firebase RTDB
│   └── 📁 utils/                      # [UTILITIES]
│       └── 📄 mimeTypes.js            # Pemetaan jenis fail MIME (HTML, CSS, APK, dll.)
│
└── 📁 public/                         # 🔵 [FRONTEND MVC & STATIC HOSTING]
    │
    ├── 📁 views/                      # 👁️ [V - VIEWS] Semua paparan HTML mengikut kategori
    │   ├── 📁 public/                 # Laman web awam (Public pages)
    │   │   ├── 📄 index.html          # Laman utama & portal muat turun
    │   │   ├── 📄 features.html       # Penerangan ciri-ciri ResQTap
    │   │   ├── 📄 flow.html           # Aliran tindak balas kecemasan
    │   │   └── 📄 safety.html         # Panduan & tips keselamatan
    │   └── 📁 admin/                  # Portal pengurusan pentadbir
    │       └── 📄 admin.html          # Dashboard Admin masa nyata
    │
    ├── 📁 models/                     # 📦 [M - MODELS] Definisi data & konfigurasi
    │   ├── 📄 config.js               # Kredensial & konfigurasi Firebase & Supabase
    │   └── 📄 schema.md               # Dokumentasi skema data RTDB & Supabase
    │
    ├── 📁 controllers/                # 🎮 [C - CONTROLLERS] Logik kawalan frontend
    │   ├── 📄 app.js                  # Controller utama & SPA Orchestrator
    │   └── 📄 admin_auth_bridge.js    # Controller jambatan pengesahan Firebase Auth
    │
    ├── 📁 css/                        # 🎨 [STYLES] Lembaran gaya visual
    │   └── 📄 styles.css              # Master stylesheet (UI components, dark mode, animasi)
    │
    ├── 📁 assets/                     # 🖼️ [ASSETS] Media dikelaskan mengikut jenis
    │   ├── 📁 images/                 # Logo, avatar, mockup, tangkapan skrin
    │   │   ├── 🖼️ resqtap_launcher.png
    │   │   ├── 🖼️ admin_3d_avatar.jpg
    │   │   ├── 🖼️ katup.jpg
    │   │   ├── 🖼️ phone_app_screen.png
    │   │   └── 🖼️ highlight_1.jpg ... highlight_4.jpg
    │   ├── 📁 downloads/              # Fail muat turun aplikasi
    │   │   └── 📦 ResQTap.apk
    │   └── 📁 fonts/                  # Fail fon sistem
    │       └── 🔤 sfuitext_regular.otf
    │
    ├── 📄 index.html                  # [Root Mirror] Entry point laman awam
    ├── 📄 admin.html                  # [Root Mirror] Entry point dashboard admin
    ├── 📄 features.html               # [Root Mirror] Entry point ciri-ciri
    ├── 📄 flow.html                   # [Root Mirror] Entry point aliran
    ├── 📄 safety.html                 # [Root Mirror] Entry point keselamatan
    ├── 📄 styles.css                  # [Root Mirror] Stylesheet serasi ke belakang
    ├── 📄 app.js                      # [Root Mirror] Controller serasi ke belakang
    ├── 📄 config.js                   # [Root Mirror] Konfigurasi serasi ke belakang
    ├── 📄 admin_auth_bridge.js        # [Root Mirror] Bridge serasi ke belakang
    └── 📄 serve.json                  # Konfigurasi rewrite pelayan setempat
```

---

## 🔍 Jadual Carian Fail Mengikut Kategori (Quick File Locator)

| Kategori Fail | Lokasi Folder Utama | Contoh Fail | Penerangan |
| :--- | :--- | :--- | :--- |
| **Views - Laman Awam** | `public/views/public/` | `index.html`, `features.html`, `flow.html`, `safety.html` | Antaramuka web untuk pengguna luar dan pelawat portal. |
| **Views - Dashboard Admin** | `public/views/admin/` | `admin.html` | Antaramuka pemantauan SOS, pengurusan bilik, dan live chat admin. |
| **Models & Data** | `public/models/` | `config.js`, `schema.md` | Konfigurasi pangkalan data, kunci API, dan struktur skema data. |
| **Controllers (Frontend)** | `public/controllers/` | `app.js`, `admin_auth_bridge.js` | Logik interaksi UI, routing SPA, pengesahan admin, dan sokongan chat. |
| **Controllers (Backend)** | `server/controllers/` | `adminApiController.js` | Pengendalian laluan API `/api/admin/clear-database` & `/delete-user`. |
| **Services (Backend)** | `server/services/` | `rtdbWatcherService.js` | Pemprosesan tugas latar belakang Realtime Database untuk pemadaman pengguna. |
| **Styles (CSS)** | `public/css/` | `styles.css` | Reka bentuk visual, tema gelap/terang, tipografi, dan susun atur grid. |
| **Imej & Gambar** | `public/assets/images/` | `resqtap_launcher.png`, `katup.jpg`, `phone_app_screen.png` | Semua grafik, ikon aplikasi, dan tangkapan skrin. |
| **Fail Muat Turun** | `public/assets/downloads/` | `ResQTap.apk` | Pakej pemasangan Android (.apk) untuk muat turun pengguna. |
| **Fon Huruf** | `public/assets/fonts/` | `sfuitext_regular.otf` | Fon tipografi sistem ResQTap. |

---

## ⚡ Arahan Pelancaran & Ujian

1. **Jalankan Pelayan Tempatan (Local Development):**
   ```bash
   cd ResQTap-Website
   npm start
   ```
   * Akses Portal Awam: [http://localhost:5000](http://localhost:5000)
   * Akses Admin Dashboard: [http://localhost:5000/admin](http://localhost:5000/admin)

2. **Jalankan Pemantau Auto-Deploy ke Firebase:**
   ```bash
   npm run watch:deploy
   ```
   * Memantau folder `public/` dan mendeploy secara automatik ke Firebase Hosting apabila fail dikemas kini.

3. **Deploy Manual ke Firebase Hosting:**
   ```bash
   npm run deploy
   ```
