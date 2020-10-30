import jdk.jfr.Frequency
import kotlin.math.pow

class ColorGenerator() {

    fun calculateBassColor(frequencyData: List<FrequencySample>, highpassFrequency: Double): RGB {
        // We need to filter out data above the highpass
        // This must include the bin of the highpass or we will accidentally trim frequencies we want
        val sortedFrequencyData = frequencyData.sortedBy { it.frequency }
        val upperFrequencyIndex = sortedFrequencyData.indexOfFirst { it.frequency > highpassFrequency } + 1
        val relevantFrequencyData = sortedFrequencyData.subList(0, upperFrequencyIndex)

        // Exponentially increase the amplitude to increase separation of signal from background noise
        // TODO: Write logic to calculate background noise or filter it out?
        // TODO: Design amplitude roll-off for upper frequency range
        relevantFrequencyData.forEach { it.amplitude = it.amplitude.pow(8) }

        // Find the average frequency
        var weightedAmplitude = 0.0
        var totalAmplitude = 0.0

        relevantFrequencyData.forEach { frequencySample ->
            weightedAmplitude += (frequencySample.frequency * frequencySample.amplitude)
            totalAmplitude += frequencySample.amplitude
        }

        val averageFrequency = weightedAmplitude / totalAmplitude
        println(averageFrequency) //TODO: Average frequency is VERY noisy.

        // Calculate the RGB values
        val minimumFrequency = relevantFrequencyData.first().frequency
        val maximumFrequency = relevantFrequencyData.last().frequency

        val r = colorWave(-(0 * 256), averageFrequency, minimumFrequency, maximumFrequency)
        val g = colorWave(-(2 * 256), averageFrequency, minimumFrequency, maximumFrequency)
        val b = colorWave(-(4 * 256), averageFrequency, minimumFrequency, maximumFrequency)

        return RGB(r, g, b)
    }

    // @ColeKainz
    private fun colorWave(offset: Int, frequency: Double, minimumFrequency: Double, maximumFrequency: Double): Int {
        val ratio = (6.0 * 256.0) / (maximumFrequency - minimumFrequency)
        var scalePosition = (((frequency - minimumFrequency) * ratio) + offset).toInt() % (6 * 256) //TODO: Make this more readable/break it up
        var colorPosition = scalePosition % 256

        if (scalePosition < 0) {
            scalePosition += 6 * 256
        }

        if (colorPosition < 0) {
            colorPosition += 256
        }

        return if (scalePosition in 0..255) {
            255
        } else if (256 <= scalePosition && scalePosition < 2 * 256) {
            255 - colorPosition
        } else if (256 * 4 <= scalePosition && scalePosition < 256 * 5) {
            colorPosition
        } else if (256 * 5 <= scalePosition && scalePosition < 256 * 6) {
            255
        } else {
            0
        }
    }

}