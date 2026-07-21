package com.qrforge.repository

import com.qrforge.database.dao.QrTemplateDao
import com.qrforge.database.entity.QrTemplateEntity
import com.qrforge.domain.model.Template
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrTemplateRepository @Inject constructor(
    private val dao: QrTemplateDao
) {
    fun getAllTemplates(): Flow<List<Template>> =
        dao.getAllTemplates().map { list -> list.map { it.toDomain() } }

    fun getTemplatesByCategory(category: String): Flow<List<Template>> =
        dao.getTemplatesByCategory(category).map { list -> list.map { it.toDomain() } }

    fun getCategories(): Flow<List<String>> = dao.getCategories()

    suspend fun getById(id: Long): Template? =
        dao.getById(id)?.toDomain()

    suspend fun seedTemplates() {
        if (dao.getCount() == 0) {
            dao.insertAll(DefaultTemplates.all.map { it.toEntity() })
        }
    }

    private fun QrTemplateEntity.toDomain() = Template(
        id = id,
        name = name,
        category = category,
        qrType = qrType,
        defaultData = defaultData,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        dotStyle = dotStyle,
        eyeShape = eyeShape,
        frameStyle = frameStyle,
        logoPath = logoPath,
        previewIcon = previewIcon
    )

    private fun Template.toEntity() = QrTemplateEntity(
        id = id,
        name = name,
        category = category,
        qrType = qrType,
        defaultData = defaultData,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        dotStyle = dotStyle,
        eyeShape = eyeShape,
        frameStyle = frameStyle,
        logoPath = logoPath,
        previewIcon = previewIcon
    )
}

object DefaultTemplates {
    val all = listOf(
        Template(name = "Classic URL", category = "Business", qrType = "URL",
            foregroundColor = 0xFF000000, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "NONE", logoPath = null),
        Template(name = "Minimal Black", category = "Minimal", qrType = "TEXT",
            foregroundColor = 0xFF090909, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "CIRCLE", frameStyle = "THIN", logoPath = null),
        Template(name = "Lime Pop", category = "Modern", qrType = "URL",
            foregroundColor = 0xFFA3E635, backgroundColor = 0xFF090909,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "NONE", logoPath = "LINK"),
        Template(name = "Instagram Grid", category = "Social", qrType = "SOCIAL",
            foregroundColor = 0xFFE1306C, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "CIRCLE", frameStyle = "ROUNDED", logoPath = "INSTAGRAM"),
        Template(name = "Business Card", category = "Professional", qrType = "CONTACT",
            foregroundColor = 0xFF1A1A2E, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "THIN", logoPath = null),
        Template(name = "WiFi Connect", category = "WiFi", qrType = "WIFI",
            foregroundColor = 0xFF2563EB, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "SQUARE", frameStyle = "BADGE", logoPath = "WIFI"),
        Template(name = "WhatsApp Me", category = "Social", qrType = "WHATSAPP",
            foregroundColor = 0xFF25D366, backgroundColor = 0xFFFFFFFF,
            dotStyle = "CIRCULAR", eyeShape = "CIRCLE", frameStyle = "NONE", logoPath = "WHATSAPP"),
        Template(name = "Event Invite", category = "Event", qrType = "EVENT",
            foregroundColor = 0xFF8B5CF6, backgroundColor = 0xFFFFFFFF,
            dotStyle = "DIAMOND", eyeShape = "MODERN", frameStyle = "ROUNDED", logoPath = null),
        Template(name = "Dark Mode", category = "Minimal", qrType = "URL",
            foregroundColor = 0xFFFFFFFF, backgroundColor = 0xFF090909,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "NONE", logoPath = null),
        Template(name = "Restaurant Menu", category = "Business", qrType = "URL",
            foregroundColor = 0xFFD97706, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "ROUNDED", frameStyle = "THIN", logoPath = "LINK"),
        Template(name = "Wedding QR", category = "Event", qrType = "URL",
            foregroundColor = 0xFFBE123C, backgroundColor = 0xFFFFF5F5,
            dotStyle = "ROUNDED", eyeShape = "CIRCLE", frameStyle = "ROUNDED", logoPath = null),
        Template(name = "Cafe Order", category = "Business", qrType = "URL",
            foregroundColor = 0xFF6B4226, backgroundColor = 0xFFFFF8F0,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "BADGE", logoPath = "LINK"),
        Template(name = "Portfolio Link", category = "Professional", qrType = "URL",
            foregroundColor = 0xFF1E1E1E, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "NONE", logoPath = null),
        Template(name = "YouTube Channel", category = "Social", qrType = "URL",
            foregroundColor = 0xFFFF0000, backgroundColor = 0xFFFFFFFF,
            dotStyle = "ROUNDED", eyeShape = "CIRCLE", frameStyle = "NONE", logoPath = "YOUTUBE"),
        Template(name = "Payment QR", category = "Business", qrType = "TEXT",
            foregroundColor = 0xFF059669, backgroundColor = 0xFFFFFFFF,
            dotStyle = "SQUARE", eyeShape = "SQUARE", frameStyle = "THICK", logoPath = "LINK"),
        Template(name = "Modern Glass", category = "Modern", qrType = "URL",
            foregroundColor = 0xFFFFFFFF, backgroundColor = 0xFF171717,
            dotStyle = "ROUNDED", eyeShape = "MODERN", frameStyle = "ROUNDED", logoPath = null)
    )
}
