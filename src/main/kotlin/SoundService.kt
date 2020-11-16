import org.jtransforms.fft.DoubleFFT_1D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt


class SoundService {

    private var audioSamplingThread: Thread? = null

    // NOTE: The lower the frequency, the more samples (and time) it takes to fill the buffer.
    // This will also lead to higher processing times as the FFT needs to process a larger dataset.
    fun startListening(lowestFrequency: Double, output: (List<FrequencySample>) -> Unit) {
        audioSamplingThread?.interrupt()

        // Open the line-in
        // Buffer size needs to factor in frameSize to account for bit depth (e.g. 16 bit audio takes 2 bytes per sample)
        var line = InputManager().getInputs()[0] as TargetDataLine //TODO: Improve how to pick input
        val sampleSize = calculateSampleSize(lowestFrequency, line.format.sampleRate) * 2
        val bufferSize = sampleSize * line.format.frameSize

        line.open(line.format, bufferSize)
        line.start()

        // Retrieve audio data
        audioSamplingThread = Thread(Runnable {
            var buffer = ByteArray(line.bufferSize)

            while (true) {
                // We only need to retrieve data and process the output if new data is available
                if (line.available() <= 0) { continue }

                // We gather data into a rolling buffer (First-In-First-Out)
                // E.g. Buffer size of 4096 updating in increments of 1024 means we can update the rolling buffer 4 times
                // in the same time it would take to get a full 4096 samples
                //TODO: Add behavior that handles mono vs stereo formats?
                val newData = ByteArray(line.available())
                val readBytes = line.read(newData, 0, newData.size)
                val rolloverData = buffer.drop(readBytes).toByteArray()
                buffer = rolloverData + newData

                // Format our data
                // We must convert our byte array into a double array,
                // then we need to apply a window algorithm to reduce spectral leakage
                val rawWave = doubleArrayFrom(buffer, line.format)
                val hannWave = applyHannWindowFilter(rawWave)

                //FFT
                val fftData = processFFT(hannWave)
                val frequencySamples = FrequencySample.listFrom(fftData, line.format.sampleRate.toDouble(), sampleSize)
                output(frequencySamples)
            }
        })

        audioSamplingThread?.start()
    }

    fun stopListening() {
        audioSamplingThread?.interrupt()
    }

    // Given a 48000hz sample rate, I can infer that a 20hz sample rate would require 2400 samples (48000 / 20 = 2400)
    // The sample size must be to the power of 2 for FFT; I would need 4096 samples to identify a 20hz signal.
    private fun calculateSampleSize(lowestFrequency: Double, samplingRate: Float): Int {
        val necessarySamples = samplingRate / lowestFrequency

        var power = 0
        while (2.0.pow(power) < necessarySamples) { power += 1 }

        return 2.0.pow(power).toInt()
    }

    // Reference: https://stackoverflow.com/questions/29560491/fourier-transforming-a-byte-array
    private fun doubleArrayFrom(data: ByteArray, format: AudioFormat): DoubleArray {
        // Audio Info
        val bits = format.sampleSizeInBits
        val max = 2.0.pow(bits.toDouble() - 1)

        // Buffer
        val byteOrder = if (format.isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
        val buffer = ByteBuffer.wrap(data)
        buffer.order(byteOrder)

        // Samples
        var samples = DoubleArray(data.size * 8 / bits)

        for (i in samples.indices) {
            when (bits) {
                8 -> samples[i] = buffer.get() / max
                16 -> samples[i] = buffer.short / max
                32 -> samples[i] = buffer.int / max
                64 -> samples[i] = buffer.long / max
                else -> throw UnsupportedAudioFileException()
            }
        }

        return samples
    }

    // Reference: https://dsp.stackexchange.com/questions/19776/is-it-necessary-to-apply-some-window-method-to-obtain-the-fft-java
    private fun applyHannWindowFilter(data: DoubleArray): DoubleArray {
        val output = DoubleArray(data.size)

        for (i in data.indices) {
            val multiplier = 0.5 * (1 - cos(2 * PI * i / (data.size - 1)))
            output[i] = multiplier * data[i]
        }

        return output
    }

    // Reference: https://github.com/wendykierp/JTransforms/issues/4
    private fun processFFT(signal: DoubleArray): DoubleArray {
        // Perform FFT
        val doubleFFT = DoubleFFT_1D(signal.size.toLong())
        doubleFFT.realForward(signal)

        // Prepare results
        val amplitudes = DoubleArray(signal.size / 2)

        for (i in amplitudes.indices) {
            val re = signal[i * 2]
            val im = signal[i * 2 + 1]
            amplitudes[i] = sqrt(re * re + im * im) / signal.size
        }

        return amplitudes
    }

}