package com.example.dailynews

import android.app.Application
import androidx.room.Room
import com.example.dailynews.data.local.NewsDatabase
import com.example.dailynews.data.remote.NewsApiService
import com.example.dailynews.data.repository.NewsRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DailyNewsApp : Application() {
    lateinit var repository: NewsRepositoryImpl
        private set

    override fun onCreate() {
        super.onCreate()

        val database = Room.databaseBuilder(
            this,
            NewsDatabase::class.java,
            "daily_news.db"
        ).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(NewsApiService::class.java)
        repository = NewsRepositoryImpl(api, database.articleDao())
    }
}
