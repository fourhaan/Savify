package com.example.bugettracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.findNestedRecyclerView
import com.example.budget.AddTransactionActivity
import com.example.budget.AppDatabase
import com.example.budget.Transaction
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_add_transaction.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: List<Transaction>
    private lateinit var oldtransactions: List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        trasaction = arrayListOf()

        transactionAdapter = TransactionAdapter(transaction)
        LayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(this,
        AppDatabase::class.java,
        'transactions').build()

        RecyclerView.apply{
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager()
        }
        fetchAll()

//        Swipe to remove

        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(recycleview)

        addBtn.setOnClickListener{
            val intent=Intent(this,AddTransactionActivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchAll(){
        GlobalScope.launch {
            transactions = db.transactionDao().getall()

            runOnUiThread{
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }
    private fun updateDashboard()
    {
        val totalAmount = transaction.map { it.amount }.sum()
        val bugetAmount = transaction.filter { it.amount>0 }.map { it.amount }.sum()
        val expenseamount = totalAmount - bugetAmount

        balance.text = "$ %2f".format(totalAmount)
        buget.text = "$ %2f".format(bugetAmount)
        expense.text = "$ %2f".format(expenseamount)
    }

    private fun undoDelete(){
        GlobalScope.launch {
            db.transactionDao().intertAll(deletedTransaction)
            transactions = oldtransactions

            runOnUiThread {
                updateDashboard()
            }
        }
    }

    private fun showSnackbar(){
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view,"Transaction Deleted!",Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(this,R.color.red))
            .setTextColor(ContextCompat.getColor(this,R.color.white))
            .show()
    }

    private fun deleteTransaction(transaction:Transaction){
        deletedTransaction = transaction
        oldtransactions = transactions

        GlobalScope.launch{
            db.transactionDao().delete(transaction)

            transactions = transactions.filter{it.id != transaction.id}
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
                showSnackbar()
            }
        }
    }

    override fun onResume(){
        super.onResume()
        fetchAll()
    }

}