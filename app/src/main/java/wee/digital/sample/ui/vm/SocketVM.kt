package wee.digital.sample.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import wee.digital.library.extension.SingleLiveData
import wee.digital.sample.log
import wee.digital.sample.ui.base.BaseVM
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket


class SocketVM : BaseVM() {

    val socketUrlLiveData = MutableLiveData("")
    val connectionLiveData = MutableLiveData(SocketStatus.closed)
    val sendLiveData = SingleLiveData<String?>()
    val messageLiveData = SingleLiveData<String?>()
    val errorLiveData = MutableLiveData<String?>()

    val isConnected: Boolean get() = socket != null
    private var socket: Socket? = null
    private var dataOutputStream: DataOutputStream? = null
    private var bufferedReader: BufferedReader? = null
    private var mRun: Boolean = false

    @Volatile
    var receivedMessage: Boolean = true

    fun connect(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionLiveData.postValue(SocketStatus.connecting)
            socket = Socket()
            try {
                val endpoint = url.substringAfter("/").substringBeforeLast(":").replace("/", "")
                val port = url.substringAfterLast(":").toInt()
                val inetAddress: InetAddress = InetAddress.getByName(endpoint)
                socket?.connect(InetSocketAddress(inetAddress, port), 5000)
                dataOutputStream = DataOutputStream(socket!!.getOutputStream())
                bufferedReader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                mRun = true
                connectionLiveData.postValue(SocketStatus.opened)
                while (mRun) {
                    try {
                        bufferedReader?.readLine()?.also {
                            messageLiveData.postValue(it.length.toString())
                            receivedMessage = true
                        }
                    } catch (e: Exception) {
                        errorLiveData.postValue(e.message)
                    }
                    delay(50)
                }

            } catch (e: Exception) {
                errorLiveData.postValue(e.message)
            } finally {
                disconnect()
            }
        }
    }

    fun disconnect() {
        mRun = false
        dataOutputStream = null
        bufferedReader = null
        socket?.close()
        socket == null
        connectionLiveData.postValue(SocketStatus.closed)
    }

    fun send(message: String) {
        log.d("socket send String: $message")
    }

    fun send(message: ByteArray) {
        receivedMessage = false
        sendLiveData.postValue("${message.size}")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataOutputStream?.write(message)
                dataOutputStream?.flush()
            } catch (e: Exception) {

            }
        }
    }

}