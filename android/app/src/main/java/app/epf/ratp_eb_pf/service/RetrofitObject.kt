package app.epf.ratp_eb_pf.service

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitFactory {
    const val BASE_URL_RATP = "https://api-ratp.pierre-grimaud.fr/v4/"
    const val BASE_URL_LOGO = "https://api-ratp.pierre-grimaud.fr/v4/"

    fun retrofitRATP(): RequestAPI {
        return Retrofit.Builder()
            .baseUrl(BASE_URL_RATP)
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(RequestAPI::class.java)
    }

    fun retrofitLogoMetro(): RequestAPI{
        return Retrofit.Builder()
            .baseUrl(BASE_URL_RATP)
            .addConverterFactory(MoshiConverterFactory.create())
            .build().create(RequestAPI::class.java)
    }
}