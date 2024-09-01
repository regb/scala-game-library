package sgl.android

import scala.None
import scala.`None$`
import scala.Option
import scala.Some
import sgl.PartsResourcePathProvider.PartsResourcePath
import sgl.proxy.ResourcePathProxy
import sgl.proxy.SystemProxy
import sgl.util.Loader
import java.net.URI

class AndroidResourcePathProxy(val parts: List<String>): ResourcePathProxy {
    override fun `$div`(filename: String?): ResourcePathProxy {
        if(filename == null)
            throw Exception("Unexpected null agr")
        val newParts: MutableList<String> = parts.toMutableList()
        newParts.add(filename)
        return AndroidResourcePathProxy(newParts.toList())
    }

    override fun extension(): Option<String> {
        val end: String = parts.last()
        val i = end.lastIndexOf('.')
        if(i > 0)
            return Some(end.substring(i+1))
        else
            return Option.apply(null)
    }

    override fun toString(): String {
        return parts.joinToString("/")
    }

}

class AndroidSystemProxy: SystemProxy {
    override fun exit() {
        TODO("Not yet implemented")
    }

    override fun currentTimeMillis(): Long {
        TODO("Not yet implemented")
    }

    override fun nanoTime(): Long {
        TODO("Not yet implemented")
    }

    override fun loadText(path: ResourcePathProxy?): Loader<Array<String>> {
        TODO("Not yet implemented")
    }

    override fun loadBinary(path: ResourcePathProxy?): Loader<ByteArray> {
        TODO("Not yet implemented")
    }

    override fun openWebpage(uri: URI?) {
        TODO("Not yet implemented")
    }
}