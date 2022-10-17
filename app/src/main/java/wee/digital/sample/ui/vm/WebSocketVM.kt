package wee.digital.sample.ui.vm

import androidx.lifecycle.MutableLiveData
import okhttp3.*
import wee.digital.library.extension.SingleLiveData
import wee.digital.sample.log
import wee.digital.sample.ui.base.BaseVM
import java.util.concurrent.TimeUnit


class WebSocketVM : BaseVM() {

    val socketUrlLiveData = MutableLiveData("172.16.16.181:9090")
    val connectionLiveData = MutableLiveData(SocketStatus.closed)
    val messageLiveData = SingleLiveData<String?>()
    val errorLiveData = MutableLiveData<String?>()

    private var webSocket: WebSocket? = null
    private val listener = Listener()
    private var isConnecting: Boolean = false
    val isConnected: Boolean get() = webSocket != null

    fun connect(url: String) {
        socketUrlLiveData.value = url
        if (isConnected || isConnecting) return
        isConnecting = true
        connectionLiveData.postValue(SocketStatus.connecting)
        val client = OkHttpClient.Builder()
            .pingInterval(5000, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        webSocket = client.newWebSocket(request, listener)
    }

    fun disconnect() {
        webSocket?.close(1000, null)
    }

    fun send(message: String) {
        log.d("socket send String: $message")
        webSocket?.send(message)
    }

    fun send(message: okio.ByteString) {
        log.d("socket send ByteString: ${message.size}")
        webSocket?.send(message)
    }

    inner class Listener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            this@WebSocketVM.webSocket = webSocket
            connectionLiveData.postValue(SocketStatus.opened)
            errorLiveData.postValue(null)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@WebSocketVM.webSocket = null
            connectionLiveData.postValue(SocketStatus.closed)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            log.d("socket onFailure: ${t.message}")
            this@WebSocketVM.webSocket = null
            connectionLiveData.postValue(SocketStatus.closed)
            errorLiveData.postValue(t.message)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            log.d("socket onMessage: $text")
            messageLiveData.postValue(text)
        }

        override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
            log.d("socket onMessage: $bytes")
            messageLiveData.postValue(bytes.toString())
        }
    }

}