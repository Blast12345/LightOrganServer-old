import java.net.DatagramSocket
import java.util.*


//High Level Todos
//TODO: Add preconditions/validation to functions (e.g. target frequency must be greater than 0)

fun main() {
    // Sound capture
    val soundService = SoundService()
    val colorGenerator = ColorGenerator()
    soundService.startListening(25.0) { frequencyData ->
        val bassColor = colorGenerator.calculateBassColor(frequencyData, 110.0) //TODO: Set minimum frequency
        //TODO: Send to color generator
        ColorStateManager.currentColors.clear()
        ColorStateManager.currentColors.add(bassColor)
    }

    // Initialize server
    val server = Server()
    server.run()
}

class Server() {

    // The master socket will handle client configuration information
    // The client socket(s) will send color data to the respective client; there will be a 1-to-1 relationship
    private lateinit var masterSocket: DatagramSocket
    private val clients: MutableList<Client> = mutableListOf()

    // TODO: Server should clean up inactive clients

    fun run() {
        masterSocket = DatagramSocket(9999)
        println("Socket is running on port ${masterSocket.localPort}")

        while (true) {

        }
    }

//    while (true) {
//        val client = server.accept()
//        println("Client connected: ${client.inetAddress.hostAddress}")
//
//        // Run client in it's own thread.
//        thread { ClientHandler(client).run() }
//    }

}

class Client(port: Int, mps: Int) {

    private val socket = DatagramSocket(port)
    private val timer = Timer()
    private var ledCount = 0

    init {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkForMessage()
                sendColors()
            }
        }, 0, (1000 / mps).toLong())
    }

    //Socket
    private fun readNextMessage(): String? {
        // TODO:
        return null
    }

    private fun write(message: String) {
        // TODO:
        println("Writing: $message")
    }

    //Reading Steps
    fun checkForMessage() {
        //TODO: Maybe we need to handle multiple messages
        val message = readNextMessage()
        if (message == null) { return }

        checkForExit(message)
        if (socket.isClosed) { return }

        checkForHeartbeat(message)
        checkForLedCount(message)
    }

    private fun checkForExit(message: String) {
        if (message == "EXIT") {
            timer.cancel()
            socket.close()
            println("${socket.inetAddress.hostAddress} closed the connection")
        }
    }

    // TODO: We need a way to clean up clients when they go offline; probably a heartbeat
    private fun checkForHeartbeat(message: String) {
        // TODO:
        // Check a heartbeat time has been sent
        // If so, set the last heartbeat
        // If not, check if we have timed out
        // If so, close the client
        println("Check heartbeat")

        //Update the number of LEDs the client is displaying to

    }

    fun checkForLedCount(message: String) {
        if (message.startsWith("LEDCOUNT=")) {
            val countString = message.removePrefix("LEDCOUNT=")
            ledCount = countString.toInt()
            println("${client.inetAddress.hostAddress} set the LED count to ${ledCount}")
        }
    }

    //Writing Steps
    var bool = true
    fun sendColors() {
        //If we don't have an LED length, then there is no point in running calculations.
        if (ledCount <= 0) {
            return
        }

        if (bool == true) {
            write("FF0000|FF0000")
            bool = false
        } else {
            write("0000FF|0000FF")
            bool = true
        }

        // TODO:
        //Create our color message
        //val ledData = ColorStateManager.interpolateForLED(12)
        //val rgbMessages = ledData.map { it.toHex() }
        //val rgbFullMessage = rgbMessages.joinToString("|")

        //Send our colors to the client
        //write(rgbFullMessage)
    }

}

//class ClientHandler(client: Socket) {
//
//    private val client: Socket = client
//    private val reader: Scanner = Scanner(client.getInputStream())
//    private val writer: OutputStream = client.getOutputStream()
//    private var running: Boolean = false
//    private var ledCount: Int = 0
//
//    fun run() {
//        running = true
//
//        while (running) {
//            //Check for message(s) from the client
//            if (reader.hasNextLine()) {
//                val message = reader.nextLine()
//                received(message)
//            }
//
//        }
//    }
//
//    private fun received(message: String) {

//    }
//
//}