package com.apnakhaata.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, CategoryRule::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun ruleDao(): CategoryRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apna_khaata.db"
                ).build().also { INSTANCE = it }
            }

        val DEFAULT_RULES = listOf(
            CategoryRule(keyword = "zepto", category = "Groceries"),
            CategoryRule(keyword = "blinkit", category = "Groceries"),
            CategoryRule(keyword = "bigbasket", category = "Groceries"),
            CategoryRule(keyword = "kirana", category = "Groceries"),
            CategoryRule(keyword = "swiggy", category = "Eating Out"),
            CategoryRule(keyword = "zomato", category = "Eating Out"),
            CategoryRule(keyword = "kabab", category = "Eating Out"),
            CategoryRule(keyword = "restaurant", category = "Eating Out"),
            CategoryRule(keyword = "hotel", category = "Eating Out"),
            CategoryRule(keyword = "medical", category = "Medical"),
            CategoryRule(keyword = "pharmacy", category = "Medical"),
            CategoryRule(keyword = "hospital", category = "Medical"),
            CategoryRule(keyword = "clinic", category = "Medical"),
            CategoryRule(keyword = "lab", category = "Medical"),
            CategoryRule(keyword = "recharge", category = "Mobile Recharge"),
            CategoryRule(keyword = "jio", category = "Mobile Recharge"),
            CategoryRule(keyword = "airtel", category = "Mobile Recharge"),
            CategoryRule(keyword = "emi", category = "EMI / Loan"),
            CategoryRule(keyword = "loan", category = "EMI / Loan"),
            CategoryRule(keyword = "snapmint", category = "EMI / Loan"),
            CategoryRule(keyword = "flipkart", category = "Shopping"),
            CategoryRule(keyword = "amazon", category = "Shopping"),
            CategoryRule(keyword = "myntra", category = "Shopping"),
            CategoryRule(keyword = "petrol", category = "Fuel"),
            CategoryRule(keyword = "fuel", category = "Fuel"),
            CategoryRule(keyword = "salary", category = "Salary"),
            CategoryRule(keyword = "atm", category = "Cash Withdrawal"),
            CategoryRule(keyword = "google", category = "Subscriptions"),
            CategoryRule(keyword = "netflix", category = "Subscriptions"),
            CategoryRule(keyword = "maintenance", category = "Society / Rent"),
            CategoryRule(keyword = "rent", category = "Society / Rent")
        )
    }
}
