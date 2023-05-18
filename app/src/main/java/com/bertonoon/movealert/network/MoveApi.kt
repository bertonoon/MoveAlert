package com.bertonoon.movealert.network

import com.bertonoon.movealert.model.Move
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET

interface MoveApi {
    @GET("index.php")
    suspend fun getIsMove() : Response<Move>
}