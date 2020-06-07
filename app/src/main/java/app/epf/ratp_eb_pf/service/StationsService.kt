package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON des stations d'une ligne en particulier

interface StationsService {

    @GET("stations/{type}/{code}")
    suspend fun getStationsService(
        @Path("type") type: String, // Type de ligne. Ex : metro
        @Path("code") code: String // Code de la ligne. Ex : 1 pour metro 1
    ): GetStationsResult
}

data class GetStationsResult(
    val result: ResultStations,
    val metadata: MetadataStations
)

data class MetadataStations(
    val call: String,
    val date: String,
    val version: Long
)

data class ResultStations(
    val stations: List<Station>
)

data class Station(
    val name: String,
    val slug: String
)
