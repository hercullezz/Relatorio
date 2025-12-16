package com.example.relatoriomanutencao.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.example.relatoriomanutencao.data.MaintenanceItem;
import kotlinx.coroutines.Dispatchers;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J0\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u0004H\u0002J$\u0010\u0013\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00100\u0015H\u0086@\u00a2\u0006\u0002\u0010\u0016J\u001e\u0010\u0017\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0018J\u001a\u0010\u0019\u001a\u0004\u0018\u00010\u001a2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\u001cH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/example/relatoriomanutencao/utils/PdfGenerator;", "", "()V", "CONTENT_WIDTH", "", "MARGIN", "PAGE_HEIGHT", "", "PAGE_WIDTH", "drawGraphItem", "", "context", "Landroid/content/Context;", "canvas", "Landroid/graphics/Canvas;", "item", "Lcom/example/relatoriomanutencao/data/MaintenanceItem;", "x", "y", "generateConsolidatedReport", "items", "", "(Landroid/content/Context;Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateMaintenanceReport", "(Landroid/content/Context;Lcom/example/relatoriomanutencao/data/MaintenanceItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBitmapFromUrlOrUri", "Landroid/graphics/Bitmap;", "path", "", "app_debug"})
public final class PdfGenerator {
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final float MARGIN = 40.0F;
    private static final float CONTENT_WIDTH = 515.0F;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.relatoriomanutencao.utils.PdfGenerator INSTANCE = null;
    
    private PdfGenerator() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateMaintenanceReport(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MaintenanceItem item, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object generateConsolidatedReport(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.relatoriomanutencao.data.MaintenanceItem> items, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void drawGraphItem(android.content.Context context, android.graphics.Canvas canvas, com.example.relatoriomanutencao.data.MaintenanceItem item, float x, float y) {
    }
    
    private final android.graphics.Bitmap getBitmapFromUrlOrUri(android.content.Context context, java.lang.String path) {
        return null;
    }
}