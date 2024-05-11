package me.matteogiordano.ppg

import io.quarkus.picocli.runtime.PicocliCommandLineFactory
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.validation.constraints.Positive
import me.matteogiordano.ppg.service.secretgenerator.SecretStrength
import me.matteogiordano.ppg.service.secretgenerator.passphrase.PassphraseGenerator
import me.matteogiordano.ppg.service.secretgenerator.passphrase.PassphraseGeneratorSpec
import picocli.CommandLine
import picocli.CommandLine.*


@Command(name = "greeting", mixinStandardHelpOptions = true)
class GeneratorCommand(
        private val passphraseGenerator: PassphraseGenerator,
) : Runnable {

    @Parameters(
            defaultValue = "1",
            description = ["How many passphrases to generate.\nDefault value \${DEFAULT-VALUE}"],
    )
    @Positive
    var count: Int = 1

    @Option(
            defaultValue = "NORMAL",
            description = ["Passphrase strength. Valid values: \${COMPLETION-CANDIDATES}.\nDefault value \${DEFAULT-VALUE}"],
            names = ["-s", "--strength"],
    )
    var strength: SecretStrength = SecretStrength.NORMAL

    @Option(
            description = ["Word beginning in upper case."],
            names = ["-u", "--uppercase"]
    )
    var wordBeginningUpperCase: Boolean = false

    @Option(
            description = ["Add a digit."],
            names = ["-d", "--digit"]
    )
    var addDigit: Boolean = false

    @Option(
            description = ["Add a special char."],
            names = ["-S", "--special"]
    )
    var addSpecialChar: Boolean = false

    @Option(
            description = ["Use extended special chars."],
            names = ["-e", "--extended"]
    )
    var useExtendedSpecialChars: Boolean = false

    override fun run() {
        val spec = PassphraseGeneratorSpec(
                strength = strength,
                wordBeginningUpperCase = wordBeginningUpperCase,
                addDigit = addDigit,
                addSpecialChar = addSpecialChar,
                useExtendedSpecialChars = useExtendedSpecialChars
        )

        for (i in 0 until count) {
            val passphrase = passphraseGenerator.generate(spec)
            println(passphrase.toFormattedPassword())
            passphrase.clear()
        }
    }

}

@ApplicationScoped
internal class CustomConfiguration {
    @Produces
    fun customCommandLine(factory: PicocliCommandLineFactory): CommandLine = factory.create()
            .setCaseInsensitiveEnumValuesAllowed(true)
}
