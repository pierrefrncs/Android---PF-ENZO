package app.epf.ratp_eb_pf.data

import androidx.room.Database
import androidx.room.RoomDatabase
import app.epf.ratp_eb_pf.model.Line

@Database(entities = arrayOf(Line::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getLineDao() : LineDao
}