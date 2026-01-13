package id.akarudev.aplikasiislamisakinah.ui.calendar

import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import id.akarudev.aplikasiislamisakinah.R
import id.akarudev.aplikasiislamisakinah.databinding.ItemCalendarDayBinding
import id.akarudev.aplikasiislamisakinah.model.DayData

class CalendarAdapter(
    private var dayList: List<DayData>,
    private var isHijriMode: Boolean
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    inner class DayViewHolder(val binding: ItemCalendarDayBinding) : RecyclerView.ViewHolder(binding.root)

    fun setHijriMode(enabled: Boolean) {
        isHijriMode = enabled
        notifyDataSetChanged()
    }

    fun updateData(newData: List<DayData>) {
        dayList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val item = dayList[position]
        val context = holder.itemView.context

        // Jika day = -1, berarti tanggal kosong (padding awal bulan)
        if (item.day == -1) {
            holder.itemView.visibility = View.INVISIBLE
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
        }

        with(holder.binding) {
            // --- 1. Mode Tampilan (Masehi vs Hijriah) ---
            if (isHijriMode) {
                // Utama: Arab (Hijri), Sub: Masehi
                tvMainDate.text = item.hijriDay
                tvMainDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvMainDate.typeface = android.graphics.Typeface.DEFAULT_BOLD

                tvSubDate.text = item.day.toString()
            } else {
                // Utama: Masehi, Sub: Arab (Hijri)
                tvMainDate.text = item.day.toString()
                tvMainDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvMainDate.typeface = android.graphics.Typeface.DEFAULT_BOLD

                tvSubDate.text = item.hijriDay
            }

            // --- 2. Highlight Hari Ini ---
            if (item.isToday) {
                bgDay.setCardBackgroundColor(ContextCompat.getColor(context, R.color.emerald_primary))
                tvMainDate.setTextColor(ContextCompat.getColor(context, R.color.white))
                tvSubDate.setTextColor(ContextCompat.getColor(context, R.color.emerald_light))
            } else {
                bgDay.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

                // Ubah warna teks jika tanggal merah (Minggu atau Libur Nasional)
                if (item.holidayName != null) {
                    tvMainDate.setTextColor(ContextCompat.getColor(context, R.color.event_holiday))
                } else {
                    tvMainDate.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                }
                tvSubDate.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            }

            // --- 3. Indikator Dot (Event) ---
            dot1.visibility = View.GONE
            dot2.visibility = View.GONE

            val activeDots = mutableListOf<Int>() // Menyimpan warna dot yang aktif

            // Prioritas 1: Libur Nasional
            if (item.holidayName != null) {
                activeDots.add(ContextCompat.getColor(context, R.color.event_holiday))
            }

            // Prioritas 2: Puasa Wajib
            if (item.isWajib) {
                activeDots.add(ContextCompat.getColor(context, R.color.gold_accent))
            }
            // Prioritas 3: Puasa Sunnah (Jika tidak wajib)
            else if (item.isSunnah) {
                activeDots.add(ContextCompat.getColor(context, R.color.event_sunnah))
            }

            // Render Dot
            if (activeDots.isNotEmpty()) {
                dot1.visibility = View.VISIBLE
                dot1.backgroundTintList = ColorStateList.valueOf(activeDots[0])
            }
            if (activeDots.size > 1) {
                dot2.visibility = View.VISIBLE
                dot2.backgroundTintList = ColorStateList.valueOf(activeDots[1])
            }
        }
    }

    override fun getItemCount() = dayList.size
}