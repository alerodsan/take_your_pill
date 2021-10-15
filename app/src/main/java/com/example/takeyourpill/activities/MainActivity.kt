package com.example.takeyourpill.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.takeyourpill.R
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),View.OnClickListener {

    companion object{

        var EXTRA_LOGIN="LOGIN"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegister.setOnClickListener {
            val intent= Intent(this@MainActivity, UserActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener(this)
    }

   override fun onClick(v: View){

        when(v.id){
            R.id.btn_login ->{
                //Controlamos que los campos no esten vacíos
                when {
                    binding.etNick.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.request_login_user), Toast.LENGTH_SHORT)
                            .show()
                    }
                    binding.etPassword.text.isNullOrEmpty() -> {
                        Toast.makeText(
                            this,
                            getString(R.string.request_login_password),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    else->{
                        //Si los campos no están vacíos comprobámos que coincidan con lso valores en la base de datos y logeamos al usuario
                        val dbHandler=DatabaseManager(this)
                            if(dbHandler.login(binding.etNick.text.toString().trim{it<=' '}, binding.etPassword.text.toString().trim{it<=' '})){

                                val user= dbHandler.getUserByNick(binding.etNick.text.toString().trim{it<=' '})
                                val intent=Intent(this@MainActivity,MainScreenActivity::class.java)
                                intent.putExtra(EXTRA_LOGIN,user)
                                startActivity(intent)

                        }else{
                            //Si no coinciden entonces mostramos el error
                            binding.tilPassword.boxStrokeColor= Color.parseColor("#f44336")
                            Toast.makeText(this@MainActivity,getString(R.string.wrong_login_user_password),Toast.LENGTH_SHORT).show()
                        }

                }

                }
        }


        }
    }
}