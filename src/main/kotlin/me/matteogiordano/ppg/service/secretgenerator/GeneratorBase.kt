package me.matteogiordano.ppg.service.secretgenerator

import me.matteogiordano.ppg.model.secret.Password
import java.security.SecureRandom
import kotlin.math.log2

abstract class GeneratorBase<T : GeneratorSpec>(
    private val secureRandom: SecureRandom? = null
) {

    companion object {
        /*
         Attention: Changing any of the default sets here can break de-obfuscation!!!!!! See Loop.kt what is actually used.
         */
        const val DEFAULT_DIGITS = "0123456789"
        const val DEFAULT_SPECIAL_CHARS = "!?-,.:/$&@#_;+*"

        const val EXTENDED_SPECIAL_CHARS = "()[]{}<>\"'=%\\~|"

        const val BRUTEFORCE_ATTEMPTS_PENTIUM = 100_000 // per second
        const val BRUTEFORCE_ATTEMPTS_SUPERCOMP = 1_000_000_000 // per second
    }

    abstract fun generate(spec: T): Password

    abstract fun calcCombinationCount(spec: T): Double

    fun calcEntropy(combinations: Double): Double {
        return log2(combinations)
    }

    fun calcBruteForceWaitingSeconds(combinations: Double, tryPerSec: Int): Double {
        return combinations / tryPerSec
    }

    internal fun random(material: String): Char {
        val rand = secureRandom ?: SecureRandom()
        val index = rand.nextInt(material.length)

        return material[index]
    }

}
