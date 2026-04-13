package com.tempttt.core.service
import com.tempttt.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST243_AvatarMakerCelebrities")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}