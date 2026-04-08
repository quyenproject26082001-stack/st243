package com.tempttt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tempttt.data.local.dao.UserDao
import com.tempttt.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}