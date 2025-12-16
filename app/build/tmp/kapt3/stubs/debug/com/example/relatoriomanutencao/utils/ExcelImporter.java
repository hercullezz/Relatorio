package com.example.relatoriomanutencao.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.example.relatoriomanutencao.data.StockItem;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010%\n\u0002\b\u0005\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u001cB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\"\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007H\u0002J\u0012\u0010\n\u001a\u00020\b2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0002J\u001e\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u001e\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00050\u00042\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011H\u0002J$\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0018H\u0002J*\u0010\u0019\u001a\u00020\u00152\f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\b0\u00042\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0018H\u0002J$\u0010\u001b\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0018H\u0002\u00a8\u0006\u001d"}, d2 = {"Lcom/example/relatoriomanutencao/utils/ExcelImporter;", "", "()V", "convertMapToList", "", "Lcom/example/relatoriomanutencao/data/StockItem;", "map", "", "", "Lcom/example/relatoriomanutencao/utils/ExcelImporter$TempItem;", "getCellValueAsString", "cell", "Lorg/apache/poi/ss/usermodel/Cell;", "parseCSV", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "parseExcel", "parseXLSX", "processArmazemSheet", "", "sheet", "Lorg/apache/poi/ss/usermodel/Sheet;", "", "processCsvLine", "tokens", "processDadosSheet", "TempItem", "app_debug"})
public final class ExcelImporter {
    @org.jetbrains.annotations.NotNull()
    public static final com.example.relatoriomanutencao.utils.ExcelImporter INSTANCE = null;
    
    private ExcelImporter() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.example.relatoriomanutencao.data.StockItem> parseExcel(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri) {
        return null;
    }
    
    private final java.util.List<com.example.relatoriomanutencao.data.StockItem> parseCSV(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private final void processCsvLine(java.util.List<java.lang.String> tokens, java.util.Map<java.lang.String, com.example.relatoriomanutencao.utils.ExcelImporter.TempItem> map) {
    }
    
    private final java.util.List<com.example.relatoriomanutencao.data.StockItem> parseXLSX(android.content.Context context, android.net.Uri uri) {
        return null;
    }
    
    private final java.util.List<com.example.relatoriomanutencao.data.StockItem> convertMapToList(java.util.Map<java.lang.String, com.example.relatoriomanutencao.utils.ExcelImporter.TempItem> map) {
        return null;
    }
    
    private final void processDadosSheet(org.apache.poi.ss.usermodel.Sheet sheet, java.util.Map<java.lang.String, com.example.relatoriomanutencao.utils.ExcelImporter.TempItem> map) {
    }
    
    private final void processArmazemSheet(org.apache.poi.ss.usermodel.Sheet sheet, java.util.Map<java.lang.String, com.example.relatoriomanutencao.utils.ExcelImporter.TempItem> map) {
    }
    
    private final java.lang.String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010#\n\u0002\b\u0010\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0082\b\u0018\u00002\u00020\u0001B)\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0003J-\u0010\u0016\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007H\u00c6\u0001J\u0013\u0010\u0017\u001a\u00020\u00182\b\u0010\u0019\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001a\u001a\u00020\u0005H\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00030\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000b\u0010\f\"\u0004\b\r\u0010\u000eR\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000f\u0010\u0010\"\u0004\b\u0011\u0010\u0012\u00a8\u0006\u001c"}, d2 = {"Lcom/example/relatoriomanutencao/utils/ExcelImporter$TempItem;", "", "description", "", "quantity", "", "addresses", "", "(Ljava/lang/String;ILjava/util/Set;)V", "getAddresses", "()Ljava/util/Set;", "getDescription", "()Ljava/lang/String;", "setDescription", "(Ljava/lang/String;)V", "getQuantity", "()I", "setQuantity", "(I)V", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
    static final class TempItem {
        @org.jetbrains.annotations.NotNull()
        private java.lang.String description;
        private int quantity;
        @org.jetbrains.annotations.NotNull()
        private final java.util.Set<java.lang.String> addresses = null;
        
        public TempItem(@org.jetbrains.annotations.NotNull()
        java.lang.String description, int quantity, @org.jetbrains.annotations.NotNull()
        java.util.Set<java.lang.String> addresses) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDescription() {
            return null;
        }
        
        public final void setDescription(@org.jetbrains.annotations.NotNull()
        java.lang.String p0) {
        }
        
        public final int getQuantity() {
            return 0;
        }
        
        public final void setQuantity(int p0) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Set<java.lang.String> getAddresses() {
            return null;
        }
        
        public TempItem() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.Set<java.lang.String> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.example.relatoriomanutencao.utils.ExcelImporter.TempItem copy(@org.jetbrains.annotations.NotNull()
        java.lang.String description, int quantity, @org.jetbrains.annotations.NotNull()
        java.util.Set<java.lang.String> addresses) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}