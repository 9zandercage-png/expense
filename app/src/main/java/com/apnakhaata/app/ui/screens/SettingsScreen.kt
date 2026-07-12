package com.apnakhaata.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnakhaata.app.ui.theme.GreenLedger
import com.apnakhaata.app.ui.theme.InkSoft
import com.apnakhaata.app.ui.theme.RedLedger

@Composable
fun SettingsScreen(
    smsPermissionGranted: Boolean,
    onRequestPermissions: () -> Unit,
    onScanInbox: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card {
            Column(Modifier.padding(14.dp)) {
                Text("SMS Permission", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (smsPermissionGranted)
                        "✓ Granted — naye bank SMS automatically capture ho rahe hain."
                    else
                        "✗ Nahi mili — bina iske SMS auto-capture kaam nahi karega.",
                    fontSize = 12.sp,
                    color = if (smsPermissionGranted) GreenLedger else RedLedger
                )
                if (!smsPermissionGranted) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRequestPermissions) { Text("Permission do") }
                }
            }
        }

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Purane SMS import karo", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Phone ke SMS inbox ko scan karke saare purane bank transaction import kar lo. Duplicates apne aap skip honge.",
                    fontSize = 12.sp, color = InkSoft
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onScanInbox, enabled = smsPermissionGranted) {
                    Text("Scan SMS Inbox")
                }
            }
        }

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Saara data ek JSON file me save karo (Google Drive/WhatsApp pe bhej ke safe rakho). Restore karne par data merge hota hai — duplicates skip.",
                    fontSize = 12.sp, color = InkSoft
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onBackup) { Text("⬇ Backup") }
                    OutlinedButton(onClick = onRestore) { Text("⬆ Restore") }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Note: Web app (Apna Khaata) ka backup file bhi yahan restore ho sakta hai — format compatible hai.",
                    fontSize = 11.sp, color = InkSoft
                )
            }
        }

        Card {
            Column(Modifier.padding(14.dp)) {
                Text("Zaroori jaankari", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "• Data sirf is phone me store hota hai — koi server/cloud nahi.\n" +
                    "• Xiaomi/Oppo/Vivo phones me 'Autostart' permission bhi on karni pad sakti hai (Settings → Apps → Apna Khaata → Autostart) taaki background me SMS capture ho.\n" +
                    "• Battery saver me app ko 'No restrictions' pe rakho.",
                    fontSize = 12.sp, color = InkSoft, lineHeight = 17.sp
                )
            }
        }
    }
}
