package com.apnakhaata.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_rules")
data class CategoryRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    val category: String
)
