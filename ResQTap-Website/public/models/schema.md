# ResQTap Data Models & Schema Reference

Dokumentasi model data bagi aplikasi web dan sistem backend ResQTap (Firebase Realtime Database & Supabase).

---

## 1. User Model (`users/{uid}`)
Menyimpan profil pengguna, status kehadiran (presence), dan tetapan kecemasan:
* `uid` *(string)*: Pengenal unik Firebase Auth
* `name` *(string)*: Nama penuh pengguna
* `email` *(string)*: Emel pengguna
* `phone` *(string)*: Nombor telefon kecemasan
* `role` *(string)*: `"user"` | `"admin"`
* `isOnline` *(boolean)*: Status aktif semasa
* `lastActive` *(number)*: Timestamp milisaat aktiviti terakhir
* `batteryLevel` *(number)*: Tahap bateri peranti pengguna (0-100)
* `latitude` / `longitude` *(number)*: Koordinat GPS semasa jika perkongsian aktif

---

## 2. SOS Alert Model (`sos_alerts/{alertId}` & `active_sos/{uid}`)
Menyimpan sesi amaran kecemasan masa nyata:
* `id` *(string)*: ID sesi SOS
* `senderUid` *(string)*: UID mangsa yang mengaktifkan SOS
* `senderName` *(string)*: Nama mangsa
* `senderPhone` *(string)*: Nombor telefon mangsa
* `status` *(string)*: `"active"` | `"cancelled"` | `"resolved"`
* `createdAt` *(number)*: Waktu pengaktifan SOS
* `resolvedAt` *(number | null)*: Waktu kes diselesaikan
* `latitude` / `longitude` *(number)*: Lokasi mangsa semasa pengaktifan
* `locationName` *(string)*: Alamat atau penerangan tempat kejadian
* `roomId` *(string)*: Bilik kecemasan yang disambungkan
* `claimedBy` *(string | null)*: Admin yang mengendalikan kes

---

## 3. Incident Report Model (`incident_reports/{reportId}`)
Laporan insiden yang dihantar oleh pengguna atau orang awam:
* `id` *(string)*: ID laporan insiden
* `reporterUid` *(string)*: UID pengguna pengirim
* `title` *(string)*: Tajuk insiden
* `description` *(string)*: Butiran penuh kejadian
* `category` *(string)*: Jenis insiden (Kecemasan, Perubatan, Jenayah, Kebakaran, Kemalangan)
* `status` *(string)*: `"new"` | `"reviewing"` | `"resolved"` | `"rejected"`
* `createdAt` *(number)*: Tarikh & masa laporan dibuat
* `location` *(object)*: `{ latitude, longitude, address }`
* `mediaUrls` *(array)*: Senarai pautan gambar/video bukti
* `adminNotes` *(string)*: Catatan tindakan pegawai penyiasat

---

## 4. Support & Livechat Models
### Support Chat (`support_chats/{chatId}`)
* `threadId` *(string)*: ID perbualan
* `userUid` *(string)*: UID pengguna
* `status` *(string)*: `"open"` | `"claimed"` | `"resolved"`
* `messages` *(object)*: Kunci mesej berantai:
  * `sender`: `"user"` | `"admin"`
  * `text`: Kandungan teks
  * `timestamp`: Masa dihantar
  * `attachment`: Fail lampiran (gambar, dokumen, audio)

### AI Katup Chat (`ai_chats/{chatId}`)
* Log perbualan interaktif bersama bot kecemasan Katup
* Tamat tempoh automatik selepas 5 minit tidak aktif

---

## 5. Broadcast & Notification Model (`admin_notifications/{noticeId}`)
Notifikasi siaran amaran yang dihantar oleh admin ke seluruh pengguna aplikasi:
* `id` *(string)*: ID notifikasi
* `title` *(string)*: Tajuk amaran
* `message` *(string)*: Mesej amaran penuh
* `category` *(string)*: `"all"` | `"calls"` | `"system"` | `"notice"` | `"sos"` | `"bell"`
* `audience` *(string)*: `"all"` atau bilik tertentu
* `createdAt` *(number)*: Masa amaran disiarkan
* `createdBy` *(string)*: UID admin yang menghantar amaran

---

## 6. Admin Task Queue Model (`admin_tasks/` & `admin_user_deletions/`)
Antrian arahan kerja untuk pelayan latar belakang (Server Watcher):
* `clear_database`: `{ status: 'pending'|'processing'|'completed'|'failed', requestedAt: timestamp }`
* `admin_user_deletions/{uid}`: `{ email, status: 'pending'|'processing'|'completed', requestedAt: timestamp }`
