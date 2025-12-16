package com.example.relatoriomanutencao.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.core.content.FileProvider;
import com.example.relatoriomanutencao.data.MaintenanceItem;
import com.example.relatoriomanutencao.data.StockItem;
import com.example.relatoriomanutencao.utils.PdfGenerator;
import com.example.relatoriomanutencao.viewmodel.MainViewModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a\u0012\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u0007\u001a8\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u0018\u0010\t\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\nH\u0007\u001a,\u0010\f\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\bH\u0007\u001a\b\u0010\u000f\u001a\u00020\u0001H\u0007\u001a\u0010\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u001a\u0010\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u0012H\u0007\u001a\u0010\u0010\u0013\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u00a8\u0006\u0014"}, d2 = {"CloudScreen", "", "viewModel", "Lcom/example/relatoriomanutencao/viewmodel/MainViewModel;", "EditServiceDialog", "item", "Lcom/example/relatoriomanutencao/data/MaintenanceItem;", "onDismiss", "Lkotlin/Function0;", "onConfirm", "Lkotlin/Function2;", "", "MaintenanceItemCard", "onDelete", "onEdit", "SavedReportsScreen", "ServicesListScreen", "StockItemCard", "Lcom/example/relatoriomanutencao/data/StockItem;", "StockScreen", "app_debug"})
public final class ScreensKt {
    
    @androidx.compose.runtime.Composable()
    public static final void StockScreen(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.viewmodel.MainViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void StockItemCard(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.StockItem item) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SavedReportsScreen() {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void CloudScreen(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.viewmodel.MainViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ServicesListScreen(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.viewmodel.MainViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EditServiceDialog(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MaintenanceItem item, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onConfirm) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void MaintenanceItemCard(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.MaintenanceItem item, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onEdit) {
    }
}