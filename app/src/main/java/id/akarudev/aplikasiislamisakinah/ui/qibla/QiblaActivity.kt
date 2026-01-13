package id.akarudev.aplikasiislamisakinah.ui.qibla

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import id.akarudev.aplikasiislamisakinah.databinding.ActivityQiblaBinding
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class QiblaActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityQiblaBinding
    private lateinit var sensorManager: SensorManager

    private var currentDegree = 0f

    // Lokasi Ka'bah
    private val kaabaLat = 21.422487
    private val kaabaLng = 39.826206

    // Lokasi User (Default: Bandung) - Idealnya ambil dari GPS
    private val userLat = -6.9175
    private val userLng = 107.6191

    private var qiblaAngle = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQiblaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Hitung sudut kiblat statis berdasarkan lokasi
        qiblaAngle = calculateQiblaAngle(userLat, userLng).toFloat()

        // Atur posisi awal jarum kiblat relatif terhadap Utara di piringan
        // Piringan (Utara) ada di 0 derajat. Kiblat ada di qiblaAngle.
        // Kita putar pointer agar menunjuk ke sudut tersebut pada piringan.
        binding.ivQiblaPointer.rotation = qiblaAngle
    }

    override fun onResume() {
        super.onResume()
        // Register Sensor (Accelerometer + Magnetometer)
        sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        } ?: run {
            Toast.makeText(this, "Sensor Kompas tidak ditemukan!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        // Dapatkan sudut arah utara (Azimuth) dari sensor
        // 0 = Utara, 90 = Timur, 180 = Selatan, 270 = Barat
        val degree = Math.round(event.values[0]).toFloat()

        binding.tvDegree.text = "${degree.toInt()}°"

        // Putar Piringan Kompas (Dial) berlawanan arah dengan rotasi HP
        // Agar 'Utara' di gambar tetap menunjuk ke Utara bumi
        val rotationAnim = RotateAnimation(
            -currentDegree,
            -degree,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )

        rotationAnim.duration = 210
        rotationAnim.fillAfter = true

        binding.ivCompassDial.startAnimation(rotationAnim)

        // Jarum kiblat menempel pada piringan (child logic),
        // tapi karena ini Image terpisah, kita juga harus memutarnya.
        // Logika: Jarum harus selalu menunjuk ke (QiblaAngle - DeviceRotation)
        // Cara simpel: Putar jarum SAMA dengan piringan, ditambah offset Kiblat
        val qiblaAnim = RotateAnimation(
            -currentDegree + qiblaAngle,
            -degree + qiblaAngle,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f // Pivot di tengah tapi gambar offset visual di XML
        )
        qiblaAnim.duration = 210
        qiblaAnim.fillAfter = true
        binding.ivQiblaPointer.startAnimation(qiblaAnim)

        currentDegree = degree
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak perlu ditangani untuk basic compass
    }

    // Rumus Great Circle Bearing untuk menghitung arah kiblat
    private fun calculateQiblaAngle(lat: Double, lng: Double): Double {
        val phiK = Math.toRadians(kaabaLat)
        val lambdaK = Math.toRadians(kaabaLng)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lng)

        val psi = 90.0 - Math.toDegrees(atan2(
            sin(lambdaK - lambda),
            cos(phi) * kotlin.math.tan(phiK) - sin(phi) * cos(lambdaK - lambda)
        ))

        return psi
    }
}