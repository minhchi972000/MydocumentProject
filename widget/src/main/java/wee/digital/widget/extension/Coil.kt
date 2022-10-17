package wee.digital.widget.extension

/*fun ImageView.imageRequest(block: (ImageRequest.Builder.() -> Unit)? = null): Disposable {
    //scaleType = ImageView.ScaleType.CENTER_CROP
    val request = ImageRequest.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
    post {
        request.size(width, height)
            .target { drawable ->
                setImageDrawable(drawable)
            }
        block?.invoke(request)
    }
    return ImageLoader(context).enqueue(request.build())
}

fun ImageView.load(url: String?, block: (ImageRequest.Builder.() -> Unit)? = null): Disposable {
    return imageRequest {
        data(url)
        block?.invoke(this)
    }
}

fun ImageView.load(bitmap: Bitmap?, block: (ImageRequest.Builder.() -> Unit)? = null): Disposable {
    return imageRequest {
        data(bitmap)
        block?.invoke(this)
    }
}

fun ImageView.load(res: Int, block: (ImageRequest.Builder.() -> Unit)? = null): Disposable {
    return imageRequest {
        data(res)
        block?.invoke(this)
    }
}

fun ImageView.load(raw: ByteArray?, block: (ImageRequest.Builder.() -> Unit)? = null): Disposable {
    return imageRequest {
        data(raw)
        block?.invoke(this)
    }
}*/


