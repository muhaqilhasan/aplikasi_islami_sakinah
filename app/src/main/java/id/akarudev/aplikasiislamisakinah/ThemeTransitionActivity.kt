package id.akarudev.aplikasiislamisakinah

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import id.akarudev.aplikasiislamisakinah.databinding.ActivityThemeTransitionBinding

class ThemeTransitionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityThemeTransitionBinding
    private var isToDark: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeTransitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isToDark = intent.getBooleanExtra("IS_TO_DARK", true)

        setupInitialState()

        Handler(Looper.getMainLooper()).postDelayed({
            startAnimation()
        }, 50)
    }

    private fun setupInitialState() {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (isToDark) {
            binding.ivIconOutgoing.setImageResource(R.drawable.ic_circle)
            binding.ivIconOutgoing.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent))

            binding.ivIconIncoming.setImageResource(R.drawable.ic_moon)
            binding.ivIconIncoming.setColorFilter(ContextCompat.getColor(this, R.color.white))

            binding.gradientOverlay.background = ContextCompat.getDrawable(this, R.drawable.gradient_to_dark)
            binding.tvLabel.text = "Mengaktifkan Mode Malam..."
            binding.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.ivIconOutgoing.setImageResource(R.drawable.ic_moon)
            binding.ivIconOutgoing.setColorFilter(ContextCompat.getColor(this, R.color.white))

            binding.ivIconIncoming.setImageResource(R.drawable.ic_circle)
            binding.ivIconIncoming.setColorFilter(ContextCompat.getColor(this, R.color.gold_accent))

            binding.gradientOverlay.background = ContextCompat.getDrawable(this, R.drawable.gradient_to_light)
            binding.tvLabel.text = "Mengaktifkan Mode Siang..."
            binding.tvLabel.setTextColor(ContextCompat.getColor(this, R.color.emerald_dark))
        }

        binding.ivIconIncoming.translationY = -screenHeight
    }

    private fun startAnimation() {
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        val alphaAnim = ObjectAnimator.ofFloat(binding.gradientOverlay, "alpha", 0f, 0.95f)
        alphaAnim.duration = 400

        val textAlphaAnim = ObjectAnimator.ofFloat(binding.tvLabel, "alpha", 0f, 1f)
        textAlphaAnim.duration = 500
        textAlphaAnim.startDelay = 100

        val outgoingAnim = ObjectAnimator.ofFloat(binding.ivIconOutgoing, "translationY", 0f, screenHeight)
        outgoingAnim.duration = 600
        outgoingAnim.interpolator = AccelerateInterpolator()

        val incomingAnim = ObjectAnimator.ofFloat(binding.ivIconIncoming, "translationY", -screenHeight/1.5f, 0f)
        incomingAnim.duration = 800
        incomingAnim.interpolator = OvershootInterpolator(1.0f)
        incomingAnim.startDelay = 100

        alphaAnim.start()
        textAlphaAnim.start()
        outgoingAnim.start()
        incomingAnim.start()

        incomingAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                Handler(Looper.getMainLooper()).postDelayed({
                    applyThemeAndFinish()
                }, 300)
            }
        })
    }

    private fun applyThemeAndFinish() {
        // 1. Terapkan Tema Global
        if (isToDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 2. Finish Activity ini saja.
        // MainActivity (di belakang) akan otomatis di-recreate oleh OS karena konfigurasi berubah.
        // Kita TIDAK start Intent manual -> Mencegah Crash BinderProxy.
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}