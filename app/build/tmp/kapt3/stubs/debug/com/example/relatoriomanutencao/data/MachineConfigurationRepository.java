package com.example.relatoriomanutencao.data;

import android.util.Log;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\rH\u0086@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u0011H\u0086@\u00a2\u0006\u0002\u0010\u001bJ\u001a\u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b2\u0006\u0010\u001d\u001a\u00020\u001eJ \u0010\u001f\u001a\u00020\u00162\u0006\u0010 \u001a\u00020\b2\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0086@\u00a2\u0006\u0002\u0010!J\u0016\u0010\"\u001a\u00020\u00162\u0006\u0010 \u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010#J\u000e\u0010$\u001a\u00020\u0016H\u0086@\u00a2\u0006\u0002\u0010%R\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u001d\u0010\n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u000fR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\f0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcom/example/relatoriomanutencao/data/MachineConfigurationRepository;", "", "productionLineDao", "Lcom/example/relatoriomanutencao/data/ProductionLineDao;", "machineDao", "Lcom/example/relatoriomanutencao/data/MachineDao;", "(Lcom/example/relatoriomanutencao/data/ProductionLineDao;Lcom/example/relatoriomanutencao/data/MachineDao;)V", "CLASS_LINE", "", "CLASS_MACHINE", "allMachines", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/relatoriomanutencao/data/Machine;", "getAllMachines", "()Lkotlinx/coroutines/flow/Flow;", "allProductionLines", "Lcom/example/relatoriomanutencao/data/ProductionLine;", "getAllProductionLines", "machinesWithoutLine", "getMachinesWithoutLine", "deleteMachine", "", "machine", "(Lcom/example/relatoriomanutencao/data/Machine;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteProductionLine", "productionLine", "(Lcom/example/relatoriomanutencao/data/ProductionLine;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getMachinesByLineId", "lineId", "", "insertMachine", "name", "(Ljava/lang/String;Ljava/lang/Long;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertProductionLine", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncFromCloud", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class MachineConfigurationRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.example.relatoriomanutencao.data.ProductionLineDao productionLineDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.relatoriomanutencao.data.MachineDao machineDao = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String CLASS_LINE = "ProductionLine";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String CLASS_MACHINE = "Machine";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.ProductionLine>> allProductionLines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> allMachines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> machinesWithoutLine = null;
    
    public MachineConfigurationRepository(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLineDao productionLineDao, @org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MachineDao machineDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.ProductionLine>> getAllProductionLines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getAllMachines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesWithoutLine() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesByLineId(long lineId) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object syncFromCloud(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertProductionLine(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteProductionLine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLine productionLine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object insertMachine(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.Long lineId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteMachine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.Machine machine, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}