package com.bertonoon.movealert.db

import androidx.room.*
import com.bertonoon.movealert.Constants
import com.bertonoon.movealert.model.Move

@Dao
interface ConfirmationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(confirmationEntity: ConfirmationEntity)

    @Update
    suspend fun update(confirmationEntity: ConfirmationEntity)

    @Query("SELECT * FROM `${Constants.CONFIRMATION_TABLE}` ")
    fun fetch(): ConfirmationEntity

}