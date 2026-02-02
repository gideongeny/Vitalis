package com.gideongeng.Vitalis.TodoDatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 3, exportSchema = false)
abstract class TodoDatabase:RoomDatabase() {
    abstract fun todoDao():TodoDao
}
