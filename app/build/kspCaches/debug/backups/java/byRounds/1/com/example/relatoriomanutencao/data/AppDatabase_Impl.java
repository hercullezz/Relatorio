package com.example.relatoriomanutencao.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile MaintenanceDao _maintenanceDao;

  private volatile StockDao _stockDao;

  private volatile ProductionLineDao _productionLineDao;

  private volatile MachineDao _machineDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `maintenance_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `machine` TEXT NOT NULL, `serviceType` TEXT NOT NULL, `description` TEXT NOT NULL, `date` INTEGER NOT NULL, `photoUris` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `code` TEXT NOT NULL, `description` TEXT NOT NULL, `address` TEXT NOT NULL, `quantity` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `production_lines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `machines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `lineId` INTEGER, FOREIGN KEY(`lineId`) REFERENCES `production_lines`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a7d2d4d09ac04526b2d24e90029d6d42')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `maintenance_items`");
        db.execSQL("DROP TABLE IF EXISTS `stock_items`");
        db.execSQL("DROP TABLE IF EXISTS `production_lines`");
        db.execSQL("DROP TABLE IF EXISTS `machines`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMaintenanceItems = new HashMap<String, TableInfo.Column>(6);
        _columnsMaintenanceItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceItems.put("machine", new TableInfo.Column("machine", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceItems.put("serviceType", new TableInfo.Column("serviceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceItems.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceItems.put("date", new TableInfo.Column("date", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMaintenanceItems.put("photoUris", new TableInfo.Column("photoUris", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMaintenanceItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMaintenanceItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMaintenanceItems = new TableInfo("maintenance_items", _columnsMaintenanceItems, _foreignKeysMaintenanceItems, _indicesMaintenanceItems);
        final TableInfo _existingMaintenanceItems = TableInfo.read(db, "maintenance_items");
        if (!_infoMaintenanceItems.equals(_existingMaintenanceItems)) {
          return new RoomOpenHelper.ValidationResult(false, "maintenance_items(com.example.relatoriomanutencao.data.MaintenanceItem).\n"
                  + " Expected:\n" + _infoMaintenanceItems + "\n"
                  + " Found:\n" + _existingMaintenanceItems);
        }
        final HashMap<String, TableInfo.Column> _columnsStockItems = new HashMap<String, TableInfo.Column>(5);
        _columnsStockItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockItems.put("code", new TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockItems.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockItems.put("address", new TableInfo.Column("address", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStockItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStockItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStockItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoStockItems = new TableInfo("stock_items", _columnsStockItems, _foreignKeysStockItems, _indicesStockItems);
        final TableInfo _existingStockItems = TableInfo.read(db, "stock_items");
        if (!_infoStockItems.equals(_existingStockItems)) {
          return new RoomOpenHelper.ValidationResult(false, "stock_items(com.example.relatoriomanutencao.data.StockItem).\n"
                  + " Expected:\n" + _infoStockItems + "\n"
                  + " Found:\n" + _existingStockItems);
        }
        final HashMap<String, TableInfo.Column> _columnsProductionLines = new HashMap<String, TableInfo.Column>(2);
        _columnsProductionLines.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProductionLines.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProductionLines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProductionLines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProductionLines = new TableInfo("production_lines", _columnsProductionLines, _foreignKeysProductionLines, _indicesProductionLines);
        final TableInfo _existingProductionLines = TableInfo.read(db, "production_lines");
        if (!_infoProductionLines.equals(_existingProductionLines)) {
          return new RoomOpenHelper.ValidationResult(false, "production_lines(com.example.relatoriomanutencao.data.ProductionLine).\n"
                  + " Expected:\n" + _infoProductionLines + "\n"
                  + " Found:\n" + _existingProductionLines);
        }
        final HashMap<String, TableInfo.Column> _columnsMachines = new HashMap<String, TableInfo.Column>(3);
        _columnsMachines.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMachines.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMachines.put("lineId", new TableInfo.Column("lineId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMachines = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysMachines.add(new TableInfo.ForeignKey("production_lines", "SET NULL", "NO ACTION", Arrays.asList("lineId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesMachines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMachines = new TableInfo("machines", _columnsMachines, _foreignKeysMachines, _indicesMachines);
        final TableInfo _existingMachines = TableInfo.read(db, "machines");
        if (!_infoMachines.equals(_existingMachines)) {
          return new RoomOpenHelper.ValidationResult(false, "machines(com.example.relatoriomanutencao.data.Machine).\n"
                  + " Expected:\n" + _infoMachines + "\n"
                  + " Found:\n" + _existingMachines);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a7d2d4d09ac04526b2d24e90029d6d42", "5c0929ac1ed71bc56cad2f38950b6499");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "maintenance_items","stock_items","production_lines","machines");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `maintenance_items`");
      _db.execSQL("DELETE FROM `stock_items`");
      _db.execSQL("DELETE FROM `production_lines`");
      _db.execSQL("DELETE FROM `machines`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MaintenanceDao.class, MaintenanceDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StockDao.class, StockDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductionLineDao.class, ProductionLineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MachineDao.class, MachineDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MaintenanceDao maintenanceDao() {
    if (_maintenanceDao != null) {
      return _maintenanceDao;
    } else {
      synchronized(this) {
        if(_maintenanceDao == null) {
          _maintenanceDao = new MaintenanceDao_Impl(this);
        }
        return _maintenanceDao;
      }
    }
  }

  @Override
  public StockDao stockDao() {
    if (_stockDao != null) {
      return _stockDao;
    } else {
      synchronized(this) {
        if(_stockDao == null) {
          _stockDao = new StockDao_Impl(this);
        }
        return _stockDao;
      }
    }
  }

  @Override
  public ProductionLineDao productionLineDao() {
    if (_productionLineDao != null) {
      return _productionLineDao;
    } else {
      synchronized(this) {
        if(_productionLineDao == null) {
          _productionLineDao = new ProductionLineDao_Impl(this);
        }
        return _productionLineDao;
      }
    }
  }

  @Override
  public MachineDao machineDao() {
    if (_machineDao != null) {
      return _machineDao;
    } else {
      synchronized(this) {
        if(_machineDao == null) {
          _machineDao = new MachineDao_Impl(this);
        }
        return _machineDao;
      }
    }
  }
}
