package com.demo.sensor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * This class is used to initialise the app database
 * we make sure there is only one instance of app db
 */
@Database(entities = [UserActivityLog::class], version = 1)
abstract class AppDatabase: RoomDatabase(){
    abstract fun userActivityLogDao(): UserActivityLogDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            val temp = INSTANCE
            if(temp != null) {
                return temp
            }
            // if no database initialised then create one
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}