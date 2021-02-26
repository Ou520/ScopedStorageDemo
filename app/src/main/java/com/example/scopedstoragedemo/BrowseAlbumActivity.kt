package com.example.scopedstoragedemo

import android.content.ContentUris

import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewTreeObserver.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.scopedstoragedemo.adapter.AlbumAdapter
import com.example.scopedstoragedemo.databinding.ActivityBrowseAlbumBinding
import kotlin.concurrent.thread
import android.view.ViewTreeObserver.OnPreDrawListener as OnPreDrawListener1

class BrowseAlbumActivity : AppCompatActivity() {

    private val imageList = ArrayList<Image>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityBrowseAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = "相册"//设置标题栏的标题

        //RecyclerView的监听
        binding.recyclerView.viewTreeObserver.addOnPreDrawListener(object : OnPreDrawListener1 {
            override fun onPreDraw(): Boolean {
                binding.recyclerView.viewTreeObserver.removeOnPreDrawListener(this)
                val columns = 2
                //设置RecyclerView中Item的大小
                val imageSize = binding.recyclerView.width / columns

                //初始化适配器
                val adapter = AlbumAdapter(this@BrowseAlbumActivity, imageList, imageSize)

                //设置RecyclerView的布局
                binding.recyclerView.layoutManager = GridLayoutManager(this@BrowseAlbumActivity, columns)
                binding.recyclerView.adapter = adapter//设置适配器

                loadImages(adapter)

                return false
            }
        })

        //返回按钮
        binding.ibBack.setOnClickListener{
            finish()
        }


    }

    //获取相册中的图片ID和Uri
    private fun loadImages(adapter: AlbumAdapter) {

        //开启子线程
        thread {

            val cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                null, null, "${MediaStore.MediaColumns.DATE_ADDED} desc")
            if (cursor != null) {
                //循环获取相册中图片ID和Uri
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    imageList.add(Image(uri))//将Uri地址存储到集合中
                }
                cursor.close()//关闭内容提供器
            }

            runOnUiThread {
                //刷新适配器
                adapter.notifyDataSetChanged()
            }
        }
    }

}
