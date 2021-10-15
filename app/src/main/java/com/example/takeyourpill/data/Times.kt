package com.example.takeyourpill.data

import android.os.Parcel
import android.os.Parcelable

data class Times(//Es el modelo que usamos para el tiempo
    val id:Int,
    val hour: String?,
    val pillId: Int
):Parcelable {//Utilizamos Parcelable en vez de Serializable por ser más rápido
//Esto nos permitira pasar nuestro objeto entre Activities
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(hour)
        parcel.writeInt(pillId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Times> {
        override fun createFromParcel(parcel: Parcel): Times {
            return Times(parcel)
        }

        override fun newArray(size: Int): Array<Times?> {
            return arrayOfNulls(size)
        }
    }
}