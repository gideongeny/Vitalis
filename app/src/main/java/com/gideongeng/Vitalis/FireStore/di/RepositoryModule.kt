package com.gideongeng.Vitalis.FireStore.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.gideongeng.Vitalis.FireStore.Repository.Repository
import com.gideongeng.Vitalis.FireStore.Repository.RepositoryImp
import com.google.firebase.firestore.CollectionReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@RequiresApi(Build.VERSION_CODES.O)
@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(
        database: CollectionReference,
    ): Repository {
        return RepositoryImp(database)
    }
}
