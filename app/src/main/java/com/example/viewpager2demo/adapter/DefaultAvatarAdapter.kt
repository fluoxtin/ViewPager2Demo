package com.example.viewpager2demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.viewpager2demo.R

import java.util.*

/**
 * @author LiWenqing on 2022/2/16
 */
class DefaultAvatarAdapter : RecyclerView.Adapter<DefaultAvatarAdapter.AvatarViewHolder>(){

    var looperEnable = true

    private var entities : List<Any>? = null

    private var onItemClickedListener : OnItemClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.register_default_avatar_item, parent, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        view.layoutParams = layoutParams
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {

        val image = entities!![position % entities!!.size]

        if (image is Int) {
            holder.bind(image)
        }

        holder.itemView.setOnClickListener {
            onItemClickedListener?.onItemClicked(position)
        }
    }

    override fun getItemCount(): Int {
        return if (looperEnable && entities!!.size > 1)
            Int.MAX_VALUE
        else
            entities!!.size
    }

    fun setEntities(entities: List<Any>) {
        this.entities = entities
    }

    fun getRealPosition(position: Int) : Int {
        val pageSize = entities!!.size

        if (pageSize == 0) return 0

        return if (looperEnable)
                (position + pageSize) % pageSize
            else
                position
    }

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        onItemClickedListener = listener
    }
    inner class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mDefaultAvatar: ImageView = itemView.findViewById(R.id.default_avatar)

        fun bind(image: String) {
            // TODO: load image by url with glide framwork
        }

        fun bind(image : Int) {
            mDefaultAvatar.setImageResource(image)
        }

    }

    interface OnItemClickedListener {
        fun onItemClicked(position: Int)
    }

}