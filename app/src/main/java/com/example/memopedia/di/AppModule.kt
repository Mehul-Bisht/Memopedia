package com.example.memopedia.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.memopedia.di.AppModule.AppModule.datastore
import com.example.memopedia.network.ApiService
import com.example.memopedia.onboarding.data.Repository
import com.example.memopedia.onboarding.datastore.DataStorePreferenceStorage
import com.example.memopedia.onboarding.datastore.DataStorePreferenceStorage.Companion.PREFS_NAME
import com.example.memopedia.onboarding.datastore.PreferenceStorage
import com.example.memopedia.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {

        return PreferenceDataStoreFactory.create(
            migrations = listOf(
                SharedPreferencesMigration(context, PREFS_NAME)
            ),
            scope = CoroutineScope(Dispatchers.Default)
        ) {
            context.preferencesDataStoreFile(PREFS_NAME)
        }
    }

    @InstallIn(SingletonComponent::class)
    @Module
    object AppModule {

        val Context.datastore by preferencesDataStore(
            name = DataStorePreferenceStorage.PREFS_NAME,
            produceMigrations = { context ->
                listOf(
                    SharedPreferencesMigration(
                        context,
                        DataStorePreferenceStorage.PREFS_NAME
                    )
                )
            }
        )
    }

    @Singleton
    @Provides
    fun providePreferencesStorage(@ApplicationContext context: Context): PreferenceStorage =
        DataStorePreferenceStorage(context.datastore)

    @Singleton
    @Provides
    fun providesRepository(): Repository =
        Repository()

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideAPIService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}