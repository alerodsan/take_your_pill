package com.example.takeyourpill.adapters

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.takeyourpill.R
import com.example.takeyourpill.activities.AddPillActivity
import com.example.takeyourpill.activities.MainScreenActivity
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.database.DatabaseManager

open class MainScreenAdapter(
        private val context: Context,
        private var list: ArrayList<Pill>,
): RecyclerView.Adapter<RecyclerView.ViewHolder> () {

    private var onClickListener: OnClickListener? = null

    //Es lo que verá el usuario en cada tarjeta que compone el layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
                LayoutInflater.from(context).inflate(
                        R.layout.item_main_screen_adapter,
                        parent,
                        false
                )
        )
    }

    //Configuramos el contenido del RecyclerView
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val pill = list[position]

        if (holder is MyViewHolder) {
            var iv = holder.itemView.findViewById<View>(R.id.iv_pill_image) as ImageView
            iv.setImageURI(Uri.parse(pill.image))
            var tv1 = holder.itemView.findViewById<View>(R.id.tvName) as TextView
            tv1.text = pill.name
            var tv2 = holder.itemView.findViewById<View>(R.id.tvDescription) as TextView
            tv2.text = pill.description
            var tv3 = holder.itemView.findViewById<View>(R.id.tvDoses) as TextView
            tv3.text = pill.doses



            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, pill)
                }
            }

        }
    }


    override fun getItemCount(): Int {
       return list.size
    }

    //Método por el cual indicamos al RecyclerView que ha habido un cambio y debe de mostrarlo
    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddPillActivity::class.java)
            intent.putExtra(MainScreenActivity.EXTRA_PILL_DETAILS, list[position])




        activity.startActivityForResult(
                intent,
                requestCode
        )

        notifyItemChanged(position)
    }

    //Método que indica qeu se borre en la posición determinada del RecyclerView

    fun removeAt(position: Int){

        val dbhandler=DatabaseManager(context)
        val isDelete= dbhandler.deletePill(list[position])
        if(isDelete>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }

    }

    //Para que podamos hacer interactuable el contenido del Recyclerview implementamos los métodos de OnClicklistener

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Pill)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}