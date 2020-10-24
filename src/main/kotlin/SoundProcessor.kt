
import org.jtransforms.fft.DoubleFFT_1D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.pow
import kotlin.math.sqrt


class SoundProcessor {

    fun startListening() {
        var line = InputManager().getInputs()[0] as TargetDataLine //TODO: Improve how to pick input; ideally would be like AudioFormat(16000.0f, 16, 2, true, true)

        //Open the line
        line.open()
        line.start()

        //Retrieve Audio
        val buffer = ByteArray(line.bufferSize) //TODO: Explore different size calculations
        var wave = DoubleArray(line.bufferSize / 2)

        val audioSamplingThread = Thread(Runnable {
            while (true) {
                val readBytes = line.read(buffer, 0, line.available())
                wave = convert(buffer, line.format)
            }
        })

        audioSamplingThread.start()

        //Process Audio
        val fftThread = Thread(Runnable {
            while (true) {
                val fftData = process(wave, line.format)
            }
        })

        fftThread.start()
    }

    //Reference: https://stackoverflow.com/questions/29560491/fourier-transforming-a-byte-array
    private fun convert(bytes: ByteArray, format: AudioFormat): DoubleArray {
        //Audio Info
        val bits = format.sampleSizeInBits
        val max = 2.0.pow(bits.toDouble() - 1)

        //Buffer
        val byteOrder = if (format.isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(byteOrder)

        //Samples
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

    //Reference: https://github.com/wendykierp/JTransforms/issues/4
    //TODO: Low frequencies are not very accurate.
    private fun process(signal: DoubleArray, format: AudioFormat): DoubleArray {
        //Perform FFT
        val doubleFFT = DoubleFFT_1D(signal.count().toLong())
        doubleFFT.realForward(signal)

        //Prepare results
        val result = DoubleArray(signal.count() / 2)

        for (s in result.indices) {
            val re = signal[s * 2]
            val im = signal[s * 2 + 1]
            result[s] = sqrt(re * re + im * im) / result.size
        }

        //Determine peak frequency (temporary)
        val maxValue = result.max()
        val maxIndex = result.indexOfFirst { it == maxValue }

        println(maxIndex * 2)

        return result
    }
}