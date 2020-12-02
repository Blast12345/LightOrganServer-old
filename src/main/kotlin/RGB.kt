data class RGB(val r: Int, val g: Int, val b: Int) {

    override fun toString(): String {
        return "$r,$g,$b"
    }

    fun toHex(): String {
        return String.format("%02x%02x%02x", r, g, b)
    }

}