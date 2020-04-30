package app.epf.ratp_eb_pf.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "lines")
data class Line(
        @PrimaryKey(autoGenerate = true) val id: Int,
        val name: String,
        val slug: String
) : Parcelable {
    companion object {
        val all = (1..20)
                .map {
                    Line(
                            it, "Nom$it", "Slug$it"
                    )
                }.toMutableList()
    }
}