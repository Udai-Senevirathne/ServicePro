package com.nibm.mynewapp

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Plumber(
    val name: String,
    val specialty: String,
    val imageResId: Int,
    val rating: Float,
    val intro: String,
    val phone: String,
    val email: String
) : Parcelable