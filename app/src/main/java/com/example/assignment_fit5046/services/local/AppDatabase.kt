package com.example.assignment_fit5046.services.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.assignment_fit5046.datamodels.Application
import com.example.assignment_fit5046.datamodels.Drive
import com.example.assignment_fit5046.datamodels.PendingAlarm
import com.example.assignment_fit5046.datamodels.User
import com.example.assignment_fit5046.services.local.dao.ApplicationDao
import com.example.assignment_fit5046.services.local.dao.DriveDao
import com.example.assignment_fit5046.services.local.dao.PendingAlarmDao
import com.example.assignment_fit5046.services.local.dao.UserDao

@Database(entities = [User::class, Drive::class, Application::class, PendingAlarm::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun driveDao(): DriveDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun pendingAlarmDao(): PendingAlarmDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE drives ADD COLUMN startTime TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE drives ADD COLUMN endTime TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN fcmToken TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS pending_alarms (" +
                        "alarmId TEXT NOT NULL PRIMARY KEY, " +
                        "applicationId TEXT NOT NULL, " +
                        "driveId TEXT NOT NULL, " +
                        "driveName TEXT NOT NULL, " +
                        "recipientUid TEXT NOT NULL, " +
                        "recipientRole TEXT NOT NULL, " +
                        "triggerTimeMs INTEGER NOT NULL, " +
                        "type TEXT NOT NULL" +
                        ")"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN ngoMetadata TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN ngoAddress TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "volunteerlink_db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
