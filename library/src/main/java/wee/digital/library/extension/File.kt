package wee.digital.library.extension

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import wee.digital.library.app
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

val cacheDir: File get() = app.cacheDir

val packageDir: File
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) app.dataDir else app.filesDir
    }

val externalDir: File
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            app.getExternalFilesDir(null)!!
        } else {
            @Suppress("DEPRECATION")
            Environment.getExternalStorageDirectory()
        }
    }

val externalPath: String get() = externalDir.absolutePath

val downloadDir: File get() = publicDir(Environment.DIRECTORY_DOWNLOADS)

fun packageDir(dir: String, fileName: String? = null): File {
    val parent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        app.getExternalFilesDir(dir)!!
    } else {
        @Suppress("DEPRECATION")
        Environment.getExternalStoragePublicDirectory(dir)
    }
    if (fileName != null) {
        return File(parent, fileName)
    }
    return parent
}

fun publicDir(dir: String, fileName: String? = null): File {
    val parent = Environment.getExternalStoragePublicDirectory(dir)
    if (fileName != null) {
        return File(parent, fileName)
    }
    return parent
}

fun saveBitmap(fileName: String, bitmap: Bitmap) {
    val file = File(externalPath, "$fileName.png")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.safeClose()
    MediaStore.Images.Media.insertImage(
        app.contentResolver,
        file.absolutePath,
        file.name,
        file.name
    )
}

@RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
fun writeFile(fileName: String, bytes: ByteArray): File? {
    return try {
        val file = File(externalDir, fileName)
        val outputStream = FileOutputStream(file)
        outputStream.write(bytes)
        outputStream.flush()
        outputStream.safeClose()
        file
    } catch (e: Exception) {
        null
    }
}

@RequiresPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
fun readFile(fileName: String): String {
    val file = File(externalDir, fileName)
    val text = java.lang.StringBuilder()
    try {
        val br = BufferedReader(FileReader(file))
        var line: String?
        while (br.readLine().also { line = it } != null) {
            text.append(line)
            text.append('\n')
        }
        br.safeClose()
    } catch (e: IOException) {
    }
    return text.toString()
}

fun File.installApk() {
    try {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(this.uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        app.startActivity(intent)
    } catch (e: Exception) {
        toast(e.message)
    }
}

val File.uri: Uri
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(app, app.packageName + ".provider", this)
        } else {
            Uri.fromFile(this)
        }
    }

val File.isExist: Boolean get() = this.absoluteFile?.exists() ?: false

val File.size: Long get() = length() / 1024

val File.sizeText: String
    get() {
        if (!this.absoluteFile.exists()) return "0 KB"
        val kb = this.length() / 1024
        if (kb < 1024) {
            return "$kb KB"
        }
        return "${kb / 1024} MB"
    }

fun File.openFile() {
    try {
        val install = Intent(Intent.ACTION_VIEW)
        install.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        install.setDataAndType(
            Uri.parse("file://${this.absolutePath}"),
            this.absolutePath
        )
        app.startActivity(install)
    } catch (e: Exception) {
        toast(e.message)
    }
}

val File.getWebpImage: ByteArray
    get() {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeStream(FileInputStream(this), null, options)
        val stream = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap?.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, stream)
        } else {
            bitmap?.compress(Bitmap.CompressFormat.WEBP, 100, stream)
        }
        return stream.toByteArray()
    }

val File.thumbnail: Bitmap?
    get() {
        try {
            val file = File(path)
            if (file.absoluteFile.exists()) {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ThumbnailUtils.createVideoThumbnail(file, Size(96, 96), CancellationSignal())
                } else {
                    ThumbnailUtils.createVideoThumbnail(
                        path,
                        MediaStore.Video.Thumbnails.MICRO_KIND
                    )
                }
            }
        } catch (e: Exception) {

        }
        return null
    }

val Uri.thumbnail: Bitmap?
    get() {
        val path = this.path ?: return null
        val file = File(path)
        return file.thumbnail
    }

fun copyFolder(sourceFile: File, destinationFile: File) {
    println("copy files from '$sourceFile' to '$destinationFile'")
    destinationFile.mkdirs()
    copyFiles(sourceFile, destinationFile)
}

fun copyFiles(sourceFile: File, destinationFile: File) {
    try {
        val folderFiles = sourceFile.list() ?: return
        sourceFile.mkdirs()
        for (file in folderFiles) {
            val childSourceFile = File(sourceFile.path, file)
            val childDestinationFile = File(destinationFile.path, file)
            if (file.contains(".")) {
                println("copying '$file''")
                copyFile(childSourceFile, childDestinationFile)
            } else {
                copyFiles(childSourceFile, childDestinationFile)
            }
        }
    } catch (e: Exception) {
        println(e.message)
    }
}

fun copyFile(sourceFile: File, destinationFile: File) {
    var inputStream: InputStream? = null
    var fos: FileOutputStream? = null
    try {
        // create destination file
        if (destinationFile.absoluteFile.exists()) {
            destinationFile.delete()
        }
        destinationFile.createNewFile()

        // write buffer
        inputStream = sourceFile.inputStream()
        fos = FileOutputStream(destinationFile)
        val buffer = ByteArray(1024)
        var read: Int = inputStream.read(buffer)
        while (read >= 0) {
            fos.write(buffer, 0, read)
            read = inputStream.read(buffer)
        }
    } catch (e: IOException) {
        println("copy file error: ${e.message}")
    }
    inputStream?.safeClose()
    fos?.flush()
    fos.safeClose()
}

fun CoroutineScope.unzip(
    zipFile: File,
    targetDirectory: File,
    onCompleted: suspend CoroutineScope.() -> Unit
) {
    launch(Dispatchers.IO) {
        try {
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zipInputStream ->
                try {
                    var ze: ZipEntry? = null
                    var count: Int
                    val buffer = ByteArray(8192)
                    while (zipInputStream.nextEntry.also { ze = it } != null) {
                        val zipEntry = ze ?: break
                        val file = File(targetDirectory, zipEntry.name)
                        val dir = if (zipEntry.isDirectory) file else file.parentFile
                        if (!dir.isDirectory && !dir.mkdirs()) throw FileNotFoundException(
                            "Failed to ensure directory: " +
                                    dir.absolutePath
                        )
                        if (zipEntry.isDirectory) continue
                        val fileOutputStream = FileOutputStream(file)
                        fileOutputStream.use { fileOut ->
                            while (zipInputStream.read(buffer)
                                    .also { count = it } != -1
                            ) fileOut.write(
                                buffer,
                                0,
                                count
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
        this.launch(Dispatchers.IO, CoroutineStart.DEFAULT, onCompleted)
    }
}


fun InputStream.createFile(fileName: String): File? {
    try {
        val file = File(fileName)
        if (file.exists()) {
            val size = this.available()
            Log.d("createFile", size.toString())
            val buffer = ByteArray(size)
            this.read(buffer)
            this.safeClose()
        }
        return file
    } catch (e: IOException) {
        e.printStackTrace()
    }
    this.safeClose()
    return null
}

fun InputStream.getAsString(): String? {
    var reader: BufferedReader?
    val sb = StringBuilder()
    try {
        reader = BufferedReader(InputStreamReader(this))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.append(line).append("\n")
        }
    } catch (e: Exception) {
        return null
    }
    reader.safeClose()
    return sb.toString()
}

fun File.getAsString(): String? {
    var fileInputStream : FileInputStream? = null
    var s: String? = null
    try {
        fileInputStream = FileInputStream(this)
        s = fileInputStream.getAsString()
    } catch (e: Exception) {

    }
    fileInputStream.safeClose()
    return s
}
