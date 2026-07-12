package com.apnakhaata.app.ui.screens

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
import com.apnakhaata.app.data.CategoryRule
import com.apnakhaata.app.ui.theme.InkSoft

@Composable
fun RulesScreen(
    rules: List<CategoryRule>,
    onAdd: (String, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
        Spacer(Modifier.height(10.dp))
        Text(
            "Keyword rules — agar SMS/merchant text me keyword mile, toh woh category automatically lag jaati hai.",
            fontSize = 12.sp, color = InkSoft
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = keyword, onValueChange = { keyword = it },
                label = { Text("Keyword") }, singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = category, onValueChange = { category = it },
                label = { Text("Category") }, singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                if (keyword.isNotBlank() && category.isNotBlank()) {
                    onAdd(keyword, category)
                    keyword = ""; category = ""
                }
            },
            enabled = keyword.isNotBlank() && category.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("+ Rule add karo") }

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 14.dp)
        ) {
            items(rules, key = { it.id }) { rule ->
                Card {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            rule.keyword,
                            fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text("→  " + rule.category, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDelete(rule.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete",
                                tint = InkSoft, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
