package com.example.data.repository

import com.example.data.db.FinanceDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {

    // --- Categories ---
    val allCategories: Flow<List<CategoryEntity>> = dao.getAllCategories()

    suspend fun addCategory(category: CategoryEntity): Long = dao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) = dao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) = dao.deleteCategory(category)

    // --- Transactions ---
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    fun getTransactionsInRange(start: Long, end: Long): Flow<List<TransactionEntity>> =
        dao.getTransactionsInRange(start, end)

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = dao.insertTransaction(transaction)
        // Automatically adjust account balance if an account is linked
        if (transaction.accountId > 0) {
            val delta = if (transaction.type == "INCOME") transaction.amount else -transaction.amount
            dao.adjustAccountBalance(transaction.accountId, delta)
        }
        return id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        // Reverse account balance adjustment
        if (transaction.accountId > 0) {
            val reverseDelta = if (transaction.type == "INCOME") -transaction.amount else transaction.amount
            dao.adjustAccountBalance(transaction.accountId, reverseDelta)
        }
        dao.deleteTransaction(transaction)
    }

    suspend fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        // Reverse old adjustment, apply new adjustment
        if (oldTransaction.accountId > 0) {
            val oldDelta = if (oldTransaction.type == "INCOME") -oldTransaction.amount else oldTransaction.amount
            dao.adjustAccountBalance(oldTransaction.accountId, oldDelta)
        }

        if (newTransaction.accountId > 0) {
            val newDelta = if (newTransaction.type == "INCOME") newTransaction.amount else -newTransaction.amount
            dao.adjustAccountBalance(newTransaction.accountId, newDelta)
        }

        dao.updateTransaction(newTransaction)
    }

    // --- Accounts ---
    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()

    suspend fun addAccount(account: AccountEntity): Long = dao.insertAccount(account)

    suspend fun updateAccount(account: AccountEntity) = dao.updateAccount(account)

    suspend fun deleteAccount(account: AccountEntity) = dao.deleteAccount(account)

    // --- Budgets ---
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()

    suspend fun addBudget(budget: BudgetEntity): Long = dao.insertBudget(budget)

    suspend fun updateBudget(budget: BudgetEntity) = dao.updateBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) = dao.deleteBudget(budget)

    // --- Savings Goals ---
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()

    suspend fun addSavingsGoal(goal: SavingsGoalEntity): Long = dao.insertSavingsGoal(goal)

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = dao.updateSavingsGoal(goal)

    suspend fun adjustGoalAmount(id: Long, delta: Double) = dao.adjustGoalAmount(id, delta)

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = dao.deleteSavingsGoal(goal)

    // --- Recurring Bills ---
    val allRecurringBills: Flow<List<RecurringBillEntity>> = dao.getAllRecurringBills()

    suspend fun addRecurringBill(bill: RecurringBillEntity): Long = dao.insertRecurringBill(bill)

    suspend fun updateRecurringBill(bill: RecurringBillEntity) = dao.updateRecurringBill(bill)

    suspend fun toggleBillPaidStatus(id: Long, currentPaidStatus: Boolean) =
        dao.setBillPaidStatus(id, !currentPaidStatus)

    suspend fun deleteRecurringBill(bill: RecurringBillEntity) = dao.deleteRecurringBill(bill)

    // --- Full Database Reset (Fresh Start) ---
    suspend fun resetAllDataToFreshStart() {
        dao.clearAllTransactions()
        dao.clearAllAccounts()
        dao.clearAllBudgets()
        dao.clearAllSavingsGoals()
        dao.clearAllRecurringBills()
        dao.clearAllCategories()
        val defaultCategories = FinanceCategories.defaultExpenseCategories + FinanceCategories.defaultIncomeCategories
        dao.insertCategories(defaultCategories)
    }
}
