package com.teniaTantoQueDarte.vuelingapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.teniaTantoQueDarte.vuelingapp.data.dao.FlightDao
import com.teniaTantoQueDarte.vuelingapp.data.dao.UserDao
import com.teniaTantoQueDarte.vuelingapp.data.model.User
import com.teniaTantoQueDarte.vuelingapp.data.model.FlightModel

@Database(entities = [User::class, FlightModel::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun flightDao(): FlightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vueling_database"
                )
                    .fallbackToDestructiveMigration() // Para manejar el cambio de versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}