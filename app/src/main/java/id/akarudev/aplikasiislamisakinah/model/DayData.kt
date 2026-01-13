package id.akarudev.aplikasiislamisakinah.model

data class DayData(
    val day: Int,                // Tanggal Masehi (1-31)
    val monthIndex: Int,         // Index Bulan (0-11)
    val year: Int,               // Tahun
    val hijriDay: String,        // Tanggal Hijriah (Angka Arab)
    val hijriMonth: String,      // Nama Bulan Hijriah
    val holidayName: String?,    // Nama Libur Nasional (jika ada)
    val isSunnah: Boolean,       // Puasa Senin-Kamis / Ayyamul Bidh
    val isWajib: Boolean,        // Puasa Ramadhan
    val isToday: Boolean         // Apakah hari ini
)