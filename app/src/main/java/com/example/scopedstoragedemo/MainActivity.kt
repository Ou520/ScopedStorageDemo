package com.example.scopedstoragedemo

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.scopedstoragedemo.databinding.ActivityMainBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

const val PICK_FILE = 1


class MainActivity : AppCompatActivity() {

    private val permissionsToRequire = ArrayList<String>()//存放需要申请的权限的集合

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        //判断是否获取读取权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequire.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        //判断是否获取写入权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequire.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        //判断权限是否已经授权
        if (permissionsToRequire.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequire.toTypedArray(), 0)
        }

        initListener(binding)

    }

    private fun initListener(binding: ActivityMainBinding) {

        //从图库中获取图片
        binding.browseAlbum.setOnClickListener {
            val intent = Intent(this, BrowseAlbumActivity::class.java)
            startActivity(intent)
        }

        //将图片添加到图库中
        binding.addImageToAlbum.setOnClickListener {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.image)//获取图片的Bitmap对象
            val displayName = "${System.currentTimeMillis()}.jpg"//图片显示的名称
            val mimeType = "image/jpeg"//MIME的类型类型
            val compressFormat = Bitmap.CompressFormat.JPEG//设置图片裁剪的格式


            addBitmapToAlbum(bitmap, displayName, mimeType, compressFormat)
        }

        //下载文件的方法
        binding.downloadFile.setOnClickListener {
            val fileUrl = "https://konachan.net/sample/bd2bb9496a33f7cbe7574deda0a39a43/Konachan.com%20-%20323758%20sample.jpg"
            val fileName = "${System.currentTimeMillis()}.jpg"
            downloadFile(fileUrl, fileName)

        }

        //打开文件选择器
        binding.pickFile.setOnClickListener {
            pickFileAndCopyUriToExternalFilesDir()
        }


    }

    //授权管理
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "没有给予权限，你将无法使用本程序", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }


    //将图片添加到相册的方法（源数据是Bitmap的形式）
    private fun addBitmapToAlbum(bitmap: Bitmap, displayName: String, mimeType: String, compressFormat: Bitmap.CompressFormat) {

        val values = ContentValues()

        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

        //判断SDK版本（并在图库中创建文件）
        //参考资料 https://developer.android.google.cn/training/camera/photobasics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName")
        }

        //获取图库已经创建好的空白文件的路径
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //向文件中写入图片数据(以流的形式)
        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(compressFormat, 100, outputStream)
                outputStream.close()
                Toast.makeText(this, "图片已成功插入图库中！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //将图片添加到相册的方法（源数据是输入流的形式）
    fun writeInputStreamToAlbum(inputStream: InputStream, displayName: String, mimeType: String) {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

        //判断SDK版本（并在图库中创建文件）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        } else {
            values.put(MediaStore.MediaColumns.DATA, "${Environment.getExternalStorageDirectory().path}/${Environment.DIRECTORY_DCIM}/$displayName")
        }

        //缓存输入流
        val bis = BufferedInputStream(inputStream)

        //获取图库已经创建好的空白文件的路径
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        //向文件中写入图片数据(以流的形式)
        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                val bos = BufferedOutputStream(outputStream)
                val buffer = ByteArray(1024)
                var bytes = bis.read(buffer)
                while (bytes >= 0) {
                    bos.write(buffer, 0 , bytes)
                    bos.flush()
                    bytes = bis.read(buffer)
                }
                bos.close()
                Toast.makeText(this, "图片已成功插入图库中！", Toast.LENGTH_SHORT).show()
            }
        }
        bis.close()
    }




    //下载文件的方法
    private fun downloadFile(fileUrl: String, fileName: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(this, "You must use device running Android 10 or higher", Toast.LENGTH_SHORT).show()
            return
        }
        thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000
                val inputStream = connection.inputStream
                val bis = BufferedInputStream(inputStream)

                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                if (uri != null) {
                    val outputStream = contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        val bos = BufferedOutputStream(outputStream)
                        val buffer = ByteArray(1024)
                        var bytes = bis.read(buffer)
                        while (bytes >= 0) {
                            bos.write(buffer, 0 , bytes)
                            bos.flush()
                            bytes = bis.read(buffer)
                        }
                        bos.close()
                        runOnUiThread {
                            Toast.makeText(this, "$fileName 下载成功返回的路径： $uri", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                bis.close()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //打开文件选择器的方法
    private fun pickFileAndCopyUriToExternalFilesDir() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE)
    }



    //Intent跳转携带的数据
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            //文件选择器的返回的数据
            PICK_FILE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val uri = data.data//文件选择器返回的路径
                    if (uri != null) {
                        val fileName = getFileNameByUri(uri)//根据Uri获取文件的名称
                        Toast.makeText(this,"选择文件的名字:${fileName}"+"\n"+"\n"+"文件选择器返回的路径:${uri}",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    }

    //根据Uri获取文件名的方法
    private fun getFileNameByUri(uri: Uri): String {
        var fileName = System.currentTimeMillis().toString()
        val cursor = contentResolver.query(uri, null, null, null, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            cursor.close()
        }
        return fileName
    }


}
