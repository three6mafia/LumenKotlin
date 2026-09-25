package com.falcofemoralis.lumen.data.api

import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface RezkaApiService {

    @GET
    suspend fun getPageHtml(
        @Url url: String,
        @QueryMap queries: Map<String, String> = emptyMap(),
        @Header("User-Agent") userAgent: String
    ): String

    @FormUrlEncoded
    @POST("/engine/ajax/get_newest_slider_content.php")
    suspend fun getSliderContent(
        @Field("id") id: String = "0",
        @Header("User-Agent") userAgent: String
    ): String

    @FormUrlEncoded
    @POST("/ajax/get_cdn_series/")
    suspend fun getCdnSeries(
        @FieldMap fields: Map<String, String>,
        @Header("User-Agent") userAgent: String,
        @Header("X-Hdrezka-Android-App") appHeader: String = "1",
        @Header("X-Hdrezka-Android-App-Version") appVersion: String = "2.2.1"
    ): String

    @GET("/search/")
    suspend fun search(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("do") doParam: String = "search",
        @Query("subaction") subaction: String = "search",
        @Header("User-Agent") userAgent: String
    ): String

    @FormUrlEncoded
    @POST("/engine/ajax/search.php")
    suspend fun searchSuggestions(
        @Field("q") query: String,
        @Header("User-Agent") userAgent: String
    ): String
}
