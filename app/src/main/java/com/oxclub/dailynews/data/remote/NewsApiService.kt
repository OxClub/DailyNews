package com.oxclub.dailynews.data.remote
import com.oxclub.dailynews.data.remote.dto.NewsResponseDto
import com.oxclub.dailynews.data.remote.dto.TrendsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query
interface NewsApiService{
 @GET("v1/search") suspend fun search(@Query("apiKey")apiKey:String?,@Query("q")q:String?=null,@Query("country")country:String?="IN",@Query("lang")lang:String="en",@Query("strict_country")strict:Boolean?=true,@Query("date")date:String="24h",@Query("sort")sort:String="date",@Query("size")size:Int=50,@Query("offset")offset:Int=0):NewsResponseDto
 @GET("v1/trends") suspend fun trends(@Query("apiKey")apiKey:String?,@Query("country")country:String="IN",@Query("window")window:String="24h",@Query("sort")sort:String="publishers",@Query("min_publishers")min:Int=2,@Query("size")size:Int=50,@Query("offset")offset:Int=0,@Query("headlines")headlines:Int=3):TrendsResponseDto
}
