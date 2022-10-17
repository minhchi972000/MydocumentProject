package wee.digital.library.extension

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import wee.digital.library.app
import java.io.*
import kotlin.reflect.KClass

fun assetsGetInputStream(fileName: String) : InputStream?{
    return try {
        return app.assets.open(fileName)
    } catch (e: FileNotFoundException) {
        null
    }
}

fun assetsGetBytes(fileName: String): ByteArray? {
    return try {
        val inputStream = app.assets.open(fileName)
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes)
        inputStream.safeClose()
        return bytes
    } catch (e: FileNotFoundException) {
        null
    }
}

fun assetsGetString(filename: String): String? {
    return try {
        val sb = StringBuilder()
        BufferedReader(InputStreamReader(app.assets.open(filename))).useLines { lines ->
            lines.forEach {
                sb.append(it)
            }
        }
        return sb.toString()
    } catch (e: FileNotFoundException) {
        null
    }
}

fun assetsGetFile(filename: String): File? {
    try {
        var inputStream = app.assets.open(filename)
        val file = File("${app.cacheDir}/${filename.substringAfterLast("/")}")
        val outputStream: OutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var length = 0
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
        outputStream.close()
        inputStream.close()
        return file
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

fun assetsGetJsonObject(fileName: String): JsonObject? {
    val s = assetsGetString(fileName)
    return s.parse(JsonObject::class)
}

fun <T : Any> assetsGetJsonObject(fileName: String, cls: KClass<T>): T? {
    return assetsGetString(fileName).parse(cls)
}

fun assetsGetJsonArray(fileName: String): JsonArray? {
    val s = assetsGetString(fileName)
    return s.parse(JsonArray::class)
}

fun <T : Any> assetsGetJsonArray(fileName: String, cls: KClass<Array<T>>): List<T>? {
    return assetsGetString(fileName).parse(cls)
}