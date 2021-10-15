package com.example.takeyourpill.activities

import android.app.*
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.takeyourpill.R
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityAddTimeBinding
import com.example.takeyourpill.utils.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.seconds

class AddTimeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding:ActivityAddTimeBinding
    private var mPillData: Pill? = null
    private var mTimeData: Times? = null
    private var cal = java.util.Calendar.getInstance()
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private var alarmManager: AlarmManager ?=null



    companion object{

        var TIME_EXTRA="TIME_EXTRA"
        var TIME_EXTRA_UPDATED="TIME_EXTRA_UPDATED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_time)

        binding= ActivityAddTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

       alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager


        setSupportActionBar(binding.toolbarAddTime)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarAddTime.setNavigationOnClickListener {
            onBackPressed()
        }
        //Ver comentarios en AddPillActivity ya que es una actividad que funciona similar
        when {

            intent.hasExtra(TimeMainScreenActivity.EXTRA_PILL_DATA) -> {
                mPillData= intent.getParcelableExtra(TimeMainScreenActivity.EXTRA_PILL_DATA)
            }
            intent.hasExtra(TimeMainScreenActivity.EXTRA_TIME_DATA)  ->{
                mTimeData=intent.getParcelableExtra(TimeMainScreenActivity.EXTRA_TIME_DATA)
            }



        }








        if(mTimeData!=null){
            supportActionBar?.title=getString(R.string.tb_edit_time)
            binding.etTime.setText(mTimeData!!.hour.toString())
            binding.btnSave.text=getString(R.string.btn_update)
        }

        timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }
        updateTimeInView()
        initChannel(this)



        binding.btnSave.setOnClickListener(this)
        binding.etTime.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {

            R.id.et_time -> {

                TimePickerDialog(
                    this@AddTimeActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE), true)
                    .show()

            }
            R.id.btn_save -> {

                        val time = Times(
                            if (mTimeData == null) 0 else mTimeData!!.id,
                            binding.etTime.text.toString(),
                            if(mTimeData==null) mPillData!!.id else mTimeData!!.pillId
                        )


                        val dbHandler = DatabaseManager(this)

                        if (mTimeData == null) {
                            val addTime = dbHandler.addTime(time)

                            val i = dbHandler.getTimeList().lastIndex
                            val timeId = dbHandler.getTimeList()[i].id

                            if (addTime > 0) {
                                //Agregamos la alarma para la notificación
                                var hour= binding.etTime.text!!.toString().substringBeforeLast(":")
                                var min= binding.etTime.text!!.toString().substringAfterLast(":")
                                cal.apply {
                                    timeInMillis=System.currentTimeMillis()
                                    set(Calendar.HOUR_OF_DAY,hour.toInt())
                                    set(Calendar.MINUTE,min.toInt())
                                    set(Calendar.SECOND,0)
                                }
                                alarmManager!!.setInexactRepeating(
                                        AlarmManager.RTC_WAKEUP,
                                        cal.timeInMillis,
                                        AlarmManager.INTERVAL_DAY,
                                        PendingIntent.getBroadcast(this,timeId,Intent(this,AlarmReceiver::class.java).apply {
                                            putExtra("ticker", mPillData!!.name)
                                            putExtra("title", mPillData!!.name)
                                            putExtra("text", mPillData!!.doses)

                                        },PendingIntent.FLAG_CANCEL_CURRENT)
                                )

                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddTimeActivity, getString(R.string.succes_time), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AddTimeActivity, TimeMainScreenActivity::class.java)
                                intent.putExtra(TIME_EXTRA, mPillData)
                                startActivity(intent)
                                finish()
                            }

                        } else {


                            val updateTime = dbHandler.updateTime(time)
                            mPillData= dbHandler.getPillById(mTimeData!!.pillId)



                            if (updateTime > 0) {
                                //Actualizamos la alarma
                                alarmManager!!.setInexactRepeating(
                                        AlarmManager.RTC_WAKEUP,
                                        cal.timeInMillis,
                                        AlarmManager.INTERVAL_DAY,
                                        PendingIntent.getBroadcast(this,mTimeData!!.id,Intent(this,AlarmReceiver::class.java).apply {
                                            putExtra("ticker", mPillData!!.name)
                                            putExtra("title", mPillData!!.name)
                                            putExtra("text", mPillData!!.doses)

                                        },PendingIntent.FLAG_CANCEL_CURRENT)
                                )
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddTimeActivity, getString(R.string.succes_update_time), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AddTimeActivity, TimeMainScreenActivity::class.java)
                                intent.putExtra(TIME_EXTRA_UPDATED,mPillData)
                                startActivity(intent)
                                finish()


                            }


                        }





                }


            }

        }



    private fun updateTimeInView() {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etTime.setText(sdf.format(cal.time).toString())
    }

    //Con este método comprobamos la versión del SDK y si es posterior al 26 tenemos que abrir el canal de comunicación con las notificaciones

    private fun initChannel(context:Context){
        if(Build.VERSION.SDK_INT>26){
            val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java) as NotificationManager
            val notificationChannel = NotificationChannel("Alarma", "Alarma creada", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description= "Alarma de medicación"
            notificationManager.createNotificationChannel(notificationChannel)
        }else{
            return
        }
    }



}