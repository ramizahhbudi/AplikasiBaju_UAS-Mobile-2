package com.kelompok3.aplikasibaju.Model

import java.io.Serializable

data class ItemsModel(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var picUrl: ArrayList<String> = ArrayList(),
    var model: ArrayList<String> = ArrayList(),
    var price: Int = 0,
    var rating: Double = 0.0,
    var numberInCart: Int = 0,
    var showRecomended: Boolean = true,
    var categoryId: String = ""
) : Serializable
