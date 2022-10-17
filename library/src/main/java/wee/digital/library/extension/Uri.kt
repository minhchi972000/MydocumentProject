package wee.digital.library.extension

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import wee.digital.library.app
import java.io.*
import java.net.URLEncoder

private val context get() = app

private val contentResolver get() = context.contentResolver

fun ContentResolver.simpleQuery(
    uri: Uri,
    projection: Array<String?>? = null,
    selection: String? = null,
    selectionArgs: Array<String?>? = null,
    sortOrder: String? = null
): Cursor? {
    return query(uri, projection, selection, selectionArgs, sortOrder, null)
}

val Uri.isExternalStorageDocument: Boolean
    get() = when (this.authority) {
        "com.android.externalstorage.documents" -> true
        else -> false
    }

val Uri.isGoogleDriveUri: Boolean
    get() = when (this.authority) {
        "com.google.android.apps.docs.storage", "com.google.android.apps.docs.storage.legacy" -> true
        else -> false
    }

val Uri.isDownloadsDocument: Boolean
    get() = when (this.authority) {
        "com.android.providers.downloads.documents" -> true
        else -> false
    }

val Uri.isMediaDocument: Boolean
    get() = when (this.authority) {
        "com.android.providers.media.documents" -> true
        else -> false
    }

val Uri.isGooglePhotosUri: Boolean
    get() = when (this.authority) {
        "com.google.android.apps.photos.content" -> true
        else -> false
    }

val Uri.fileName: String?
    get() {
        var result: String? = null
        if (this.scheme == "content") {
            val cursor: Cursor? = contentResolver.simpleQuery(this)
            cursor?.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    result = cursor.getString(nameIndex)
                }
            }
        }
        if (result == null) {
            result = this.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

val Uri.getDriveFilePath: String
    get() {
        val returnUri = this
        val returnCursor: Cursor? = contentResolver.simpleQuery(returnUri)
        val nameIndex: Int = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
        val name: String = (returnCursor.getString(nameIndex))
        val size = returnCursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, URLEncoder.encode(name, "utf-8"))
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(this)
            val outputStream = FileOutputStream(file)
            val maxBufferSize: Int = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream!!.available()
            val bufferSize: Int = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            inputStream.use { inputStream: InputStream ->
                outputStream.use { fileOut ->
                    while (true) {
                        val length = inputStream.read(buffers)
                        if (length <= 0)
                            break
                        fileOut.write(buffers, 0, length)
                    }
                    fileOut.flush()
                    fileOut.safeClose()
                }
            }
            inputStream.safeClose()
            println("File Size ${file.length()}")
            println("File Path ${file.path}")
        } catch (e: Exception) {
            println()
        }
        return file.path
    }

fun copyStream(inputStream: InputStream, outputStream: OutputStream): Long {
    var total: Long = 0
    var read: Int
    val buffer = ByteArray(1024)
    while (inputStream.read(buffer).also { read = it } >= 0) {
        outputStream.write(buffer, 0, read)
        total += read.toLong()
    }
    return total
}

val Uri.realPath: String?
    get() {
        val uri = this
        val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // DocumentProvider
            if (uri.isGoogleDriveUri) {
                return uri.getDriveFilePath
            }

            // ExternalStorageProvider
            if (uri.isExternalStorageDocument) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split: List<String> = docId.split(":")
                val type: String = split[0]
                // This is for checking Main Memory
                if ("primary".equals(type, ignoreCase = true)) {
                    if (split.size > 1) {
                        return context.getExternalFilesDir(null).toString() + "/" + split[1]
                    }
                    // This is for checking SD Card
                    return context.getExternalFilesDir(null).toString() + "/"
                }
                return "storage/${docId.replace(":", "/")}"
            }

            if (uri.isDownloadsDocument) {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r", null)
                parcelFileDescriptor?.let {
                    val inputStream = FileInputStream(it.fileDescriptor)
                    val file = File(
                        app.cacheDir,
                        uri.fileName
                    )
                    val outputStream = FileOutputStream(file)
                    copyStream(inputStream, outputStream)
                    return file.path
                }
            }

            if (uri.isMediaDocument) {
                return copyFileToInternalStorage(uri, "app name")
            }
        }

        if ("content".equals(uri.scheme, ignoreCase = true)) {
            if (uri.isGooglePhotosUri) {
                return uri.lastPathSegment
            }
            return copyFileToInternalStorage(uri, "your app name")
        }

        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String {

    val returnCursor: Cursor? = contentResolver.simpleQuery(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
    )
    val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor.moveToFirst()
    val name = returnCursor.getString(nameIndex)
    val output: File = if (newDirName.isNotEmpty()) {
        val dir = File("${context.filesDir}/$newDirName")
        if (!dir.exists()) {
            dir.mkdir()
        }
        File("${context.filesDir}/$newDirName/${URLEncoder.encode(name, "utf-8")}")
    } else {
        File("${context.filesDir}/$newDirName/${URLEncoder.encode(name, "utf-8")}")
    }
    val inputStream: InputStream = contentResolver.openInputStream(uri)!!
    val outputStream = FileOutputStream(output)
    copyStream(inputStream, outputStream)
    inputStream.safeClose()
    outputStream.safeClose()
    return output.path

}



