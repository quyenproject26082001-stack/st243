package com.avatar.maker.celebrity.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.avatar.maker.celebrity.data.local.dao.UserDao
import com.avatar.maker.celebrity.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}