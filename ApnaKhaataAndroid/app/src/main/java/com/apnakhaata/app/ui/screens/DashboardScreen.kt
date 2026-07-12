package com.apnakhaata.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(transactions: List<TransactionEntity>) {
    val cal = Calendar.getInstance()
    val thisMonth = cal.get(Calendar.MONTH)
    val thisYear = cal.get(Calendar.YEAR)

    val monthTx = transactions.filter {
        val c = Calendar.getInstance().apply { timeInMillis = it.date }
        c.get(Calendar.MONTH) == thisMonth && c.get(Calendar.YEAR) == thisYear
    }
    val debit = monthTx.filter { it.type == "debit" }.sumOf { it.amount }
    val credit = monthTx.filter { it.type == "credit" }.sumOf { it.amount }
    val byCategory = monthTx.filter { it.type == "debit" }
        .groupBy { it.category }
        .mapValues { e -> e.value.sumOf { it.amount } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Is mahine kharcha", formatInr(debit), RedLedger, Modifier.weight(1f))
                StatCard("Is mahine aaya", formatInr(credit), GreenLedger, Modifier.weight(1f))
            }
        }
        if (byCategory.isNotEmpty()) {
            item {
                Card {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Category-wise kharcha (is mahine)",
                            fontWeight = FontWeight.Bold, fontSize = 14.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        CategoryPieChart(byCategory)
                    }
                }
            }
        }
        item {
            Text(
                "Recent entries",
                fontWeight = FontWeight.Bold, fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        items(transactions.take(15), key = { it.id }) { t ->
            TransactionRow(t)
        }
        if (transactions.isEmpty()) {
            item {
                Text(
                    "Abhi koi entry nahi. SMS aane par automatically add hogi, ya Settings me 'Scan SMS Inbox' dabao purane SMS import karne ke liye.",
                    fontSize = 12.sp, color = InkSoft,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = InkSoft)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
fun TransactionRow(t: TransactionEntity) {
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
                    Text(t.purpose, fontSize = 11.sp, color = InkSoft, maxLines = 1)
                }
                Text(
                    "${df.format(Date(t.date))} · ${t.category}",
                    fontSize = 10.sp, color = InkSoft
                )
            }
            Text(
                text = (if (t.type == "debit") "−" else "+") + formatInr(t.amount),
                color = if (t.type == "debit") RedLedger else GreenLedger,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
