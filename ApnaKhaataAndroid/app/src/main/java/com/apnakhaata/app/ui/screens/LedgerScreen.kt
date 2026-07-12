@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apnakhaata.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnakhaata.app.data.TransactionEntity
import com.apnakhaata.app.ui.theme.GreenLedger
import com.apnakhaata.app.ui.theme.InkSoft
import com.apnakhaata.app.ui.theme.RedLedger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LedgerScreen(
    transactions: List<TransactionEntity>,
    knownCategories: List<String>,
    onUpdateCategory: (Long, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf<TransactionEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<TransactionEntity?>(null) }

    val confirmed = transactions.filter { it.confirmed }
    val filtered = if (query.isBlank()) confirmed else confirmed.filter {
        it.merchant.contains(query, true) ||
        it.purpose.contains(query, true) ||
        it.category.contains(query, true)
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search (merchant / purpose / category)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 14.dp)
        ) {
            items(filtered, key = { it.id }) { t ->
                LedgerRow(
                    t = t,
                    onEditCategory = { editTarget = t },
                    onDelete = { deleteTarget = t }
                )
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "Koi entry nahi mili.",
                        fontSize = 12.sp, color = InkSoft,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        }
    }

    // Category edit dialog
    editTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text("Category badlo — ${t.merchant}", fontSize = 15.sp) },
            text = {
                Column {
                    knownCategories.forEach { cat ->
                        Text(
                            text = (if (cat == t.category) "●  " else "○  ") + cat,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateCategory(t.id, cat)
                                    editTarget = null
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { editTarget = null }) { Text("Band karo") }
            }
        )
    }

    // Delete confirm dialog
    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Entry delete karein?") },
            text = { Text("${t.merchant} — ${formatInr(t.amount)}\nYeh wapas nahi aayegi.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(t.id)
                    deleteTarget = null
                }) { Text("Haan, delete", color = RedLedger) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Nahi") }
            }
        )
    }
}

@Composable
private fun LedgerRow(
    t: TransactionEntity,
    onEditCategory: () -> Unit,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(t.merchant, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
                if (t.purpose.isNotBlank()) {
                    Text(t.purpose, fontSize = 11.sp, color = InkSoft, maxLines = 2)
                }
                Text(
                    "${df.format(Date(t.date))} · ",
                    fontSize = 10.sp, color = InkSoft
                )
                AssistChip(
                    onClick = onEditCategory,
                    label = { Text(t.category, fontSize = 10.sp) }
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (t.type == "debit") "−" else "+") + formatInr(t.amount),
                    color = if (t.type == "debit") RedLedger else GreenLedger,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint = InkSoft, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
