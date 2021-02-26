package com.example.scopedstoragedemo.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.scopedstoragedemo.Image
import com.example.scopedstoragedemo.PhotoShowActivity
import com.example.scopedstoragedemo.R


//RecyclerView的适配器
class AlbumAdapter(private val context: Context, private val imageList: List<Image>,private val imageSize: Int, ) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.album_image_item, parent, false)

        return ViewHolder(view)
    }

    //Item的数量
    override fun getItemCount() = imageList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.layoutParams.width = imageSize
        holder.imageView.layoutParams.height = imageSize
        val image = imageList[position]


        val options = RequestOptions().placeholder(R.drawable.ic_loading).override(imageSize, imageSize)
        Glide.with(context).load(image.uri).apply(options).into(holder.imageView)



        holder.imageView.setOnClickListener{

            //让intent携带数据跳转到ImgShow
            val intent = Intent(context, PhotoShowActivity::class.java)
            intent.putExtra("urlKey", image.uri.toString())
            context.startActivity(intent)
        }

    }

}