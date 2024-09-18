package com.example.coroutines

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

const val N = 100

class MainActivity : ComponentActivity() {
    class Account {

        private var amount: Double = 0.0
        private val mutex = Mutex()

        suspend fun deposit(amount: Double) {
            mutex.withLock {
                val x = this.amount
                delay(1) // simulates processing time
                this.amount = x + amount
            }
        }

        fun saldo(): Double = amount
    }

    /* Approximate measurement of the given block's execution time */
    fun withTimeMeasurement(title: String, isActive: Boolean = true, code: () -> Unit) {
        if (!isActive) return
        val time = measureTimeMillis { code() }
        Log.i("COR", "operation in '$title' took ${time} ms")
    }

    data class Saldos(val saldo1: Double, val saldo2: Double)

    fun bankProcess(account: Account): Saldos {
        var saldo1: Double = 0.0
        var saldo2: Double = 0.0

        /* we measure the execution time of one deposit task with zero deposit amounts */
        withTimeMeasurement("Single coroutine deposit $N times") {
            runBlocking {
                val job = launch {
                    for (i in 1..N) account.deposit(0.0)
                }
                job.join()
            }
            saldo1 = account.saldo()
            Log.i("COR", "Saldo1 after single coroutine: $saldo1")
        }

        /* then we measure the execution time of two simultaneous deposit tasks using coroutines */
        withTimeMeasurement("Two $N times deposit coroutines together", isActive = true) {
            runBlocking {
                val job1 = launch {
                    for (i in 1..N) account.deposit(1.0)
                }
                val job2 = launch {
                    for (i in 1..N) account.deposit(1.0)
                }
                // Wait for both jobs to complete
                job1.join()
                job2.join()
                saldo2 = account.saldo()
            }
            Log.i("COR", "Saldo2 after two coroutines: $saldo2") // Debug output
        }

        return Saldos(saldo1, saldo2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val results = bankProcess(Account())
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShowResults(saldo1 = results.saldo1, saldo2 = results.saldo2)
                }
            }
        }
    }
}

@Composable
fun ShowResults(saldo1: Double, saldo2: Double) {
    Column {
        Text(text = "Saldo1: $saldo1")
        Text(text = "Saldo2: $saldo2")
    }
}