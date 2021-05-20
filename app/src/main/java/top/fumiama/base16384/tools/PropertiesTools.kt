package top.fumiama.base16384.tools
//PropertiesTools.kt
//created by fumiama 20200724
import android.util.Log
import java.io.File
import java.io.InputStream
import java.util.*

class PropertiesTools(private val f: File):Properties() {
    private var cache = hashMapOf<String, String>()

    init {
        if(!f.exists()) {
            if(f.parentFile?.exists() != true) f.parentFile?.mkdirs()
            if(f.parentFile?.canWrite() != true) f.parentFile?.setWritable(true)
            createNew(f)
        } else if(f.isDirectory) {
            if(f.parentFile?.canWrite() != true) f.parentFile?.setWritable(true)
            f.delete()
            createNew(f)
        }
        if(f.parentFile?.canWrite() != true) f.parentFile?.setWritable(true)
        if(f.parentFile?.canRead() != true) f.parentFile?.setReadable(true)
    }

    private fun createNew(f: File) {
        f.createNewFile()
        val o = f.outputStream()
        this.storeToXML(o, "store")
        Log.d("MyPT", "Generate new prop.")
        o.close()
    }

    private fun loadFromXml(`in`: InputStream?): PropertiesTools {
        this.loadFromXML(`in`)
        return this
    }

    private fun setProp(key: String?, value: String?): PropertiesTools {
        this.setProperty(key, value)
        return this
    }

    operator fun get(key: String): String{
        return if(cache.containsKey(key)) cache[key]?:"null"
        else {
            val i = f.inputStream()
            val re = this.loadFromXml(i).getProperty(key)?:"null"
            Log.d("MyPT", "Read $key = $re")
            i.close()
            cache[key] = re
            re
        }
    }

    operator fun set(key: String, value: String) {
        cache[key] = value
        val o = f.outputStream()
        this.setProp(key, value).storeToXML(o, "store")
        Log.d("MyPT", "Set $key = $value")
        o.close()
    }
}