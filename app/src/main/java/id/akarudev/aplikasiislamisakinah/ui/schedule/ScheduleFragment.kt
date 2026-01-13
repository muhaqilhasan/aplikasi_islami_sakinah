package id.akarudev.aplikasiislamisakinah.ui.schedule

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.akarudev.aplikasiislamisakinah.databinding.FragmentScheduleBinding
import id.akarudev.aplikasiislamisakinah.model.PrayerTime
import id.akarudev.aplikasiislamisakinah.ui.qibla.QiblaActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ScheduleAdapter

    private var prayerTimesList: List<PrayerTime> = listOf()

    // Permission Request Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Izin notifikasi diberikan", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()

        binding.rvSchedule.layoutManager = LinearLayoutManager(context)

        // Ambil Data Jadwal
        fetchPrayerTimesFromApi()

        // Tombol Kompas Kiblat
        binding.fabQibla.setOnClickListener {
            val intent = Intent(requireContext(), QiblaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkPermissions() {
        // Cek Izin Notifikasi (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Cek Izin Alarm Exact (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Opsional: Arahkan user ke setting jika alarm harus sangat presisi
                // val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                // startActivity(intent)
            }
        }
    }

    private fun fetchPrayerTimesFromApi() {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                // Mengambil data untuk hari ini (Bandung, Method 20 Kemenag)
                val url = URL("https://api.aladhan.com/v1/timingsByCity?city=Bandung&country=Indonesia&method=20")
                val jsonString = url.readText()
                val jsonObject = JSONObject(jsonString)
                val timings = jsonObject.getJSONObject("data").getJSONObject("timings")

                val rawData = mapOf(
                    "Imsak" to timings.getString("Imsak"),
                    "Subuh" to timings.getString("Fajr"),
                    "Terbit" to timings.getString("Sunrise"),
                    "Dzuhur" to timings.getString("Dhuhr"),
                    "Ashar" to timings.getString("Asr"),
                    "Maghrib" to timings.getString("Maghrib"),
                    "Isya" to timings.getString("Isha")
                )

                activity?.runOnUiThread {
                    updateUIAndScheduleAlarms(rawData)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    if (isAdded) {
                        Toast.makeText(context, "Gagal memuat jadwal online", Toast.LENGTH_SHORT).show()
                        loadMockData()
                    }
                }
            }
        }
    }

    private fun updateUIAndScheduleAlarms(rawData: Map<String, String>) {
        if (!isAdded) return

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = sdf.format(Date())

        var nextFound = false
        val newPrayerTimes = rawData.map { (name, time) ->
            // Bandingkan jam string "HH:mm"
            val isPassed = time < currentTime
            var isActive = false

            // Tandai jadwal PERTAMA yang belum lewat sebagai "Active" (Next Adzan)
            if (!isPassed && !nextFound) {
                isActive = true
                nextFound = true
            }
            // Jika waktu sama persis
            if (time == currentTime) isActive = true

            // Jadwalkan Alarm di Background jika belum lewat
            if (!isPassed && name != "Terbit" && name != "Imsak") { // Biasanya Imsak/Terbit tidak ada Adzan
                scheduleAdzanAlarmSafe(requireContext(), name, time)
            }

            PrayerTime(name, time, isPassed, isActive)
        }

        prayerTimesList = newPrayerTimes

        adapter = ScheduleAdapter(prayerTimesList) { prayerTime ->
            // Klik pada item hanya menampilkan info, tidak play audio langsung
            Toast.makeText(context, "Alarm ${prayerTime.name} dijadwalkan pukul ${prayerTime.time}", Toast.LENGTH_SHORT).show()
        }
        binding.rvSchedule.adapter = adapter

        // Scroll otomatis ke jadwal aktif
        val activeIndex = prayerTimesList.indexOfFirst { it.isActive }
        if (activeIndex != -1) {
            binding.rvSchedule.scrollToPosition(activeIndex)
        }
    }

    private fun loadMockData() {
        val mockData = mapOf(
            "Imsak" to "04:05", "Subuh" to "04:15", "Terbit" to "05:35",
            "Dzuhur" to "11:58", "Ashar" to "15:22", "Maghrib" to "18:12", "Isya" to "19:26"
        )
        updateUIAndScheduleAlarms(mockData)
    }

    private fun scheduleAdzanAlarmSafe(context: Context, name: String, time: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // Intent ke AdzanReceiver
            val intent = Intent(context, AdzanReceiver::class.java).apply {
                putExtra("PRAYER_NAME", name)
            }

            // RequestCode unik berdasarkan hash nama shalat
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                name.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Parsing waktu
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Hanya jadwalkan jika waktu masih di masa depan
            if (calendar.timeInMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}