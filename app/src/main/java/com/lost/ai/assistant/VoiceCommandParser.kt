package com.lost.ai.assistant

sealed class VoiceIntent {
    data class LaunchApp(val appName: String, val packageName: String) : VoiceIntent()
    data class SystemGesture(val action: String) : VoiceIntent()
    data class SensitiveAction(val title: String, val detail: String) : VoiceIntent()
    data class AiQuery(val query: String) : VoiceIntent()
}

object VoiceCommandParser {

    private val APP_MAP = mapOf(
        "facebook" to Pair("Facebook", "com.facebook.katana"),
        "فەیسبووک" to Pair("Facebook", "com.facebook.katana"),
        "فيسبوك" to Pair("Facebook", "com.facebook.katana"),
        
        "whatsapp" to Pair("WhatsApp", "com.whatsapp"),
        "واتسئاپ" to Pair("WhatsApp", "com.whatsapp"),
        "واتساب" to Pair("WhatsApp", "com.whatsapp"),
        
        "chrome" to Pair("Chrome", "com.android.chrome"),
        "کرۆم" to Pair("Chrome", "com.android.chrome"),
        "كروم" to Pair("Chrome", "com.android.chrome"),

        "youtube" to Pair("YouTube", "com.google.android.youtube"),
        "یوتیوب" to Pair("YouTube", "com.google.android.youtube"),
        "يوتيوب" to Pair("YouTube", "com.google.android.youtube"),

        "settings" to Pair("Settings", "com.android.settings"),
        "ڕێکخستن" to Pair("Settings", "com.android.settings"),
        "إعدادات" to Pair("Settings", "com.android.settings")
    )

    fun parseCommand(spokenText: String): VoiceIntent {
        val text = spokenText.trim().lowercase()

        if (text.contains("home") || text.contains("سەرشاشە") || text.contains("الرئيسية")) {
            return VoiceIntent.SystemGesture("HOME")
        }

        if (text.contains("back") || text.contains("بگەڕێوە") || text.contains("رجوع")) {
            return VoiceIntent.SystemGesture("BACK")
        }

        for ((key, appPair) in APP_MAP) {
            if (text.contains(key)) {
                return VoiceIntent.LaunchApp(appPair.first, appPair.second)
            }
        }

        if (text.contains("send message") || text.contains("پەیام بنێرە") || text.contains("أرسل رسالة")) {
            return VoiceIntent.SensitiveAction("Send SMS", "Send text message via SMS")
        }

        return VoiceIntent.AiQuery(spokenText)
    }
}