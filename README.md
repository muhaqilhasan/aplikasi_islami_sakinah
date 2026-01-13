Sakinah - Aplikasi Asisten Ibadah Harian 🌙

Sakinah adalah aplikasi Android modern yang dirancang untuk membantu umat Muslim dalam menjalankan ibadah harian. Aplikasi ini mengutamakan antarmuka yang bersih (clean UI), pengalaman pengguna yang mulus (smooth UX), dan akurasi data yang tinggi.

📱 Fitur Unggulan

1. 🕌 Jadwal Salat & Adzan Otomatis
Akurasi Tinggi: Menggunakan AlAdhan API (Metode 20 - Kemenag RI).
Real-time: Penanda waktu salat yang sedang berlangsung (Active Highlight).
Background Alarm: Notifikasi dan suara Adzan tetap berjalan otomatis meskipun aplikasi ditutup (menggunakan AlarmManager & BroadcastReceiver).
Audio Custom: Mendukung pemilihan suara Adzan (Makkah, Madinah, Beep).

2. 🧭 Arah Kiblat (Qibla Finder)
Kompas digital presisi menggunakan sensor perangkat (Accelerometer & Magnetometer).
Visualisasi jarum yang halus dengan ikon Ka'bah.
Kalibrasi arah Utara magnetik secara real-time dengan animasi rotasi yang mulus.

3. 📅 Kalender Hijriah & Masehi Pintar
Tampilan kalender ganda (Masehi & Hijriah).
Data Event Otomatis:
Hari Libur Nasional (Sumber: Guangrei API).
Tanggal Hijriah (Sumber: AlAdhan Calendar API).
Penanda Warna:
🔴 Merah: Libur Nasional / Minggu.
🟣 Ungu: Puasa Sunnah (Senin-Kamis, Ayyamul Bidh).
🟡 Emas: Puasa Wajib (Ramadhan).
🟢 Hijau: Hari Besar Islam.
Mendukung mode tampilan angka Arab Timur (١, ٢, ٣) saat mode bahasa Arab aktif.

4. 🤲 Kumpulan Doa Lengkap
Database doa harian yang diambil dari API publik (Open-API Doa).
Fitur Offline: Fallback data internal yang lengkap jika tidak ada internet.
Smart Search: Pencarian cepat berdasarkan judul atau terjemahan.
Filter Kategori: Pengelompokan doa (Pagi Petang, Makanan, Perjalanan, dll) menggunakan Chips.

5. ⚙️ Personalisasi & UI Modern
Tema Gelap & Terang (Dark/Light Mode):
Warna Emerald & Slate yang nyaman di mata.
Splash Screen Animasi: Transisi matahari/bulan yang unik saat mengganti tema.
Multi-Bahasa (i18n): Dukungan penuh untuk Bahasa Indonesia, Inggris (English), dan Arab (العربية).
RTL Support: Tata letak otomatis membalik (Mirroring) saat menggunakan Bahasa Arab.

🛠️ Teknologi & Arsitektur

Aplikasi ini dibangun menggunakan standar pengembangan Android modern:
Bahasa: Kotlin
Minimum SDK: API 33 (Android 13)
Target SDK: API 34+
UI Toolkit: XML Layouts, Material Design 3 Components.
Core Libraries:
View Binding: Pengganti findViewById untuk keamanan tipe data null.
Jetpack Navigation: Single Activity Architecture.
Core Splashscreen: API Splash Screen standar Android 12+.
Concurrency: Executors & Handler untuk networking asinkron ringan.
Networking: java.net.URL (Native) & org.json untuk parsing data JSON.

🔗 Sumber Data (API)

| Fitur           | Sumber API        | Deskripsi                               |
|-----------------|-------------------|-----------------------------------------|
| Jadwal Salat    | AlAdhan API       | Timing Method 20 (Kemenag RI).          |
| Kalender Hijriah| AlAdhan Calendar  | Konversi tanggal Masehi ke Hijriah.     |
| Hari Libur      | Guangrei API      | Database hari libur nasional Indonesia. |
| Data Doa        | Open-API Doa      | Kumpulan doa harian beserta terjemahannya.|

🚀 Panduan Instalasi & Setup

1. Clone Repository
   ```bash
   git clone https://github.com/muhaqilhasan/sakinah-android.git
   ```
2. Persiapan Aset Audio (Penting!)
   Karena file audio berukuran besar biasanya tidak disertakan dalam source code dasar, Anda perlu menambahkannya manual:
      - Siapkan file audio adzan dengan format `.mp3`.
      - Beri nama file: `adzan.mp3`.
      - Buat folder `raw` di direktori: `app/src/main/res/raw/`.
      - Paste file `adzan.mp3` ke dalam folder tersebut.

3. Build & Run
   - Buka proyek di Android Studio.
   - Tunggu proses Gradle Sync selesai.
   - Sambungkan perangkat Android atau jalankan Emulator.
   - Tekan tombol `Run` (Shift+F10).


🤝 Kontribusi

Kontribusi sangat diterima! Jika Anda ingin meningkatkan aplikasi ini:
1. Fork proyek ini.
2. Buat branch fitur baru (`git checkout -b fitur-keren`).
3. Commit perubahan Anda (`git commit -m 'Menambahkan fitur keren'`).
4. Push ke branch (`git push origin fitur-keren`).
5. Buat Pull Request.

📄 Lisensi

Didistribusikan di bawah Lisensi MIT. Lihat `LICENSE` untuk informasi lebih lanjut.

<p align="center">Dibuat dengan ❤️ oleh <b>AkaruDev</b></p>
