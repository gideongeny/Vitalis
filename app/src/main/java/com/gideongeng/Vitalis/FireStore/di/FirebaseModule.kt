package com.gideongeng.Vitalis.FireStore.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseModule {
    var userDitails: DocumentReference =  FirebaseFirestore.getInstance().collection("user").document(
        FirebaseAuth.getInstance().currentUser!!.uid)
    @Provides
    @Singleton
    fun provideFireStoreInstance(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("user")
    }
}
