package me.alex

import com.google.api.services.sheets.v4.Sheets
import java.util.concurrent.RecursiveTask
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class Sigma(private val lowerBound : Int, private val upperBound : Int, private val N : Int, private val step : Int) : RecursiveTask<List<Int>>() {
    private val sheetsService: Sheets = SheetsService.sheetsService
    private fun computeSigma(n: Int): Int {
        // Traversing through all prime factors.
        var betterN = n
        var res = 1
        var i = 2
        while (i <= sqrt(betterN.toDouble())) {
            var currSum = 1
            var currTerm = 1
            while (betterN % i == 0) {
                // THE BELOW STATEMENT MAKES
                // IT BETTER THAN ABOVE METHOD
                // AS WE REDUCE VALUE OF n.
                betterN /= i
                currTerm *= i
                currSum += currTerm
            }
            res *= currSum
            i++
        }

        // This condition is to handle
        // the case when n is a prime
        // number greater than 2
        if (betterN > 2) res *= 1 + betterN
        return res
    }

    private fun nTerriblePerfect() : List<Int> {
        val sigmaN = 2 * N
        val terrible :  MutableList<Int> = ArrayList()
        for (i in lowerBound..upperBound step step) {
            val mod = i % N
            if (mod != 0 && (sigmaN * ((i / N.toDouble()) + 1)) == computeSigma(i).toDouble()) {
                terrible.add(i)
            }
        }
        return terrible
    }

    override fun compute(): List<Int> {
        val difference : Int = (upperBound - lowerBound)
        return if (difference > 1000000) {
            val mid = (lowerBound + difference / 2)
            val left = Sigma(lowerBound, mid, N, step)
            val right = Sigma(mid, upperBound, N, step)
            invokeAll(left, right)
            mergeParts(left, right)
        }
        else {
            nTerriblePerfect()
        }
    }

    private fun mergeParts(left : Sigma, right : Sigma) : List<Int> {
        val list : MutableList<Int> = ArrayList(left.rawResult)
        list.addAll(right.rawResult)
        list.sort()
        return list
    }

}
