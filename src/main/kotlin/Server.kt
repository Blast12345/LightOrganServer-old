import java.io.OutputStream
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import kotlin.concurrent.thread

//High Level Todos
//TODO: Add preconditions/validation to functions (e.g. target frequency must be greater than 0)

fun main() {
    // Initialize server
    val server = Server()
    thread { server.run() }

    // Written sloppily for testing
//    val soundService = SoundService()
//    val colorGenerator = ColorGenerator()
//    soundService.startListening(25.0) { frequencyData ->
//        val bassColor = colorGenerator.calculateBassColor(frequencyData, 110.0) //TODO: Set minimum frequency
//        //TODO: Send to color generator
//        ColorStateManager.currentColors.clear()
//        ColorStateManager.currentColors.add(bassColor)
//    }
}

class Server() {

    private lateinit var socket: DatagramSocket
    private val timer = Timer()

    fun run() {
        socket = DatagramSocket(9999)
        println("Socket is running on port ${socket.localPort}")
    }

    fun stop() {
        if (socket.isConnected) {
            socket.close()
        }
    }

}