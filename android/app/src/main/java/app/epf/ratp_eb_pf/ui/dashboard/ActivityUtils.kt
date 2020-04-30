package app.epf.ratp_eb_pf.ui.dashboard

import android.content.Context
import androidx.room.Room
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import com.facebook.stetho.okhttp3.StethoInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun dao(context: Context): LineDao {
    val database = Room.databaseBuilder(context, AppDatabase::class.java, "gestionlines").build()

    return database.getLineDao()
}

/**
 * Retourne l'accès à l'API Rest
 *
 */
fun retrofit(): Retrofit {
    val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    val client = OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addNetworkInterceptor(StethoInterceptor())
            .build()

    return Retrofit.Builder()
            .baseUrl("https://api-ratp.pierre-grimaud.fr/v4/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

}