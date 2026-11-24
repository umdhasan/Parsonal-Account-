package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, timestamp DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, timestamp DESC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()

    @Query("SELECT * FROM loans ORDER BY status ASC, date DESC, timestamp DESC")
    fun observeLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans ORDER BY status ASC, date DESC, timestamp DESC")
    suspend fun getAllLoansOnce(): List<LoanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoan(loan: LoanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLoans(loans: List<LoanEntity>)

    @Query("DELETE FROM loans WHERE id = :id")
    suspend fun deleteLoanById(id: String)

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Transaction
    suspend fun replaceAllData(
        transactions: List<TransactionEntity>,
        loans: List<LoanEntity>,
    ) {
        clearTransactions()
        clearLoans()
        if (transactions.isNotEmpty()) upsertTransactions(transactions)
        if (loans.isNotEmpty()) upsertLoans(loans)
    }

    @Transaction
    suspend fun mergeData(
        transactions: List<TransactionEntity>,
        loans: List<LoanEntity>,
    ) {
        if (transactions.isNotEmpty()) upsertTransactions(transactions)
        if (loans.isNotEmpty()) upsertLoans(loans)
    }
}
