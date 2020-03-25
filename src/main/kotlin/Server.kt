
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread

//Reference:
//https://gist.github.com/Silverbaq/a14fe6b3ec57703e8cc1a63b59605876

fun main(args: Array<String>) {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")

    while (true) {
        val client = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        // Run client in it's own thread.
        thread { ClientHandler(client).run() }
    }
}

class ClientHandler(client: Socket) {
    private val client: Socket = client
    private val writer: OutputStream = client.getOutputStream()
    private var running: Boolean = false

    //TODO: implement timeout?

    fun run() {
        running = true

        while (running) {
            write("Fake Data!")
        }
    }

    private fun write(message: String) {
        writer.write((message + '\n').toByteArray(Charset.defaultCharset()))
    }

    private fun shutdown() {
        running = false
        client.close()
        println("${client.inetAddress.hostAddress} closed the connection")
    }

}