package com.example.relatoriomanutencao.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MaintenanceDao_Impl(
  __db: RoomDatabase,
) : MaintenanceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMaintenanceItem: EntityInsertAdapter<MaintenanceItem>

  private val __updateAdapterOfMaintenanceItem: EntityDeleteOrUpdateAdapter<MaintenanceItem>
  init {
    this.__db = __db
    this.__insertAdapterOfMaintenanceItem = object : EntityInsertAdapter<MaintenanceItem>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `maintenance_items` (`id`,`machine`,`serviceType`,`description`,`date`,`photoUris`,`isSynced`,`shiftId`,`workDateMillisFromServer`,`objectId`,`isPendingUpdate`,`isPendingDeletion`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MaintenanceItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.machine)
        statement.bindText(3, entity.serviceType)
        statement.bindText(4, entity.description)
        statement.bindLong(5, entity.date)
        statement.bindText(6, entity.photoUris)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmpShiftId: Int? = entity.shiftId
        if (_tmpShiftId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpShiftId.toLong())
        }
        val _tmpWorkDateMillisFromServer: Long? = entity.workDateMillisFromServer
        if (_tmpWorkDateMillisFromServer == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpWorkDateMillisFromServer)
        }
        val _tmpObjectId: String? = entity.objectId
        if (_tmpObjectId == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpObjectId)
        }
        val _tmp_1: Int = if (entity.isPendingUpdate) 1 else 0
        statement.bindLong(11, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isPendingDeletion) 1 else 0
        statement.bindLong(12, _tmp_2.toLong())
      }
    }
    this.__updateAdapterOfMaintenanceItem = object : EntityDeleteOrUpdateAdapter<MaintenanceItem>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `maintenance_items` SET `id` = ?,`machine` = ?,`serviceType` = ?,`description` = ?,`date` = ?,`photoUris` = ?,`isSynced` = ?,`shiftId` = ?,`workDateMillisFromServer` = ?,`objectId` = ?,`isPendingUpdate` = ?,`isPendingDeletion` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: MaintenanceItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.machine)
        statement.bindText(3, entity.serviceType)
        statement.bindText(4, entity.description)
        statement.bindLong(5, entity.date)
        statement.bindText(6, entity.photoUris)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmpShiftId: Int? = entity.shiftId
        if (_tmpShiftId == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpShiftId.toLong())
        }
        val _tmpWorkDateMillisFromServer: Long? = entity.workDateMillisFromServer
        if (_tmpWorkDateMillisFromServer == null) {
          statement.bindNull(9)
        } else {
          statement.bindLong(9, _tmpWorkDateMillisFromServer)
        }
        val _tmpObjectId: String? = entity.objectId
        if (_tmpObjectId == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpObjectId)
        }
        val _tmp_1: Int = if (entity.isPendingUpdate) 1 else 0
        statement.bindLong(11, _tmp_1.toLong())
        val _tmp_2: Int = if (entity.isPendingDeletion) 1 else 0
        statement.bindLong(12, _tmp_2.toLong())
        statement.bindLong(13, entity.id)
      }
    }
  }

  public override suspend fun insertMaintenanceItem(item: MaintenanceItem): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMaintenanceItem.insertAndReturnId(_connection, item)
    _result
  }

  public override suspend fun updateMaintenanceItem(item: MaintenanceItem): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfMaintenanceItem.handle(_connection, item)
  }

  public override fun getAllMaintenanceItems(): Flow<List<MaintenanceItem>> {
    val _sql: String = "SELECT * FROM maintenance_items ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("maintenance_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMachine: Int = getColumnIndexOrThrow(_stmt, "machine")
        val _columnIndexOfServiceType: Int = getColumnIndexOrThrow(_stmt, "serviceType")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfPhotoUris: Int = getColumnIndexOrThrow(_stmt, "photoUris")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfShiftId: Int = getColumnIndexOrThrow(_stmt, "shiftId")
        val _columnIndexOfWorkDateMillisFromServer: Int = getColumnIndexOrThrow(_stmt, "workDateMillisFromServer")
        val _columnIndexOfObjectId: Int = getColumnIndexOrThrow(_stmt, "objectId")
        val _columnIndexOfIsPendingUpdate: Int = getColumnIndexOrThrow(_stmt, "isPendingUpdate")
        val _columnIndexOfIsPendingDeletion: Int = getColumnIndexOrThrow(_stmt, "isPendingDeletion")
        val _result: MutableList<MaintenanceItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: MaintenanceItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMachine: String
          _tmpMachine = _stmt.getText(_columnIndexOfMachine)
          val _tmpServiceType: String
          _tmpServiceType = _stmt.getText(_columnIndexOfServiceType)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpPhotoUris: String
          _tmpPhotoUris = _stmt.getText(_columnIndexOfPhotoUris)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          val _tmpShiftId: Int?
          if (_stmt.isNull(_columnIndexOfShiftId)) {
            _tmpShiftId = null
          } else {
            _tmpShiftId = _stmt.getLong(_columnIndexOfShiftId).toInt()
          }
          val _tmpWorkDateMillisFromServer: Long?
          if (_stmt.isNull(_columnIndexOfWorkDateMillisFromServer)) {
            _tmpWorkDateMillisFromServer = null
          } else {
            _tmpWorkDateMillisFromServer = _stmt.getLong(_columnIndexOfWorkDateMillisFromServer)
          }
          val _tmpObjectId: String?
          if (_stmt.isNull(_columnIndexOfObjectId)) {
            _tmpObjectId = null
          } else {
            _tmpObjectId = _stmt.getText(_columnIndexOfObjectId)
          }
          val _tmpIsPendingUpdate: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPendingUpdate).toInt()
          _tmpIsPendingUpdate = _tmp_1 != 0
          val _tmpIsPendingDeletion: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPendingDeletion).toInt()
          _tmpIsPendingDeletion = _tmp_2 != 0
          _item = MaintenanceItem(_tmpId,_tmpMachine,_tmpServiceType,_tmpDescription,_tmpDate,_tmpPhotoUris,_tmpIsSynced,_tmpShiftId,_tmpWorkDateMillisFromServer,_tmpObjectId,_tmpIsPendingUpdate,_tmpIsPendingDeletion)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPendingSyncItemsSync(): List<MaintenanceItem> {
    val _sql: String = "SELECT * FROM maintenance_items WHERE isSynced = 0 OR isPendingUpdate = 1 OR isPendingDeletion = 1 ORDER BY date DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMachine: Int = getColumnIndexOrThrow(_stmt, "machine")
        val _columnIndexOfServiceType: Int = getColumnIndexOrThrow(_stmt, "serviceType")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfPhotoUris: Int = getColumnIndexOrThrow(_stmt, "photoUris")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfShiftId: Int = getColumnIndexOrThrow(_stmt, "shiftId")
        val _columnIndexOfWorkDateMillisFromServer: Int = getColumnIndexOrThrow(_stmt, "workDateMillisFromServer")
        val _columnIndexOfObjectId: Int = getColumnIndexOrThrow(_stmt, "objectId")
        val _columnIndexOfIsPendingUpdate: Int = getColumnIndexOrThrow(_stmt, "isPendingUpdate")
        val _columnIndexOfIsPendingDeletion: Int = getColumnIndexOrThrow(_stmt, "isPendingDeletion")
        val _result: MutableList<MaintenanceItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: MaintenanceItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpMachine: String
          _tmpMachine = _stmt.getText(_columnIndexOfMachine)
          val _tmpServiceType: String
          _tmpServiceType = _stmt.getText(_columnIndexOfServiceType)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpDate: Long
          _tmpDate = _stmt.getLong(_columnIndexOfDate)
          val _tmpPhotoUris: String
          _tmpPhotoUris = _stmt.getText(_columnIndexOfPhotoUris)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          val _tmpShiftId: Int?
          if (_stmt.isNull(_columnIndexOfShiftId)) {
            _tmpShiftId = null
          } else {
            _tmpShiftId = _stmt.getLong(_columnIndexOfShiftId).toInt()
          }
          val _tmpWorkDateMillisFromServer: Long?
          if (_stmt.isNull(_columnIndexOfWorkDateMillisFromServer)) {
            _tmpWorkDateMillisFromServer = null
          } else {
            _tmpWorkDateMillisFromServer = _stmt.getLong(_columnIndexOfWorkDateMillisFromServer)
          }
          val _tmpObjectId: String?
          if (_stmt.isNull(_columnIndexOfObjectId)) {
            _tmpObjectId = null
          } else {
            _tmpObjectId = _stmt.getText(_columnIndexOfObjectId)
          }
          val _tmpIsPendingUpdate: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsPendingUpdate).toInt()
          _tmpIsPendingUpdate = _tmp_1 != 0
          val _tmpIsPendingDeletion: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsPendingDeletion).toInt()
          _tmpIsPendingDeletion = _tmp_2 != 0
          _item = MaintenanceItem(_tmpId,_tmpMachine,_tmpServiceType,_tmpDescription,_tmpDate,_tmpPhotoUris,_tmpIsSynced,_tmpShiftId,_tmpWorkDateMillisFromServer,_tmpObjectId,_tmpIsPendingUpdate,_tmpIsPendingDeletion)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markAsSynced(id: Long, objectId: String) {
    val _sql: String = "UPDATE maintenance_items SET isSynced = 1, isPendingUpdate = 0, isPendingDeletion = 0, objectId = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, objectId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteMaintenanceItem(id: Long) {
    val _sql: String = "DELETE FROM maintenance_items WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearSyncedMaintenanceItems() {
    val _sql: String = "DELETE FROM maintenance_items WHERE isSynced = 1 AND isPendingUpdate = 0 AND isPendingDeletion = 0"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
