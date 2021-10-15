package com.example.takeyourpill.activities

import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.takeyourpill.R
import com.example.takeyourpill.adapters.MainScreenAdapter
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.data.User
import com.example.takeyourpill.database.DatabaseManager
import com.example.takeyourpill.databinding.ActivityAddPillBinding
import com.example.takeyourpill.utils.AlarmReceiver
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddPillActivity : AppCompatActivity() , View.OnClickListener {

    private lateinit var binding: ActivityAddPillBinding
    private var saveImageToInternalStorage: Uri? = null
    private var mUserData: User? = null
    private var mPillData: Pill? = null
    private var cal = java.util.Calendar.getInstance()
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener
    private var alarmManager:AlarmManager?=null


    companion object {

        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "TakeYOurPillImages"
        var ADD_PILL_EXTRA= "ADD_PILL_EXTRA"
        var ADD_PILL_EXTRA_UPDATED="ADD_PILL_EXTRA_UPDATED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pill)

        binding = ActivityAddPillBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarAddPill)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarAddPill.setNavigationOnClickListener {
            onBackPressed()
        }

        alarmManager = this.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

        //Comprobamos y extraemos si hay un objeto en el extra de la actividad de la que viene
        when {
            intent.hasExtra(MainScreenActivity.SCREEN_EXTRA) -> {
                mUserData = intent.getParcelableExtra(MainScreenActivity.SCREEN_EXTRA)

            }
            intent.hasExtra(MainScreenActivity.EXTRA_PILL_DETAILS) -> {
                mPillData= intent.getParcelableExtra(MainScreenActivity.EXTRA_PILL_DETAILS)
            }
            intent.hasExtra(MainScreenActivity.EXTRA_PILL_DETAILS)  ->{
                mPillData=intent.getParcelableExtra(MainScreenActivity.EXTRA_PILL_DETAILS)
            }


        }

        //Cambiamos los datos si hay información en el objeto Pill

        if(mPillData!=null){
            supportActionBar?.title="Editar ${mPillData!!.name}"
            binding.etTitle.setText(mPillData!!.name.toString())
            binding.etDescription.setText(mPillData!!.description.toString())
            binding.etDoses.setText(mPillData!!.doses.toString())
            saveImageToInternalStorage= Uri.parse(mPillData!!.image)
            binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)

            binding.etTime.visibility=View.GONE
            binding.btnSave.text=getString(R.string.btn_update)
        }

        timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            cal.set(Calendar.MINUTE, minute)
            updateTimeInView()
        }
        updateTimeInView()



        binding.btnSave.setOnClickListener(this)
        binding.tvAddImage.setOnClickListener(this)
        binding.etTime.setOnClickListener(this)
        initChannel(this)


    }

    //Sobreescribimos onClick pues utilizaremos varios setOnClickListener

    override fun onClick(v: View?) {
        when (v!!.id) {
            //Mostramos un selector para saber si queremos imagen o foto
            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle(getString(R.string.alert_dialog_choose))
                val pictureDialogItems =
                        arrayOf(getString(R.string.alert_dialog_choose_picture), getString(R.string.alert_dialog_choose_camera))
                pictureDialog.setItems(pictureDialogItems) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }.show()
            }
            //Configuramos la hora para que aparezca en su editText
            R.id.et_time -> {

                TimePickerDialog(
                        this@AddPillActivity, timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE), true)
                        .show()

            }

            //Configuramos las acciones dentro del botón guardar
            R.id.btn_save -> {

                //Comprobamos que los campos no estén vacíos
                when {
                    binding.etTitle.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.error_no_name), Toast.LENGTH_SHORT)
                                .show()
                    }
                    binding.etDescription.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.error_no_description), Toast.LENGTH_SHORT)
                                .show()
                    }

                    binding.etDoses.text.isNullOrEmpty() -> {
                        Toast.makeText(this, getString(R.string.error_no_dose), Toast.LENGTH_SHORT)
                                .show()
                    }

                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, getString(R.string.error_no_image), Toast.LENGTH_SHORT).show()
                    }
                    //Procedemos a añadir los datos de la Pill, si el objeto estaba ya. es que es un update y lo controlamos
                    else -> {
                        val pill = Pill(
                                if (mPillData == null) 0 else mPillData!!.id,
                                binding.etTitle.text.toString(),
                                saveImageToInternalStorage.toString(),
                                binding.etDoses.text.toString(),
                                binding.etDescription.text.toString(),
                                if(mUserData==null) mPillData!!.userId else mUserData!!.id
                        )


                        val dbHandler = DatabaseManager(this)
                        //Si el objeto estaba vacío procedemos a crear una nueva entrada en la base de datos
                        if (mPillData == null) {
                            val addPill = dbHandler.addPill(pill)

                            //Cogemos la foreign key indicando que la obtenga de la última entrada añadida al Array de Pills
                            val i = dbHandler.getPillList().lastIndex
                            val pillId = dbHandler.getPillList()[i].id

                            val time = Times(
                                    0,
                                    binding.etTime.text.toString(),
                                    pillId
                            )
                            //Añadimos el tiempo a su tabla en la base de datos

                            val addTime = dbHandler.addTime(time)

                            val j = dbHandler.getTimeList().lastIndex
                            val timeId = dbHandler.getTimeList()[j].id

                            if (addPill > 0 && addTime > 0) {

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
                                        PendingIntent.getBroadcast(this,timeId,Intent(this, AlarmReceiver::class.java).apply {
                                            putExtra("ticker", binding.etTitle.text.toString())
                                            putExtra("title", binding.etTitle.text.toString())
                                            putExtra("text", binding.etDoses.text.toString())

                                        }, PendingIntent.FLAG_CANCEL_CURRENT)
                                )

                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddPillActivity, getString(R.string.success_pill), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AddPillActivity, MainScreenActivity::class.java)
                                intent.putExtra(ADD_PILL_EXTRA, mUserData)
                                startActivity(intent)
                                finish()
                            }

                        } else {

                            //Si hay obkjeto de Pill que venga de la activity anterior entonces actualizamos
                            val updatePill = dbHandler.updatePill(pill)
                            mPillData=pill
                            mUserData= dbHandler.getUserById(mPillData!!.userId)




                            if (updatePill > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddPillActivity, getString(R.string.succes_update_pill), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@AddPillActivity, MainScreenActivity::class.java)
                                intent.putExtra(ADD_PILL_EXTRA_UPDATED, mUserData)
                                startActivity(intent)
                                finish()


                            }


                        }


                    }


                }


            }

        }

    }
    //Método para configuirar la hora como la verá el usuario
    private fun updateTimeInView() {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etTime.setText(sdf.format(cal.time).toString())
    }

    //Sobreescribimos el método onActivityResult para que controle los eventos de añadir imagen o fotografía
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImageBitmap =
                                MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage =
                                saveImageToInternalStorage(selectedImageBitmap)
                        binding.ivPlaceImage.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddPillActivity, getString(R.string.error_add_image), Toast.LENGTH_SHORT).show()
                    }
                }

            } else if (requestCode == CAMERA) {
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap
                saveImageToInternalStorage = saveImageToInternalStorage(thumbnail)
                binding.ivPlaceImage.setImageBitmap(thumbnail)

            }
        }
    }

    //Método con el cual guardamos en un fichero la imagen
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(
                file,
                "${UUID.randomUUID()}.jpg"
        )

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    //Método que utilizando la librería Dexter se encarga de gestionar los permisos de la cámara
    private fun takePhotoFromCamera() {
        Dexter.withContext(this)
                .withPermissions(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.CAMERA
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        if (report!!.areAllPermissionsGranted()) {
                            val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startActivityForResult(galleryIntent, CAMERA)
                        }

                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                            token: PermissionToken?
                    ) {
                        showRationaleDialogForPermissions()
                    }
                }).onSameThread()
                .check()
    }

    //Método que utilizando la librería Dexter se encarga de gestionar los permisos de la galería

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
                .withPermissions(
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                        if (report!!.areAllPermissionsGranted()) {
                            val galleryIntent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(galleryIntent, GALLERY)
                        }

                    }

                    override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                            token: PermissionToken?
                    ) {
                        showRationaleDialogForPermissions()
                    }
                }).onSameThread()
                .check()


    }

    //Método qeu indica no ha dado permisos el usuario y le permite ir a ajustes desde la propia app para concederlos
    private fun showRationaleDialogForPermissions() {
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.request_permission))
                .setPositiveButton(getString(R.string.go_settings))

                { _, _ ->
                    try {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.show()


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

