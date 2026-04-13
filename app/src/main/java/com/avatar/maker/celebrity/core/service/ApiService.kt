package com.avatar.maker.celebrity.core.service
import com.avatar.maker.celebrity.data.model.PartAPI
import retrofit2.Response
import retrofit2.http.GET
interface ApiService {
    @GET("api/app/ST243_AvatarMakerCelebrities")
    suspend fun getAllData(): Response<Map<String, List<PartAPI>>>
}