package wee.digital.widget.extension

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import wee.digital.widget.app
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit

@GlideModule
class MyGlideApp : AppGlideModule()

abstract class SimpleRequestListener : RequestListener<Drawable> {

    abstract fun onCompleted(drawable: Drawable?)

    final override fun onLoadFailed(
        e: GlideException?, model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
    ): Boolean {
        onCompleted(null)
        return false
    }

    final override fun onResourceReady(
        resource: Drawable?, model: Any?,
        target: Target<Drawable>?, dataSource: DataSource?,
        isFirstResource: Boolean,
    ): Boolean {
        onCompleted(resource)
        return false
    }
}

fun ImageView.requestDrawable(block: GlideRequests.() -> GlideRequest<Drawable>) {
    GlideApp.with(context)
        .block()
        .override(width, height)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

fun ImageView.load(url: String?, block: (GlideRequest<Drawable>.() -> Unit)? = null) {
    requestDrawable {
        load(url).also { block?.invoke(it) }
    }
}

fun ImageView.loadThen(url: String?, block: ((drawable: Drawable?) -> Unit)? = null) {
    load(url) {
        listener(object : SimpleRequestListener() {
            override fun onCompleted(drawable: Drawable?) {
                block?.invoke(drawable)
            }
        })
    }
}

fun ImageView.load(bitmap: Bitmap?, block: (GlideRequest<Drawable>.() -> Unit)? = null) {
    requestDrawable {
        load(bitmap).also { block?.invoke(it) }
    }
}

fun ImageView.loadThen(bitmap: Bitmap?, block: ((drawable: Drawable?) -> Unit)? = null) {
    load(bitmap) {
        listener(object : SimpleRequestListener() {
            override fun onCompleted(drawable: Drawable?) {
                block?.invoke(drawable)
            }
        })
    }
}

fun ImageView.load(res: Int, block: (GlideRequest<Drawable>.() -> Unit)? = null) {
    requestDrawable {
        load(res).also { block?.invoke(it) }
    }
}

fun ImageView.loadThen(res: Int, block: ((drawable: Drawable?) -> Unit)? = null) {
    load(res) {
        listener(object : SimpleRequestListener() {
            override fun onCompleted(drawable: Drawable?) {
                block?.invoke(drawable)
            }
        })
    }
}

fun ImageView.load(bytes: ByteArray?, block: (GlideRequest<Drawable>.() -> Unit)? = null) {
    requestDrawable {
        load(bytes).also { block?.invoke(it) }
    }
}

fun ImageView.loadThen(bytes: ByteArray?, block: ((drawable: Drawable?) -> Unit)? = null) {
    GlideApp.with(context).clear(this)
    load(bytes) {
        listener(object : SimpleRequestListener() {
            override fun onCompleted(drawable: Drawable?) {
                block?.invoke(drawable)
            }
        })
    }
}

fun ImageView.load(drawable: Drawable?) {
    requestDrawable {
        load(drawable)
    }
}

fun ImageView.load(file: File) {
    requestDrawable {
        load(file)
    }
}

fun ImageView.load(uri: Uri) {
    requestDrawable {
        load(uri)
    }
}

fun ImageView.reload(res: Int) {
    val request = GlideApp.with(context)
        .load(res)
        .thumbnail(0.1f)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
    request.into(this)
}

fun ImageView.reload(url: String?) {
    val request = GlideApp.with(context)
        .load(url)
        .thumbnail(0.1f)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
    request.into(this)
}

fun ImageView.clear() {
    val bitmap: Bitmap? = null
    GlideApp.with(context)
        .load(bitmap)
        .into(this)
}

val ImageView.hasImage get() = drawable != null

fun downloadImage(url: String?): ByteArray? {
    if (url.isNullOrEmpty()) return null
    return try {
        val bitmap: Bitmap = Glide.with(app)
            .asBitmap()
            .load(url)
            .submit()
            .get(10, TimeUnit.SECONDS)
        val outputStream = ByteArrayOutputStream()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, outputStream)
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        }
        outputStream.toByteArray()
    } catch (e: Exception) {
        null
    }
}

fun Uri.thumbnail(): Bitmap? {
    val path = this.path ?: return null
    val file = File(path)
    return file.thumbnail()
}

fun File.thumbnail(): Bitmap? {
    try {
        val file = File(path)
        if (file.absoluteFile.exists()) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(file, Size(96, 96), CancellationSignal())
            } else {
                ThumbnailUtils.createVideoThumbnail(path!!, MediaStore.Video.Thumbnails.MICRO_KIND)
            }
        }
    } catch (e: Exception) {

    }
    return null
}

