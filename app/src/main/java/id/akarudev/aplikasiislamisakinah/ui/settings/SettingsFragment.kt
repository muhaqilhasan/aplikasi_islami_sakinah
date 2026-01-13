package id.akarudev.aplikasiislamisakinah.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import id.akarudev.aplikasiislamisakinah.R
import id.akarudev.aplikasiislamisakinah.ThemeTransitionActivity
import id.akarudev.aplikasiislamisakinah.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("SakinahPrefs", Context.MODE_PRIVATE)

        setupDarkMode()
        setupLanguage()
        setupAudioSettings()
    }

    private fun setupDarkMode() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        binding.switchDarkMode.setOnCheckedChangeListener(null)
        binding.switchDarkMode.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // 1. Simpan flag bahwa kita sedang ganti tema
            // Ini akan dibaca oleh MainActivity saat restart nanti
            sharedPreferences.edit().putBoolean("is_changing_theme", true).apply()

            // 2. Buka Animasi
            val intent = Intent(requireContext(), ThemeTransitionActivity::class.java)
            intent.putExtra("IS_TO_DARK", isChecked)
            startActivity(intent)

            // JANGAN finish() MainActivity. Biarkan dia hidup di belakang layar transparan.
        }
    }

    private fun setupLanguage() {
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLang = if (!currentLocales.isEmpty) currentLocales[0]?.language else "id"

        when (currentLang) {
            "en" -> binding.toggleLanguage.check(R.id.btnEnglish)
            "ar" -> binding.toggleLanguage.check(R.id.btnArabic)
            else -> binding.toggleLanguage.check(R.id.btnIndo)
        }

        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnIndo -> setLocale("id")
                    R.id.btnEnglish -> setLocale("en")
                    R.id.btnArabic -> setLocale("ar")
                }
            }
        }
    }

    private fun setLocale(languageCode: String) {
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    private fun setupAudioSettings() {
        val savedSound = sharedPreferences.getString("adzan_sound", "makkah")

        when (savedSound) {
            "makkah" -> binding.rbMakkah.isChecked = true
            "madinah" -> binding.rbMadinah.isChecked = true
            "beep" -> binding.rbBeep.isChecked = true
        }

        binding.rgAdzanSound.setOnCheckedChangeListener { _, checkedId ->
            val editor = sharedPreferences.edit()
            val soundName = when (checkedId) {
                R.id.rbMakkah -> "makkah"
                R.id.rbMadinah -> "madinah"
                R.id.rbBeep -> "beep"
                else -> "makkah"
            }

            editor.putString("adzan_sound", soundName)
            editor.apply()

            Toast.makeText(context, "Audio set: $soundName", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}