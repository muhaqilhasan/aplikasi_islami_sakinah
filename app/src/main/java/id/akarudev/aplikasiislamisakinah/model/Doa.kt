package id.akarudev.aplikasiislamisakinah.model

data class Doa(
    val id: Int,
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val category: String
)
