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

class UpdateAsync(): AsyncTask<Void, Void, Void>(){

    private var stationsDao: StationsDao? = null
    private var stations: MutableList<Stations>? = null

    private var lineDao: LineDao? = null
    private var lines: MutableList<Line>? = null

    private var trafficDao: TrafficDao? = null

    private var list: MutableList<String> = mutableListOf()

    override fun doInBackground(vararg params: Void?): Void? {

        // si la BDD des lines et stations est vide
        if (lines.isNullOrEmpty() || stations.isNullOrEmpty() || stations?.last()?.slug != "olympiades") {
            val service =
                retrofit().create(LinesService::class.java) // Fonction retrofit d'ActivityUtils
            runBlocking {
                lineDao?.deleteLines() // Supprime les anciennes lines
                val result =
                    service.getLinesService("metros") // Obtient les lines du type correspondant
                var id = 1 // Pour toujours avoir le premier id à 1
                result.result.metros.map {

                    val line = Line(id, it.code, it.name, it.directions, it.id.toInt(), false)
                    // Enlève les lines de metro inutiles
                    if (it.id != "79" && it.id != "455") {
                        list.add(it.code)
                        lineDao?.addLine(line)  // Ajoute la station dans la bdd
                        id += 1
                    }
                }
            }
        }

        if (stations.isNullOrEmpty() || stations?.last()?.slug != "olympiades") {
            runBlocking {
                stationsDao?.deleteStations() // Supprime les anciennes stations

                var id = 1 // Pour toujours avoir le premier id à 1
                // Boucle sur toutes les lines
                for (code: String in list) {
                    val service = retrofit().create(StationsService::class.java) // Fonction retrofit d'ActivityUtils

                    // Obtient les stations de la line du type correspondant
                    val result = service.getStationsService("metros", code)

                    result.result.stations.map {
                        val station = Stations(id, it.name, it.slug, code, false)
                        stationsDao?.addStation(station)  // Ajoute la station dans la bdd
                        id += 1
                    }
                }
            }
        }

        //update l'état du traffic
        runBlocking {
            trafficDao?.deleteTraffics()

            var id=1
            val service = retrofit().create(TrafficService::class.java) // Fonction retrofit d'ActivityUtils

            // Obtient les stations de la line du type correspondant
            val result = service.getTrafficService("metros")

            result.result.metros.map {
                val traffic = Traffic(id, it.line, it.slug, it.title, it.message)
                trafficDao?.addTraffic(traffic)  // Ajoute la traffic dans la bdd
                id += 1
            }
        }

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)

    }

}