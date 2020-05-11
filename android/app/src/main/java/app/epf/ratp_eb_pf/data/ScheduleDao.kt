package app.epf.ratp_eb_pf.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Schedules

@Dao
interface SchedulesDao {

    @Query("select * from schedules")
    suspend fun getSchedules() : MutableList<Schedules>

    @Insert
    suspend fun addSchedules(station: Schedules)

    @Query("delete from schedules where uuid = :uuid")
    suspend fun deleteSchedules(uuid: String)

    @Query("delete from schedules")
    suspend fun deleteSchedules()

    @Query("select * from schedules where uuid = :uuid")
    suspend fun getSchedules(uuid: String) : Schedules
}