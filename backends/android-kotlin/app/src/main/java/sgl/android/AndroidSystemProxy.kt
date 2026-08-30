package sgl.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import sgl.proxy.ProxyResourceNotFoundException
import sgl.proxy.SystemProxy
import sgl.util.Loader
import java.io.IOException
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import android.content.ActivityNotFoundException

class AndroidSystemProxy(private val activity: Activity): SystemProxy {
    override fun exit() {
        activity.finish()
    }

    override fun currentTimeMillis(): Long = java.lang.System.currentTimeMillis()

    override fun nanoTime(): Long = java.lang.System.nanoTime()

    override fun loadText(resourceName: String?): Loader<Array<String>> = AndroidAsync.loader {
        val assetPath = resourceName ?: throw IllegalArgumentException("Resource name cannot be null for loadText")
        try {
            activity.assets.open(assetPath).bufferedReader().use {
                it.readLines().toTypedArray()
            }
        } catch (e: IOException) {
            throw ProxyResourceNotFoundException(assetPath)
        }
    }

    override fun loadBinary(resourceName: String?): Loader<ByteArray> = AndroidAsync.loader {
        val assetPath = resourceName ?: throw IllegalArgumentException("Resource name cannot be null for loadBinary")
        try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            activity.assets.open(assetPath).use { inputStream ->
                BufferedInputStream(inputStream).use { bufferedInputStream ->
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (bufferedInputStream.read(buffer).also { len = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, len)
                    }
                }
            }
            byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            throw ProxyResourceNotFoundException(assetPath)
        }
    }

    override fun openWebpage(uri: URI?) {
        if (uri == null) return
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()))
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // Ignore; mirrors previous backend behavior.
        }
    }

    override fun openGooglePlayApp(id: String?, params: scala.collection.immutable.Map<String, String>?) {
        if (id == null) return
        val paramString = buildString {
            params?.iterator()?.let { iterator ->
                while (iterator.hasNext()) {
                    val tuple = iterator.next()
                    append("&")
                    append(tuple._1())
                    append("=")
                    append(tuple._2())
                }
            }
        }
        val uri = Uri.parse("market://details?id=$id$paramString")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            activity.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            openWebpage(URI("https://play.google.com/store/apps/details?id=$id$paramString"))
        }
    }
}
