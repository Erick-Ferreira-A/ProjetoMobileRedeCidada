package com.example.projetorevisao.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Local copy of an incident report. This is device persistence, not a server submission. */
@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val category: String,
    val customBadgeTitle: String?,
    val title: String,
    val description: String,
    val address: String,
    val neighborhood: String,
    val distanceMeters: Int,
    val timeAgo: String,
    val verified: Boolean,
    val confirmations: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val status: String,
    val authorityAssigned: String,
    val isAnonymous: Boolean,
    val mapXFraction: Float,
    val mapYFraction: Float,
    val latitude: Double,
    val longitude: Double,
    val aiSafetyRecommendation: String,
    val userConfirmed: Boolean,
    /** Anexos, um por linha no formato "image|url" ou "video|url". */
    @ColumnInfo(defaultValue = "") val media: String = "",
    /** Quando o relato foi criado (ms). 0 = desconhecido (registros antigos). */
    @ColumnInfo(defaultValue = "0") val createdAtEpochMs: Long = 0,
    /** true = cópia de um relato do servidor; false = criado aqui e ainda não enviado. */
    @ColumnInfo(defaultValue = "0") val fromServer: Boolean = false
)
