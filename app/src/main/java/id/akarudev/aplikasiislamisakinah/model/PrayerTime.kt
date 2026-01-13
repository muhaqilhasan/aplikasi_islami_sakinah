package id.akarudev.aplikasiislamisakinah.model

data class PrayerTime(
    val name: String,
    val time: String,
    val isPassed: Boolean = false,
    val isActive: Boolean = false
)