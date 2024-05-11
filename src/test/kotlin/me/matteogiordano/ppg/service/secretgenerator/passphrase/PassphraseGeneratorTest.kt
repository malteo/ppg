package me.matteogiordano.ppg.service.secretgenerator.passphrase

import io.quarkus.test.junit.main.QuarkusMainTest
import me.matteogiordano.ppg.service.secretgenerator.GeneratorBase.Companion.BRUTEFORCE_ATTEMPTS_PENTIUM
import me.matteogiordano.ppg.service.secretgenerator.GeneratorBase.Companion.BRUTEFORCE_ATTEMPTS_SUPERCOMP
import me.matteogiordano.ppg.service.secretgenerator.SecretStrength
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.SecureRandom
import java.util.concurrent.atomic.AtomicInteger

@QuarkusMainTest
class PassphraseGeneratorTest {

    private val rnd = SecureRandom()

    @Test
    fun generateAndClearPassphrase() {
        val spec = PassphraseGeneratorSpec(
            SecretStrength.STRONG,
            wordBeginningUpperCase = true, addDigit = false, addSpecialChar = true)
        val passphraseGenerator = PassphraseGenerator(secureRandom = rnd)

        for (i in 0..100) {
            val passphrase = passphraseGenerator.generate(spec)
            println("passphrase = ${passphrase.toFormattedPassword()}")
            passphrase.clear()
        }

        val calcCombinationCount = passphraseGenerator.calcCombinationCount(spec)
        println("combinations: $calcCombinationCount")

        val calcBruteForceWaitingPentiumSeconds = passphraseGenerator.calcBruteForceWaitingSeconds(calcCombinationCount, BRUTEFORCE_ATTEMPTS_PENTIUM)
        val calcBruteForceWaitingSupercompSeconds = passphraseGenerator.calcBruteForceWaitingSeconds(calcCombinationCount, BRUTEFORCE_ATTEMPTS_SUPERCOMP)

        println("brute force years for Pentum: ${calcBruteForceWaitingPentiumSeconds.secondsToYear()}")
        println("brute force years for Supercomp: ${calcBruteForceWaitingSupercompSeconds.secondsToYear()}")
    }

    @Test
    fun testCalcCombinations() {
        val spec = PassphraseGeneratorSpec(SecretStrength.ONE_WORD, addSpecialChar = true, useExtendedSpecialChars = false)
        val passphraseGenerator = PassphraseGenerator(vocals = "ai", consonants = "hst", secureRandom = rnd)

        val hits = HashSet<String>()
        for (i in 0..500000) {
            val passphrase = passphraseGenerator.generate(spec)
            hits.add(passphrase.toString())
            passphrase.clear()
        }

        val combinationCount = passphraseGenerator.calcCombinationCount(spec)

        println("hits=$hits")
        println("real combinations: ${hits.size}")
        println("calculated combinations: $combinationCount")

        Assertions.assertEquals(hits.size.toDouble(), combinationCount, 0.1)

    }

    @Test
    fun testCalcCombinationsRealWord() {
        val spec = PassphraseGeneratorSpec(SecretStrength.ONE_WORD)
        val passphraseGenerator = PassphraseGenerator(secureRandom = rnd)

        val combinationCount = passphraseGenerator.calcCombinationCount(spec)
        println("calculated combinations: $combinationCount")

        val hits = HashSet<String>()
        val counter = AtomicInteger()
        while (hits.size < combinationCount.toLong()) {
            val passphrase = passphraseGenerator.generate(spec)
            val new = hits.add(passphrase.toString())
            if (new || counter.incrementAndGet() % 100000 == 0) {
                println("current attempt: ${counter.get()} (${hits.size} < ${combinationCount.toLong()}): $passphrase isNew=$new")
            }
            passphrase.clear()

        }

        println("real combinations: ${hits.size}")

        Assertions.assertEquals(hits.size.toDouble(), combinationCount, 0.1)

    }

    private fun Double.secondsToYear(): Double {
        return this / 60 / 60 / 24 / 365
    }
}
