package app.epf.ratp_eb_pf.data

import androidx.room.Database
import androidx.room.RoomDatabase
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.model.Traffic

// Gestion Database

@Database(entities = [Stations::class, Line::class, Traffic::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getStationsDao() : StationsDao

    abstract fun getLineDao(): LineDao

    abstract fun getTrafficDao(): TrafficDao
}