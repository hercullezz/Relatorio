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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0000\n\u0002\u0010\t\n\u0000\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u0016\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00f8\u0001\u0000\u0082\u0002\u0006\n\u0004\b!0\u0001\u00a8\u0006\f\u00c0\u0006\u0001"}, d2 = {"Lcom/example/relatoriomanutencao/data/ProductionLineDao;", "", "deleteProductionLine", "", "productionLine", "Lcom/example/relatoriomanutencao/data/ProductionLine;", "(Lcom/example/relatoriomanutencao/data/ProductionLine;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllProductionLines", "Lkotlinx/coroutines/flow/Flow;", "", "insertProductionLine", "", "app_debug"})
@androidx.room.Dao()
public abstract interface ProductionLineDao {
    
    @androidx.room.Query(value = "SELECT * FROM production_lines ORDER BY name ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.ProductionLine>> getAllProductionLines();
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertProductionLine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLine productionLine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteProductionLine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLine productionLine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}