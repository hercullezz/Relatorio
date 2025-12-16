package com.example.relatoriomanutencao.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.lifecycle.AndroidViewModel;
import com.example.relatoriomanutencao.data.AppDatabase;
import com.example.relatoriomanutencao.data.Machine;
import com.example.relatoriomanutencao.data.MachineConfigurationRepository;
import com.example.relatoriomanutencao.data.MaintenanceItem;
import com.example.relatoriomanutencao.data.ProductionLine;
import com.example.relatoriomanutencao.data.StockItem;
import com.example.relatoriomanutencao.utils.CsvImporter;
import com.parse.ParseCloud;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.FlowPreview;
import kotlinx.coroutines.flow.SharingStarted;
import kotlinx.coroutines.flow.StateFlow;
import java.io.ByteArrayOutputStream;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0094\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u000e\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001d\u0010(\u001a\u00020)2\u0006\u0010*\u001a\u00020\u000f2\b\u0010+\u001a\u0004\u0018\u00010,\u00a2\u0006\u0002\u0010-J&\u0010.\u001a\u00020)2\u0006\u0010/\u001a\u00020\u000f2\u0006\u00100\u001a\u00020\u000f2\u0006\u00101\u001a\u00020\u000f2\u0006\u00102\u001a\u00020\u000fJ\u000e\u00103\u001a\u00020)2\u0006\u0010*\u001a\u00020\u000fJ\u0006\u00104\u001a\u00020)J\u000e\u00105\u001a\u00020)2\u0006\u0010/\u001a\u00020\u0012J\u000e\u00106\u001a\u00020)2\u0006\u00107\u001a\u00020\rJ\u000e\u00108\u001a\u00020)2\u0006\u00109\u001a\u00020\u0016J\u0012\u0010:\u001a\u0004\u0018\u00010;2\u0006\u0010<\u001a\u00020\u000fH\u0002J\u001a\u0010=\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00070\u00112\u0006\u0010+\u001a\u00020,J\u000e\u0010>\u001a\u00020)2\u0006\u0010?\u001a\u00020@J\u000e\u0010A\u001a\u00020)2\u0006\u0010?\u001a\u00020@J\u0006\u0010B\u001a\u00020)J\u000e\u0010C\u001a\u00020)2\u0006\u0010D\u001a\u00020\u000fJH\u0010E\u001a:\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0G0Fj$\u0012 \u0012\u001e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0Gj\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f`I`H2\u0006\u00102\u001a\u00020\u000fH\u0002J\u0006\u0010J\u001a\u00020)J\u0016\u0010K\u001a\u00020)2\u0006\u0010D\u001a\u00020\u000fH\u0082@\u00a2\u0006\u0002\u0010LJ\u0006\u0010M\u001a\u00020)J\u0006\u0010N\u001a\u00020)J\u0006\u0010O\u001a\u00020)J\u001e\u0010P\u001a\u00020)2\u0006\u0010Q\u001a\u00020\r2\u0006\u0010R\u001a\u00020\u000f2\u0006\u0010S\u001a\u00020\u000fR\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u001d\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00160\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0014R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\n0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0014R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\n0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0014R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u001e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0014R\u001d\u0010 \u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\r0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0014R\u000e\u0010\"\u001a\u00020#X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0014R\u0017\u0010&\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0014\u00a8\u0006T"}, d2 = {"Lcom/example/relatoriomanutencao/viewmodel/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_cloudStockItems", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/relatoriomanutencao/data/StockItem;", "_isLoading", "", "_isSearchByCode", "_maintenanceItems", "Lcom/example/relatoriomanutencao/data/MaintenanceItem;", "_stockSearchQuery", "", "allMachines", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/example/relatoriomanutencao/data/Machine;", "getAllMachines", "()Lkotlinx/coroutines/flow/StateFlow;", "allProductionLines", "Lcom/example/relatoriomanutencao/data/ProductionLine;", "getAllProductionLines", "database", "Lcom/example/relatoriomanutencao/data/AppDatabase;", "isLoading", "isSearchByCode", "machineConfigRepository", "Lcom/example/relatoriomanutencao/data/MachineConfigurationRepository;", "machinesWithoutLine", "getMachinesWithoutLine", "maintenanceItems", "getMaintenanceItems", "stockDao", "Lcom/example/relatoriomanutencao/data/StockDao;", "stockItems", "getStockItems", "stockSearchQuery", "getStockSearchQuery", "addMachine", "", "name", "lineId", "", "(Ljava/lang/String;Ljava/lang/Long;)V", "addMaintenanceItem", "machine", "serviceType", "description", "photoUris", "addProductionLine", "cleanOldCloudData", "deleteMachine", "deleteMaintenanceItem", "item", "deleteProductionLine", "productionLine", "getBytesFromUri", "", "uriString", "getMachinesByLine", "importCsvDirectToCloud", "uri", "Landroid/net/Uri;", "importExcelData", "importStockData", "onSearchQueryChanged", "query", "processPhotos", "Ljava/util/ArrayList;", "Ljava/util/HashMap;", "Lkotlin/collections/ArrayList;", "Lkotlin/collections/HashMap;", "refreshMaintenanceList", "searchStockInBack4App", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "syncMachineConfiguration", "syncStockToCloud", "toggleSearchMode", "updateMaintenanceItem", "originalItem", "newDescription", "newPhotoUris", "app_debug"})
@kotlin.OptIn(markerClass = {kotlinx.coroutines.FlowPreview.class})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.relatoriomanutencao.data.AppDatabase database = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.relatoriomanutencao.data.StockDao stockDao = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.relatoriomanutencao.data.MachineConfigurationRepository machineConfigRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.relatoriomanutencao.data.MaintenanceItem>> _maintenanceItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.MaintenanceItem>> maintenanceItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _stockSearchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> stockSearchQuery = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.relatoriomanutencao.data.StockItem>> _cloudStockItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.StockItem>> stockItems = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isSearchByCode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSearchByCode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.ProductionLine>> allProductionLines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.Machine>> allMachines = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.Machine>> machinesWithoutLine = null;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.MaintenanceItem>> getMaintenanceItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getStockSearchQuery() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.StockItem>> getStockItems() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isSearchByCode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.ProductionLine>> getAllProductionLines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getAllMachines() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesWithoutLine() {
        return null;
    }
    
    public final void refreshMaintenanceList() {
    }
    
    public final void addMaintenanceItem(@org.jetbrains.annotations.NotNull()
    java.lang.String machine, @org.jetbrains.annotations.NotNull()
    java.lang.String serviceType, @org.jetbrains.annotations.NotNull()
    java.lang.String description, @org.jetbrains.annotations.NotNull()
    java.lang.String photoUris) {
    }
    
    public final void updateMaintenanceItem(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MaintenanceItem originalItem, @org.jetbrains.annotations.NotNull()
    java.lang.String newDescription, @org.jetbrains.annotations.NotNull()
    java.lang.String newPhotoUris) {
    }
    
    private final byte[] getBytesFromUri(java.lang.String uriString) {
        return null;
    }
    
    private final java.util.ArrayList<java.util.HashMap<java.lang.String, java.lang.String>> processPhotos(java.lang.String photoUris) {
        return null;
    }
    
    public final void deleteMaintenanceItem(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MaintenanceItem item) {
    }
    
    public final void cleanOldCloudData() {
    }
    
    private final java.lang.Object searchStockInBack4App(java.lang.String query, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void onSearchQueryChanged(@org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    public final void toggleSearchMode() {
    }
    
    public final void importStockData() {
    }
    
    public final void importCsvDirectToCloud(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public final void importExcelData(@org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
    }
    
    public final void syncStockToCloud() {
    }
    
    public final void syncMachineConfiguration() {
    }
    
    public final void addProductionLine(@org.jetbrains.annotations.NotNull()
    java.lang.String name) {
    }
    
    public final void deleteProductionLine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLine productionLine) {
    }
    
    public final void addMachine(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.Nullable()
    java.lang.Long lineId) {
    }
    
    public final void deleteMachine(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.Machine machine) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.relatoriomanutencao.data.Machine>> getMachinesByLine(long lineId) {
        return null;
    }
}