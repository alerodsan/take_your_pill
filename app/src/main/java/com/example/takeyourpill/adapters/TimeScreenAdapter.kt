package com.example.takeyourpill.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.takeyourpill.R
import com.example.takeyourpill.activities.AddTimeActivity
import com.example.takeyourpill.activities.TimeMainScreenActivity
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.database.DatabaseManager

open class TimeScreenAdapter(
        private val context: Context,
        private var list: ArrayList<Times>,
): RecyclerView.Adapter<RecyclerView.ViewHolder> () {

    private var onClickListener: OnClickListener? = null

    //Ver comentarios en MainScreenAdapter ya que esta actividad es similar

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_time_screen_adapter,
                        parent,
                        false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val time = list[position]

        if (holder is MyViewHolder) {
            var tv = holder.itemView.findViewById<View>(R.id.tvTime) as TextView
            tv.text = time.hour


            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, time)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddTimeActivity::class.java)
        intent.putExtra(TimeMainScreenActivity.EXTRA_TIME_DATA, list[position])

        activity.startActivityForResult(
                intent,
                requestCode
        )

        notifyItemChanged(position)
    }

    fun removeAt(position: Int){
        val dbhandler= DatabaseManager(context)
        val isDelete= dbhandler.deleteTime(list[position])
        if(isDelete>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Times)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}