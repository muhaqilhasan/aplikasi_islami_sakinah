package id.akarudev.aplikasiislamisakinah.ui.prayers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import id.akarudev.aplikasiislamisakinah.R
import id.akarudev.aplikasiislamisakinah.databinding.FragmentPrayersBinding
import id.akarudev.aplikasiislamisakinah.model.Doa
import org.json.JSONArray
import java.net.URL
import java.util.Locale
import java.util.concurrent.Executors

class PrayersFragment : Fragment() {

    private var _binding: FragmentPrayersBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DoaAdapter

    private var allDoas: List<Doa> = listOf()
    private var currentSearchQuery = ""
    private var currentCategory = "Semua"

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    private var categoryPriority: Map<String, Int> = emptyMap()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPrayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup UI String berdasarkan Bahasa
        currentCategory = getString(R.string.category_all)
        binding.chipAll.text = currentCategory

        // 2. Setup Prioritas Kategori
        categoryPriority = mapOf(
            getString(R.string.category_morning_evening) to 1,
            getString(R.string.category_sholat) to 2,
            getString(R.string.category_food) to 3,
            getString(R.string.category_home) to 4,
            getString(R.string.category_travel) to 5,
            getString(R.string.category_family) to 6,
            getString(R.string.category_daily) to 7,
            getString(R.string.category_other) to 99
        )

        // 3. Update Text Chip
        binding.chipMorning.text = getString(R.string.category_morning_evening)
        binding.chipPrayer.text = getString(R.string.category_sholat)
        binding.chipFood.text = getString(R.string.category_food)
        binding.chipHome.text = getString(R.string.category_home)
        binding.chipTravel.text = getString(R.string.category_travel)

        adapter = DoaAdapter(emptyList())
        binding.rvPrayers.layoutManager = LinearLayoutManager(context)
        binding.rvPrayers.adapter = adapter

        setupSearch()
        setupCategoryChips()

        // 4. Putuskan Sumber Data berdasarkan Bahasa
        val lang = Locale.getDefault().language
        if (lang == "in" || lang == "id") {
            // Khusus Indo: Coba fetch API dulu
            fetchDoaFromApi()
        } else {
            // Inggris/Arab: Langsung load data internal (agar pasti muncul & rapi)
            loadFallbackData()
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentSearchQuery = s.toString()
                applyFilter()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds[0]
                currentCategory = when (chipId) {
                    R.id.chipAll -> getString(R.string.category_all)
                    R.id.chipMorning -> getString(R.string.category_morning_evening)
                    R.id.chipPrayer -> getString(R.string.category_sholat)
                    R.id.chipFood -> getString(R.string.category_food)
                    R.id.chipHome -> getString(R.string.category_home)
                    R.id.chipTravel -> getString(R.string.category_travel)
                    else -> getString(R.string.category_all)
                }
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val query = currentSearchQuery.lowercase()
        val catAll = getString(R.string.category_all)

        val filteredList = allDoas.filter { doa ->
            val matchesSearch = doa.title.lowercase().contains(query) ||
                    doa.translation.lowercase().contains(query) ||
                    doa.category.lowercase().contains(query)

            val matchesCategory = if (currentCategory == catAll) true else doa.category == currentCategory

            matchesSearch && matchesCategory
        }

        val sortedList = if (currentCategory == catAll) {
            sortDoasByCategory(filteredList)
        } else {
            filteredList.sortedBy { it.title }
        }

        adapter.updateData(sortedList)
    }

    private fun fetchDoaFromApi() {
        binding.etSearch.hint = "Memuat data..." // Loading indicator sederhana

        executor.execute {
            try {
                // Gunakan API open-api.my.id yang stabil untuk konten Indonesia
                val url = URL("https://open-api.my.id/api/doa")
                val jsonString = url.readText()
                val doaArray = JSONArray(jsonString)
                val fetchedDoas = mutableListOf<Doa>()

                for (i in 0 until doaArray.length()) {
                    val obj = doaArray.getJSONObject(i)

                    val id = obj.optInt("id", i)
                    val title = obj.getString("judul")
                    val arabic = obj.getString("arab")
                    val latin = obj.getString("latin")
                    val translation = obj.getString("terjemah")
                    val category = determineCategory(title)

                    fetchedDoas.add(Doa(id, title, arabic, latin, translation, category))
                }

                handler.post {
                    if (_binding != null) {
                        binding.etSearch.hint = getString(R.string.search_hint)
                        if (fetchedDoas.isNotEmpty()) {
                            allDoas = sortDoasByCategory(fetchedDoas)
                            applyFilter()
                        } else {
                            loadFallbackData()
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    if (_binding != null && isAdded) {
                        // Jika gagal, gunakan data offline
                        binding.etSearch.hint = getString(R.string.search_hint)
                        loadFallbackData()
                    }
                }
            }
        }
    }

    private fun sortDoasByCategory(list: List<Doa>): List<Doa> {
        return list.sortedWith(
            compareBy<Doa> { categoryPriority[it.category] ?: 99 }
                .thenBy { it.title }
        )
    }

    // Fungsi ini hanya dipakai untuk data API Indonesia
    private fun determineCategory(title: String): String {
        val t = title.lowercase()
        return when {
            t.contains("tidur") || t.contains("bangun") || t.contains("pagi") || t.contains("sore") -> getString(R.string.category_morning_evening)
            t.contains("masjid") || t.contains("wudhu") || t.contains("sholat") || t.contains("azan") || t.contains("iqamah") -> getString(R.string.category_sholat)
            t.contains("makan") || t.contains("minum") || t.contains("puasa") -> getString(R.string.category_food)
            t.contains("rumah") || t.contains("wc") || t.contains("cermin") || t.contains("pakaian") -> getString(R.string.category_home)
            t.contains("perjalanan") || t.contains("kendaraan") || t.contains("safar") -> getString(R.string.category_travel)
            t.contains("orang tua") || t.contains("keluarga") -> getString(R.string.category_family)
            else -> getString(R.string.category_other)
        }
    }

    private fun loadFallbackData() {
        val lang = Locale.getDefault().language
        val data = when (lang) {
            "ar" -> getArabicDoas()
            "en" -> getEnglishDoas()
            else -> getIndonesianDoas()
        }
        allDoas = sortDoasByCategory(data)
        applyFilter()
    }

    // --- DATA OFFLINE (LENGKAP DI SEMUA BAHASA) ---
    // Digunakan sebagai sumber utama untuk EN/AR dan cadangan untuk ID

    private fun getIndonesianDoas(): List<Doa> = listOf(
        Doa(1, "Doa Sebelum Makan", "اَللّٰهُمَّ بَارِكْ لَنَا فِيْمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ", "Allahumma baarik lanaa...", "Ya Allah berkahilah kami dalam rezeki yang telah Engkau berikan...", getString(R.string.category_food)),
        Doa(2, "Doa Sesudah Makan", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "Alhamdulillahilladzi...", "Segala puji bagi Allah yang telah memberi kami makan dan minum...", getString(R.string.category_food)),
        Doa(3, "Doa Sebelum Tidur", "بِسْمِكَ اللّهُمَّ اَحْيَا وَ بِسْمِكَ اَمُوْتُ", "Bismika Allahumma...", "Dengan nama-Mu Ya Allah aku hidup dan dengan nama-Mu aku mati.", getString(R.string.category_morning_evening)),
        Doa(4, "Doa Bangun Tidur", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "Alhamdulillahilladzi...", "Segala puji bagi Allah yang menghidupkan kami setelah mematikan kami.", getString(R.string.category_morning_evening)),
        Doa(5, "Doa Masuk Masjid", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "Allahummaf-tahlii...", "Ya Allah, bukakanlah untukku pintu-pintu rahmat-Mu.", getString(R.string.category_sholat)),
        Doa(6, "Doa Keluar Masjid", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "Allahumma innii...", "Ya Allah, sesungguhnya aku memohon keutamaan dari-Mu.", getString(R.string.category_sholat)),
        Doa(7, "Doa Masuk Rumah", "بِسْمِ اللهِ وَلَجْنَا، وَ بِسْمِ اللهِ خَرَجْنَا", "Bismillahi walajnaa...", "Dengan nama Allah kami masuk...", getString(R.string.category_home)),
        Doa(8, "Doa Keluar Rumah", "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ", "Bismillahi tawakkaltu...", "Dengan nama Allah, aku bertawakkal kepada Allah.", getString(R.string.category_home)),
        Doa(9, "Doa Naik Kendaraan", "سُبْحَانَ الَّذِى سَخَّرَ لَنَا هَذَا", "Subhanalladzi...", "Maha Suci Allah yang telah menundukkan semua ini bagi kami.", getString(R.string.category_travel)),
        Doa(10, "Doa Kedua Orang Tua", "رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَارْحَمْهُمَا", "Rabbighfir lii...", "Ya Tuhanku, ampunilah dosaku dan dosa kedua orang tuaku...", getString(R.string.category_family))
    )

    private fun getEnglishDoas(): List<Doa> = listOf(
        Doa(1, "Prayer Before Eating", "اَللّٰهُمَّ بَارِكْ لَنَا فِيْمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ", "Allahumma baarik lanaa...", "O Allah, bless us in what You have provided us and save us from the punishment of the Fire.", getString(R.string.category_food)),
        Doa(2, "Prayer After Eating", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "Alhamdulillahilladzi...", "Praise be to Allah Who has fed us and given us drink and made us Muslims.", getString(R.string.category_food)),
        Doa(3, "Prayer Before Sleeping", "بِسْمِكَ اللّهُمَّ اَحْيَا وَ بِسْمِكَ اَمُوْتُ", "Bismika Allahumma...", "In Your Name O Allah, I live and I die.", getString(R.string.category_morning_evening)),
        Doa(4, "Prayer Waking Up", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "Alhamdulillahilladzi...", "Praise is to Allah Who gives us life after He has caused us to die and to Him is the return.", getString(R.string.category_morning_evening)),
        Doa(5, "Entering Mosque", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "Allahummaf-tahlii...", "O Allah, open for me the doors of Your mercy.", getString(R.string.category_sholat)),
        Doa(6, "Leaving Mosque", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "Allahumma innii...", "O Allah, I ask You from Your favour.", getString(R.string.category_sholat)),
        Doa(7, "Entering Home", "بِسْمِ اللهِ وَلَجْنَا، وَ بِسْمِ اللهِ خَرَجْنَا", "Bismillahi walajnaa...", "In the name of Allah we enter and in the name of Allah we leave, and upon our Lord we rely.", getString(R.string.category_home)),
        Doa(8, "Leaving Home", "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ", "Bismillahi tawakkaltu...", "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.", getString(R.string.category_home)),
        Doa(9, "Riding Vehicle", "سُبْحَانَ الَّذِى سَخَّرَ لَنَا هَذَا", "Subhanalladzi...", "Glory to Him who has subjected this to us, and we could not have otherwise subdued it.", getString(R.string.category_travel)),
        Doa(10, "Prayer for Parents", "رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَارْحَمْهُمَا", "Rabbighfir lii...", "My Lord, forgive me and my parents and have mercy upon them as they brought me up [when I was] small.", getString(R.string.category_family))
    )

    private fun getArabicDoas(): List<Doa> = listOf(
        Doa(1, "دعاء قبل الأكل", "اَللّٰهُمَّ بَارِكْ لَنَا فِيْمَا رَزَقْتَنَا وَقِنَا عَذَابَ النَّارِ", "", "اللهم بارك لنا فيما رزقتنا وقنا عذاب النار.", getString(R.string.category_food)),
        Doa(2, "دعاء بعد الأكل", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنَا وَسَقَانَا وَجَعَلَنَا مُسْلِمِينَ", "", "الحمد لله الذي أطعمنا وسقانا وجعلنا مسلمين.", getString(R.string.category_food)),
        Doa(3, "دعاء قبل النوم", "بِسْمِكَ اللّهُمَّ اَحْيَا وَ بِسْمِكَ اَمُوْتُ", "", "باسمك اللهم أحيا وباسمك أموت.", getString(R.string.category_morning_evening)),
        Doa(4, "دعاء الاستيقاظ", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ", "", "الحمد لله الذي أحيانا بعد ما أماتنا وإليه النشور.", getString(R.string.category_morning_evening)),
        Doa(5, "دعاء دخول المسجد", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "", "اللهم افتح لي أبواب رحمتك.", getString(R.string.category_sholat)),
        Doa(6, "دعاء الخروج من المسجد", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "", "اللهم إني أسألك من فضلك.", getString(R.string.category_sholat)),
        Doa(7, "دعاء دخول المنزل", "بِسْمِ اللهِ وَلَجْنَا، وَ بِسْمِ اللهِ خَرَجْنَا", "", "بسم الله ولجنا، وبسم الله خرجنا، وعلى ربنا توكلنا.", getString(R.string.category_home)),
        Doa(8, "دعاء الخروج من المنزل", "بِسْمِ اللهِ تَوَكَّلْتُ عَلَى اللهِ", "", "بسم الله توكلت على الله، ولا حول ولا قوة إلا بالله.", getString(R.string.category_home)),
        Doa(9, "دعاء ركوب الدابة", "سُبْحَانَ الَّذِى سَخَّرَ لَنَا هَذَا", "", "سبحان الذي سخر لنا هذا وما كنا له مقرنين.", getString(R.string.category_travel)),
        Doa(10, "دعاء للوالدين", "رَبِّ اغْفِرْ لِيْ وَلِوَالِدَيَّ وَارْحَمْهُمَا", "", "رب اغفر لي ولوالدي وارحمهما كما ربياني صغيرا.", getString(R.string.category_family))
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}