package top.fumiama.base16384

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        sv.viewTreeObserver.addOnGlobalLayoutListener { setTitleVisibility() }
        fab.setOnClickListener { if(checkReadPermission()) pickFile() }
        ben.setOnClickListener { clickButton(true, cm) }
        bde.setOnClickListener { clickButton(false, cm) }
        tti.setOnLongClickListener {
            AlertDialog.Builder(this).setTitle(R.string.info).setMessage(R.string.info_content).setIcon(R.mipmap.ic_launcher).show()
            true
        }
        setLongPress2Paste(ten, cm)
        setLongPress2Paste(tde, cm)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) when (requestCode) {
            1 -> data?.data?.let { doFromFile(it) }
            2 -> data?.data?.let { save2Uri(it) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) pickFile()
                else Toast.makeText(this, R.string.permissionDenied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doFromFile(uri: Uri){
        val inputFile = generateCacheFile("input")
        val outputFile = generateCacheFile("output")
        saveFile(inputFile, uri)
        val bbf = ByteArray(2)
        val br = inputFile.inputStream()
        br.read(bbf)
        br.close()
        val isDecode = bbf[0] == (-2).toByte() && bbf[1] == (-1).toByte()

        val re = if(isDecode) decode(inputFile.absolutePath, outputFile.absolutePath)
        else encode(inputFile.absolutePath, outputFile.absolutePath)
        Toast.makeText(
            this,
            if(re == 0) {
                createFile(getString(R.string.output))
                if(isDecode) R.string.decode_succeed else R.string.encode_succeed
            } else R.string.failed,
            Toast.LENGTH_SHORT
        ).show()
    }

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

    private fun checkReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
            false
        } else true
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
            Toast.makeText(this, getString(R.string.copied)+t.text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLongPress2Paste(t: TextInputEditText, cm: ClipboardManager){
        t.setOnLongClickListener {
            if(t.text?.isEmpty() == true) t.setText(cm.primaryClip?.getItemAt(0)?.text)
            false
        }
    }

    private fun clickButton(isEncode: Boolean, cm:ClipboardManager){
        val tin = if(isEncode)ten else tde
        val tou = if(isEncode)tde else ten
        tin.text?.let {
            if(it.isNotEmpty()){
                val inputFile = generateCacheFile("input")
                val outputFile = generateCacheFile("output")
                inputFile.writeText(it.toString(), Charsets.UTF_16BE)
                if(isEncode) encode(inputFile.absolutePath, outputFile.absolutePath)
                else decode(inputFile.absolutePath, outputFile.absolutePath)
                tou.setText(outputFile.readText(Charsets.UTF_16BE))
                copyText(tou, cm)
            }
        }
    }

    private fun setTitleVisibility(){
        val h = sv.getChildAt(0).height
        val r = Rect()
        window.decorView.rootView.getWindowVisibleDisplayFrame(r)
        tti.visibility = if(h > r.bottom) View.GONE else View.VISIBLE
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private external fun encode(sf: String, df: String): Int
    private external fun decode(sf: String, df: String): Int

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("base16384")
        }
    }
}