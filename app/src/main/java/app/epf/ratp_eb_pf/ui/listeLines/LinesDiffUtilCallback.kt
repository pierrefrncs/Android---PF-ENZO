package app.epf.ratp_eb_pf.ui.listeLines

import androidx.recyclerview.widget.DiffUtil
import app.epf.ratp_eb_pf.model.Line

// Pour pouvoir filtrer les lignes de la recyclerView de la page d'accueil

class LinesDiffUtilCallback(
    private val oldList: MutableList<Line>,
    private val newList: MutableList<Line>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
}