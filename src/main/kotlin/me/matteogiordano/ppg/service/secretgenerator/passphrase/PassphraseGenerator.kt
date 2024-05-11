package me.matteogiordano.ppg.service.secretgenerator.passphrase

import jakarta.enterprise.context.ApplicationScoped
import me.matteogiordano.ppg.model.secret.Password
import me.matteogiordano.ppg.service.secretgenerator.GeneratorBase
import me.matteogiordano.ppg.service.secretgenerator.SecretStrength
import java.security.SecureRandom
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

const val DEFAULT_VOCALS = "aeiouy"
const val DEFAULT_CONSONANTS = "bcdfghjklmnpqrstvwxz"

@ApplicationScoped
class PassphraseGenerator(
        val vocals: String = DEFAULT_VOCALS,
        val consonants: String = DEFAULT_CONSONANTS,
        private val digits: String = DEFAULT_DIGITS,
        private val specialChars: String = DEFAULT_SPECIAL_CHARS,
        private val extendedSpecialChars: String = EXTENDED_SPECIAL_CHARS,
        secureRandom: SecureRandom? = null,
    ): GeneratorBase<PassphraseGeneratorSpec>(secureRandom) {


    override fun generate(spec: PassphraseGeneratorSpec): Password {
        val buffer = Password(CharArray(0))

        for (i in 0 until strengthToWordCount(spec.strength)) {
            val word = generateWord()
            buffer.add(word)
        }

        if (spec.wordBeginningUpperCase) {
            buffer.replace(0, buffer[0].uppercaseChar())
        }

        if (spec.addDigit) {
            buffer.add(random(digits))
        }

        if (spec.addSpecialChar) {
            if (spec.useExtendedSpecialChars) {
                buffer.add(random(specialChars + extendedSpecialChars))
            }
            else {
                buffer.add(random(specialChars))
            }
        }

        return buffer
    }

    private fun generateWord(): Password {
        val word = generateTuple()
        val recentEndsWithVocal = isVocal(word.last())
        val next = generateTuple(recentEndsWithVocal)

        word.add(next)

        return word
    }

    private fun generateTuple(recentEndsWithVocal: Boolean = false): Password {
        val buffer = CharArray(2)
        buffer[0] = random(alphabet())
        val char = buffer[0]
        var material = when(isVocal(char)) {
            true ->  alphabet()
            else -> vocals
        }
        if (recentEndsWithVocal && isConsonant(char)) {
            material += char
        }

        buffer[1] = random(material)
        return Password(buffer)
    }

    override fun calcCombinationCount(spec: PassphraseGeneratorSpec): Double {
        val vocalLength = vocals.length.toDouble()
        val consonantLength = consonants.length.toDouble()
        val alphabetLength = alphabet().length.toDouble()

        val vocalChance = vocalLength / alphabetLength
        val consonantChance = consonantLength / alphabetLength

        val tupleCombinations = alphabetLength * ((vocalChance * alphabetLength) + (consonantChance * vocalLength)) // this is the material = when(isVocal) expression
        val tupleCombinationsWithVocalAtLast = vocalLength * alphabetLength // these tuples allow subsequent tuples with duplicate consonants

        val wordCombinations = floor((tupleCombinations * tupleCombinations) + (tupleCombinationsWithVocalAtLast * consonantLength)) // add tuples with duplicate consonants for each consonant
        var totalCombinations = ceil(wordCombinations.pow(strengthToWordCount(spec.strength).toDouble()))

        if (spec.addDigit) {
            totalCombinations *= digits.length
        }
        if (spec.addSpecialChar) {
            totalCombinations *= if (spec.useExtendedSpecialChars) {
                (extendedSpecialChars.length + specialChars.length)
            } else {
                specialChars.length
            }
        }
        return totalCombinations
    }

    private fun isVocal(char: Char): Boolean {
        return vocals.contains(char, true)
    }

    private fun isConsonant(char: Char): Boolean {
            return consonants.contains(char, true)
    }

    private fun strengthToWordCount(strength: SecretStrength): Int {
        return strength.pseudoPhraseLength / 4
    }

    private fun alphabet() = vocals + consonants

}
