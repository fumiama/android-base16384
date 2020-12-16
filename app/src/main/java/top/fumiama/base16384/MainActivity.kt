package top.fumiama.base16384

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream


class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sv.viewTreeObserver.addOnGlobalLayoutListener {
            val h = sv.getChildAt(0).height
            tti.visibility = if(h > resources.displayMetrics.heightPixels) View.GONE else View.VISIBLE
        }
        fab.setOnClickListener { if(checkReadPermission()) pickFile() }
        ben.setOnClickListener {
            ten.text?.let {
                if(it.isNotEmpty()){
                    val inputFile = generateCacheFile("input")
                    val outputFile = generateCacheFile("output")
                    inputFile.writeText(it.toString(), Charsets.UTF_16BE)
                    encode(inputFile.absolutePath, outputFile.absolutePath)
                    tde.setText(outputFile.readText(Charsets.UTF_16BE))
                }
            }
        }
        bde.setOnClickListener {
            tde.text?.let {
                if(it.isNotEmpty()){
                    val inputFile = generateCacheFile("input")
                    val outputFile = generateCacheFile("output")
                    inputFile.writeText(it.toString(), Charsets.UTF_16BE)
                    decode(inputFile.absolutePath, outputFile.absolutePath)
                    ten.setText(outputFile.readText(Charsets.UTF_16BE))
                }
            }
        }
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

    /*private fun packZip(zipf: File, outf: File){
        zipf.parentFile?.let { if(!it.exists()) it.mkdirs() }
        if(zipf.exists()) zipf.delete()
        zipf.createNewFile()
        val zip = ZipOutputStream(CheckedOutputStream(zipf.outputStream(), CRC32()))
        zip.setLevel(9)
        zip.putNextEntry(ZipEntry("output"))
        zip.write(outf.readBytes())
        zip.flush()
        zip.close()
    }*/


    private fun doFromFile(uri: Uri){
        val inputFile = generateCacheFile("input")
        val outputFile = generateCacheFile("output")
        saveFile(inputFile, uri)
        val br = inputFile.bufferedReader(Charsets.US_ASCII)
        val head1 = br.read()
        val head2 = br.read()
        val re = if(head1 == 0xFE && head2 == 0xFF) decode(inputFile.absolutePath, outputFile.absolutePath)
        else encode(inputFile.absolutePath, outputFile.absolutePath)
        Toast.makeText(
            this,
            if(re == 0) {
                createFile(getString(R.string.output))
                R.string.succeed
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

    /*private fun shareFile(file: File, type: String) {
        if (file.exists()) {
            val share = Intent(Intent.ACTION_SEND)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
                share.putExtra(Intent.EXTRA_STREAM, contentUri)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }else share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
            share.type = type //此处可发送多种文件
            share.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(share, getString(R.string.share)))
        } else Toast.makeText(this, getString(R.string.read_file_err), Toast.LENGTH_SHORT).show()
    }*/

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