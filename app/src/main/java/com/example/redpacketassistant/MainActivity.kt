package com.example.redpacketassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.redpacketassistant.ui.theme.RedPacketAssistantTheme
import com.example.redpacketassistant.utils.PreferenceHelper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            RedPacketAssistantTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.app_name)) }
                        )
                    }
                ) { innerPadding ->
                    val serviceEnabled = remember { mutableStateOf(PreferenceHelper.isServiceEnabled(this)) }
                    val monitoredChats = remember { mutableStateOf(PreferenceHelper.getMonitoredChats(this)) }
                    
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        serviceEnabled = serviceEnabled.value,
                        monitoredChats = monitoredChats.value,
                        onServiceEnabledChanged = { enabled ->
                            PreferenceHelper.setServiceEnabled(this, enabled)
                            serviceEnabled.value = enabled
                        },
                        onAddChat = { chat ->
                            PreferenceHelper.addMonitoredChat(this, chat)
                            monitoredChats.value = PreferenceHelper.getMonitoredChats(this)
                        },
                        onRemoveChat = { chat ->
                            PreferenceHelper.removeMonitoredChat(this, chat)
                            monitoredChats.value = PreferenceHelper.getMonitoredChats(this)
                        }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // No need to refresh data here as it's handled by the state management
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    serviceEnabled: Boolean,
    monitoredChats: Set<String>,
    onServiceEnabledChanged: (Boolean) -> Unit,
    onAddChat: (String) -> Unit,
    onRemoveChat: (String) -> Unit
) {
    val context = LocalContext.current
    var showAddChatDialog by remember { mutableStateOf(false) }
    var newChatName by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Service status section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.service_status),
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (serviceEnabled) 
                            stringResource(R.string.active) 
                        else 
                            stringResource(R.string.inactive)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Switch(
                        checked = serviceEnabled,
                        onCheckedChange = onServiceEnabledChanged
                    )
                }
                
                Button(
                    onClick = { openAccessibilitySettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.accessibility_settings))
                }
            }
        }
        
        // Monitored chats section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.monitored_chats),
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (monitoredChats.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_monitored_chats),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(monitoredChats.toList()) { chat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = chat,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                TextButton(onClick = { onRemoveChat(chat) }) {
                                    Text(stringResource(R.string.remove))
                                }
                            }
                            Divider()
                        }
                    }
                }
                
                Button(
                    onClick = { showAddChatDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add_chat))
                }
            }
        }
    }
    
    if (showAddChatDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddChatDialog = false
                newChatName = ""
            },
            title = { Text(stringResource(R.string.add_chat)) },
            text = {
                OutlinedTextField(
                    value = newChatName,
                    onValueChange = { newChatName = it },
                    label = { Text(stringResource(R.string.enter_chat_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newChatName.isNotBlank()) {
                            onAddChat(newChatName.trim())
                            newChatName = ""
                        }
                        showAddChatDialog = false
                    }
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddChatDialog = false
                        newChatName = ""
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}