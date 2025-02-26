package com.example.redpacketassistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.redpacketassistant.utils.PreferenceHelper

class RedPacketService : AccessibilityService() {
    private val TAG = "RedPacketService"
    private lateinit var prefs: SharedPreferences
    
    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        prefs = PreferenceHelper.getPrefs(this)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only process if the service is enabled
        if (!isServiceEnabled()) return
        
        // Only process certain events like window content changed or window state changed
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && 
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }
        
        val rootNode = rootInActiveWindow ?: return
        
        // Check if we're in WeChat
        val packageName = event.packageName?.toString() ?: return
        if (packageName != "com.tencent.mm") return
        
        try {
            // Check if we're in a chat screen with red packets
            findRedPackets(rootNode)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        } finally {
            rootNode.recycle()
        }
    }
    
    private fun findRedPackets(rootNode: AccessibilityNodeInfo) {
        // Find nodes that match red packet characteristics
        val redPacketNodes = rootNode.findAccessibilityNodeInfosByText("微信红包")
        
        if (redPacketNodes.isEmpty()) return
        
        for (redPacketNode in redPacketNodes) {
            // Get the chat name to check if we should process this red packet
            val chatName = getChatName(rootNode)
            if (shouldProcessChat(chatName)) {
                // Click on the red packet
                clickOnNode(redPacketNode)
                
                // After clicking, we need to click the "Open" button on the red packet dialog
                // Wait a moment for the dialog to appear
                Thread.sleep(500)
                
                // Find and click the "Open" button
                val openButton = findOpenButton()
                if (openButton != null) {
                    clickOnNode(openButton)
                    openButton.recycle()
                }
            }
            redPacketNode.recycle()
        }
    }
    
    private fun getChatName(rootNode: AccessibilityNodeInfo): String {
        // This implementation depends on WeChat's UI structure
        // Try to find the title of the chat screen
        val titleNodes = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/conversation_title")
        if (titleNodes.isNotEmpty()) {
            val title = titleNodes[0].text?.toString() ?: ""
            titleNodes.forEach { it.recycle() }
            return title
        }
        return ""
    }
    
    private fun shouldProcessChat(chatName: String): Boolean {
        if (chatName.isEmpty()) return false
        
        // Get the list of monitored chats from preferences
        val monitoredChats = PreferenceHelper.getMonitoredChats(this)
        
        // If the list is empty, monitor all chats
        if (monitoredChats.isEmpty()) return true
        
        // Check if this chat is in our monitored list
        return monitoredChats.contains(chatName)
    }
    
    private fun findOpenButton(): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        
        // Try to find the "Open" button in the red packet dialog
        val openNodes = rootNode.findAccessibilityNodeInfosByText("开")
        if (openNodes.isNotEmpty()) {
            return openNodes[0]
        }
        
        // Alternative way to find the button
        val buttons = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/open_button")
        if (buttons.isNotEmpty()) {
            return buttons[0]
        }
        
        return null
    }
    
    private fun clickOnNode(node: AccessibilityNodeInfo) {
        // Get the bounds of the node
        val rect = Rect()
        node.getBoundsInScreen(rect)
        
        // Create a path for the gesture
        val path = Path()
        path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
        
        // Create the gesture and dispatch it
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        
        dispatchGesture(gesture, null, null)
    }
    
    private fun isServiceEnabled(): Boolean {
        return PreferenceHelper.isServiceEnabled(this)
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }
} 