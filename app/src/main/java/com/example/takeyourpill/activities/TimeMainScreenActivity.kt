package com.example.takeyourpill.activities

import android.app.Activity
import android.app.AlertDialog
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
import com.example.takeyourpill.adapters.TimeScreenAdapter
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityTimeMainScreenBinding
import com.example.takeyourpill.utils.SwipeToDelete
import com.example.takeyourpill.utils.SwipeToEdit


class TimeMainScreenActivity : AppCompatActivity() {

    companion object{
        var EXTRA_PILL_DATA="EXTRA_PILL_DATA"
        var ADD_TIME_REQUEST_CODE=1
        var EXTRA_TIME_DATA="EXTRA_TIME_DATA"
    }

    private lateinit var binding: ActivityTimeMainScreenBinding
    private var mPillData: Pill?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_main_screen)
        binding= ActivityTimeMainScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Mirar los comentarios en MainScreenActivity pues esta actividad funciona de manera similar
        when {
            intent.hasExtra(PillActivity.EXTRA_TIME) -> {
                mPillData = intent.getParcelableExtra(PillActivity.EXTRA_TIME)
            }
            intent.hasExtra(AddTimeActivity.TIME_EXTRA) -> {
                mPillData = intent.getParcelableExtra(AddTimeActivity.TIME_EXTRA)
            }
            intent.hasExtra(AddTimeActivity.TIME_EXTRA_UPDATED) -> {
                mPillData = intent.getParcelableExtra(AddTimeActivity.TIME_EXTRA_UPDATED)

            }
        }

        binding.fabAddTime.setOnClickListener{
            val intent= Intent (this@TimeMainScreenActivity ,AddTimeActivity::class.java)
            intent.putExtra(EXTRA_PILL_DATA,mPillData)
            startActivityForResult(intent, ADD_TIME_REQUEST_CODE)
            finish()

        }

        binding.fabMainScreen.setOnClickListener {
            val intent= Intent (this@TimeMainScreenActivity ,MainScreenActivity::class.java)
            intent.putExtra(EXTRA_PILL_DATA,mPillData)
            startActivity(intent)
            finish()
        }




        getTimeListFromLocalDB()


    }

    private fun getTimeListFromLocalDB() {

        val dbHandler = DatabaseManager(this)


        val id=mPillData!!.id

        val getTimeList = dbHandler.getTimeListbyId(id)

        if (getTimeList.size > 0) {
            binding.rvTimeList.visibility = View.VISIBLE
            binding.tvNoRecordsAvailable.visibility = View.GONE
            setUpTimeRecyclerView(getTimeList)
        } else {
            binding.rvTimeList.visibility = View.GONE
            binding.tvNoRecordsAvailable.visibility = View.VISIBLE
        }

    }

    private fun setUpTimeRecyclerView(timeList:ArrayList<Times>) {
        binding.rvTimeList.layoutManager = LinearLayoutManager(this)
        binding.rvTimeList.setHasFixedSize(true)
        val timeAdapter = TimeScreenAdapter(this, timeList)
        binding.rvTimeList.adapter = timeAdapter

        timeAdapter.setOnClickListener(object: TimeScreenAdapter.OnClickListener {
            override fun onClick(position: Int, model: Times) {
                val intent= Intent(this@TimeMainScreenActivity, TimeActivity::class.java)
                intent.putExtra(EXTRA_TIME_DATA, model)
                startActivity(intent)



            }




        })

        val editSwipeHandler = object : SwipeToEdit(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvTimeList.adapter as TimeScreenAdapter
                adapter.notifyEditItem(this@TimeMainScreenActivity, viewHolder.adapterPosition,
                    ADD_TIME_REQUEST_CODE
                )


            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding.rvTimeList)

        val deleteSwipeHandler = object : SwipeToDelete(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.rvTimeList.adapter as TimeScreenAdapter
                val pos= viewHolder.adapterPosition
                val dialog= AlertDialog.Builder(this@TimeMainScreenActivity)
                    .setTitle(getString(R.string.alert_dialog_delete))
                    .setMessage(getString(R.string.alert_dialog_confirm_delete))
                    .setNegativeButton(getString(R.string.alert_dialog_no)){ view, _ ->
                        Toast.makeText(this@TimeMainScreenActivity,getString(R.string.alert_dialog_denied_delete), Toast.LENGTH_LONG).show()
                        view.dismiss()
                        getTimeListFromLocalDB()
                    }

                    .setPositiveButton(getString(R.string.alert_dialog_yes)){ view,_ ->
                        adapter.removeAt(pos)
                        Toast.makeText(this@TimeMainScreenActivity,getString(R.string.alert_dialog_delete_confirmed), Toast.LENGTH_LONG).show()
                        view.dismiss()
                        getTimeListFromLocalDB()
                    }
                    .setCancelable(false)
                    .create()
                dialog.show()




            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding.rvTimeList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== ADD_TIME_REQUEST_CODE){
            if(resultCode== Activity.RESULT_OK)
                getTimeListFromLocalDB()
        }
    }


    }