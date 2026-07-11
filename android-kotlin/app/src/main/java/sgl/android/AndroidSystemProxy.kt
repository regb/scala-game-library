package sgl.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import scala.Option
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SystemProxy
import sgl.util.Loader
import java.io.IOException
import java.io.InputStream
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.URI
import android.content.ActivityNotFoundException
import sgl.SystemProvider
import sgl.SystemProvider.ResourceNotFoundException

class AndroidResourcePathProxy(val parts: List<String>): ResourcePathProxy {
    override fun `$div`(filename: String?): ResourcePathProxy {
        if(filename == null)
            throw NullPointerException("Filename cannot be null for ResourcePathProxy.div")
        val newParts: MutableList<String> = parts.toMutableList()
        newParts.add(filename)
        return AndroidResourcePathProxy(newParts.toList())
    }

    override fun extension(): Option<String> {
        if (parts.isEmpty()) return Option.empty()
        val end: String = parts.last()
        val i = end.lastIndexOf('.')
        return if(i > 0 && i < end.length - 1) // ensure dot is not last char
            Option.apply(end.substring(i+1))
        else
            Option.empty()
    }

    fun generatePathString(): String {
        return parts.joinToString("/")
    }

    override fun toString(): String {
        return generatePathString()
    }
}

// Changed constructor to accept Activity
class AndroidSystemProxy(private val activity: Activity): SystemProxy {
    override fun exit() {
        activity.finish()
    }

    override fun currentTimeMillis(): Long {
        return java.lang.System.currentTimeMillis()
    }

    override fun nanoTime(): Long {
        return java.lang.System.nanoTime()
    }

    override fun loadText(path: ResourcePathProxy?): Loader<Array<String>> = AndroidAsync.loader {
        if (path !is AndroidResourcePathProxy) {
            throw IllegalArgumentException("Path must be an AndroidResourcePathProxy for loadText")
        }
        val assetPath = path.generatePathString()
        try {
            activity.assets.open(assetPath).bufferedReader().use {
                it.readLines().toTypedArray()
            }
        } catch (e: IOException) {
            throw Exception("Resource not found: $path", e)
        }
    }

    override fun loadBinary(path: ResourcePathProxy?): Loader<ByteArray> = AndroidAsync.loader {
        if (path !is AndroidResourcePathProxy) {
            throw IllegalArgumentException("Path must be an AndroidResourcePathProxy for loadBinary")
        }
        val assetPath = path.generatePathString()
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
            throw Exception("Resource not found: $path", e)
        }
    }

    override fun openWebpage(uri: URI?) {
        if (uri == null) {
            // Optionally log or handle this error, but Scala version didn't explicitly
            return
        }
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()))
            activity.startActivity(browserIntent)
        } catch (e: Exception) {
            // Log error or handle, e.g., if no browser is available, though typically Android handles this.
            // For now, mimicking Scala's lack of explicit catch here for this specific method.
        }
    }

    // Commented out as it's not in SystemProxy interface
    /*
    override fun openGooglePlayApp(id: String, params: Map<String, String>) {
        val paramString = params.map { (k, v) -> "&$k=$v" }.joinToString("")
        try {
            val marketUri = Uri.parse("market://details?id=$id$paramString")
            val intent = Intent(Intent.ACTION_VIEW, marketUri)
            activity.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Fallback to opening webpage
            val webUriString = "https://play.google.com/store/apps/details?id=$id$paramString"
            try {
                openWebpage(URI(webUriString))
            } catch (e: Exception) {
                // Failed to even open fallback webpage, log or handle.
            }
        }
    }
    */
}