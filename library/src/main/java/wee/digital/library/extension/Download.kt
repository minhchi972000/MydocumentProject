package wee.digital.library.extension

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import wee.digital.library.app
import java.io.File

val downloadManager = app.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

val isDownloading: Boolean
    get() {
        val query = DownloadManager.Query()
        query.setFilterByStatus(DownloadManager.STATUS_RUNNING)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            cursor.safeClose()
            return true
        }
        return false
    }

/**
 * Usage:
 * In manifest:
 * <provider
 *      android:name="androidx.core.content.FileProvider"
 *      android:authorities="wee.digital.ft.provider"
 *      android:exported="false"
 *      android:grantUriPermissions="true"
 *      tools:replace="android:authorities, android:exported">
 *      <meta-data
 *          android:name="android.support.FILE_PROVIDER_PATHS"
 *          android:resource="@xml/provider_paths"
 *          tools:replace="android:resource"/>
 * </provider>
 *
 * In xml/file_provider_paths:
 * <?xml version="1.0" encoding="utf-8"?>
 * <paths>
 *      <external-path
 *          name="external"
 *          path="." />
 *      <external-files-path
 *          name="external_files"
 *          path="/" />
 *      <files-path
 *          name="files"
 *          path="." />
 * </paths>
 * ===============
 * DownloadBuilder()
 *      .url("download_url")
 *      .file(File(downloadDir, "DownloadFile.ext"))
 *      .openOnComplete(true)
 *      .onSuccess {
 *          toast("download 'RsUpdate.apk' completed")
 *      }
 *      .onCancel {
 *          toast("download 'RsUpdate.apk' canceled")
 *      }
 *      .download()
 */
class DownloadBuilder {

    lateinit var file: File
    lateinit var url: String
    private var downloadId: Long = 0
    private var replaceExistFile: Boolean = true
    private var title: String? = null
    private var description: String? = null
    private var onCompleted: (File?) -> Unit = {
        unregisterReceiver()
    }
    private var onSuccess: (File) -> Unit = {
        onCompleted(it)
    }
    private var onCancel: (Exception?) -> Unit = {
        onCompleted(null)
    }
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (downloadId < 0) {
                app.unregisterReceiver(this)
                return
            }
            try {
                val cursor: Cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
                if (!cursor.moveToFirst()) {
                    onCancel(null)
                    return
                }
                val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                val status = cursor.getInt(statusIndex)
                if (status != DownloadManager.STATUS_SUCCESSFUL) {
                    onCancel(null)
                    return
                }
                val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                val uri = cursor.getString(uriIndex)!!

                val downloadedFile = File(Uri.parse(uri).path!!)
                Handler(Looper.getMainLooper()).postDelayed({
                    onSuccess(downloadedFile)
                }, 1000)
            } catch (e: Exception) {
                onCancel(e)
            }

        }
    }

    fun title(s: String): DownloadBuilder {
        this.title = s
        return this
    }

    fun description(s: String): DownloadBuilder {
        this.description = s
        return this
    }

    fun url(url: String): DownloadBuilder {
        this.url = url
        return this
    }

    fun file(file: File): DownloadBuilder {
        this.file = file
        return this
    }

    fun onCompleted(block: (File?) -> Unit): DownloadBuilder {
        this.onCompleted = {
            unregisterReceiver()
            block(it)
        }
        return this
    }

    fun onSuccess(block: (File) -> Unit): DownloadBuilder {
        this.onSuccess = {
            onCompleted(it)
            block(it)
        }
        return this
    }

    fun onCancel(block: (Exception?) -> Unit): DownloadBuilder {
        this.onCancel = {
            onCompleted(null)
            block(it)
        }
        return this
    }

    fun replaceExist(value: Boolean): DownloadBuilder {
        replaceExistFile = value
        return this
    }

    fun download(): DownloadBuilder {
        if (file.absoluteFile.exists()) {
            if (replaceExistFile) {
                file.delete()
            } else {
                downloadId = -1
                onSuccess(file)
                return this
            }
        }
        if (!receiver.isOrderedBroadcast) {
            val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            app.registerReceiver(receiver, filter)
        }
        try {
            val request = DownloadManager.Request(Uri.parse(url))
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle(title ?: "${app.packageName}")
            request.setDescription(description ?: "Download ${file.name.substringAfterLast("/")}")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationUri(Uri.fromFile(file))
            downloadId = downloadManager.enqueue(request)
        } catch (e: Exception) {
            toast(e.message)
        }
        return this
    }

    fun cancel() {
        unregisterReceiver()
        downloadManager.remove(downloadId)
    }

    private fun unregisterReceiver() {
        kotlin.runCatching {
            app.unregisterReceiver(receiver)
        }
    }

}

