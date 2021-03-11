package me.alex

import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.system.exitProcess

fun main() {
    val input = getInput()
    val threads = input[0]
    val upperBound = input[1]
    val lowerBound = input[2]
    val N = input[3]
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
}

fun calculateChunkSize(threads: Int, upperBound: Int, lowerBound: Int) {

}

fun getInput() : Array<Int> {
    println("Enter the number of threads to use (recommended not to use the number of threads your system has)")
    val threadStr = readLine()
    println("Enter the upper bound to check up to")
    val upperBoundStr = readLine()
    println("Enter the lower bound")
    val lowerBoundStr = readLine()
    println("Enter N")
    val nStr = readLine()
    if (threadStr.isNullOrEmpty() || upperBoundStr.isNullOrEmpty() || lowerBoundStr.isNullOrEmpty() || nStr.isNullOrEmpty()) {
        println("You need to enter valid inputs, crashing program now...")
        exitProcess(1)
    }
    return arrayOf(threadStr.toInt(), upperBoundStr.toInt(), lowerBoundStr.toInt(), nStr.toInt())
}

class TheMaths(private val lowerBound : Int, private val upperBound : Int, val N : Int, private var threadNumber : Int, private val numberOfThreads : Int) : Runnable {
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

    fun nStratum(x : Int, N : Int) : Int {
        return sigma(N) * (x/N + 1)
    }

    private fun nTerriblePerfect() : IntArray {
        val sigmaN = 2 * N
        val terrible : IntArray = IntArray((upperBound - lowerBound) / numberOfThreads)
        var maxInd = 0
        for (i in (lowerBound + threadNumber)..upperBound step numberOfThreads) {
            val mod = i % N
            val secondBoolean : Boolean = (sigmaN * ((i / N.toDouble()) + 1)) == sigma(i).toDouble()
            if (mod != 0 && secondBoolean) {
                terrible[maxInd] = i
                maxInd += 1
            }
        }
        return terrible.sliceArray(0 until maxInd)
    }

    override fun run() {
        nTerriblePerfect().forEach { println(it) }
    }
}

