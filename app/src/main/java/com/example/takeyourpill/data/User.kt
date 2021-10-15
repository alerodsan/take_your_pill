package com.example.takeyourpill.data

import android.os.Parcel
import android.os.Parcelable

data class User (//Es el modelo que utilizamos para el usuario
        var id:Int,
        val nick:String?,
        val password: String?,
        val name: String?,
        val surname1: String?,
        val surname2: String?,
        val birthDate: String?
        ) : Parcelable {//Utilizamos Parcelable en vez de Serializable por ser más rápido
//Esto nos permitira pasar nuestro objeto entre Activities
        constructor(parcel: Parcel) : this(
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeInt(id)
                parcel.writeString(nick)
                parcel.writeString(password)
                parcel.writeString(name)
                parcel.writeString(surname1)
                parcel.writeString(surname2)
                parcel.writeString(birthDate)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<User> {
                override fun createFromParcel(parcel: Parcel): User {
                        return User(parcel)
                }

                override fun newArray(size: Int): Array<User?> {
                        return arrayOfNulls(size)
                }
        }
}