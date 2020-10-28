
class FrequencySample(val frequency: Double, val amplitude: Double) {

    companion object {
        fun listFrom(fftData: DoubleArray, sampleRate: Double, sampleSize: Int): List<FrequencySample> {
            var frequencySamples = mutableListOf<FrequencySample>()

            for (i in fftData.indices) {
                val frequency = i * sampleRate / sampleSize
                val amplitude = fftData[i]
                val frequencySample = FrequencySample(frequency, amplitude)
                frequencySamples.add(frequencySample)
            }

            return frequencySamples
        }
    }

    override fun toString(): String {
        return "$frequency:${amplitude.toBigDecimal()}"
    }

}