package app.epf.ratp_eb_pf

import android.content.Context
import androidx.room.Room
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.data.TrafficDao
import com.facebook.stetho.okhttp3.StethoInterceptor
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

// Configure la bdd avec les traffics
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