package me.matteogiordano.ppg.service.secretgenerator

enum class SecretStrength(val pseudoPhraseLength: Int) {
    ONE_WORD(4),
    TWO_WORDS(8),
    EASY(12),
    NORMAL(16),
    STRONG(20),
    ULTRA(24),
    EXTREME(28),
    HYPER(32),
}
