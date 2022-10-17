package wee.digital.widget.extension

import android.content.ContentResolver
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import wee.digital.widget.app


fun anim(@AnimRes res: Int): Animation {
    return AnimationUtils.loadAnimation(app, res)
}

fun drawable(@DrawableRes res: Int): Drawable {
    return ContextCompat.getDrawable(app, res)!!
}

fun createDrawable(@DrawableRes res: Int): Drawable? {
    return drawable(res).constantState?.newDrawable()?.mutate()
}

fun Drawable?.tint(@ColorInt color: Int): Drawable? {
    this ?: return null
    DrawableCompat.setTint(this, color)
    DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
    return this
}

fun Drawable?.tintRes(@ColorRes res: Int): Drawable? {
    return tint(color(res))
}

fun pixels(@DimenRes res: Int): Float {
    return app.resources.getDimensionPixelSize(res).toFloat()
}

fun uri(resId: Int): Uri {
    val res: Resources = app.resources
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(res.getResourcePackageName(resId))
        .appendPath(res.getResourceTypeName(resId))
        .appendPath(res.getResourceEntryName(resId))
        .build()
}

fun color(colorStr: String): Int {
    return Color.parseColor(colorStr)
}

fun color(@ColorRes res: Int): Int {
    return ContextCompat.getColor(app, res)
}

fun string(@StringRes res: Int): String {
    return app.getString(res)
}

fun string(@StringRes res: Int, vararg args: Any?): String {
    return try {
        String.format(app.getString(res), *args)
    } catch (ignore: Exception) {
        ""
    }
}

fun resName(id: Int): String {
    return try {
        app.resources.getResourceEntryName(id)
    } catch (e: Resources.NotFoundException) {
        "ResourcesNotFound"
    }
}

fun resName(v: View?): String {
    v ?: return ""
    return try {
        app.resources.getResourceEntryName(v.id)
    } catch (e: Resources.NotFoundException) {
        ""
    }
}