package com.example.relatoriomanutencao.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
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
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MachineDao_Impl(
  __db: RoomDatabase,
) : MachineDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMachine: EntityInsertAdapter<Machine>

  private val __deleteAdapterOfMachine: EntityDeleteOrUpdateAdapter<Machine>
  init {
    this.__db = __db
    this.__insertAdapterOfMachine = object : EntityInsertAdapter<Machine>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `machines` (`id`,`name`,`lineId`) VALUES (nullif(?, 0),?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Machine) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        val _tmpLineId: Long? = entity.lineId
        if (_tmpLineId == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpLineId)
        }
      }
    }
    this.__deleteAdapterOfMachine = object : EntityDeleteOrUpdateAdapter<Machine>() {
      protected override fun createQuery(): String = "DELETE FROM `machines` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Machine) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertMachine(machine: Machine): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfMachine.insertAndReturnId(_connection, machine)
    _result
  }

  public override suspend fun deleteMachine(machine: Machine): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfMachine.handle(_connection, machine)
  }

  public override fun getAllMachines(): Flow<List<Machine>> {
    val _sql: String = "SELECT * FROM machines ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("machines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLineId: Int = getColumnIndexOrThrow(_stmt, "lineId")
        val _result: MutableList<Machine> = mutableListOf()
        while (_stmt.step()) {
          val _item: Machine
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLineId: Long?
          if (_stmt.isNull(_columnIndexOfLineId)) {
            _tmpLineId = null
          } else {
            _tmpLineId = _stmt.getLong(_columnIndexOfLineId)
          }
          _item = Machine(_tmpId,_tmpName,_tmpLineId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMachinesByLineId(lineId: Long): Flow<List<Machine>> {
    val _sql: String = "SELECT * FROM machines WHERE lineId = ? ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("machines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, lineId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLineId: Int = getColumnIndexOrThrow(_stmt, "lineId")
        val _result: MutableList<Machine> = mutableListOf()
        while (_stmt.step()) {
          val _item: Machine
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLineId: Long?
          if (_stmt.isNull(_columnIndexOfLineId)) {
            _tmpLineId = null
          } else {
            _tmpLineId = _stmt.getLong(_columnIndexOfLineId)
          }
          _item = Machine(_tmpId,_tmpName,_tmpLineId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getMachinesWithoutLine(): Flow<List<Machine>> {
    val _sql: String = "SELECT * FROM machines WHERE lineId IS NULL ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("machines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfLineId: Int = getColumnIndexOrThrow(_stmt, "lineId")
        val _result: MutableList<Machine> = mutableListOf()
        while (_stmt.step()) {
          val _item: Machine
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpLineId: Long?
          if (_stmt.isNull(_columnIndexOfLineId)) {
            _tmpLineId = null
          } else {
            _tmpLineId = _stmt.getLong(_columnIndexOfLineId)
          }
          _item = Machine(_tmpId,_tmpName,_tmpLineId)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
