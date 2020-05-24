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
    suspend fun getTraffics(): MutableList<Traffic>

    @Insert
    suspend fun addTraffic(traffic: Traffic)

    @Delete
    suspend fun deleteTraffic(traffic: Traffic)

    @Query("delete from traffic")
    suspend fun deleteTraffics()

    @Query("select * from traffic where id = :idTraffic")
    suspend fun getTraffic(idTraffic: Int): Traffic
}