package me.matteogiordano.ppg.model.secret

import me.matteogiordano.ppg.model.Clearable
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Represents a real password.
 *
 * Passwords are internally stored as ByteArray (encoded with system default UTF-8), not as String. This is due the VM may cache
 * all Strings internally and would them make visible in heap dumps. To at least reduce that risk
 * Strings are only created by the UI framework when displaying it (this is not in our hand unfortunately).
 * Furthermore Password instances should be cleared (#clear) if not anymore needed.
 *
 * To convert it to a readable CharSequence the ByteArray has first to be converted
 * to a CharArray. This happens without explicit Charset encoding, which means, all non-ASCII chars
 * may be displayed wrong. To get a CharArray decoded with system default Charset (UTF-8),
 * use #decodeToCharArray or #toFormattedPassword to retrieve a FormattedPassword.
 */
class Password: Secret, CharSequence {

    enum class FormattingStyle {
        IN_WORDS, IN_WORDS_MULTI_LINE, RAW;

        fun next(): FormattingStyle {
            val nextIdx = (ordinal + 1) % entries.size
            return entries[nextIdx]
        }

        fun isMultiLine(): Boolean = this == IN_WORDS_MULTI_LINE

        companion object {
            val DEFAULT = IN_WORDS
        }
    }

    /**
     * Represents a formatted real AND encoded password to increase readability.
     */
    class FormattedPassword(): CharSequence, Clearable {
        private val charList = ArrayList<Char>(32)

        private constructor(charList: MutableList<Char>): this() {
            charList.addAll(charList)
        }

        override val length: Int
            get() = charList.size

        override fun get(index: Int): Char {
            return charList[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
            return FormattedPassword(charList.subList(startIndex, endIndex))
        }


        operator fun plus(string: String): FormattedPassword {
            charList.addAll(string.toCharArray().asList())
            return this
        }

        operator fun plus(char: Char): FormattedPassword {
            charList.add(char)
            return this
        }

        override fun clear() {
            charList.clear()
        }

        /**
         * Avoid using this since it returns a String which remains in the VM heap.
         */
        override fun toString(): String {
            return String(charList.toCharArray())
        }

        companion object {
            fun create(
                    formattingStyle: FormattingStyle,
                    password: Password
            ): FormattedPassword {
                val decoded = password.decodeToCharArray()

                val multiLine = formattingStyle.isMultiLine()
                val raw = formattingStyle == FormattingStyle.RAW
                val formattedPasswordLength = decoded.size
                val formattedPassword = FormattedPassword()
                for (i in 0 until formattedPasswordLength) {
                    if (!raw && i != 0 && i % 4 == 0) {
                        if (i % 8 == 0) {
                            if (multiLine) {
                                formattedPassword + System.lineSeparator()
                            } else {
                                formattedPassword + "  "
                            }
                        } else {
                            formattedPassword + " "
                        }
                    }

                    formattedPassword + decoded[i]
                }

                return formattedPassword
            }
        }
    }

    /**
     * Use this constructor only for testing
     */
    constructor(chars: CharArray) : this(chars.map { it.code.toByte() }.toByteArray())
    constructor(bytes: ByteArray) : super(bytes)

    /**
     * Returns a CharArray 1:1 mapped from the underlying ByteArray.
     * Don't use this for non-ascii passwords since the result may look wrong! For that
     * use #decodeToCharArray instead.
     */
    private fun toEncodedCharArray(): CharArray {
        return data.map { it.toInt().toChar() }.toCharArray()
    }

    fun add(other: Char) {
        val buffer = data + other.code.toByte()
        clear()
        data = buffer
    }

    fun replace(index: Int, other: Char) {
        data[index] = other.code.toByte()
    }

    fun toFormattedPassword() = toFormattedPassword(FormattingStyle.DEFAULT)

    private fun toFormattedPassword(
            formattingStyle: FormattingStyle,
    ) = FormattedPassword.create(formattingStyle, this)


    private fun toRawFormattedPassword() = toFormattedPassword(FormattingStyle.RAW)
    
    /**
     * Returns the encoded length of this password.
     * Use #toFormattedPassword to work with non-ASCII passwords
     */
    override val length: Int
        get() = data.size

    /**
     * Returns the char at position index of the encoded password.
     * Use #toFormattedPassword to work with non-ASCII passwords
     */
    override fun get(index: Int) = toEncodedCharArray()[index]

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return Password(data.copyOfRange(startIndex, endIndex))
    }

    /**
     * Avoid using this since it returns a String which remains in the VM heap.
     */
    override fun toString() = toRawFormattedPassword().toString()

    fun decodeToCharArray(): CharArray {
        val charset = Charset.defaultCharset()
        val charBuffer = charset.decode(ByteBuffer.wrap(data))
        val charArray = CharArray(charBuffer.limit())
        charBuffer[charArray]
        return charArray
    }
}
