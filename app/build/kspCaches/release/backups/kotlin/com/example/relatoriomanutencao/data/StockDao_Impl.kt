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
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class StockDao_Impl(
  __db: RoomDatabase,
) : StockDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfStockItem: EntityInsertAdapter<StockItem>
  init {
    this.__db = __db
    this.__insertAdapterOfStockItem = object : EntityInsertAdapter<StockItem>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `stock_items` (`id`,`code`,`description`,`address`,`quantity`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: StockItem) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.code)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.address)
        statement.bindLong(5, entity.quantity.toLong())
      }
    }
  }

  public override suspend fun insertStockItem(item: StockItem): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfStockItem.insert(_connection, item)
  }

  public override suspend fun insertAllStockItems(items: List<StockItem>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfStockItem.insert(_connection, items)
  }

  public override fun getInitialStockItems(): Flow<List<StockItem>> {
    val _sql: String = "SELECT * FROM stock_items ORDER BY code ASC LIMIT 100"
    return createFlow(__db, false, arrayOf("stock_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCode: String
          _tmpCode = _stmt.getText(_columnIndexOfCode)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          _item = StockItem(_tmpId,_tmpCode,_tmpDescription,_tmpAddress,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun searchStockItemsByCode(query: String): Flow<List<StockItem>> {
    val _sql: String = "SELECT * FROM stock_items WHERE code LIKE '%' || ? || '%' ORDER BY code ASC LIMIT 100"
    return createFlow(__db, false, arrayOf("stock_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCode: String
          _tmpCode = _stmt.getText(_columnIndexOfCode)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          _item = StockItem(_tmpId,_tmpCode,_tmpDescription,_tmpAddress,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun searchStockItemsByDescription(query: String): Flow<List<StockItem>> {
    val _sql: String = "SELECT * FROM stock_items WHERE description LIKE '%' || ? || '%' ORDER BY description ASC LIMIT 100"
    return createFlow(__db, false, arrayOf("stock_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, query)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCode: Int = getColumnIndexOrThrow(_stmt, "code")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfQuantity: Int = getColumnIndexOrThrow(_stmt, "quantity")
        val _result: MutableList<StockItem> = mutableListOf()
        while (_stmt.step()) {
          val _item: StockItem
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpCode: String
          _tmpCode = _stmt.getText(_columnIndexOfCode)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpQuantity: Int
          _tmpQuantity = _stmt.getLong(_columnIndexOfQuantity).toInt()
          _item = StockItem(_tmpId,_tmpCode,_tmpDescription,_tmpAddress,_tmpQuantity)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM stock_items"
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
