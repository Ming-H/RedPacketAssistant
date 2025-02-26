package com.example.redpacketassistant.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val PREF_NAME = "red_packet_preferences"
    private const val KEY_SERVICE_ENABLED = "service_enabled"
    private const val KEY_MONITORED_CHATS = "monitored_chats"
    
    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun isServiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICE_ENABLED, false)
    }
    
    fun setServiceEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICE_ENABLED, enabled).apply()
    }
    
    fun getMonitoredChats(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_MONITORED_CHATS, emptySet()) ?: emptySet()
    }
    
    fun setMonitoredChats(context: Context, chats: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_MONITORED_CHATS, chats).apply()
    }
    
    fun addMonitoredChat(context: Context, chat: String) {
        val currentChats = getMonitoredChats(context).toMutableSet()
        currentChats.add(chat)
        setMonitoredChats(context, currentChats)
    }
    
    fun removeMonitoredChat(context: Context, chat: String) {
        val currentChats = getMonitoredChats(context).toMutableSet()
        currentChats.remove(chat)
        setMonitoredChats(context, currentChats)
    }
} 