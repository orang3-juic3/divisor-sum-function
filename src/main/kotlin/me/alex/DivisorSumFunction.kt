package me.alex

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.ValueRange
import java.util.*
import java.util.concurrent.ForkJoinPool
import java.util.stream.Collectors
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.system.exitProcess

data class GoogleApi(val sheets: Sheets, val startCol: Char, val id: String)

fun main() {

    val sheetsService: Sheets = SheetsService.sheetsService

    val input = getInput()
    val upperBound = input[0].toInt()
    val lowerBound = input[1].toInt()
    val N = input[2].toInt()
    val id = input[3]
    var targetNumber = 66
    var range: ValueRange = sheetsService.spreadsheets().values().get(id,"Sheet1!${targetNumber.toChar()}1").execute()
    var results: List<List<Any>?>? = range.getValues()
    while (results != null) {
        val result = results[0]?.get(0)
        if (result is String) {
            if (result == N.toString()) {
                break
            }
        }
        targetNumber++
        range = sheetsService.spreadsheets().values().get(id, "Sheet1!${targetNumber.toChar()}1").execute()
        results = range.getValues()
    }
    if (results == null) {
        range = ValueRange().setValues(listOf(listOf(N)))
        sheetsService.spreadsheets().values().update(id, "Sheet1!${targetNumber.toChar()}1", range).setValueInputOption("RAW").execute()
    }
    print(targetNumber)
    range = sheetsService.spreadsheets().values().get(id, "Sheet1!${targetNumber.toChar()}2").execute()
    results = range.getValues()
    var replace=  true
    if (results != null) {
        val result = results[0]?.get(0)
        if (result is String) {
            val max: Double = result.toDouble()
            if (max >= upperBound) {
                replace = false
            }
        }
    }
    if (replace) {
        range = ValueRange().setValues(listOf(listOf(upperBound)))
        sheetsService.spreadsheets().values().update(id, "Sheet1!${targetNumber.toChar()}2", range).setValueInputOption("RAW").execute()
    }

    /*
    val newFixedThreadPool = Executors.newFixedThreadPool(threads);
    val futureArray : MutableList<Future<*>> = ArrayList()
    for (i in 0 until threads) {
        val threadNumber = i + 1
        println("$threadNumber is being executed!")
        val maf : TheMaths = TheMaths(lowerBound, upperBound, N, i, threads)
        val submit = newFixedThreadPool.submit(maf)
        futureArray.add(submit)
    }
    var threadCounter = 1
    while (threadCounter <= threads) {
        Thread.sleep(1000)
        for (future in futureArray) {
            if (future.isDone) {
                println("$threadCounter is done!")
                threadCounter++
            }
        }
        futureArray.removeIf() { it.isDone }
    }
    exitProcess(0)
     */
    val forkJoinPool = ForkJoinPool.commonPool()
    val api =GoogleApi(sheetsService, targetNumber.toChar(), id)
    val initialTask = Sigma(lowerBound, upperBound, N, 1, api)
    val list : List<Int> = forkJoinPool.invoke(initialTask)
    updateSheets(api, list)
    list.forEach {println(it)}
}
fun updateSheets(api: GoogleApi, items: List<Int>) {
    if (items.isNullOrEmpty()) {
        println("Nothing to update!")
        return
    }
    val col = api.startCol
    val sheetsService = api.sheets
    val id = api.id
    val nums: MutableList<Int> = items.toMutableList()
    var index = 3
    var range: ValueRange = sheetsService.spreadsheets().values().get(id, "Sheet1!${col}${index}").execute()
    var results: List<List<Any>?>? = range.getValues()
    while (results != null) {
        val result = results[0]?.get(0)
        if (result is String) {
            nums.add(result.toInt())
        }
        index++
        range = sheetsService.spreadsheets().values().get(id, "Sheet1!${col}${index}").execute()
        results = range.getValues()
    }
    var replace: MutableList<MutableList<Any>> = mutableListOf()
    for (i in 3..index) {
        replace.add(mutableListOf(""))
    }
    sheetsService.spreadsheets().values().update(id, "Sheet1!${col}${3}:${col}${index}", ValueRange().setValues(replace)).setValueInputOption("RAW").execute()
    val finalItems: List<String> = nums.stream().distinct().sorted().map { it.toString() }.collect(Collectors.toList())
    replace = mutableListOf()
    for (i in finalItems) {
        replace.add(mutableListOf(i))
    }
    for (i in 0 until replace.size) {
        sheetsService.spreadsheets().values()
                .update(id, "Sheet1!${col}${i + 3}",
                        ValueRange().setValues(listOf(listOf(finalItems[i]))))
                .setValueInputOption("RAW").execute()
    }



}

fun calculateChunkSize(threads: Int, upperBound: Int, lowerBound: Int) {

}

fun getInput() : Array<String> {
    println("Enter the upper bound to check up to")
    val upperBoundStr: String? = readLine()
    println("Enter the lower bound")
    val lowerBoundStr: String? = readLine()
    println("Enter N")
    val nStr: String? = readLine()
    println("Enter the spreadsheet link")
    val rawLink: String? = readLine()
    val splits: Array<String> = rawLink!!.split("/").toTypedArray()
    val index = listOf(*splits).indexOf("d") + 1
    if (upperBoundStr!!.isEmpty() || lowerBoundStr!!.isEmpty() || nStr!!.isEmpty()) {
        println("You need to enter valid inputs, crashing program now...")
        exitProcess(1)
    }
    return arrayOf(upperBoundStr, lowerBoundStr, nStr, splits[index])
}

class TheMaths(private val lowerBound: Int, private val upperBound: Int, val N: Int, private var threadNumber: Int, private val numberOfThreads: Int) : Runnable {
    fun sigma(number: Int) : Int{
        if (number == 1) {
            return 1
        }
        var total : Int = 1 + number
        val lim : Int = floor(0.5 + sqrt(number.toDouble())).toInt()
        for (i in 2..lim) {
            val mod : Int = number % i
            if (mod == 0) {
                val div : Int = number / i
                total += i
                if (i != div) {
                    total += div
                }
            }
        }
        return total
    }

    fun experimentalSigma(n: Int): Int {
        // Traversing through all prime factors.
        var n = n
        var res = 1
        var i = 2
        while (i <= sqrt(n.toDouble())) {
            var curr_sum = 1
            var curr_term = 1
            while (n % i == 0) {
                // THE BELOW STATEMENT MAKES
                // IT BETTER THAN ABOVE METHOD
                // AS WE REDUCE VALUE OF n.
                n /= i
                curr_term *= i
                curr_sum += curr_term
            }
            res *= curr_sum
            i++
        }

        // This condition is to handle
        // the case when n is a prime
        // number greater than 2
        if (n > 2) res *= 1 + n
        return res
    }

    fun nStratum(x: Int, N: Int) : Int {
        return experimentalSigma(N) * (x/N + 1)
    }

    private fun nTerriblePerfect() : List<Int> {
        val sigmaN = 2 * N
        val terrible :  MutableList<Int> = ArrayList()
        for (i in (lowerBound + threadNumber)..upperBound step numberOfThreads) {
            val mod = i % N
            if (mod != 0 && (sigmaN * ((i / N.toDouble()) + 1)) == experimentalSigma(i).toDouble()) {
                terrible.add(i)
            }
        }
        return terrible
    }

    override fun run() {
        nTerriblePerfect().forEach { println(it) }
    }
}
