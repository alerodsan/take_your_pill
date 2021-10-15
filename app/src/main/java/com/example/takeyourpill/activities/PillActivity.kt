package com.example.takeyourpill.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.takeyourpill.R
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.databinding.ActivityPillBinding

class PillActivity : AppCompatActivity() {

    companion object{

        var EXTRA_TIME="EXTRA_TIME"
    }

    private lateinit var binding:ActivityPillBinding
    private var mPillData:Pill?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pill)

        binding= ActivityPillBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if(intent.hasExtra(MainScreenActivity.EXTRA_PILL_DETAILS)){
            mPillData=intent.getParcelableExtra(MainScreenActivity.EXTRA_PILL_DETAILS)
        }

        //Aquí mostramos la info obtenida de la Pill que hemos elegido en MainScreenActivity

        if(mPillData!=null){
            setSupportActionBar(binding.toolbarPillActivity)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title=mPillData!!.name

            binding.toolbarPillActivity.setNavigationOnClickListener {
                onBackPressed()
            }

            binding.ivPillImage.setImageURI(Uri.parse(mPillData!!.image))
            binding.tvDoses.text=mPillData!!.doses
            binding.tvDescription.text=mPillData!!.description
            binding.tvName.text=mPillData!!.name
        }

        binding.btnViewTimes.setOnClickListener{
            val intent= Intent(this,TimeMainScreenActivity::class.java)
            if (mPillData!=null) {
                intent.putExtra(EXTRA_TIME, mPillData)
            }
            startActivity(intent)
        }


    }
}