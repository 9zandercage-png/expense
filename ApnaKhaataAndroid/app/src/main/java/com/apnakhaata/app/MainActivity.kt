@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.apnakhaata.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apnakhaata.app.ui.AppViewModel
import com.apnakhaata.app.ui.screens.*
import com.apnakhaata.app.ui.theme.ApnaKhaataTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val openTab = intent?.getStringExtra("open_tab")
        setContent {
            ApnaKhaataTheme {
                AppRoot(initialTab = if (openTab == "pending") 2 else 0)
            }
        }
    }
}

private fun hasSmsPermission(ctx: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
    ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED

@Composable
fun AppRoot(initialTab: Int, vm: AppViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var smsGranted by remember { mutableStateOf(hasSmsPermission(context)) }
    var showAddDialog by remember { mutableStateOf(false) }

    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    val rules by vm.rules.collectAsStateWithLifecycle()

    val knownCategories = remember(rules, transactions) {
        (rules.map { it.category } + transactions.map { it.category } +
            listOf("Family / Transfers", "Uncategorised")).distinct().sorted()
    }

    // ---- Permission launcher ----
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        smsGranted = (result[Manifest.permission.RECEIVE_SMS] == true &&
                result[Manifest.permission.READ_SMS] == true) || hasSmsPermission(context)
    }

    fun requestPermissions() {
        val perms = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    // First launch: ask permissions automatically
    LaunchedEffect(Unit) {
        if (!smsGranted) requestPermissions()
    }

    // ---- Backup (create file) ----
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = vm.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(context, "Backup save ho gaya ✓", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Backup fail: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // ---- Restore (open file) ----
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(Charsets.UTF_8)
                    } ?: ""
                    val inserted = vm.importJson(text)
                    Toast.makeText(context, "$inserted entries restore hui ✓", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Restore fail: file sahi backup nahi lag rahi", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apna Khaata", fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add entry")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Ledger", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        if (pending.isNotEmpty()) {
                            BadgedBox(badge = { Badge { Text("${pending.size}") } }) {
                                Icon(Icons.Default.Notifications, null)
                            }
                        } else {
                            Icon(Icons.Default.Notifications, null)
                        }
                    },
                    label = { Text("Pending", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Edit, null) },
                    label = { Text("Rules", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings", fontSize = 10.sp) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> DashboardScreen(transactions)
                1 -> LedgerScreen(
                    transactions = transactions,
                    knownCategories = knownCategories,
                    onUpdateCategory = vm::updateCategory,
                    onDelete = vm::deleteTransaction
                )
                2 -> PendingScreen(
                    pending = pending,
                    knownCategories = knownCategories,
                    onConfirm = vm::confirmTransaction,
                    onDelete = vm::deleteTransaction
                )
                3 -> RulesScreen(
                    rules = rules,
                    onAdd = vm::addRule,
                    onDelete = vm::deleteRule
                )
                4 -> SettingsScreen(
                    smsPermissionGranted = smsGranted,
                    onRequestPermissions = { requestPermissions() },
                    onScanInbox = {
                        Toast.makeText(context, "Inbox scan ho raha hai…", Toast.LENGTH_SHORT).show()
                        vm.scanInbox { count ->
                            Toast.makeText(
                                context,
                                "$count naye transactions mile — Pending tab me confirm karo",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onBackup = {
                        val name = "apna-khaata-backup-" +
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".json"
                        backupLauncher.launch(name)
                    },
                    onRestore = {
                        restoreLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            knownCategories = knownCategories,
            autoCategorize = vm::autoCategorize,
            onDismiss = { showAddDialog = false },
            onSave = { merchant, purpose, amount, type, category, dateMillis ->
                vm.addManual(merchant, purpose, amount, type, category, dateMillis)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddEntryDialog(
    knownCategories: List<String>,
    autoCategorize: (String) -> String,
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, String, Long) -> Unit
) {
    var merchant by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("debit") }
    var category by remember { mutableStateOf("Uncategorised") }
    var catExpanded by remember { mutableStateOf(false) }
    var dateText by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date()))
    }
    var categoryTouched by remember { mutableStateOf(false) }

    // Auto-suggest category as merchant types (until user manually picks one)
    LaunchedEffect(merchant, purpose) {
        if (!categoryTouched && (merchant.isNotBlank() || purpose.isNotBlank())) {
            category = autoCategorize("$merchant $purpose")
        }
    }

    val amount = amountText.replace(",", "").toDoubleOrNull()
    val valid = merchant.isNotBlank() && amount != null && amount > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nayi entry", fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = merchant, onValueChange = { merchant = it },
                    label = { Text("Merchant / Naam") }, singleLine = true
                )
                OutlinedTextField(
                    value = purpose, onValueChange = { purpose = it },
                    label = { Text("Kis liye?") }, singleLine = true
                )
                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = dateText, onValueChange = { dateText = it },
                    label = { Text("Date (dd/MM/yyyy)") }, singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "debit",
                        onClick = { type = "debit" },
                        label = { Text("Debit (gaya)") }
                    )
                    FilterChip(
                        selected = type == "credit",
                        onClick = { type = "credit" },
                        label = { Text("Credit (aaya)") }
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        knownCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryTouched = true
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    val millis = try {
                        SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(dateText)?.time
                            ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    onSave(merchant.trim(), purpose.trim(), amount!!, type, category, millis)
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
