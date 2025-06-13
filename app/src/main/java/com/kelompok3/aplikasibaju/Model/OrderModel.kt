package com.kelompok3.aplikasibaju.Model

data class OrderModel(
    val id: String = "",
    val items: List<CartItemModel> = emptyList(),
    val totalAmount: Int = 0,
    val orderTime: Long = System.currentTimeMillis()
)
