import org.jtransforms.fft.DoubleFFT_1D
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt


class SoundService {

    // NOTE: The lower the frequency, the more samples (and time) it takes to fill the buffer.
    // This will also lead to higher processing times as the FFT needs to process a larger dataset.
    fun startListening(lowestFrequency: Double, output: (List<FrequencySample>) -> Unit) {
        //WAV Testing
        val fileIn = File("40hz.wav") //44.1khz 16-bit
        val line = AudioSystem.getAudioInputStream(fileIn)

        // Determine sample size
        // 2048 should be sufficient; 40hz signal takes 0.025 seconds and 2048 will contain ~0.046 of audio (2048/44100)
        val sampleSize = 2048

        // Buffer size needs to factor in frameSize to account for bit depth (e.g. 16 bit audio takes 2 bytes per sample)
        val bufferSize = sampleSize * line.format.frameSize
        var buffer = ByteArray(bufferSize)

        // Read data into the buffer
        val readBytes = line.read(buffer, 0, buffer.count())


        val wave = doubleArrayFrom(buffer, line.format)
        val waveAfterHanning = DoubleArray(wave.count())

        //Windowing
        for (i in wave.indices) {
            val multiplier: Double = 0.5 * (1 - cos(2 * PI * i / (wave.count() - 1)))
            waveAfterHanning[i] = multiplier * wave[i]
        }

        //FFT
        val fftData = processFFT(wave)
        val frequencySamples = FrequencySample.listFrom(fftData, line.format.sampleRate.toDouble(), sampleSize)
        output(frequencySamples)
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

        for (i in samples.indices)  {
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
    private fun processFFT(signal: DoubleArray): DoubleArray {
        // Perform FFT
        val doubleFFT = DoubleFFT_1D(signal.count().toLong())
        doubleFFT.realForward(signal)

        // Prepare results
        val magnitudes = DoubleArray(signal.count() / 2)

        for (i in magnitudes.indices) {
            val re = signal[2 * i]
            val im = signal[2 * i + 1]
            magnitudes[i] = sqrt(re * re + im * im) // / magnitudes.size
        }

        return magnitudes
    }

}