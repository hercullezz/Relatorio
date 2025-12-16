package com.example.relatoriomanutencao.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Report(
    val id: String = "",
    val title: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val createdBy: String = "",
    val items: List<ReportItem> = emptyList()
)
