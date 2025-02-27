package com.example.redpacketassistant.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.redpacketassistant.utils.PreferenceHelper
import java.util.*

class RedPacketService : AccessibilityService() {
    private val TAG = "RedPacketService"
    private lateinit var prefs: SharedPreferences
    
    override fun onServiceConnected() {
        Log.d(TAG, "Service connected")
        prefs = PreferenceHelper.getPrefs(this)
        Log.d(TAG, "Service enabled: ${isServiceEnabled()}")
        Log.d(TAG, "Monitored chats: ${PreferenceHelper.getMonitoredChats(this)}")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(TAG, "Received event: ${event.eventType}")
        
        if (!isServiceEnabled()) {
            Log.d(TAG, "Service is disabled, ignoring event")
            return
        }
        
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
        Log.d(TAG, "Searching for red packets...")
        
        // 尝试多种文本匹配来查找红包
        val searchTexts = listOf(
            "微信红包",
            "发了一个红包",
            "给你发了一个红包",
            "[微信红包]",
            "红包",
            "恭喜发财",
            "大吉大利",
            "恭喜发财，大吉大利"  // 完整的红包祝福语
        )
        
        val redPacketNodes = mutableListOf<AccessibilityNodeInfo>()
        
        // 1. 通过文本查找
        for (searchText in searchTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(searchText)
            if (nodes.isNotEmpty()) {
                Log.d(TAG, "Found ${nodes.size} nodes with text: '$searchText'")
                redPacketNodes.addAll(nodes)
            }
        }
        
        // 2. 通过ID查找
        val redPacketIds = listOf(
            "com.tencent.mm:id/red_packet_container",
            "com.tencent.mm:id/red_packet_view",
            "com.tencent.mm:id/red_packet_layout"
        )
        
        for (id in redPacketIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) {
                Log.d(TAG, "Found ${nodes.size} nodes with id: $id")
                redPacketNodes.addAll(nodes)
            }
        }
        
        // 3. 遍历所有节点查找可能的红包
        val queue = LinkedList<AccessibilityNodeInfo>()
        queue.add(rootNode)
        
        while (queue.isNotEmpty()) {
            val node = queue.poll()
            
            // 检查节点的文本和描述
            val nodeText = node.text?.toString() ?: ""
            val nodeDesc = node.contentDescription?.toString() ?: ""
            val className = node.className?.toString() ?: ""
            
            // 记录详细的节点信息用于调试
            if (nodeText.isNotEmpty() || nodeDesc.isNotEmpty()) {
                Log.v(TAG, "Node: text='$nodeText', desc='$nodeDesc', class='$className', clickable=${node.isClickable}")
            }
            
            // 检查是否是红包节点
            if ((nodeText.contains("红包") || nodeDesc.contains("红包") || 
                 nodeText.contains("恭喜发财") || nodeDesc.contains("恭喜发财")) &&
                !redPacketNodes.contains(node)) {
                Log.d(TAG, "Found potential red packet node: text='$nodeText', desc='$nodeDesc'")
                redPacketNodes.add(node)
            }
            
            // 添加子节点到队列
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }
        
        // 去重并获取唯一的红包节点
        val uniqueRedPackets = redPacketNodes.distinctBy { node -> 
            val rect = Rect()
            node.getBoundsInScreen(rect)
            Pair(node.text?.toString() ?: "", rect.toShortString())
        }
        
        Log.d(TAG, "Total found ${uniqueRedPackets.size} red packets")
        
        if (uniqueRedPackets.isNotEmpty()) {
            // 获取监控列表
            val monitoredChats = PreferenceHelper.getMonitoredChats(this)
            Log.d(TAG, "Monitored chats: $monitoredChats")
            
            // 遍历所有红包节点
            for (redPacketNode in uniqueRedPackets) {
                try {
                    // 记录红包节点的详细信息
                    val rect = Rect()
                    redPacketNode.getBoundsInScreen(rect)
                    Log.d(TAG, "Processing red packet at: ${rect.toShortString()}")
                    
                    handleRedPacket(redPacketNode, rootNode)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing red packet", e)
                } finally {
                    redPacketNode.recycle()
                }
            }
        }
    }
    
    private fun getChatName(rootNode: AccessibilityNodeInfo): String {
        // 更新微信的 ViewId 列表
        val possibleIds = listOf(
            "com.tencent.mm:id/kox",  // 新版本的聊天标题
            "com.tencent.mm:id/k2m",  // 个人聊天标题
            "com.tencent.mm:id/kh9",  // 群聊标题
            "com.tencent.mm:id/conversation_title", // 通用标题
            "com.tencent.mm:id/title", // 通用标题
            "com.tencent.mm:id/kp5",  // 可能的新ID
            "com.tencent.mm:id/kp6"   // 可能的新ID
        )
        
        // 先尝试通过ID查找
        for (id in possibleIds) {
            val titleNodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (titleNodes.isNotEmpty()) {
                val title = titleNodes[0].text?.toString() ?: ""
                Log.d(TAG, "Found title with id $id: '$title'")
                titleNodes.forEach { it.recycle() }
                if (title.isNotEmpty()) {
                    return title
                }
            }
        }

        // 如果通过ID没找到，尝试遍历所有文本节点
        val textNodes = rootNode.findAccessibilityNodeInfosByText("")
        for (node in textNodes) {
            val text = node.text?.toString() ?: ""
            // 检查是否可能是聊天标题
            if (text.isNotEmpty() && text.length < 30 && !text.contains("微信红包") && !text.contains("开")) {
                val parent = node.parent
                if (parent != null) {
                    // 检查父节点的类名，通常聊天标题会在特定的布局中
                    val className = parent.className?.toString() ?: ""
                    if (className.contains("TextView") || className.contains("ActionBar")) {
                        Log.d(TAG, "Found potential title by text: '$text' in $className")
                        return text
                    }
                }
            }
        }
        
        Log.w(TAG, "Could not find chat name")
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
    
    private fun clickOpenButton(rootNode: AccessibilityNodeInfo?) {
        if (rootNode == null) {
            Log.d(TAG, "Root node is null when trying to click open button")
            return
        }
        
        try {
            // 尝试多种方式查找"开"按钮
            val buttonTexts = listOf("開", "开", "开红包", "開紅包")
            var openButton: AccessibilityNodeInfo? = null
            
            // 1. 通过文本查找
            for (text in buttonTexts) {
                val buttons = rootNode.findAccessibilityNodeInfosByText(text)
                for (button in buttons) {
                    val buttonText = button.text?.toString() ?: ""
                    Log.d(TAG, "Found button with text: '$buttonText'")
                    if (button.isClickable) {
                        openButton = button
                        break
                    }
                    button.recycle()
                }
                if (openButton != null) break
            }
            
            // 2. 通过ID查找
            if (openButton == null) {
                val buttonIds = listOf(
                    "com.tencent.mm:id/open_button",
                    "com.tencent.mm:id/den",
                    "com.tencent.mm:id/gvm"
                )
                for (id in buttonIds) {
                    val buttons = rootNode.findAccessibilityNodeInfosByViewId(id)
                    if (buttons.isNotEmpty() && buttons[0].isClickable) {
                        openButton = buttons[0]
                        break
                    }
                }
            }
            
            // 点击找到的按钮
            if (openButton != null) {
                Log.d(TAG, "Clicking open button")
                val clicked = openButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Open button click result: $clicked")
                openButton.recycle()
            } else {
                Log.d(TAG, "Open button not found")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clicking open button", e)
        }
    }
    
    private fun isServiceEnabled(): Boolean {
        return PreferenceHelper.isServiceEnabled(this)
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    private fun handleRedPacket(redPacketNode: AccessibilityNodeInfo, rootNode: AccessibilityNodeInfo) {
        try {
            Log.d(TAG, "Attempting to handle red packet")
            
            // 检查红包状态
            var parent = redPacketNode.parent
            var attempts = 0
            var clicked = false
            
            // 先尝试直接点击红包节点
            if (redPacketNode.isClickable) {
                Log.d(TAG, "Clicking red packet node directly")
                clicked = redPacketNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Direct click result: $clicked")
            }
            
            // 如果直接点击失败，尝试使用手势点击
            if (!clicked) {
                val rect = Rect()
                redPacketNode.getBoundsInScreen(rect)
                Log.d(TAG, "Attempting gesture click at: ${rect.centerX()}, ${rect.centerY()}")
                
                val path = Path()
                path.moveTo(rect.centerX().toFloat(), rect.centerY().toFloat())
                
                val gestureBuilder = GestureDescription.Builder()
                val gesture = gestureBuilder
                    .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                    .build()
                
                clicked = dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription) {
                        Log.d(TAG, "Gesture completed")
                        // 延迟查找和点击"开"按钮
                        Handler(Looper.getMainLooper()).postDelayed({
                            performOpenButtonClick()
                        }, 500)
                    }
                    
                    override fun onCancelled(gestureDescription: GestureDescription) {
                        Log.d(TAG, "Gesture cancelled")
                    }
                }, null)
                
                Log.d(TAG, "Gesture dispatch result: $clicked")
            }
            
            if (clicked) {
                Log.d(TAG, "Successfully triggered red packet")
            } else {
                Log.w(TAG, "Failed to click red packet")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleRedPacket", e)
        }
    }

    private fun performOpenButtonClick() {
        try {
            val root = rootInActiveWindow
            if (root == null) {
                Log.d(TAG, "Root node is null when trying to click open button")
                return
            }

            var openButton: AccessibilityNodeInfo? = null
            var buttonRect: Rect? = null

            // 遍历所有节点查找可能的按钮
            val queue = LinkedList<AccessibilityNodeInfo>()
            queue.add(root)

            while (queue.isNotEmpty() && openButton == null) {
                val node = queue.poll()
                
                // 检查当前节点
                val nodeText = node.text?.toString() ?: ""
                val nodeDesc = node.contentDescription?.toString() ?: ""
                val className = node.className?.toString() ?: ""
                
                Log.d(TAG, "Checking node: text='$nodeText', desc='$nodeDesc', class='$className', clickable=${node.isClickable}")
                
                // 检查是否是按钮
                if (node.isClickable && (
                    nodeText == "開" || nodeText == "开" ||  // 匹配文本
                    nodeDesc == "開" || nodeDesc == "开" ||  // 匹配描述
                    className.contains("Button") && (nodeText.isEmpty() && nodeDesc.isEmpty()) // 匹配空文本的按钮
                )) {
                    openButton = node
                    val rect = Rect()
                    node.getBoundsInScreen(rect)
                    buttonRect = rect
                    Log.d(TAG, "Found open button at: ${rect.centerX()}, ${rect.centerY()}")
                    break
                }

                // 添加子节点到队列
                for (i in 0 until node.childCount) {
                    node.getChild(i)?.let { queue.add(it) }
                }
            }

            // 尝试点击找到的按钮
            if (openButton != null && buttonRect != null) {
                // 先尝试普通点击
                var clicked = openButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "Direct click result: $clicked")
                
                // 如果普通点击失败，尝试手势点击
                if (!clicked) {
                    val path = Path()
                    path.moveTo(buttonRect.centerX().toFloat(), buttonRect.centerY().toFloat())
                    
                    val gestureBuilder = GestureDescription.Builder()
                    val gesture = gestureBuilder
                        .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
                        .build()
                    
                    clicked = dispatchGesture(gesture, object : AccessibilityService.GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription) {
                            Log.d(TAG, "Gesture click completed")
                        }
                        
                        override fun onCancelled(gestureDescription: GestureDescription) {
                            Log.d(TAG, "Gesture click cancelled")
                        }
                    }, null)
                    Log.d(TAG, "Gesture click result: $clicked")
                }
            } else {
                Log.d(TAG, "Open button not found after traversing all nodes")
            }

            // 清理资源
            root.recycle()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error clicking open button", e)
        }
    }
} 