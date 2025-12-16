package com.example.relatoriomanutencao.data

data class ReportItem(
    val id: String = "",
    val machine: String = "",
    val subMachine: String? = null,
    val serviceType: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val author: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
