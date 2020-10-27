
import org.jtransforms.fft.DoubleFFT_1D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.pow
import kotlin.math.sqrt

class SoundProcessor {

    private var audioSamplingThread: Thread? = null

    // NOTE: The lower the frequency, the more samples (and time) it takes to fill the buffer.
    // This will also lead to higher processing times as the FFT needs to process a larger dataset.
    fun startListening(lowestFrequency: Double, output: (List<FrequencySample>) -> Unit) {
        audioSamplingThread?.interrupt()

        // Open the line-in
        var line = InputManager().getInputs()[0] as TargetDataLine //TODO: Improve how to pick input

        val sampleSize = calculateSampleSize(lowestFrequency, line.format.sampleRate)
        val bufferSize = sampleSize * line.format.frameSize //Multiply by frame size to account for bit depth (e.g. 16 bit audio takes 2 bytes per sample)

        line.open(line.format, bufferSize)
        line.start()

        // Retrieve audio data
        audioSamplingThread = Thread(Runnable {
            var buffer = ByteArray(line.bufferSize)

            while (true) {
                // We gather data into a rolling buffer (First-In-First-Out)
                // E.g. Buffer size of 4096 updating in increments of 1024 means we can update the rolling buffer 4 times
                // in the same time it would take to get a full 4096 samples
                val newData = ByteArray(line.available())
                val readBytes = line.read(newData, 0, newData.count())
                val rolloverData = buffer.drop(readBytes).toByteArray()
                buffer = rolloverData + newData

                // Process FFT
                val wave = doubleArrayFrom(buffer, line.format)
                val fftData = processFFT(wave, line.format)
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
    private fun doubleArrayFrom(bytes: ByteArray, format: AudioFormat): DoubleArray {
        // Audio Info
        val bits = format.sampleSizeInBits
        val max = 2.0.pow(bits.toDouble() - 1)

        // Buffer
        val byteOrder = if (format.isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(byteOrder)

        // Samples
        var samples = DoubleArray(bytes.count() * 8 / bits)

        for (i in 0 until samples.count()) {
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

    // Reference: https://github.com/wendykierp/JTransforms/issues/4
    private fun processFFT(signal: DoubleArray, format: AudioFormat): DoubleArray {
        // Perform FFT
        val doubleFFT = DoubleFFT_1D(signal.count().toLong())
        doubleFFT.realForward(signal)

        // Prepare results
        val result = DoubleArray(signal.count() / 2)

        for (i in result.indices) {
            val re = signal[i * 2]
            val im = signal[i * 2 + 1]
            result[i] = sqrt(re * re + im * im) / result.size
        }

        return result
    }

}