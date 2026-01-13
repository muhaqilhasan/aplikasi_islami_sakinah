package id.akarudev.aplikasiislamisakinah.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.akarudev.aplikasiislamisakinah.R
import id.akarudev.aplikasiislamisakinah.databinding.ItemScheduleBinding
import id.akarudev.aplikasiislamisakinah.model.PrayerTime

class ScheduleAdapter(
    private val items: List<PrayerTime>,
    private val onNotificationClick: (PrayerTime) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvPrayerName.text = item.name
            tvPrayerTime.text = item.time

            // Listener Klik Lonceng
            ivNotification.setOnClickListener {
                onNotificationClick(item)
                ivNotification.alpha = 0.5f
                ivNotification.animate().alpha(1f).setDuration(300).start()
            }

            if (item.isActive) {
                // === KONDISI AKTIF (Jadwal Selanjutnya/Sekarang) ===
                // Background Hijau, Teks Putih
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.emerald_primary))

                // Pastikan teks putih agar kontras dengan hijau
                tvPrayerName.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvPrayerTime.setTextColor(ContextCompat.getColor(context, R.color.white))
                ivNotification.setColorFilter(ContextCompat.getColor(context, R.color.white))

                tvStatus.text = "Sedang Berlangsung" // Atau "Selanjutnya"
                tvStatus.visibility = android.view.View.VISIBLE
            } else {
                // === KONDISI NORMAL (Jadwal Lain) ===
                // PERBAIKAN: Gunakan 'surface_card' bukan 'white'
                // Agar di Dark Mode warnanya jadi Gelap, bukan Putih silau
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface_card))

                // Kembalikan warna teks sesuai tema (Hitam di Light, Putih di Dark)
                tvPrayerName.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                tvPrayerTime.setTextColor(ContextCompat.getColor(context, R.color.text_primary))

                if (item.isPassed) {
                    // Jadwal Lewat: Ikon abu-abu, kartu agak transparan
                    ivNotification.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary))
                    root.alpha = 0.5f
                } else {
                    // Jadwal Belum Lewat: Ikon Hijau, kartu normal
                    ivNotification.setColorFilter(ContextCompat.getColor(context, R.color.emerald_primary))
                    root.alpha = 1.0f
                }
                tvStatus.visibility = android.view.View.GONE
            }
        }
    }

    override fun getItemCount() = items.size
}