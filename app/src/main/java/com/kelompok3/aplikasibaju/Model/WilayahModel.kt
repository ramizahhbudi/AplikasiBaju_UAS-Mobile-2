package com.kelompok3.aplikasibaju.Model

data class Province(
    val id: String,
    val nama: String
) {
    override fun toString(): String = nama
}

data class Regency(
    val id: String,
    val nama: String
) {
    override fun toString(): String = nama
}

data class District(
    val id: String,
    val nama: String
) {
    override fun toString(): String = nama
}

data class Village(
    val id: String,
    val nama: String
) {
    override fun toString(): String = nama
}
