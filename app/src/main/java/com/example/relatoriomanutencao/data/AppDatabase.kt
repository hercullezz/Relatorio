package com.example.relatoriomanutencao.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_items ORDER BY date DESC")
    fun getAllMaintenanceItems(): Flow<List<MaintenanceItem>>

    @Insert
    suspend fun insertMaintenanceItem(item: MaintenanceItem): Long
    
    @Query("DELETE FROM maintenance_items WHERE id = :id")
    suspend fun deleteMaintenanceItem(id: Long)
}

@Dao
interface StockDao {
    // Busca paginada/limitada para evitar OOM
    @Query("SELECT * FROM stock_items ORDER BY code ASC LIMIT 100")
    fun getInitialStockItems(): Flow<List<StockItem>>
    
    // Busca OTIMIZADA direto no banco
    @Query("SELECT * FROM stock_items WHERE code LIKE '%' || :query || '%' ORDER BY code ASC LIMIT 100")
    fun searchStockItemsByCode(query: String): Flow<List<StockItem>>
    
    @Query("SELECT * FROM stock_items WHERE description LIKE '%' || :query || '%' ORDER BY description ASC LIMIT 100")
    fun searchStockItemsByDescription(query: String): Flow<List<StockItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockItem(item: StockItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllStockItems(items: List<StockItem>)

    @Query("DELETE FROM stock_items")
    suspend fun clearAll()
}

@Dao
interface ProductionLineDao {
    @Query("SELECT * FROM production_lines ORDER BY name ASC")
    fun getAllProductionLines(): Flow<List<ProductionLine>>

    @Insert
    suspend fun insertProductionLine(productionLine: ProductionLine): Long

    @Delete
    suspend fun deleteProductionLine(productionLine: ProductionLine)
}

@Dao
interface MachineDao {
    @Query("SELECT * FROM machines ORDER BY name ASC")
    fun getAllMachines(): Flow<List<Machine>>

    @Query("SELECT * FROM machines WHERE lineId = :lineId ORDER BY name ASC")
    fun getMachinesByLineId(lineId: Long): Flow<List<Machine>>
    
    @Query("SELECT * FROM machines WHERE lineId IS NULL ORDER BY name ASC")
    fun getMachinesWithoutLine(): Flow<List<Machine>>

    @Insert
    suspend fun insertMachine(machine: Machine): Long

    @Delete
    suspend fun deleteMachine(machine: Machine)
}

@Database(entities = [MaintenanceItem::class, StockItem::class, ProductionLine::class, Machine::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun stockDao(): StockDao
    abstract fun productionLineDao(): ProductionLineDao
    abstract fun machineDao(): MachineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .fallbackToDestructiveMigration() // Handles version increment by clearing DB
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
