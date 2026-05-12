package com.example.assignment_fit5046.services.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.services.local.dao.ApplicationDao
import com.example.assignment_fit5046.services.local.dao.DriveDao
import com.example.assignment_fit5046.services.local.dao.UserDao

@Database(entities = [User::class, Drive::class, Application::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun driveDao(): DriveDao
    abstract fun applicationDao(): ApplicationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volunteerlink_db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
