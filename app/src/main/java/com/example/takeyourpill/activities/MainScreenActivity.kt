package com.example.takeyourpill.activities

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.takeyourpill.R
import com.example.takeyourpill.adapters.MainScreenAdapter
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.data.User
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityMainScreenBinding
import com.example.takeyourpill.utils.SwipeToDelete
import com.example.takeyourpill.utils.SwipeToEdit


class MainScreenActivity : AppCompatActivity() {

    companion object{
        var SCREEN_EXTRA="SCREEN_EXTRA"
        var ADD_PILL_REQUEST_CODE=1
        var EXTRA_PILL_DETAILS="PILL_DETAILS_EXTRA"
        var EXTRA_PILL_UPDATED="PILL UPDATED"
    }

    private lateinit var binding: ActivityMainScreenBinding
    private var mUserData: User?=null
    private var mPillData: Pill?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)


        binding= ActivityMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        when {
            intent.hasExtra(MainActivity.EXTRA_LOGIN) -> {
                mUserData = intent.getParcelableExtra(MainActivity.EXTRA_LOGIN)

            }
            intent.hasExtra(AddPillActivity.ADD_PILL_EXTRA) -> {
                mUserData=intent.getParcelableExtra(AddPillActivity.ADD_PILL_EXTRA)


            }
            intent.hasExtra(UserActivity.EXTRA_USER) -> {
                mUserData=intent.getParcelableExtra(UserActivity.EXTRA_USER)


            }
            intent.hasExtra(AddPillActivity.ADD_PILL_EXTRA_UPDATED) -> {
                mUserData=intent.getParcelableExtra(AddPillActivity.ADD_PILL_EXTRA_UPDATED)

            }
            intent.hasExtra(TimeMainScreenActivity.EXTRA_PILL_DATA) ->{
                mPillData=intent.getParcelableExtra(TimeMainScreenActivity.EXTRA_PILL_DATA)
            }
        }

        //Controlan los botones que cambian de actividad, utilizan el Parcelable para llevar la info del User a las otras actividades

        binding.fabAddUser.setOnClickListener{

                val intent2= Intent (this@MainScreenActivity ,UserActivity::class.java)
                intent2.putExtra(SCREEN_EXTRA, mUserData)
                startActivity(intent2)
                finish()

        }

        binding.fabAddPill.setOnClickListener{
            val intent2= Intent (this@MainScreenActivity ,AddPillActivity::class.java).apply {
                putExtra(SCREEN_EXTRA,mUserData)
                putExtra(EXTRA_PILL_UPDATED,mPillData)
            }
            startActivityForResult(intent2, ADD_PILL_REQUEST_CODE)
            finish()


        }




        getPillListFromLocalDB()


    }

    //Método por el cual obtenemos la lista de Pills de ese usuario mediante su id o la foreign key de la Pill pues es la misma que la id del usuario

    private fun getPillListFromLocalDB() {

        val dbHandler = DatabaseManager(this)

        when {

            mUserData != null -> {
                val id = mUserData!!.id

                val getPillList = dbHandler.getPillListbyId(id)

                if (getPillList.size > 0) {
                    binding.rvPillList.visibility = View.VISIBLE
                    binding.tvNoRecordsAvailable.visibility = View.GONE
                    setUpPillsRecyclerView(getPillList)
                } else {
                    binding.rvPillList.visibility = View.GONE
                    binding.tvNoRecordsAvailable.visibility = View.VISIBLE
                }
            }
            mPillData != null -> {

                val id = mPillData!!.userId

                val getPillList = dbHandler.getPillListbyId(id)

                if (getPillList.size > 0) {
                    binding.rvPillList.visibility = View.VISIBLE
                    binding.tvNoRecordsAvailable.visibility = View.GONE
                    setUpPillsRecyclerView(getPillList)
                } else {
                    binding.rvPillList.visibility = View.GONE
                    binding.tvNoRecordsAvailable.visibility = View.VISIBLE
                }
            }
        }

    }

    //Método por el cual configuramos el RecyclerView que mostrará la lista
    private fun setUpPillsRecyclerView(pillList:ArrayList<Pill>) {
        binding.rvPillList.layoutManager = LinearLayoutManager(this)
        binding.rvPillList.setHasFixedSize(true)
        val pillAdapter = MainScreenAdapter(this, pillList)
        binding.rvPillList.adapter = pillAdapter

        pillAdapter.setOnClickListener(object:MainScreenAdapter.OnClickListener{
            override fun onClick(position: Int, model: Pill) {
                val intent=Intent(this@MainScreenActivity, PillActivity::class.java)
                intent.putExtra(EXTRA_PILL_DETAILS, model)
                startActivity(intent)



            }




        })

        //Controlamos la acción que se ejecutará al realizar el gesto de mover hacia derecha que nos llevará a actualizar la información de la Pill
        val editSwipeHandler = object : SwipeToEdit(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvPillList.adapter as MainScreenAdapter
                adapter.notifyEditItem(this@MainScreenActivity, viewHolder.adapterPosition,
                        ADD_PILL_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvPillList)

        //Controlamos el gesto de mover hacia la izquierda que eliminará la entrada, antes mostramos una advertencia
        val deleteSwipeHandler = object : SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvPillList.adapter as MainScreenAdapter
                val pos= viewHolder.adapterPosition
                val dialog= AlertDialog.Builder(this@MainScreenActivity)
                        .setTitle(getString(R.string.alert_dialog_delete))
                        .setMessage(getString(R.string.alert_dialog_confirm_delete))
                        .setNegativeButton(getString(R.string.alert_dialog_no)){ view, _ ->
                            Toast.makeText(this@MainScreenActivity,getString(R.string.alert_dialog_denied_delete),Toast.LENGTH_LONG).show()
                            view.dismiss()
                            getPillListFromLocalDB()
                        }

                        .setPositiveButton(getString(R.string.alert_dialog_yes)){ view, _ ->
                            adapter.removeAt(pos)
                            Toast.makeText(this@MainScreenActivity,getString(R.string.alert_dialog_delete_confirmed), Toast.LENGTH_LONG).show()
                            view.dismiss()
                            getPillListFromLocalDB()
                        }
                        .setCancelable(false)
                        .create()
                dialog.show()




            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvPillList)
    }

    //Controlamos que si la Pill se añadió correctamente nos muestre la lista actualizada
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== ADD_PILL_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK)
                getPillListFromLocalDB()
        }
    }
}
