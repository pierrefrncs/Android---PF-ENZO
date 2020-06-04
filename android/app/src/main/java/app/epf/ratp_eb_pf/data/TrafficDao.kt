package app.epf.ratp_eb_pf.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Traffic

// Dao du traffic

@Dao
interface TrafficDao {

    @Query("select * from traffic")
    suspend fun getTraffic(): MutableList<Traffic>

    @Insert
    suspend fun addTraffic(traffic: Traffic)

    @Delete
    suspend fun deleteTraffic(traffic: Traffic)

    @Query("delete from traffic")
    suspend fun deleteTraffics()

    @Query("select * from traffic where line = :codeLine")
    suspend fun getTraffic(codeLine: String): Traffic

    @Query("update traffic set title = :title, message = :message where line = :codeLine")
    suspend fun updateTraffic(codeLine: String, title: String, message: String)
}