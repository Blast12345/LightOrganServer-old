
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

//Reference:
//https://gist.github.com/Silverbaq/a14fe6b3ec57703e8cc1a63b59605876

fun main() {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")

    while (true) {
        val client = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        // This timeout is fairly arbitrary; I just don't want to keep firing data to clients that no longer exist.
        client.soTimeout = 1000

        // Run client in it's own thread.
        thread { ClientHandler(client).run() }
    }
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

data class RGB(val r: Int, val g: Int, val b: Int) {

    override fun toString(): String {
        return "$r,$g,$b"
    }

}

class ColorStateManager {


    //TODO: This needs to be improved. Lack of static syntax makes me think that there is a better design pattern.
    companion object {
        var currentColors: MutableList<RGB> = mutableListOf()

        fun interpolateForLED(count: Int): List<RGB> {
            //TODO: Write an interpolation algorithm
            val output: MutableList<RGB> = mutableListOf()

            for (led in 1..count) { //Maybe do 0 to count-1?
                val first = currentColors.first()
                output.add(first)
            }

            return output
        }
    }

}

class ColorGenerator() {

    //Testing
    private var r = 0;
    private var g = 1;
    private var b = 2;
    //Testing

    fun foo() {
//        r += 1
//        g += 1
//        b += 1
//        r = r % 256
//        g = g % 256
//        b = b % 256

        val colorData = RGB(1, 1, 1)
        val list = mutableListOf(colorData)




        ColorStateManager.currentColors = list
    }

}