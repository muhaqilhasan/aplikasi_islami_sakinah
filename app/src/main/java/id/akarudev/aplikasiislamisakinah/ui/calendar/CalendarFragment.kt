package id.akarudev.aplikasiislamisakinah.ui.calendar

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import id.akarudev.aplikasiislamisakinah.R
import id.akarudev.aplikasiislamisakinah.databinding.FragmentCalendarBinding
import id.akarudev.aplikasiislamisakinah.model.DayData
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.Executors

class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CalendarAdapter

    private var isHijriMode = false
    private val calendar = Calendar.getInstance()
    private val executor = Executors.newFixedThreadPool(2)
    private val handler = Handler(Looper.getMainLooper())

    // Cache untuk data libur agar tidak request berulang kali
    private var cachedHolidays: Map<String, String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Adapter
        adapter = CalendarAdapter(emptyList(), isHijriMode)
        binding.rvCalendar.layoutManager = GridLayoutManager(context, 7)
        binding.rvCalendar.adapter = adapter

        // Setup Teks Tombol Awal
        updateToggleText()

        binding.btnToggleMode.setOnClickListener {
            isHijriMode = !isHijriMode
            adapter.setHijriMode(isHijriMode)
            updateToggleText()
        }

        binding.btnPrevMonth.setOnClickListener { changeMonth(-1) }
        binding.btnNextMonth.setOnClickListener { changeMonth(1) }

        // Muat Data
        refreshCalendarData()
    }

    private fun updateToggleText() {
        binding.btnToggleMode.text = if (isHijriMode) getString(R.string.toggle_hijri) else getString(R.string.toggle_masehi)
    }

    private fun changeMonth(offset: Int) {
        calendar.add(Calendar.MONTH, offset)
        refreshCalendarData()
    }

    private fun refreshCalendarData() {
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // GUNAKAN Locale.getDefault() agar format bulan mengikuti bahasa aplikasi (ID/EN/AR)
        val currentLocale = Locale.getDefault()
        val sdf = SimpleDateFormat("MMMM yyyy", currentLocale)

        // Fix: Menggunakan NumberFormat atau manual replace untuk tahun jika locale Arab
        var dateString = sdf.format(calendar.time)
        if (currentLocale.language == "ar") {
            // Konversi tahun masehi di header ke angka arab jika mode bahasa Arab
            val yearStr = calendar.get(Calendar.YEAR).toString()
            val arabYear = toArabicNumerals(yearStr.toInt())
            dateString = dateString.replace(yearStr, arabYear)
        }

        binding.tvMonthYear.text = dateString

        binding.tvHijriMonth.text = "..."
        binding.tvAgendaList.text = "..."

        executor.execute {
            try {
                // 1. Fetch Hijriah (AlAdhan API)
                val apiMonth = currentMonth + 1
                val hijriUrl = "https://api.aladhan.com/v1/gToHCalendar/$apiMonth/$currentYear"

                val hijriJson = URL(hijriUrl).readText()
                val hijriResponse = JSONObject(hijriJson)

                val hijriDataArray = if (hijriResponse.getInt("code") == 200) {
                    hijriResponse.getJSONArray("data")
                } else {
                    throw Exception("API AlAdhan Error")
                }

                // 2. Fetch Libur Nasional (Cache)
                if (cachedHolidays == null) {
                    val holidaysUrl = "https://raw.githubusercontent.com/guangrei/APIHariLibur_V2/main/calendar.min.json"
                    val holidaysJson = URL(holidaysUrl).readText()
                    cachedHolidays = parseHolidays(holidaysJson)
                }

                // 3. Proses Data dengan Bahasa yang sesuai
                val langCode = currentLocale.language // "en", "ar", "in"
                val processedDays = processCalendarData(currentMonth, currentYear, hijriDataArray, cachedHolidays!!, langCode)

                // 4. Update UI
                handler.post {
                    if (_binding != null && isAdded) {
                        adapter.updateData(processedDays)

                        val validDays = processedDays.filter { it.day != -1 }
                        if (validDays.isNotEmpty()) {
                            // Ambil nama bulan Hijriah dari tengah bulan agar akurat
                            val midDay = validDays[validDays.size / 2]
                            binding.tvHijriMonth.text = "/ ${midDay.hijriMonth}"
                        } else {
                            binding.tvHijriMonth.text = ""
                        }

                        updateAgendaText(processedDays)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    if (_binding != null && isAdded) {
                        // Fallback jika offline
                        val fallbackData = generateFallbackData(currentMonth, currentYear)
                        adapter.updateData(fallbackData)
                        binding.tvHijriMonth.text = "/ (Offline)"
                        binding.tvAgendaList.text = getString(R.string.doa_not_found).replace("Doa", "Data")
                    }
                }
            }
        }
    }

    private fun processCalendarData(
        month: Int,
        year: Int,
        hijriArray: org.json.JSONArray,
        holidays: Map<String, String>,
        lang: String
    ): List<DayData> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val startOffset = dayOfWeek - 1

        val list = mutableListOf<DayData>()

        // Padding Kosong
        for (i in 0 until startOffset) {
            list.add(DayData(-1, month, year, "", "", null, false, false, false))
        }

        // Isi Tanggal
        for (i in 0 until daysInMonth) {
            val date = i + 1

            var hijriDayStr = "-"
            var hijriMonthName = ""
            var hijriYear = ""
            var hDayInt = 0

            if (i < hijriArray.length()) {
                val dayObj = hijriArray.getJSONObject(i)
                val hijriDateObj = dayObj.getJSONObject("hijri")
                hDayInt = hijriDateObj.getString("day").toInt()

                // Konversi Angka ke Arab jika bahasa aplikasi = Arab
                hijriDayStr = toArabicNumerals(hDayInt)

                // Ambil Nama Bulan sesuai Bahasa (API support 'en' dan 'ar')
                // Jika locale aplikasi 'ar', ambil field 'ar', jika tidak ambil 'en'
                val monthKey = if (lang == "ar") "ar" else "en"
                hijriMonthName = hijriDateObj.getJSONObject("month").getString(monthKey)

                // Tahun Hijriah
                val yStr = hijriDateObj.getString("year")
                // Selalu konversi tahun hijriah ke angka Arab untuk konsistensi estetika Islami
                try {
                    hijriYear = toArabicNumerals(yStr.toInt())
                } catch (e: Exception) {
                    hijriYear = yStr
                }
            }

            // Cek Libur
            val monthStr = (month + 1).toString().padStart(2, '0')
            val dateStr = date.toString().padStart(2, '0')
            val dateKey = "$year-$monthStr-$dateStr"
            val holidayName = holidays[dateKey]

            // Cek Hari Minggu
            tempCal.set(year, month, date)
            val isSunday = tempCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

            // Logika Puasa
            val isWajib = hijriMonthName.contains("Ramadan", ignoreCase = true) ||
                    hijriMonthName.contains("Ramadhan", ignoreCase = true) ||
                    hijriMonthName.contains("رمضان")

            val isMonThu = (tempCal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY ||
                    tempCal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY)
            val isAyyamulBidh = (hDayInt in 13..15) && !isWajib
            val isSunnah = (isMonThu || isAyyamulBidh) && !isWajib

            val todayCal = Calendar.getInstance()
            val isToday = (todayCal.get(Calendar.YEAR) == year &&
                    todayCal.get(Calendar.MONTH) == month &&
                    todayCal.get(Calendar.DAY_OF_MONTH) == date)

            list.add(DayData(
                day = date,
                monthIndex = month,
                year = year,
                hijriDay = hijriDayStr,
                hijriMonth = "$hijriMonthName $hijriYear",
                holidayName = if (isSunday && holidayName == null) "Minggu" else holidayName,
                isSunnah = isSunnah,
                isWajib = isWajib,
                isToday = isToday
            ))
        }

        return list
    }

    private fun parseHolidays(jsonString: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val jsonObject = JSONObject(jsonString)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val dateKey = keys.next()
                val obj = jsonObject.optJSONObject(dateKey)
                if (obj != null && obj.optBoolean("holiday", false)) {
                    map[dateKey] = obj.optString("summary", "Libur")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    private fun updateAgendaText(days: List<DayData>) {
        val sb = StringBuilder()
        var count = 0

        val isAr = Locale.getDefault().language == "ar"
        val labelMinggu = if (isAr) "يوم الأحد" else "Minggu"
        val emptyMsg = if (isAr) "لا توجد عطلات رسمية هذا الشهر." else "Tidak ada hari libur nasional bulan ini."

        for (day in days) {
            if (day.day == -1) continue

            // Konversi tanggal masehi ke arab jika locale Arab
            val dayStr = if (isAr) toArabicNumerals(day.day) else day.day.toString()

            if (day.holidayName != null && day.holidayName != "Minggu" && day.holidayName != "Hari Minggu") {
                sb.append("• $dayStr: ${day.holidayName}\n")
                count++
            } else if (day.isWajib && day.day == 1) {
                val ramadanLabel = if(isAr) "بداية رمضان" else "Awal Ramadhan"
                sb.append("• $dayStr: $ramadanLabel\n")
                count++
            }
        }

        if (count == 0) {
            binding.tvAgendaList.text = emptyMsg
        } else {
            binding.tvAgendaList.text = sb.toString().trim()
        }

        // Align text sesuai bahasa
        binding.tvAgendaList.textAlignment = if (isAr) View.TEXT_ALIGNMENT_VIEW_START else View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun generateFallbackData(month: Int, year: Int): List<DayData> {
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val tempCal = Calendar.getInstance()
        tempCal.set(year, month, 1)
        val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val startOffset = dayOfWeek - 1

        val list = mutableListOf<DayData>()
        for (i in 0 until startOffset) {
            list.add(DayData(-1, month, year, "", "", null, false, false, false))
        }
        for (i in 0 until daysInMonth) {
            val date = i + 1
            val todayCal = Calendar.getInstance()
            val isToday = (todayCal.get(Calendar.YEAR) == year &&
                    todayCal.get(Calendar.MONTH) == month &&
                    todayCal.get(Calendar.DAY_OF_MONTH) == date)

            list.add(DayData(
                day = date, monthIndex = month, year = year,
                hijriDay = "-", hijriMonth = "-", holidayName = null,
                isSunnah = false, isWajib = false, isToday = isToday
            ))
        }
        return list
    }

    private fun toArabicNumerals(n: Int): String {
        return n.toString().map { char ->
            when (char) {
                '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'
                '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'
                else -> char
            }
        }.joinToString("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}