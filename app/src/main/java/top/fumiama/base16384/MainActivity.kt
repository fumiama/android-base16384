package top.fumiama.base16384

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Switch
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import top.fumiama.base16384.tools.PropertiesTools
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset

class MainActivity : Activity() {
    var forceDecode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val pCharsets = PropertiesTools(File(filesDir, "charsets.prop"))

        sv.viewTreeObserver.addOnGlobalLayoutListener { setViewsVisibility() }
        fab.setOnClickListener { pickFile() }
        fab.setOnLongClickListener {
            AlertDialog.Builder(this)
                    .setTitle(R.string.alert)
                    .setMessage(R.string.force_decode)
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton(android.R.string.ok){ _, _ ->
                        forceDecode = true
                        pickFile()
                    }
                    .setNegativeButton(android.R.string.cancel){ _, _ ->}
                    .show()
            false
        }
        ben.setOnClickListener { clickButton(true, cm, pCharsets) }
        bde.setOnClickListener { clickButton(false, cm, pCharsets) }
        ben.setOnLongClickListener {
            callCharsetSelectList(pCharsets)
            false
        }
        /*bde.setOnLongClickListener {
            callCharsetSelectList(false, pCharsets)
            false
        }*/
        tti.setOnLongClickListener {
            AlertDialog.Builder(this).setTitle(R.string.info).setMessage(R.string.info_content).setIcon(R.mipmap.ic_launcher).show()
            true
        }
        setLongPress2Paste(ten, cm)
        setLongPress2Paste(tde, cm)
        buildSwitch(pCharsets, sl, "use_lzma")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) when (requestCode) {
            1 -> data?.data?.let { doFromFile(it) }
            2 -> data?.data?.let { save2Uri(it) }
        }
    }

    private fun doFromFile(uri: Uri) = Thread{
        val inputFile = generateCacheFile("input")
        val outputFile = generateCacheFile("output")
        saveFile(inputFile, uri)
        val bbf = ByteArray(2)
        val br = inputFile.inputStream()
        br.read(bbf)
        br.close()
        val isDecode = (bbf[0] == (-2).toByte() && bbf[1] == (-1).toByte()) || forceDecode
        if (forceDecode) forceDecode = false

        val re = base16(!isDecode, inputFile.absolutePath, outputFile.absolutePath, true)
        val fileName = DocumentFile.fromSingleUri(this, uri)?.name?:getString(R.string.output)
        val frontName = fileName.substringBeforeLast('.')
        runOnUiThread {
            if(re != "") Toast.makeText(this, re, Toast.LENGTH_SHORT).show()
            else createFile(if(isDecode) frontName else "$fileName.txt")
        }
    }.start()

    private fun saveFile(f: File?, uri: Uri) {
        val fd = contentResolver.openFileDescriptor(uri, "r")
        fd?.fileDescriptor?.let {
            val fi = FileInputStream(it)
            f?.outputStream()?.let {
                fi.copyTo(it)
                it.close()
            }
            fi.close()
        }
        fd?.close()
    }

    private fun save2Uri(uri: Uri){
        val outputFile = generateCacheFile("output")
        contentResolver.openOutputStream(uri)?.let {
            val fi = outputFile.inputStream()
            fi.copyTo(it)
            fi.close()
            it.close()
        }
    }

    private fun createFile(fileName: String, type: String = "*/*"){
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = type
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, 2)
    }

    private fun pickFile() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "*/*"
        startActivityForResult(i, 1)
    }
    
    private fun generateCacheFile(name: String) = File(cacheDir, name)

    private fun copyText(t: TextInputEditText, cm: ClipboardManager){
        if(t.text?.isNotEmpty() == true) {
            ClipData.newPlainText(getString(R.string.app_name), t.text)?.let { cm.setPrimaryClip(it) }
            Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLongPress2Paste(t: TextInputEditText, cm: ClipboardManager){
        t.setOnLongClickListener {
            if(t.text?.isEmpty() == true) t.setText(cm.primaryClip?.getItemAt(0)?.text)
            false
        }
    }

    private fun clickButton(isEncode: Boolean, cm:ClipboardManager, pc: PropertiesTools) = Thread{
        val tin = if(isEncode)ten else tde
        val tou = if(isEncode)tde else ten
        tin.text?.let {
            if(it.isNotEmpty()){
                val inputFile = generateCacheFile("input")
                val outputFile = generateCacheFile("output")
                
                inputFile.writeText(it.toString(), getCharset(getCustomCharsetPosition(isEncode, pc)))
                val re = base16(isEncode, inputFile.absolutePath, outputFile.absolutePath)
                runOnUiThread {
                    tou.setText(outputFile.readText(getCharset(getCustomCharsetPosition(!isEncode, pc))))
                    copyText(tou, cm)
                    if(re != "") Toast.makeText(this, re, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }.start()
    
    private fun base16(isEncode: Boolean, sf: String, of: String, makeToast: Boolean = false): String {
        var re = ""
        var bre = 0
        if(sl.isChecked) {
            val tf = generateCacheFile("lzma_temp").absolutePath
            if(isEncode) re += lzma(sf, tf, true)
            bre = base16384(isEncode, if(isEncode) tf else sf, if(isEncode) of else tf)
            if(!isEncode) re += lzma(tf, of, false)
        } else bre = base16384(isEncode, sf, of)
        if(makeToast) runOnUiThread {
            Toast.makeText(this, if(bre == 0) (if(isEncode) R.string.encode_succeed else R.string.decode_succeed) else R.string.read_file_err, Toast.LENGTH_SHORT).show()
        }
        return re
    }

    private fun base16384(isEncode: Boolean, sf: String, of: String): Int = if(isEncode) encode(sf, of) else decode(sf, of)

    private fun setViewsVisibility(){
        val h = sv.getChildAt(0).height
        val r = Rect()
        window.decorView.rootView.getWindowVisibleDisplayFrame(r)
        val visibility = if(h > r.bottom) View.GONE else View.VISIBLE
        tti.visibility = visibility
        sl.visibility = visibility
    }

    private fun callCharsetSelectList(pc: PropertiesTools){
        val charsetsArr = resources.getStringArray(R.array.charsets)
        AlertDialog.Builder(this)
                .setTitle(R.string.select_charset)
                .setIcon(R.mipmap.ic_launcher)
                .setSingleChoiceItems(ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, charsetsArr), getCustomCharsetPosition(true, pc)){ d, p ->
                    setCustomCharsetPosition(p, pc)
                    d.cancel()
                }.show()
    }

    private fun getCustomCharsetPosition(isEncode: Boolean, pc: PropertiesTools): Int{
        return if(!isEncode) 3 else {
            val cs = pc["encode"]
            if (cs == "null") 8 else cs.toInt()
        }
    }

    private fun setCustomCharsetPosition(p: Int, pc: PropertiesTools){
        pc["encode"] = p.toString()
    }

    private fun getCharset(p: Int) = when (p) {
        0 -> Charsets.ISO_8859_1
        1 -> Charsets.US_ASCII
        2 -> Charsets.UTF_16
        3 -> Charsets.UTF_16BE
        4 -> Charsets.UTF_16LE
        5 -> Charsets.UTF_32
        6 -> Charsets.UTF_32BE
        7 -> Charsets.UTF_32LE
        8 -> Charsets.UTF_8
        else -> Charset.defaultCharset()
    }

    private fun buildSwitch(p: PropertiesTools, s: Switch, key: String, click: (() -> Unit)? = null) {
        s.isChecked = p[key] == "true"
        s.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) p[key] = "true"
            else p[key] = "false"
        }
        click?.let { s.setOnClickListener { it() } }
    }

    /**
     * Native methods that are implemented by native libraries,
     * which is packaged with this application.
     */
    private external fun encode(sf: String, df: String): Int
    private external fun decode(sf: String, df: String): Int
    private external fun lzma(sf: String, df: String, isEncode: Boolean): String

    companion object {
        // Used to load libraries on application startup.
        init {
            System.loadLibrary("2_14")
            System.loadLibrary("lzma")
        }
    }
}