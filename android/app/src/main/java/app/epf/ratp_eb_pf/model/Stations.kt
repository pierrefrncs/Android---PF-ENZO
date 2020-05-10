package app.epf.ratp_eb_pf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "stations")
data class Stations(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val slug: String
) : Serializable