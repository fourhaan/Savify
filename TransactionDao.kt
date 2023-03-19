package com.example.budget

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TransactionDao {
    @Query ("SELECT * from transactions")
    fun getAll(): List<Transaction>

    @Insert
    fun intertAll(vararg transaction: Transaction)

    @Delete
    fun delete(transaction: Transaction)

    @Udpate
    fun update(vararg transaction: Transaction)
}