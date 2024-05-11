package me.matteogiordano.ppg.model.secret

import me.matteogiordano.ppg.model.Clearable

open class Secret(var data: ByteArray) : Clearable {

    fun add(other: Secret) {
        val buffer = data + other.data
        clear()
        other.clear()
        data = buffer
    }

    override fun clear() {
        data.fill(0, 0, data.size)
    }

}
