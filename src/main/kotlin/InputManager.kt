import org.apache.commons.math3.complex.Complex
import javax.sound.sampled.*

class InputManager() {

    //https://stackoverflow.com/questions/48342974/java-sound-getting-default-microphone-port
    fun getInputs(): List<Line> {
        val inputs = mutableListOf<Line>()

        //Loop through the mixer to find all valid inputs
        val mixerInfo = AudioSystem.getMixerInfo()

        for (deviceInfo in mixerInfo) {
            //TODO: Verify names
            //TODO: It would be nice to grab audio from a SourceDataLine, but it probably isn't possible. Explore options later.
            val audioDevice = AudioSystem.getMixer(deviceInfo)
            val lineInfoList = audioDevice.targetLineInfo
            val targetDataLineInfo = lineInfoList.filter { it.lineClass == TargetDataLine::class.java }

            //If there is line info, let's grab the appropriate line and add it to our inputs list.
            for (lineInfo in targetDataLineInfo) {
                try {
                    val line = audioDevice.getLine(lineInfo)
                    inputs.add(line)
                } catch (e: LineUnavailableException) {
                    e.printStackTrace(); continue
                }
            }

        }

        //Return the data
        return inputs
    }

    //val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
    //bufferSize = 2 * sampleSize

    val bandwidth = 5000
    var targetLine: TargetDataLine? = null

//    fun startListening(line: TargetDataLine) {
//        val dataLine = DataLine.Info(TargetDataLine::class.java, format)
//        targetLine = AudioSystem.getLine(dataLine) as TargetDataLine
//        targetLine?.open(format, bufferSize)
//        targetLine?.start()
//
//        val buffer = ByteArray(targetLine!!.bufferSize)
//
//        var wave = DoubleArray(buffer.size / 2)
//        var c = arrayOfNulls<Complex>(buffer.size / 2)
//
//        val size = c.size * bandwidth / format.sampleRate
//        val freq = DoubleArray(size.toInt())
//
//        while (true) {
//            val readBytes = targetLine!!.read(buffer, 0, targetLine!!.available())
////            wave = convert(wave, buffer, readBytes)
////            for (i in 0 until wave.length) {
////                c[i] = Complex(wave[i], 0.0)
////            }
////            c = fft(c)
////            for (i in 1 until freq.length) {
////                freq[i] = c[i]!!.abs() / 65536.0 * 2.0 / sample_size * multiplier
////            }
//        }
//    }


//    private fun convert(wave: DoubleArray, buffer: ByteArray, readBytes: Int): DoubleArray? {
//        val r = DoubleArray(wave.size)
//        for (i in wave.size - 1 downTo readBytes / 2) {
//            r[i] = wave[i - readBytes / 2]
//        }
//        for (i in 0 until readBytes / 2) {
//            r[i] = buffer[readBytes - 1 - 2 * i] shl 8 or buffer[readBytes - 2 - 2 * i] and 0xFF
//        }
//        return r
//    }

}