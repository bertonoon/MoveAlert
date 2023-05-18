package com.bertonoon.movealert.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bertonoon.movealert.Constants
import com.bertonoon.movealert.model.Move


@Database(
    entities = [
        ConfirmationEntity::class
    ],
    version = 3
)
abstract class DatabaseMove : RoomDatabase() {

    abstract fun confirmationDao(): ConfirmationDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseMove? = null

        fun getInstance(context: Context): DatabaseMove {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DatabaseMove::class.java,
                        Constants.DATABASE_NAME
                    ).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

    }


}