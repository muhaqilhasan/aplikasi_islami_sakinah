package id.akarudev.aplikasiislamisakinah.ui.prayers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.akarudev.aplikasiislamisakinah.databinding.ItemDoaBinding
import id.akarudev.aplikasiislamisakinah.model.Doa

class DoaAdapter(private var listDoa: List<Doa>) : RecyclerView.Adapter<DoaAdapter.DoaViewHolder>() {

    inner class DoaViewHolder(val binding: ItemDoaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoaViewHolder {
        val binding = ItemDoaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DoaViewHolder, position: Int) {
        val doa = listDoa[position]
        with(holder.binding) {
            tvTitle.text = doa.title
            tvCategory.text = doa.category
            tvArabic.text = doa.arabic

            // Logika Tampilan Latin: Sembunyikan jika kosong (API HadithEnc tidak ada Latin)
            if (doa.latin.isNullOrEmpty() || doa.latin == "-") {
                tvLatin.visibility = View.GONE
            } else {
                tvLatin.visibility = View.VISIBLE
                tvLatin.text = doa.latin
            }

            // Bersihkan format HTML jika ada dari API (misal <br> atau <p>)
            // API HadithEnc kadang menyertakan tag HTML di explanation
            val cleanTranslation = android.text.Html.fromHtml(doa.translation, android.text.Html.FROM_HTML_MODE_COMPACT)
            tvTranslation.text = cleanTranslation
        }
    }

    override fun getItemCount() = listDoa.size

    fun updateData(newList: List<Doa>) {
        listDoa = newList
        notifyDataSetChanged()
    }
}