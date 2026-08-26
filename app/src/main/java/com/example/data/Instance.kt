package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instances")
data class Instance(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val iconUri: String? = null,
    val osImageUri: String? = null,
    val ramMb: Int = 2048,
    val storageGb: Int = 16
)
