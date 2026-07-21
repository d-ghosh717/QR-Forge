package com.qrforge.util

import com.qrforge.domain.model.QrType

object QrContentEncoder {

    fun encode(type: QrType, data: Map<String, String>): String {
        return when (type) {
            QrType.URL -> data["url"] ?: ""
            QrType.TEXT -> data["text"] ?: ""
            QrType.WIFI -> encodeWifi(data)
            QrType.EMAIL -> encodeEmail(data)
            QrType.SMS -> encodeSms(data)
            QrType.PHONE -> "tel:${data["phone"] ?: ""}"
            QrType.WHATSAPP -> encodeWhatsApp(data)
            QrType.CONTACT -> encodeVCard(data)
            QrType.LOCATION -> encodeLocation(data)
            QrType.EVENT -> encodeEvent(data)
            QrType.PLAY_STORE -> "market://details?id=${data["package"] ?: ""}"
            QrType.APP_STORE -> data["url"] ?: ""
            QrType.SOCIAL -> data["url"] ?: ""
            QrType.CUSTOM -> data["content"] ?: ""
        }
    }

    private fun encodeWifi(data: Map<String, String>): String {
        val ssid = data["ssid"] ?: ""
        val password = data["password"] ?: ""
        val security = data["security"] ?: "WPA"
        return "WIFI:T:$security;S:$ssid;P:$password;;"
    }

    private fun encodeEmail(data: Map<String, String>): String {
        val address = data["address"] ?: ""
        val subject = data["subject"] ?: ""
        val body = data["body"] ?: ""
        return "mailto:$address?subject=$subject&body=$body"
    }

    private fun encodeSms(data: Map<String, String>): String {
        val phone = data["phone"] ?: ""
        val message = data["message"] ?: ""
        return "smsto:$phone:$message"
    }

    private fun encodeWhatsApp(data: Map<String, String>): String {
        val phone = data["phone"] ?: ""
        val message = data["message"] ?: ""
        val encoded = java.net.URLEncoder.encode(message, "UTF-8")
        return "https://wa.me/$phone?text=$encoded"
    }

    private fun encodeVCard(data: Map<String, String>): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            data["name"]?.let { appendLine("FN:$it") }
            data["organization"]?.let { appendLine("ORG:$it") }
            data["phone"]?.let { appendLine("TEL:$it") }
            data["email"]?.let { appendLine("EMAIL:$it") }
            data["website"]?.let { appendLine("URL:$it") }
            data["address"]?.let { appendLine("ADR:;;$it;;;") }
            appendLine("END:VCARD")
        }
    }

    private fun encodeLocation(data: Map<String, String>): String {
        val lat = data["latitude"] ?: "0"
        val lng = data["longitude"] ?: "0"
        return "geo:$lat,$lng"
    }

    private fun encodeEvent(data: Map<String, String>): String {
        val title = data["title"] ?: ""
        val description = data["description"] ?: ""
        val location = data["location"] ?: ""
        val start = data["start"] ?: ""
        val end = data["end"] ?: ""
        return buildString {
            appendLine("BEGIN:VEVENT")
            appendLine("SUMMARY:$title")
            appendLine("DESCRIPTION:$description")
            appendLine("LOCATION:$location")
            appendLine("DTSTART:$start")
            appendLine("DTEND:$end")
            appendLine("END:VEVENT")
        }
    }
}
