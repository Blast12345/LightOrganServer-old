data class FrequencyData(val frequency: Double, val amplitude: Double) {

    override fun toString(): String {
        return "$frequency:$amplitude"
    }

}