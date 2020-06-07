package app.epf.ratp_eb_pf

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.data.TrafficDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.model.Traffic
import app.epf.ratp_eb_pf.service.LinesService
import app.epf.ratp_eb_pf.service.StationsService
import app.epf.ratp_eb_pf.service.TrafficService
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import java.util.*

// Splash activity s'affichant au démarrage de l'application et permettant de précharger des données

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val load = UpdateAsync(this)
        load.execute()
    }

    companion object {
        // Pour charger les données et passer à la MainActivity quand c'est terminé
        private class UpdateAsync internal constructor(contextSplash: SplashActivity) :
            AsyncTask<Void, Void, Void>() {

            private val activityReference: WeakReference<SplashActivity> =
                WeakReference(contextSplash)
            private val context: Context = contextSplash
            private var stationsDao: StationsDao? = null
            private var stations: MutableList<Stations>? = null
            private var lineDao: LineDao? = null
            private var lines: MutableList<Line>? = null
            private var trafficDao: TrafficDao? = null
            private var list: MutableList<String> = mutableListOf()

            // Charge en background
            override fun doInBackground(vararg params: Void?): Void? {
                stationsDao = daoSta(context)
                lineDao = daoLi(context)
                trafficDao = daoTraf(context)

                runBlocking {
                    stations = stationsDao?.getStations()
                    lines = lineDao?.getLines()
                }


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

                            val line =
                                Line(id, it.code, it.name, it.directions, it.id.toInt(), false)
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
                            val service =
                                retrofit().create(StationsService::class.java) // Fonction retrofit d'ActivityUtils

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

                // Update l'état du traffic
                runBlocking {
                    trafficDao?.deleteTraffics()

                    var id = 1
                    val service =
                        retrofit().create(TrafficService::class.java) // Fonction retrofit d'ActivityUtils

                    // Obtient les stations de la line du type correspondant
                    val result = service.getTrafficService("metros")

                    result.result.metros.map {
                        val traffic = Traffic(
                            id,
                            it.line.toLowerCase(Locale.ROOT), it.slug, it.title, it.message
                        )
                        trafficDao?.addTraffic(traffic)  // Ajoute la traffic dans la bdd
                        id += 1
                    }
                }
                return null
            }

            // Une fois que toutes les données sont chargées, redirige vers MainActivity
            override fun onPostExecute(result: Void?) {
                val act = activityReference.get()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                act?.finish()
            }
        }
    }

}