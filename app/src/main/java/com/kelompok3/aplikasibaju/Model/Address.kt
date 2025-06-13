package com.kelompok3.aplikasibaju.Model

data class Address(
    val id: Long = System.currentTimeMillis(),
    val name: String = "",
    val phone: String = "",
    val detail: String = "",
    val province: String = "",
    val regency: String = "",
    val district: String = "",
    val village: String = "",
    var isSelected: Boolean = false
)
