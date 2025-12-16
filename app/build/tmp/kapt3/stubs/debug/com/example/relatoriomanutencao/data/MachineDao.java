package com.example.relatoriomanutencao.data;

import android.content.Context;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u001c\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\b2\u0006\u0010\u000b\u001a\u00020\fH\'J\u0014\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u0016\u0010\u000e\u001a\u00020\f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00f8\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001\u00a8\u0006\u000f\u00c0\u0006\u0001"}, d2 = {"Lcom/example/relatoriomanutencao/data/MachineDao;", "", "deleteMachine", "", "machine", "Lcom/example/relatoriomanutencao/data/Machine;", "(Lcom/example/relatoriomanutencao/data/Machine;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMachines", "Lkotlinx/coroutines/flow/Flow;", "", "getMachinesByLineId", "lineId", "", "getMachinesWithoutLine", "insertMachine", "app_debug"})
@androidx.room.Dao()
public abstract interface MachineDao {
    
    @androidx.room.Query(value = "SELECT * FROM machines ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getAllMachines();
    
    @androidx.room.Query(value = "SELECT * FROM machines WHERE lineId = :lineId ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesByLineId(long lineId);
    
    @androidx.room.Query(value = "SELECT * FROM machines WHERE lineId IS NULL ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesWithoutLine();
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertMachine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.Machine machine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteMachine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.Machine machine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}