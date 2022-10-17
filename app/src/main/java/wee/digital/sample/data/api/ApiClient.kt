package wee.digital.sample.data.api

import android.graphics.Bitmap
import android.net.http.X509TrustManagerExtensions
import android.os.Build
import android.os.Environment
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.buffer
import okio.sink
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import wee.digital.sample.BuildConfig
import wee.digital.sample.app
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.cert.CertificateException

private val trustManager: X509TrustManager = object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
    }

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
    }
}

private val sslContext: SSLContext
    get() = SSLContext.getInstance("SSL").also {
        it.init(null, arrayOf(trustManager), SecureRandom())
    }

private const val publicKey: String = "m9Z7mswlRljf8acQ07EesjKOVJDGy2nR0ZrOM22PE40="

private var token: String? = null

private val serviceUrl get() = "http://"

private val certPinner: CertificatePinner by lazy {
    CertificatePinner.Builder()
        .add(domainName(serviceUrl), "sha256/$publicKey")
        .build()
}

private val debugInterceptor = ApiDebugInterceptor("ApiService")

val apiService: ApiService by lazy {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
    Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(client.build())
        .baseUrl(serviceUrl)
        .build()
        .create(ApiService::class.java)
}

@Throws(SSLException::class)
private fun validatePinning(
    extensions: X509TrustManagerExtensions,
    connection: HttpsURLConnection,
    validPins: Set<String>
) {
    var certChainMsg = ""
    try {
        val md: MessageDigest = MessageDigest.getInstance("SHA-256")
        val trustedChain = trustedChain(extensions, connection)
        for (cert in trustedChain) {
            val publicKey = cert.publicKey.encoded
            md.update(publicKey, 0, publicKey.size)
            val pin: String = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
            certChainMsg += "sha256/$pin : ${cert.subjectDN}"
            if (validPins.contains(pin)) {
                return
            }
        }
    } catch (e: NoSuchAlgorithmException) {
        throw SSLException(e)
    }
    throw SSLPeerUnverifiedException("Peer certificate chain:$certChainMsg")
}

@Throws(SSLException::class)
private fun trustedChain(
    extensions: X509TrustManagerExtensions,
    connection: HttpsURLConnection
): List<X509Certificate> {
    val serverCerts: Array<out Certificate> = connection.serverCertificates
    val untrustedCerts: Array<X509Certificate> =
        Arrays.copyOf(serverCerts, serverCerts.size, Array<X509Certificate>::class.java)
    val host: String = connection.url.host
    return try {
        extensions.checkServerTrusted(untrustedCerts, "RSA", host)
    } catch (e: CertificateException) {
        throw SSLException(e)
    }
}

@Throws(NoSuchAlgorithmException::class)
fun String.sha256(): String? {
    val digest = MessageDigest.getInstance("SHA-256").digest(this.toByteArray(Charsets.UTF_8))
    return Base64.encodeToString(digest, Base64.DEFAULT)
}

private fun trustClient(client: OkHttpClient.Builder) {
    client.sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier(HostnameVerifier { _, _ -> true })
}

fun domainName(url: String): String {
    return try {
        val uri = URI(url)
        val domain = uri.host ?: return ""
        if (domain.startsWith("www.")) domain.substring(4) else domain
    } catch (e: URISyntaxException) {
        ""
    }
}

val loggingInterceptor: Interceptor
    get() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
        return interceptor
    }

fun authInterceptor(token: String): Interceptor {
    return Interceptor { chain: Interceptor.Chain ->
        val request = chain.request().newBuilder()
        request.addHeader("Authorization", token)
        chain.proceed(request.build())
    }
}

fun writeFile(response: Response<ResponseBody>, fileName: String): Result<File> {
    return runCatching {
        val source = response.body()?.source()
            ?: throw NullPointerException("download data is empty")
        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            File(
                app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath,
                fileName
            )
        } else {
            @Suppress("DEPRECATION")
            (File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            ))
        }
        file.sink().buffer().apply {
            writeAll(source)
            close()
        }
        file
    }
}

private fun convertBitmapToFile(fileName: String, bitmap: Bitmap): File {
    //create a file to write bitmap data
    val file = File(app.cacheDir, fileName)
    file.createNewFile()

    //Convert bitmap to byte array
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    val bitMapData = bos.toByteArray()

    //write the bytes in file
    var fos: FileOutputStream? = null
    try {
        fos = FileOutputStream(file)
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    }
    try {
        fos?.write(bitMapData)
        fos?.flush()
        fos?.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

fun getMultipartBody(key: String, bitmap: Bitmap): MultipartBody.Part {
    val file = convertBitmapToFile("img.jpg", bitmap)
    val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(key, file.name, reqFile)
}