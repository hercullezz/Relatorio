package com.example.relatoriomanutencao.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ReportRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    fun getReports(): Flow<List<Report>> {
        return db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { document ->
                    val report = document.toObject(Report::class.java)!!
                    report.copy(id = document.id)
                }
            }
    }

    fun getReport(reportId: String): Flow<Report?> {
        return db.collection("reports").document(reportId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(Report::class.java)?.copy(id = snapshot.id)
            }
    }

    suspend fun findReportByTitle(title: String): Report? {
        val snapshot = db.collection("reports")
            .whereEqualTo("title", title)
            .get()
            .await()
        return snapshot.documents.firstOrNull()?.toObject(Report::class.java)?.copy(id = snapshot.documents.first().id)
    }

    suspend fun createOrOpenReportForToday(): Report {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        val existingReport = findReportByTitle(todayDate)
        if (existingReport != null) {
            return existingReport
        } else {
            val newReport = Report(title = todayDate, createdBy = "current_user_id_here") // You might need to adjust createdBy
            val documentRef = db.collection("reports").add(newReport).await()
            return newReport.copy(id = documentRef.id)
        }
    }

    suspend fun addReport(report: Report) {
        val documentRef = db.collection("reports").add(report).await()
        db.collection("reports").document(documentRef.id).update("id", documentRef.id).await()
    }

    suspend fun addReportItem(reportId: String, item: ReportItem) {
        val reportRef = db.collection("reports").document(reportId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(reportRef)
            val currentReport = snapshot.toObject(Report::class.java)
            if (currentReport != null) {
                val newItems = currentReport.items + item
                transaction.update(reportRef, "items", newItems)
            }
        }.await()
    }

    suspend fun updateReportItem(reportId: String, oldItem: ReportItem, newItem: ReportItem) {
        val reportRef = db.collection("reports").document(reportId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(reportRef)
            val currentReport = snapshot.toObject(Report::class.java)
            if (currentReport != null) {
                val updatedItems = currentReport.items.toMutableList()
                val index = updatedItems.indexOfFirst { it.id == oldItem.id } // Encontra o item pelo ID
                if (index != -1) {
                    updatedItems[index] = newItem // Substitui pelo novo item
                    transaction.update(reportRef, "items", updatedItems)
                }
            }
        }.await()
    }

    suspend fun deleteReportItem(reportId: String, itemToDelete: ReportItem) {
        val reportRef = db.collection("reports").document(reportId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(reportRef)
            val currentReport = snapshot.toObject(Report::class.java)
            if (currentReport != null) {
                val updatedItems = currentReport.items.filter { it.id != itemToDelete.id } // Filtra pelo ID
                transaction.update(reportRef, "items", updatedItems)
            }
        }.await()
    }

    suspend fun deleteReport(reportId: String) {
        db.collection("reports").document(reportId).delete().await()
    }
}
