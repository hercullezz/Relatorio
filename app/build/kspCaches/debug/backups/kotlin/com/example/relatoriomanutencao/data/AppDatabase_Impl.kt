package com.example.relatoriomanutencao.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _maintenanceDao: Lazy<MaintenanceDao> = lazy {
    MaintenanceDao_Impl(this)
  }

  private val _stockDao: Lazy<StockDao> = lazy {
    StockDao_Impl(this)
  }

  private val _productionLineDao: Lazy<ProductionLineDao> = lazy {
    ProductionLineDao_Impl(this)
  }

  private val _machineDao: Lazy<MachineDao> = lazy {
    MachineDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "2d696bf4d41269a288b105d2c1bb4112", "8c3e3c421819e28cd92e6033a065b3dc") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `machine` TEXT NOT NULL, `serviceType` TEXT NOT NULL, `description` TEXT NOT NULL, `date` INTEGER NOT NULL, `photoUris` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, `shiftId` INTEGER, `workDateMillisFromServer` INTEGER, `objectId` TEXT, `isPendingUpdate` INTEGER NOT NULL, `isPendingDeletion` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `stock_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `code` TEXT NOT NULL, `description` TEXT NOT NULL, `address` TEXT NOT NULL, `quantity` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `production_lines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `machines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `lineId` INTEGER, FOREIGN KEY(`lineId`) REFERENCES `production_lines`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2d696bf4d41269a288b105d2c1bb4112')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `maintenance_items`")
        connection.execSQL("DROP TABLE IF EXISTS `stock_items`")
        connection.execSQL("DROP TABLE IF EXISTS `production_lines`")
        connection.execSQL("DROP TABLE IF EXISTS `machines`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsMaintenanceItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMaintenanceItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("machine", TableInfo.Column("machine", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("serviceType", TableInfo.Column("serviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("date", TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("photoUris", TableInfo.Column("photoUris", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("shiftId", TableInfo.Column("shiftId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("workDateMillisFromServer", TableInfo.Column("workDateMillisFromServer", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("objectId", TableInfo.Column("objectId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("isPendingUpdate", TableInfo.Column("isPendingUpdate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMaintenanceItems.put("isPendingDeletion", TableInfo.Column("isPendingDeletion", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMaintenanceItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMaintenanceItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMaintenanceItems: TableInfo = TableInfo("maintenance_items", _columnsMaintenanceItems, _foreignKeysMaintenanceItems, _indicesMaintenanceItems)
        val _existingMaintenanceItems: TableInfo = read(connection, "maintenance_items")
        if (!_infoMaintenanceItems.equals(_existingMaintenanceItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |maintenance_items(com.example.relatoriomanutencao.data.MaintenanceItem).
              | Expected:
              |""".trimMargin() + _infoMaintenanceItems + """
              |
              | Found:
              |""".trimMargin() + _existingMaintenanceItems)
        }
        val _columnsStockItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsStockItems.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("code", TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("address", TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsStockItems.put("quantity", TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysStockItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesStockItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoStockItems: TableInfo = TableInfo("stock_items", _columnsStockItems, _foreignKeysStockItems, _indicesStockItems)
        val _existingStockItems: TableInfo = read(connection, "stock_items")
        if (!_infoStockItems.equals(_existingStockItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |stock_items(com.example.relatoriomanutencao.data.StockItem).
              | Expected:
              |""".trimMargin() + _infoStockItems + """
              |
              | Found:
              |""".trimMargin() + _existingStockItems)
        }
        val _columnsProductionLines: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsProductionLines.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsProductionLines.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysProductionLines: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesProductionLines: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoProductionLines: TableInfo = TableInfo("production_lines", _columnsProductionLines, _foreignKeysProductionLines, _indicesProductionLines)
        val _existingProductionLines: TableInfo = read(connection, "production_lines")
        if (!_infoProductionLines.equals(_existingProductionLines)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |production_lines(com.example.relatoriomanutencao.data.ProductionLine).
              | Expected:
              |""".trimMargin() + _infoProductionLines + """
              |
              | Found:
              |""".trimMargin() + _existingProductionLines)
        }
        val _columnsMachines: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMachines.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMachines.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMachines.put("lineId", TableInfo.Column("lineId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMachines: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMachines.add(TableInfo.ForeignKey("production_lines", "SET NULL", "NO ACTION", listOf("lineId"), listOf("id")))
        val _indicesMachines: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMachines: TableInfo = TableInfo("machines", _columnsMachines, _foreignKeysMachines, _indicesMachines)
        val _existingMachines: TableInfo = read(connection, "machines")
        if (!_infoMachines.equals(_existingMachines)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |machines(com.example.relatoriomanutencao.data.Machine).
              | Expected:
              |""".trimMargin() + _infoMachines + """
              |
              | Found:
              |""".trimMargin() + _existingMachines)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "maintenance_items", "stock_items", "production_lines", "machines")
  }

  public override fun clearAllTables() {
    super.performClear(true, "maintenance_items", "stock_items", "production_lines", "machines")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(MaintenanceDao::class, MaintenanceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(StockDao::class, StockDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(ProductionLineDao::class, ProductionLineDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MachineDao::class, MachineDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun maintenanceDao(): MaintenanceDao = _maintenanceDao.value

  public override fun stockDao(): StockDao = _stockDao.value

  public override fun productionLineDao(): ProductionLineDao = _productionLineDao.value

  public override fun machineDao(): MachineDao = _machineDao.value
}
