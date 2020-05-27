
package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON du traffic d'une ligne en particulier

interface TrafficSpecService {

    @GET("traffic/{type}/{code}")
    suspend fun getTrafficSpecService(
        @Path("type") type: String, // Type de ligne. Ex :metro
        @Path("code") code: String // Code de la ligne. Ex: 1 pour metro 1
    ): GetTrafficSpecResult
}//

data class GetTrafficSpecResult(
    val result: SpecTraffic,
    val metadata: MetadataSpecTraffic
)

data class MetadataSpecTraffic(
    val call: String,
    val date: String,
    val version: Long
)


data class SpecTraffic(
    val line: String,
    val slug: String,
    val title: String,
    val message: String
)
