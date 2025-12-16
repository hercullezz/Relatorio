package com.example.relatoriomanutencao.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_items")
data class MaintenanceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val machine: String = "",
    val serviceType: String = "",
    val description: String = "",
    val date: Long = 0,
    val photoUris: String = "" 
)

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String = "",
    val description: String = "",
    val address: String = "",
    val quantity: Int = 0
)

@Entity(tableName = "production_lines")
data class ProductionLine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = ""
)

@Entity(
    tableName = "machines",
    foreignKeys = [
        ForeignKey(
            entity = ProductionLine::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Machine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val lineId: Long? = null
)
