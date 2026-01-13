package id.akarudev.aplikasiislamisakinah.ui.schedule

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import id.akarudev.aplikasiislamisakinah.R

class AdzanReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Waktu Shalat"

        // 1. Tampilkan Notifikasi
        showNotification(context, prayerName)

        // 2. Mainkan Suara Adzan
        playAdzanAudio(context)
    }

    private fun showNotification(context: Context, prayerName: String) {
        val channelId = "adzan_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Buat Channel untuk Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Jadwal Adzan", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Notifikasi waktu shalat"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Pastikan icon ic_bell ada di drawable
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bell)
            .setContentTitle("Waktu $prayerName Telah Tiba")
            .setContentText("Mari segera tunaikan ibadah shalat.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        notificationManager.notify(prayerName.hashCode(), builder.build())
    }

    private fun playAdzanAudio(context: Context) {
        try {
            // Pastikan file adzan.mp3 ada di folder res/raw
            // Jika tidak ada, gunakan default ringtone atau tangani error
            val resId = try {
                R.raw.adzan
            } catch (e: Exception) {
                0 // File tidak ditemukan
            }

            if (resId != 0) {
                val mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    it.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}