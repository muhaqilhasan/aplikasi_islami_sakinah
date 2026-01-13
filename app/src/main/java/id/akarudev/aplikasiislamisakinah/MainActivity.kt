package id.akarudev.aplikasiislamisakinah

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import id.akarudev.aplikasiislamisakinah.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install Splash Screen (WAJIB dipanggil sebelum super.onCreate)
        // Ini akan menangani transisi dari tema Splash ke tema Aplikasi
        installSplashScreen()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        checkThemeChangeRedirect()
    }

    private fun checkThemeChangeRedirect() {
        // ... (kode logic tema yang sudah ada sebelumnya) ...
        val prefs = getSharedPreferences("SakinahPrefs", android.content.Context.MODE_PRIVATE)
        val isChangingTheme = prefs.getBoolean("is_changing_theme", false)

        if (isChangingTheme) {
            prefs.edit().putBoolean("is_changing_theme", false).apply()
            binding.root.post {
                try {
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .setLaunchSingleTop(true)
                        .build()
                    navController.navigate(R.id.nav_settings, null, navOptions)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}