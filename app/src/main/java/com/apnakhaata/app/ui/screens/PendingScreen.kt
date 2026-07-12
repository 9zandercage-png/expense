@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apnakhaata.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnakhaata.app.data.TransactionEntity
import com.apnakhaata.app.ui.theme.GreenLedger
import com.apnakhaata.app.ui.theme.Gold
import com.apnakhaata.app.ui.theme.InkSoft
import com.apnakhaata.app.ui.theme.RedLedger

/**
 * The "yeh payment kis liye thi?" confirm flow — same as web app:
 * user must enter a purpose + confirm category before the entry counts
 * as confirmed.
 */
@Composable
fun PendingScreen(
    pending: List<TransactionEntity>,
    knownCategories: List<String>,
    onConfirm: (id: Long, purpose: String, category: String) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (pending.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "Koi pending entry nahi 🎉\nNaya bank SMS aate hi yahan dikhega.",
                fontSize = 13.sp, color = InkSoft
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Text(
                "${pending.size} entries confirm karni hain — har ek ke liye likho ki payment kis liye thi, aur category check karo.",
                fontSize = 12.sp, color = InkSoft
            )
        }
        items(pending, key = { it.id }) { t ->
            PendingCard(t, knownCategories, onConfirm, onDelete)
        }
    }
}

@Composable
private fun PendingCard(
    t: TransactionEntity,
    knownCategories: List<String>,
    onConfirm: (Long, String, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var purpose by remember(t.id) { mutableStateOf("") }
    var category by remember(t.id) { mutableStateOf(t.category) }
    var expanded by remember { mutableStateOf(false) }
    var newCatMode by remember { mutableStateOf(false) }
    var newCatText by remember { mutableStateOf("") }

    val ready = purpose.trim().isNotEmpty()

    Card {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t.merchant,
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    modifier = Modifier.weight(1f), maxLines = 1
                )
                Text(
                    (if (t.type == "debit") "−" else "+") + formatInr(t.amount),
                    color = if (t.type == "debit") RedLedger else GreenLedger,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
            }
            if (t.rawSms.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    t.rawSms.take(140),
                    fontSize = 10.sp, color = InkSoft, lineHeight = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = purpose,
                onValueChange = { purpose = it },
                label = { Text("Yeh payment kis liye thi?") },
                placeholder = { Text("e.g. Sabzi, dost ko udhaar, hospital visit…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            if (newCatMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCatText,
                        onValueChange = { newCatText = it },
                        label = { Text("Nayi category") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (newCatText.isNotBlank()) {
                            category = newCatText.trim()
                            newCatMode = false
                        }
                    }) { Text("Set") }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        knownCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("+ Nayi category…") },
                            onClick = {
                                expanded = false
                                newCatMode = true
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Row {
                TextButton(onClick = { onDelete(t.id) }) {
                    Text("Hatao", color = RedLedger)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onConfirm(t.id, purpose.trim(), category) },
                    enabled = ready,
                    colors = ButtonDefaults.buttonColors(containerColor = if (ready) GreenLedger else Gold)
                ) {
                    Text("✓ Confirm")
                }
            }
        }
    }
}
