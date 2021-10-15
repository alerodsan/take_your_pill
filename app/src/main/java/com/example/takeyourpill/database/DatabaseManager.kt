package com.example.takeyourpill.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.takeyourpill.data.Pill
import com.example.takeyourpill.data.Times
import com.example.takeyourpill.data.User


class DatabaseManager(context: Context):
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        //Declaramos el nombre de la base de datos y su version
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "TakeYourPillDatabase"

        //Declaramos los nombre de las tablas
        private const val TABLE_USER = "UserTable"
        private const val TABLE_PILL = "PillTable"
        private const val TABLE_TIME = "TimeTable"

        //Declaramos los valores de las columnas
        //TABLA UserTable
        private const val ID_USER = "_id"
        private const val NICK_USER = "nick"
        private const val PASSWORD_USER = "password"
        private const val NAME_USER = "name"
        private const val SURNAME1_USER = "surname1"
        private const val SURNAME2_USER = "surname2"
        private const val BIRTHDAY_USER = "birthday"

        //TABLA PillTable
        private const val ID_PILL = "_id"
        private const val NAME_PILL = "name"
        private const val IMAGE_PILL = "image"
        private const val DOSES_PILL= "dosis"
        private const val DESCRIPTION_PILL = "description"
        private const val USER_ID_PILL = "fk_user_id"

        //TABLA TimeTable
        private const val ID_TIMETABLE = "_id"
        private const val HOUR_TIMETABLE = "hour"
        private const val PILL_ID_TIMETABLE = "fk_pill_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "("
                + ID_USER + " INTEGER PRIMARY KEY,"
                + NICK_USER + " TEXT,"
                + PASSWORD_USER + " TEXT,"
                + NAME_USER + " TEXT,"
                + SURNAME1_USER + " TEXT,"
                + SURNAME2_USER + " TEXT,"
                + BIRTHDAY_USER + " TEXT)")
        db?.execSQL(CREATE_USER_TABLE)
        val CREATE_PILL_TABLE = ("CREATE TABLE " + TABLE_PILL + "("
                + ID_PILL + " INTEGER PRIMARY KEY,"
                + NAME_PILL + " TEXT,"
                + IMAGE_PILL + " TEXT,"
                + DOSES_PILL + " TEXT,"
                + DESCRIPTION_PILL + " TEXT,"
                + USER_ID_PILL + " INTEGER," +
                "FOREIGN KEY (" + USER_ID_PILL + ") REFERENCES " +
                TABLE_USER + "(" + ID_USER + ")" + ")")
        db?.execSQL(CREATE_PILL_TABLE)
        val CREATE_TIME_TABLE = ("CREATE TABLE " + TABLE_TIME + "("
                + ID_TIMETABLE + " INTEGER PRIMARY KEY,"
                + HOUR_TIMETABLE + " TEXT,"
                + PILL_ID_TIMETABLE + " INTEGER," +
                "FOREIGN KEY (" + PILL_ID_TIMETABLE + ") REFERENCES " +
                TABLE_PILL + "(" + ID_PILL + ")" + ")")
        db?.execSQL(CREATE_TIME_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PILL")
        onCreate(db)
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_TIME")
        onCreate(db)
    }

    //Métodos para añadir los datos

    fun addUser(user: User): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NICK_USER, user.nick)
        contentValues.put(PASSWORD_USER, user.password)
        contentValues.put(NAME_USER, user.name)
        contentValues.put(SURNAME1_USER, user.surname1)
        contentValues.put(SURNAME2_USER, user.surname2)
        contentValues.put(BIRTHDAY_USER, user.birthDate)

        val result = db.insert(TABLE_USER, null, contentValues)

        db.close()
        return result
    }

    fun addPill(pill: Pill): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NAME_PILL, pill.name)
        contentValues.put(IMAGE_PILL, pill.image)
        contentValues.put(DOSES_PILL,pill.doses)
        contentValues.put(DESCRIPTION_PILL, pill.description)
        contentValues.put(USER_ID_PILL,pill.userId)

        val result = db.insert(TABLE_PILL, null, contentValues)

        db.close()
        return result
    }

    fun addTime(time: Times): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(HOUR_TIMETABLE, time.hour)
        contentValues.put(PILL_ID_TIMETABLE,time.pillId)

        val result = db.insert(TABLE_TIME, null, contentValues)

        db.close()
        return result
    }


    //Métodos pàra obtener la lista de datos a través del ID
    fun getPillListbyId(idUserPill:Int): ArrayList<Pill>{

        val pillList: ArrayList<Pill> = ArrayList()
        val columns = arrayOf(ID_PILL, NAME_PILL, IMAGE_PILL, DOSES_PILL, DESCRIPTION_PILL, USER_ID_PILL)
        val db = this.readableDatabase
        val selection = "$USER_ID_PILL = ?"
        val selectQuery = "SELECT  * FROM $TABLE_PILL"
        val selectionArgs = arrayOf(idUserPill.toString())

        try{
        val cursor = db.query(
                TABLE_PILL, columns, selection, selectionArgs,
                null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val pill = Pill(
                            cursor.getInt(cursor.getColumnIndex(ID_PILL)),
                            cursor.getString(cursor.getColumnIndex(NAME_PILL)),
                            cursor.getString(cursor.getColumnIndex(IMAGE_PILL)),
                            cursor.getString(cursor.getColumnIndex(DOSES_PILL)),
                            cursor.getString(cursor.getColumnIndex(DESCRIPTION_PILL)),
                            cursor.getInt(cursor.getColumnIndex(USER_ID_PILL))
                    )
                    pillList.add(pill)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return pillList

    }

    fun getTimeListbyId(idPillTime:Int): ArrayList<Times>{

        val timeList: ArrayList<Times> = ArrayList()
        val columns = arrayOf(ID_TIMETABLE, HOUR_TIMETABLE, PILL_ID_TIMETABLE)
        val db = this.readableDatabase
        val selection = "$PILL_ID_TIMETABLE = ?"
        val selectQuery = "SELECT  * FROM $TABLE_TIME"
        val selectionArgs = arrayOf(idPillTime.toString())

        try{
            val cursor = db.query(
                    TABLE_TIME, columns, selection, selectionArgs,
                    null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val time = Times(
                            cursor.getInt(cursor.getColumnIndex(ID_TIMETABLE)),
                            cursor.getString(cursor.getColumnIndex(HOUR_TIMETABLE)),
                            cursor.getInt(cursor.getColumnIndex(PILL_ID_TIMETABLE))
                    )
                    timeList.add(time)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return timeList

    }

    //Método para obtenert toda la lista de Pills

    fun getPillList(): ArrayList<Pill> {

        val pillList: ArrayList<Pill> = ArrayList()

        val selectQuery = "SELECT  * FROM $TABLE_PILL"

        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val pill = Pill(
                        cursor.getInt(cursor.getColumnIndex(ID_PILL)),
                        cursor.getString(cursor.getColumnIndex(NAME_PILL)),
                        cursor.getString(cursor.getColumnIndex(IMAGE_PILL)),
                            cursor.getString(cursor.getColumnIndex(DOSES_PILL)),
                        cursor.getString(cursor.getColumnIndex(DESCRIPTION_PILL)),
                        cursor.getInt(cursor.getColumnIndex(USER_ID_PILL))
                    )
                    pillList.add(pill)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return pillList
    }

    fun getTimeList(): ArrayList<Times> {

        val timeList: ArrayList<Times> = ArrayList()

        val selectQuery = "SELECT  * FROM $TABLE_TIME"

        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val time = Times(
                            cursor.getInt(cursor.getColumnIndex(ID_TIMETABLE)),
                            cursor.getString(cursor.getColumnIndex(HOUR_TIMETABLE)),
                            cursor.getInt(cursor.getColumnIndex(PILL_ID_TIMETABLE))
                    )
                    timeList.add(time)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return timeList
    }

    //Métodos para actualizar los datos
    fun updateUser(user: User): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NICK_USER, user.nick)
        contentValues.put(PASSWORD_USER, user.password)
        contentValues.put(NAME_USER, user.name)
        contentValues.put(SURNAME1_USER, user.surname1)
        contentValues.put(SURNAME2_USER, user.surname2)
        contentValues.put(BIRTHDAY_USER, user.birthDate)

        val result = db.update(TABLE_USER, contentValues, ID_USER + "=" + user.id, null)

        db.close()
        return result
    }

    fun updatePill(pill: Pill): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(NAME_PILL, pill.name)
        contentValues.put(IMAGE_PILL, pill.image)
        contentValues.put(DOSES_PILL, pill.doses)
        contentValues.put(DESCRIPTION_PILL, pill.description)

        val result = db.update(TABLE_PILL, contentValues, ID_PILL + "=" + pill.id, null)

        db.close()
        return result
    }

    fun updateTime(time: Times): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(HOUR_TIMETABLE, time.hour)

        val result = db.update(TABLE_TIME, contentValues, ID_TIMETABLE + "=" + time.id, null)

        db.close()
        return result
    }

    //Métodos para eliminar los datos

    fun deletePill(pill: Pill): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_PILL, ID_PILL + "=" + pill.id, null)
        db.close()
        return success
    }

    fun deleteTime(time: Times): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_TIME, ID_TIMETABLE + "=" + time.id, null)
        db.close()
        return success
    }


    //Método para comprobar si existe el usuario por su nick

    fun checkUser(nick: String): Boolean {
        val columns = arrayOf(ID_USER)
        val db = this.readableDatabase
        val selection = "$NICK_USER = ?"
        val selectionArgs = arrayOf(nick)
        val cursor = db.query(
            TABLE_USER,
            columns,
            selection,
            selectionArgs,
            null,
            null,
            null
        )
        val cursorCount = cursor.count
        cursor.close()
        db.close()
        if (cursorCount > 0) {
            return true
        }
        return false
    }


    //Método para comprobar si existe el usuario por su nick o contraseña (El login)

    fun login(nick: String, password: String): Boolean {

        val columns = arrayOf(ID_USER)
        val db = this.readableDatabase
        val selection = "$NICK_USER = ? AND $PASSWORD_USER = ?"
        val selectionArgs = arrayOf(nick, password)

        val cursor = db.query(
            TABLE_USER, columns, selection, selectionArgs,
            null, null, null)

        val cursorCount = cursor.count
        cursor.close()
        db.close()

        if (cursorCount > 0) return true
        return false

    }

    //Mëtodos para obtener los objetos a través de un valor

    fun getUserByNick(nick:String):User{

        var i=0
        var user:User?=null
        val columns = arrayOf(ID_USER, NICK_USER, PASSWORD_USER, NAME_USER, SURNAME1_USER, SURNAME2_USER, BIRTHDAY_USER)
        val db = this.readableDatabase
        val selection = "$NICK_USER = ?"
        val selectionArgs = arrayOf(nick)

        val cursor = db.query(
                TABLE_USER, columns, selection, selectionArgs,
                null, null, null)
        if(cursor.moveToFirst()) {
            do {
            user = User(

                        cursor.getInt(cursor.getColumnIndex(ID_USER)),
                        cursor.getString(cursor.getColumnIndex(NICK_USER)),
                        cursor.getString(cursor.getColumnIndex(PASSWORD_USER)),
                        cursor.getString(cursor.getColumnIndex(NAME_USER)),
                        cursor.getString(cursor.getColumnIndex(SURNAME1_USER)),
                        cursor.getString(cursor.getColumnIndex(SURNAME2_USER)),
                        cursor.getString(cursor.getColumnIndex(BIRTHDAY_USER))
                        )
                    }while(cursor.moveToNext())

            i++
        }


        cursor.close()
        db.close()
        if(i>0) return user!!
        return user!!
    }

    fun getUserById(id: Int):User{

        var i=0
        var user:User?=null
        val columns = arrayOf(ID_USER, NICK_USER, PASSWORD_USER, NAME_USER, SURNAME1_USER, SURNAME2_USER, BIRTHDAY_USER)
        val db = this.readableDatabase
        val selection = "$ID_USER = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
                TABLE_USER, columns, selection, selectionArgs,
                null, null, null)
        if(cursor.moveToFirst()) {
            do {
                user = User(

                        cursor.getInt(cursor.getColumnIndex(ID_USER)),
                        cursor.getString(cursor.getColumnIndex(NICK_USER)),
                        cursor.getString(cursor.getColumnIndex(PASSWORD_USER)),
                        cursor.getString(cursor.getColumnIndex(NAME_USER)),
                        cursor.getString(cursor.getColumnIndex(SURNAME1_USER)),
                        cursor.getString(cursor.getColumnIndex(SURNAME2_USER)),
                        cursor.getString(cursor.getColumnIndex(BIRTHDAY_USER))
                )
            }while(cursor.moveToNext())

            i++
        }


        cursor.close()
        db.close()
        if(i>0) return user!!
        return user!!
    }

    fun getPillById(id: Int):Pill{

        var i=0
        var pill:Pill?=null
        val columns = arrayOf(ID_PILL, NAME_PILL, IMAGE_PILL, DOSES_PILL, DESCRIPTION_PILL, USER_ID_PILL)
        val db = this.readableDatabase
        val selection = "$ID_PILL = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
            TABLE_PILL, columns, selection, selectionArgs,
            null, null, null)
        if(cursor.moveToFirst()) {
            do {
                pill = Pill(

                    cursor.getInt(cursor.getColumnIndex(ID_PILL)),
                    cursor.getString(cursor.getColumnIndex(NAME_PILL)),
                    cursor.getString(cursor.getColumnIndex(IMAGE_PILL)),
                    cursor.getString(cursor.getColumnIndex(DOSES_PILL)),
                    cursor.getString(cursor.getColumnIndex(DESCRIPTION_PILL)),
                    cursor.getInt(cursor.getColumnIndex(USER_ID_PILL))
                )
            }while(cursor.moveToNext())

            i++
        }


        cursor.close()
        db.close()
        if(i>0) return pill!!
        return pill!!
    }


}





