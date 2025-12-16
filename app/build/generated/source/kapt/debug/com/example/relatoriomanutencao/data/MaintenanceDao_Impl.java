package com.example.relatoriomanutencao.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MaintenanceDao_Impl implements MaintenanceDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MaintenanceItem> __insertionAdapterOfMaintenanceItem;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMaintenanceItem;

  public MaintenanceDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMaintenanceItem = new EntityInsertionAdapter<MaintenanceItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `maintenance_items` (`id`,`machine`,`serviceType`,`description`,`date`,`photoUris`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final MaintenanceItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getMachine() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getMachine());
        }
        if (entity.getServiceType() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getServiceType());
        }
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        statement.bindLong(5, entity.getDate());
        if (entity.getPhotoUris() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPhotoUris());
        }
      }
    };
    this.__preparedStmtOfDeleteMaintenanceItem = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM maintenance_items WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMaintenanceItem(final MaintenanceItem item,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMaintenanceItem.insertAndReturnId(item);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMaintenanceItem(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMaintenanceItem.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteMaintenanceItem.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<MaintenanceItem>> getAllMaintenanceItems() {
    final String _sql = "SELECT * FROM maintenance_items ORDER BY date DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"maintenance_items"}, new Callable<List<MaintenanceItem>>() {
      @Override
      @NonNull
      public List<MaintenanceItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMachine = CursorUtil.getColumnIndexOrThrow(_cursor, "machine");
          final int _cursorIndexOfServiceType = CursorUtil.getColumnIndexOrThrow(_cursor, "serviceType");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfPhotoUris = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUris");
          final List<MaintenanceItem> _result = new ArrayList<MaintenanceItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MaintenanceItem _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMachine;
            if (_cursor.isNull(_cursorIndexOfMachine)) {
              _tmpMachine = null;
            } else {
              _tmpMachine = _cursor.getString(_cursorIndexOfMachine);
            }
            final String _tmpServiceType;
            if (_cursor.isNull(_cursorIndexOfServiceType)) {
              _tmpServiceType = null;
            } else {
              _tmpServiceType = _cursor.getString(_cursorIndexOfServiceType);
            }
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final long _tmpDate;
            _tmpDate = _cursor.getLong(_cursorIndexOfDate);
            final String _tmpPhotoUris;
            if (_cursor.isNull(_cursorIndexOfPhotoUris)) {
              _tmpPhotoUris = null;
            } else {
              _tmpPhotoUris = _cursor.getString(_cursorIndexOfPhotoUris);
            }
            _item = new MaintenanceItem(_tmpId,_tmpMachine,_tmpServiceType,_tmpDescription,_tmpDate,_tmpPhotoUris);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
