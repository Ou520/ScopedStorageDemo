package com.example.scopedstoragedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class PhotoShowActivity : AppCompatActivity() {

    private lateinit var photoView: PhotoView
    private lateinit var ib_back: ImageButton

    var url:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_show)
        photoView = findViewById(R.id.photo_view)
        ib_back = findViewById(R.id.ib_back)

        initData()
        initListener()
    }

    private fun initData() {

        //接收Intent传过来的网址
        val intent = intent
        url = intent.getStringExtra("urlKey") //图片地址

        if (url !=""){
            Glide.with(this).load(url).into(photoView)
        }else{
            Toast.makeText(this,"图片获取失败",Toast.LENGTH_SHORT).show()
        }


    }

    private fun initListener() {

        //返回按钮
        ib_back.setOnClickListener(View.OnClickListener {
            finish()
        })
    }
}