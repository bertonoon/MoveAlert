package com.bertonoon.movealert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bertonoon.movealert.Constants

@Entity(tableName = Constants.CONFIRMATION_TABLE)
data class ConfirmationEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 1,
    val confirmed: Boolean = false
) {

}