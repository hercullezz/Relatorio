package com.example.relatoriomanutencao.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
public final class MachineDao_Impl implements MachineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Machine> __insertionAdapterOfMachine;

  private final EntityDeletionOrUpdateAdapter<Machine> __deletionAdapterOfMachine;

  public MachineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMachine = new EntityInsertionAdapter<Machine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `machines` (`id`,`name`,`lineId`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Machine entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getLineId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindLong(3, entity.getLineId());
        }
      }
    };
    this.__deletionAdapterOfMachine = new EntityDeletionOrUpdateAdapter<Machine>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `machines` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Machine entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insertMachine(final Machine machine, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMachine.insertAndReturnId(machine);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMachine(final Machine machine, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMachine.handle(machine);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Machine>> getAllMachines() {
    final String _sql = "SELECT * FROM machines ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"machines"}, new Callable<List<Machine>>() {
      @Override
      @NonNull
      public List<Machine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLineId = CursorUtil.getColumnIndexOrThrow(_cursor, "lineId");
          final List<Machine> _result = new ArrayList<Machine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Machine _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final Long _tmpLineId;
            if (_cursor.isNull(_cursorIndexOfLineId)) {
              _tmpLineId = null;
            } else {
              _tmpLineId = _cursor.getLong(_cursorIndexOfLineId);
            }
            _item = new Machine(_tmpId,_tmpName,_tmpLineId);
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

  @Override
  public Flow<List<Machine>> getMachinesByLineId(final long lineId) {
    final String _sql = "SELECT * FROM machines WHERE lineId = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, lineId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"machines"}, new Callable<List<Machine>>() {
      @Override
      @NonNull
      public List<Machine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLineId = CursorUtil.getColumnIndexOrThrow(_cursor, "lineId");
          final List<Machine> _result = new ArrayList<Machine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Machine _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final Long _tmpLineId;
            if (_cursor.isNull(_cursorIndexOfLineId)) {
              _tmpLineId = null;
            } else {
              _tmpLineId = _cursor.getLong(_cursorIndexOfLineId);
            }
            _item = new Machine(_tmpId,_tmpName,_tmpLineId);
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

  @Override
  public Flow<List<Machine>> getMachinesWithoutLine() {
    final String _sql = "SELECT * FROM machines WHERE lineId IS NULL ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"machines"}, new Callable<List<Machine>>() {
      @Override
      @NonNull
      public List<Machine> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfLineId = CursorUtil.getColumnIndexOrThrow(_cursor, "lineId");
          final List<Machine> _result = new ArrayList<Machine>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Machine _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final Long _tmpLineId;
            if (_cursor.isNull(_cursorIndexOfLineId)) {
              _tmpLineId = null;
            } else {
              _tmpLineId = _cursor.getLong(_cursorIndexOfLineId);
            }
            _item = new Machine(_tmpId,_tmpName,_tmpLineId);
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
