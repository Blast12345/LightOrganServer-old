
import org.jtransforms.fft.DoubleFFT_1D
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.TargetDataLine
import javax.sound.sampled.UnsupportedAudioFileException
import kotlin.math.pow
import kotlin.math.sqrt


class SoundProcessor {

    //Ideally I'd capture a minimum frequency of 20hz (give or take)
    //20hz means a sample every 0.05 seconds
    //48000hz means a sample every 0.000020833333333 seconds
    //I can infer that a 20 hz signal would require 2500 samples (0.05 / 0.00002 = 2400)
    //The sample size must be to the power of 2 for FFT; I would need 4096 samples to identify a 20hz signal.
    //I could cut the number of samples (and thus time) in half by capturing 2048 samples; this would give me a min frequency of 24hz.
    //24hz is definitely worth the tradeoff considering music seldom goes this low

    val kSampleSize = 2048 //TODO: Calculate dynamically based on the sample rate

    fun startListening() { //TODO: Add condition for prefer performance (pad LF up) vs quality (pad LF down)
        //TODO: Improve how to pick input
        var line = InputManager().getInputs()[0] as TargetDataLine

        //Open the line
        line.open(line.format, line.format.frameSize * kSampleSize)
        line.start()

        //Retrieve Audio
        val buffer = ByteArray(line.bufferSize)
        var wave: DoubleArray

        val audioSamplingThread = Thread(Runnable {
            while (true) {
                //TODO: Try to make a rolling buffer; for example - if we need 4096 samples to compute, but they only come in 1024 at a time, then insert using FIFO so FFT and compute on more incremental data
                val readBytes = line.read(buffer, 0, buffer.count())
                wave = convert(buffer, line.format)

                val fftData = process(wave, line.format)
            }
        })

        audioSamplingThread.start()
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
        //TODO: This is peak frequency frame, NOT PEAK FREQUENCY! Fix.
        val maxValue = result.max()
        val maxIndex = result.indexOfFirst { it == maxValue }

        println(maxIndex * format.sampleRate / kSampleSize)

        return result
    }

}