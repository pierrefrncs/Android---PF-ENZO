package app.epf.ratp_eb_pf.service

import app.epf.ratp_eb_pf.model.Ligne
import app.epf.ratp_eb_pf.model.TransportFrancilien
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

public interface RequestAPI {

    @GET("/lines")
    suspend fun getLines() : Response<List<TransportFrancilien>>

    @GET("/lines/{type}")
    suspend fun getLinesByType(@Path("type") type: String) : Response<List<Ligne>>

    @GET("/lines/{type}/{code}")
    suspend fun getLinesByTypeAndCode(@Path("type") type: String,
                                      @Path("code") code: String) : Response<Ligne>

    @GET("/schedules/{type}/{code}/{station}/{way}")
    suspend fun getSchedules(@Path("type") type: String,
                             @Path("code") code: String,
                             @Path("station") station: String,
                             @Path("way") way: String)

    @GET("/stations/{type}/{code}")
    suspend fun getStations(@Path("type") type: String,
                             @Path("code") code: String)
}