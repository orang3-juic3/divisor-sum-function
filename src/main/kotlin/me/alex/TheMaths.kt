import java.util.*
import java.util.concurrent.RecursiveTask
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.math.sqrt

class Sigma(private val lowerBound : Int, private val upperBound : Int, val N : Int, val step : Int) : RecursiveTask<List<Int>>() {
    fun computeSigma(n: Int): Int {
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
        if (difference > 1000000) {
            val mid = (lowerBound + difference / 2)
            val left = Sigma(lowerBound, mid, N, step)
            val right = Sigma(mid, upperBound, N, step)
            invokeAll(left, right)
            return mergeParts(left, right)
        }
        else {
            return nTerriblePerfect()
        }
    }

    private fun mergeParts(left : Sigma, right : Sigma) : List<Int> {
        val list : MutableList<Int> = ArrayList(left.rawResult)
        list.addAll(right.rawResult)
        list.sort()
        return list
    }

}
