package me.matteogiordano.ppg.service.secretgenerator.passphrase

import me.matteogiordano.ppg.service.secretgenerator.GeneratorSpec
import me.matteogiordano.ppg.service.secretgenerator.SecretStrength

data class PassphraseGeneratorSpec(
        val strength: SecretStrength = SecretStrength.NORMAL,
        val wordBeginningUpperCase: Boolean = false,
        val addDigit: Boolean = false,
        val addSpecialChar: Boolean = false,
        val useExtendedSpecialChars: Boolean = false,
): GeneratorSpec
