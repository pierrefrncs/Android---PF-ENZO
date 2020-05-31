package app.epf.ratp_eb_pf

import android.content.Context
import android.os.AsyncTask
import androidx.room.Room
import app.epf.ratp_eb_pf.data.*
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.model.Traffic
import app.epf.ratp_eb_pf.service.LinesService
import app.epf.ratp_eb_pf.service.StationsService
import app.epf.ratp_eb_pf.service.TrafficService
import com.facebook.stetho.okhttp3.StethoInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

// Configure la bdd avec les stations
fun daoSta(context: Context): StationsDao {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "defaultDatabase").build()

    return database.getStationsDao()
}

// Configure la bdd avec les lignes
fun daoLi(context: Context): LineDao {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "defaultDatabase").build()

    return database.getLineDao()
}


// Configure la bdd avec les lignes
fun daoSch(context: Context): SchedulesDao {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "defaultDatabase").build()

    return database.getSchedulesDao()
}

// Configure la bdd avec les lignes
fun daoTraf(context: Context): TrafficDao {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "defaultDatabase").build()

    return database.getTrafficDao()
}

/**
 * Retourne l'accès à l'API Rest de la Ratp
 *
 */
fun retrofit(): Retrofit {
    val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Ajoute du temps en cas de ralentissement de l'API
    val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .addInterceptor(httpLoggingInterceptor)
        .addNetworkInterceptor(StethoInterceptor())
        .build()

    return Retrofit.Builder()
        .baseUrl("https://api-ratp.pierre-grimaud.fr/v4/")
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client)
        .build()
}