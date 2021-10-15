package com.example.takeyourpill.data

import android.os.Parcel
import android.os.Parcelable

data class Pill ( //Es el modelo que usamos con Pill
    val id:Int,
    val name:String?,
    val image: String?,
    val doses: String?,
    val description:String?,
    val userId:Int
        ):Parcelable { //Utilizamos Parcelable en vez de Serializable por ser más rápido
                       //Esto nos permitira pasar nuestro objeto entre Activities
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(image)
        parcel.writeString(doses)
        parcel.writeString(description)
        parcel.writeInt(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Pill> {
        override fun createFromParcel(parcel: Parcel): Pill {
            return Pill(parcel)
        }

        override fun newArray(size: Int): Array<Pill?> {
            return arrayOfNulls(size)
        }
    }
}