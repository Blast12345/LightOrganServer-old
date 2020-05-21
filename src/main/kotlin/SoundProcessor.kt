import org.jtransforms.fft.FloatFFT_1D

class SoundProcessor {

    private val sample_rate = 48000


    fun process(signal: FloatArray) {
        //FFT
        val floatFFT = FloatFFT_1D(sample_rate.toLong())
        floatFFT.realForward(signal)

        //Foobar
        var localMax = Float.MIN_VALUE
        var maxValueFreq = -1
        val result = FloatArray(signal.count() / 2)
        for (s in result.indices) {
            //result[s] = Math.abs(signal[2*s]);
            val re = signal[s * 2]
            val im = signal[s * 2 + 1]
            result[s] = Math.sqrt(re * re + im * im.toDouble()).toFloat() / result.size
            if (result[s] > localMax) {
                maxValueFreq = s
            }
            localMax = Math.max(localMax, result[s])
        }

        println("HERE")
    }
}