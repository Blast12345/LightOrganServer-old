data class RGB(val r: Int, val g: Int, val b: Int) {

    override fun toString(): String {
        return "$r,$g,$b"
    }

    fun toByteArray(): ByteArray {
        var result = ByteArray(3)

        result[0] = r.toByte()
        result[1] = g.toByte()
        result[2] = b.toByte()

        return result
    }

}