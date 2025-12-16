package com.example.relatoriomanutencao.ui;

import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontStyle;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import com.example.relatoriomanutencao.data.Machine;
import com.example.relatoriomanutencao.data.ProductionLine;
import com.example.relatoriomanutencao.viewmodel.MainViewModel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000:\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0005\u001a2\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\u0010\u0010\b\u001a\u00020\u00012\u0006\u0010\t\u001a\u00020\u0003H\u0007\u001a\u0012\u0010\n\u001a\u00020\u00012\b\b\u0002\u0010\u000b\u001a\u00020\fH\u0007\u001a\u001e\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001aN\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000f0\u00152\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\u0010\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u0003H\u0007\u00a8\u0006\u001a"}, d2 = {"AddNameDialog", "", "title", "", "onDismiss", "Lkotlin/Function0;", "onConfirm", "Lkotlin/Function1;", "EmptyStateMessage", "text", "MachineConfigurationScreen", "viewModel", "Lcom/example/relatoriomanutencao/viewmodel/MainViewModel;", "MachineItem", "machine", "Lcom/example/relatoriomanutencao/data/Machine;", "onDelete", "ProductionLineItem", "line", "Lcom/example/relatoriomanutencao/data/ProductionLine;", "machines", "", "onDeleteLine", "onAddMachineToLine", "onDeleteMachine", "SectionHeader", "app_debug"})
public final class MachineConfigurationScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void MachineConfigurationScreen(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.viewmodel.MainViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SectionHeader(@org.jetbrains.annotations.NotNull()
    java.lang.String title) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EmptyStateMessage(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable()
    public static final void ProductionLineItem(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.ProductionLine line, @org.jetbrains.annotations.NotNull()
    java.util.List<com.example.relatoriomanutencao.data.Machine> machines, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteLine, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onAddMachineToLine, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.example.relatoriomanutencao.data.Machine, kotlin.Unit> onDeleteMachine) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void MachineItem(@org.jetbrains.annotations.NotNull()
    com.example.relatoriomanutencao.data.Machine machine, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AddNameDialog(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onConfirm) {
    }
}