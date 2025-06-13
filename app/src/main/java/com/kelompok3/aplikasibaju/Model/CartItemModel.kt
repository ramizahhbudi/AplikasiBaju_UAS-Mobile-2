package com.kelompok3.aplikasibaju.Model

data class CartItemModel(
    val item: ItemsModel = ItemsModel(),
    var selectedModel: String = "",
    var quantity: Int = 1,
    val firebaseId: String = ""
) {
    constructor() : this(ItemsModel(), "", 1, "")
}