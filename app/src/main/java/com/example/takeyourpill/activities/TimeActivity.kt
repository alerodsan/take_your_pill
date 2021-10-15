package com.example.takeyourpill.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.takeyourpill.R
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.databinding.ActivityTimeBinding


class TimeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pill)

        binding= ActivityTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Aquí mostramos la info obtenida del tiempo que hemos elegido en TimeMainScreenActivity

        var time: Times?=null

        if(intent.hasExtra(TimeMainScreenActivity.EXTRA_TIME_DATA)){
            time=intent.getParcelableExtra(TimeMainScreenActivity.EXTRA_TIME_DATA)
        }


        if(time!=null){
            setSupportActionBar(binding.toolbarTimeActivity)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=getString(R.string.tb_time)

            binding.toolbarTimeActivity.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.tvTime.text=time.hour.toString()
        }

    }
}