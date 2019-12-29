package com.marcohc.terminator.sample.data.di

import com.google.gson.GsonBuilder
import com.marcohc.terminator.core.koin.CoreModule
import com.marcohc.terminator.sample.data.api.VenueApi
import com.marcohc.terminator.sample.data.db.AppDatabase
import com.marcohc.terminator.sample.data.db.RoomDatabaseImpl
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.VenueRepository
import com.marcohc.terminator.sample.data.repositories.VenueRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DataModule : CoreModule {

    override val module = module {
        single { provideRetrofit() }
        single { get<Retrofit>().create(VenueApi::class.java) }
        single { AppDatabase.getAppDatabase(get()) }
        single { get<RoomDatabaseImpl>().venueDao() }
        single { ConnectionManager(androidApplication(), get()) }
        single<VenueRepository> {
            VenueRepositoryImpl(
                api = get(),
                dao = get(),
                scheduler = get()
            )
        }
    }

    private fun provideRetrofit(): Retrofit {

        val okHttpClientBuilder = OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIME_OUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(READ_TIME_OUT.toLong(), TimeUnit.SECONDS)

        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClientBuilder.addInterceptor(httpLoggingInterceptor)

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            .create()

        val builder = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClientBuilder.build())

        return builder.build()
    }

    private const val CONNECTION_TIME_OUT = 30
    private const val WRITE_TIME_OUT = 30
    private const val READ_TIME_OUT = 30
    private const val BASE_URL = "https://api.foursquare.com/v2/"
}
