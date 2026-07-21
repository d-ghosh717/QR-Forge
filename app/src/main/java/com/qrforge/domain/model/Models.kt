package com.qrforge.domain.model

enum class QrType(
    val displayName: String,
    val iconName: String,
    val description: String
) {
    URL("URL", "link", "Website or link"),
    TEXT("Plain Text", "text_fields", "Any text message"),
    WIFI("WiFi", "wifi", "Network credentials"),
    EMAIL("Email", "email", "Email address"),
    SMS("SMS", "sms", "Text message"),
    PHONE("Phone", "phone", "Phone number"),
    WHATSAPP("WhatsApp", "chat", "WhatsApp link"),
    CONTACT("Contact", "contact_page", "vCard contact"),
    LOCATION("Location", "location_on", "GPS coordinates"),
    EVENT("Event", "event", "Calendar event"),
    PLAY_STORE("Play Store", "play_store", "App link"),
    APP_STORE("App Store", "apple", "iOS app link"),
    SOCIAL("Social Media", "share", "Social profile"),
    CUSTOM("Custom", "build", "Custom format");

    companion object {
        fun fromName(name: String): QrType =
            entries.find { it.name.equals(name, ignoreCase = true) } ?: TEXT
    }
}

data class QrContent(
    val type: QrType,
    val rawData: Map<String, String>,
    val encodedContent: String
)

data class CustomizationOptions(
    val foregroundColor: Long = 0xFF000000,
    val backgroundColor: Long = 0xFFFFFFFF,
    val dotStyle: DotStyle = DotStyle.ROUNDED,
    val eyeShape: EyeShape = EyeShape.MODERN,
    val frameStyle: FrameStyle = FrameStyle.NONE,
    val logoPath: String? = null,
    val logoSize: Float = 0.22f,
    val logoOpacity: Float = 1.0f
)

enum class DotStyle(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCULAR("Circular"),
    DIAMOND("Diamond")
}

enum class EyeShape(val displayName: String) {
    SQUARE("Square"),
    ROUNDED("Rounded"),
    CIRCLE("Circle"),
    MODERN("Modern")
}

enum class FrameStyle(val displayName: String) {
    NONE("None"),
    THIN("Thin Border"),
    THICK("Thick Border"),
    ROUNDED("Rounded Frame"),
    BADGE("Badge Style"),
    STICKER("Sticker")
}

data class Template(
    val id: Long = 0,
    val name: String,
    val category: String,
    val qrType: String,
    val defaultData: String = "",
    val foregroundColor: Long,
    val backgroundColor: Long,
    val dotStyle: String,
    val eyeShape: String,
    val frameStyle: String,
    val logoPath: String? = null,
    val previewIcon: String = ""
)

enum class TemplateCategory(val displayName: String) {
    ALL("All"),
    BUSINESS("Business"),
    SOCIAL("Social"),
    WIFI("WiFi"),
    EVENT("Event"),
    PERSONAL("Personal"),
    PROFESSIONAL("Professional"),
    MINIMAL("Minimal"),
    MODERN("Modern")
}

data class QrHistoryItem(
    val id: Long = 0,
    val type: String,
    val title: String,
    val content: String,
    val rawData: String,
    val foregroundColor: Long = 0xFF000000,
    val backgroundColor: Long = 0xFFFFFFFF,
    val dotStyle: String = "ROUNDED",
    val eyeShape: String = "MODERN",
    val logoPath: String? = null,
    val frameStyle: String = "NONE",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
