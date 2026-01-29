package com.example.relatoriomanutencao.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
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
  init {
    this.__db = __db
    this.__insertAdapterOfMaintenanceItem = object : EntityInsertAdapter<MaintenanceItem>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `maintenance_items` (`id`,`machine`,`serviceType`,`description`,`date`,`photoUris`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MaintenanceItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.machine)
        statement.bindText(3, entity.serviceType)
        statement.bindText(4, entity.description)
        statement.bindLong(5, entity.date)
        statement.bindText(6, entity.photoUris)
      }
    }
  }

  public override suspend fun insertMaintenanceItem(item: MaintenanceItem): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMaintenanceItem.insertAndReturnId(_connection, item)
    _result
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
          _item = MaintenanceItem(_tmpId,_tmpMachine,_tmpServiceType,_tmpDescription,_tmpDate,_tmpPhotoUris)
          _result.add(_item)
        }
        _result
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
