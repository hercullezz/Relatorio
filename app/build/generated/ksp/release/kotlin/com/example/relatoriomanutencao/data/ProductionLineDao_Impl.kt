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
public class ProductionLineDao_Impl(
  __db: RoomDatabase,
) : ProductionLineDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfProductionLine: EntityInsertAdapter<ProductionLine>

  private val __deleteAdapterOfProductionLine: EntityDeleteOrUpdateAdapter<ProductionLine>
  init {
    this.__db = __db
    this.__insertAdapterOfProductionLine = object : EntityInsertAdapter<ProductionLine>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `production_lines` (`id`,`name`) VALUES (nullif(?, 0),?)"

      protected override fun bind(statement: SQLiteStatement, entity: ProductionLine) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
      }
    }
    this.__deleteAdapterOfProductionLine = object : EntityDeleteOrUpdateAdapter<ProductionLine>() {
      protected override fun createQuery(): String = "DELETE FROM `production_lines` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ProductionLine) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertProductionLine(productionLine: ProductionLine): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfProductionLine.insertAndReturnId(_connection, productionLine)
    _result
  }

  public override suspend fun deleteProductionLine(productionLine: ProductionLine): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfProductionLine.handle(_connection, productionLine)
  }

  public override fun getAllProductionLines(): Flow<List<ProductionLine>> {
    val _sql: String = "SELECT * FROM production_lines ORDER BY name ASC"
    return createFlow(__db, false, arrayOf("production_lines")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _result: MutableList<ProductionLine> = mutableListOf()
        while (_stmt.step()) {
          val _item: ProductionLine
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          _item = ProductionLine(_tmpId,_tmpName)
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
