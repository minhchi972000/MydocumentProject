package wee.digital.sample.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Request
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ApiDebugInterceptor(private val tag: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        try {
            val request: Request = chain.request()
            request.newBuilder().build().apply {
                d("==========================================")
                val buffer = Buffer()
                body?.writeTo(buffer)
                val requestBody = buffer.readUtf8()
                d("Request: %s - %s".format(method, url.toString()))
                d(requestBody.jsonFormat())
            }
            val response = chain.proceed(request)
            response.newBuilder().build().apply {
                d("Response: %s - %s".format(code, message))
                val source = body?.source()
                source?.request(Long.MAX_VALUE)
                val responseBody = source?.buffer?.clone()?.readUtf8()
                d(responseBody.jsonFormat())
            }
            return response

        } catch (e: Throwable) {
            d("Throwable:")
            val className = e::class.java.simpleName
            when (e) {
                is SocketException, is UnknownHostException, is SocketTimeoutException -> {
                    d("$className: ${e.message}")
                }
                is HttpException -> {
                    d("$className: code ${e.code()}, message: ${e.message()}, body: ${e.errorBody()}")
                }
                else -> {
                    d("$className: ${e.message}\n${e.stackTraceToString()}")
                }
            }
            throw e
        }
    }

    private fun d(s: String?) {
        Log.d(tag, s ?: "null")
    }

    private fun HttpException.errorBody(): String {
        val errorBody = response()?.errorBody()?.string() ?: return this.toString()
        return try {
            JSONObject(errorBody).toString(2)
        } catch (e: JSONException) {
            this.toString()
        }
    }

    private fun String?.jsonFormat(): String? {
        this ?: return null
        return try {
            val obj = JSONObject(this)
            obj.toString(2)
        } catch (ignore: Exception) {
            null
        }
    }

}