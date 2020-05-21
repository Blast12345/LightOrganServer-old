import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread
import java.io.File
import javax.sound.sampled.*

const val RECORD_TIME = 10000L // ten seconds
fun testRecording() {
    val foobar = InputManager()
    val inputs = foobar.getInputs() //TODO: How to pick input
    val line = inputs[0] as TargetDataLine
    val format = line.format
    val info = DataLine.Info(TargetDataLine::class.java, format)

    val wavFile = File("RecordAudio.wav")
    val fileType = AudioFileFormat.Type.WAVE
//    val format = AudioFormat(16000.0f, 16, 2, true, true)
//    val info = DataLine.Info(TargetDataLine::class.java, format)
//    val line = AudioSystem.getLine(info) as TargetDataLine

    // Creates a new thread that waits for 'RECORD_TIME' before stopping
    Thread(Runnable {
        try {
            Thread.sleep(RECORD_TIME)
        } catch (ie: InterruptedException) {
            println(ie.message)
        } finally {
            line.stop()
            line.close()
        }
        println("Finished")
    }).start()

    // Captures the sound and saves it in a WAV file
    try {
        if (AudioSystem.isLineSupported(info)) {
            line.open(format)
            line.start()
            println("Recording started")
            AudioSystem.write(AudioInputStream(line), fileType, wavFile)
        }
        else println("Line not supported")
    }
    catch (lue: LineUnavailableException) {
        println(lue.message)
    }
}

fun main() {
    testRecording()
//    val server = ServerSocket(9999)
//    println("Server is running on port ${server.localPort}")
//
//    val foobar = SoundProcessor()
//    foobar.process()
//
//    while (true) {
//        val client = server.accept()
//        println("Client connected: ${client.inetAddress.hostAddress}")
//
//        // This timeout is fairly arbitrary; I just don't want to keep firing data to clients that no longer exist.
//        client.soTimeout = 1000
//
//        // Run client in it's own thread.
//        thread { ClientHandler(client).run() }
//    }
}

class ClientHandler(client: Socket) {

    private val client: Socket = client
    private val reader: Scanner = Scanner(client.getInputStream())
    private val writer: OutputStream = client.getOutputStream()
    private var running: Boolean = false
    private var ledCount: Int = 0

    fun run() {
        running = true

        while (running) {
            //Check for message(s) from the client
            if (reader.hasNextLine()) {
                val message = reader.nextLine()
                received(message)
            }

            //If we don't have an LED length, then there is no point in running calculations.
            if (ledCount <= 0) {
                continue
            }

            //Create our color message
            val ledData = ColorStateManager.interpolateForLED(ledCount)
            val rgbMessages = ledData.map { it.toString() }
            val rgbFullMessage = rgbMessages.joinToString("|")

            //Send our colors to the client
            write(rgbFullMessage)
        }
    }

    private fun write(message: String) {
        println("Writing: $message")

        val message = (message + '\n')
        val byteArray = message.toByteArray(Charset.defaultCharset())
        writer.write(byteArray)
    }

    private fun received(message: String) {
        //End the client session
        if (message == "EXIT") {
            running = false
            client.close()
            println("${client.inetAddress.hostAddress} closed the connection")
        }

        //Update the number of LEDs the client is displaying to
        if (message.startsWith("LEDCOUNT=")) {
            val countString = message.removePrefix("LEDCOUNT=")
            ledCount = countString.toInt()
            println("${client.inetAddress.hostAddress} set the LED count to ${ledCount}")
        }
    }

}