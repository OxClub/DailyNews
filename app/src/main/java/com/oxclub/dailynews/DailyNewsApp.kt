package com.oxclub.dailynews
import android.app.Application
import androidx.room.Room
import com.oxclub.dailynews.data.local.NewsDatabase
import com.oxclub.dailynews.data.remote.NewsApiService
import com.oxclub.dailynews.data.repository.NewsRepositoryImpl
import com.oxclub.dailynews.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class DailyNewsApp:Application(){lateinit var repository:NewsRepositoryImpl;private set
 override fun onCreate(){super.onCreate();val db=Room.databaseBuilder(this,NewsDatabase::class.java,"daily_news.db").fallbackToDestructiveMigration().build();val api=Retrofit.Builder().baseUrl("https://freenewsapi.ai/").addConverterFactory(GsonConverterFactory.create()).build().create(NewsApiService::class.java);repository=NewsRepositoryImpl(api,db.articleDao(),BuildConfig.NEWS_API_KEY)}}
