package com.example.takeyourpill.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.takeyourpill.R
import com.example.takeyourpill.data.User
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityUserBinding
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        var EXTRA_USER="EXTRA_USER"
    }

    private lateinit var binding: ActivityUserBinding
    private var cal=Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var mUserData:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        binding= ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarUser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//

        binding.toolbarUser.setNavigationOnClickListener {
            onBackPressed()
        }

        //Mostramos la fecha actual

        dateSetListener=DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        //Si hay datos de usuario que venga de otra actividad los mostraremos
        if(intent.hasExtra(MainScreenActivity.SCREEN_EXTRA)){
            mUserData=intent.getParcelableExtra(MainScreenActivity.SCREEN_EXTRA)
        }

        //En ese caso configuramos el layout para que no muestre el usuario ni la contraseña
        if(mUserData != null){
            supportActionBar?.title="Editar ${mUserData!!.nick}"

            binding.tilNick.visibility=View.GONE
            binding.tilPassword.visibility=View.GONE
            binding.etNick.visibility=View.GONE
            binding.etPassword.visibility=View.GONE
            binding.etNick.setText(mUserData!!.nick)
            binding.etPassword.setText(mUserData!!.password)
            binding.etName.setText(mUserData!!.name)
            binding.etSurname1.setText(mUserData!!.surname1)
            binding.etSurname2.setText(mUserData!!.surname2)
            binding.etDate.setText(mUserData!!.birthDate)
            binding.btnUserNickUpdate.visibility=View.VISIBLE

            binding.btnSave.text=getString(R.string.btn_update)

        }
        binding.etDate.setOnClickListener(this)
        binding.btnSave.setOnClickListener(this)
        binding.btnUserNickUpdate.setOnClickListener(this)
        binding.btnSaveUpdate.setOnClickListener(this)

    }


    override fun onClick(v: View?) { //Implementando view.oncliclistener podemos definir en un metodo distintos eventos en clicks
        when (v!!.id) {
            //Mostramos el calendario
            R.id.et_date -> {
                DatePickerDialog(
                    this@UserActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
            //Configuramos las acciones del botón guardar
            R.id.btn_save -> {
                val dbHandler = DatabaseManager(this)
                //Controlamos que los campos no estén vacíos
                when {
                    binding.etNick.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.user_request), Toast.LENGTH_SHORT)
                                .show()
                    }
                    binding.etPassword.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.password_request), Toast.LENGTH_SHORT)
                                .show()
                    }
                    binding.etName.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.name_request), Toast.LENGTH_SHORT).show()
                    }
                    binding.etSurname1.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.surname1_request), Toast.LENGTH_SHORT)
                                .show()
                    }
                    binding.etSurname2.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.surname2_request), Toast.LENGTH_SHORT).show()
                    }
                    mUserData==null && dbHandler.checkUser(binding.etNick.text.toString().trim { it<=' '} ) -> {
                        Toast.makeText(this, getString(R.string.same_user_exists_alert), Toast.LENGTH_LONG).show()
                    }


                    //Si no están vacíos procedemos a guardar el usuario
                    else -> {
                        val dbHandler = DatabaseManager(this)


                        val user = User(
                           if(mUserData==null) 0 else mUserData!!.id,
                            binding.etNick.text.toString(),
                            binding.etPassword.text.toString(),
                            binding.etName.text.toString(),
                            binding.etSurname1.text.toString(),
                            binding.etSurname2.text.toString(),
                            binding.etDate.text.toString()
                        )

                        //Si el objeto está vacío de otra actividad añadimos el usuario
                        if(mUserData==null){

                                val addUser = dbHandler.addUser(user)

                            if (addUser > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@UserActivity,getString(R.string.register_success),Toast.LENGTH_LONG).show()
                                val intent=Intent(this@UserActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()}
                            }else{
                                //Si el objeto User ya viene de otra actividad lo actualizamos
                                val updateUser=dbHandler.updateUser(user)
                                if(updateUser>0){
                                    setResult(Activity.RESULT_OK)
                                    Toast.makeText(this@UserActivity,getString(R.string.user_update_success),Toast.LENGTH_LONG).show()
                                    val intent=Intent(this@UserActivity, MainScreenActivity::class.java)
                                    mUserData=user
                                    intent.putExtra(EXTRA_USER,mUserData)
                                    startActivity(intent)
                                }

                            }

                    }
                }
            }

            R.id.btn_user_nick_update -> {

                //Si pulsamos en actualizar password configuramos el layout para que meustre solo los EditText de contraseñas

                binding.btnSave.visibility = View.GONE
                binding.btnUserNickUpdate.visibility = View.GONE
                binding.etNick.visibility = View.VISIBLE
                binding.etNick.isFocusable = false
                binding.etNick.isFocusableInTouchMode = false
                binding.tilPasswordChange.visibility = View.VISIBLE
                binding.tilPasswordConfirm.visibility = View.VISIBLE
                binding.etPasswordChange.visibility = View.VISIBLE
                binding.etPasswordConfirm.visibility = View.VISIBLE
                binding.tilName.visibility = View.GONE
                binding.tilPassword.visibility=View.GONE
                binding.tilSurname1.visibility = View.GONE
                binding.tilSurname2.visibility = View.GONE
                binding.tilDate.visibility = View.GONE
                binding.etName.visibility = View.GONE
                binding.etPassword.visibility=View.GONE
                binding.etSurname1.visibility = View.GONE
                binding.etSurname2.visibility = View.GONE
                binding.etDate.visibility = View.GONE
                binding.btnSaveUpdate.visibility = View.VISIBLE

            }

            R.id.btn_save_update-> {
                //Cuando ya actualizamos al clicar el botón controlamos que no estén vacíos los campos

                when {
                    binding.etPassword.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.password_request), Toast.LENGTH_SHORT).show()
                    }
                    binding.etPasswordConfirm.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.password_request_confirm), Toast.LENGTH_SHORT).show()

                    }
                    //si no están vacíos actualizamos el valor de la contraseña
                    else -> {
                        if (binding.etPasswordChange.text.toString() == binding.etPasswordConfirm.text.toString()) {
                            val user = User(
                                    mUserData!!.id,
                                    mUserData!!.nick,
                                    binding.etPasswordConfirm.text.toString(),
                                    mUserData!!.name,
                                    mUserData!!.surname1,
                                    mUserData!!.surname2,
                                    mUserData!!.birthDate
                            )

                            val dbHandler = DatabaseManager(this)
                            val updateUser = dbHandler.updateUser(user)
                            if (updateUser > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@UserActivity, getString(R.string.password_updated_success), Toast.LENGTH_LONG).show()
                                val intent = Intent(this@UserActivity, MainScreenActivity::class.java)
                                mUserData=user
                                intent.putExtra(EXTRA_USER, mUserData)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, getString(R.string.wrong_same_password), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            }
        }


    //Método por el que mostramos el formato de la fecha que verá el usuario
    private fun updateDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDate.setText(sdf.format(cal.time).toString())
    }

}