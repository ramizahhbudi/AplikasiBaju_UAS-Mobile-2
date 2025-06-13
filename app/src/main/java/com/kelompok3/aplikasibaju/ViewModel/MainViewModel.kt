package com.kelompok3.aplikasibaju.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.kelompok3.aplikasibaju.Model.CategoryModel
import com.kelompok3.aplikasibaju.Model.ItemsModel
import com.kelompok3.aplikasibaju.Repository.MainRepository // Pastikan import ini menunjuk ke MainRepository yang sudah diperbarui

class MainViewModel() : ViewModel() {
    private val repository = MainRepository()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }

    fun loadAllItems(): LiveData<MutableList<ItemsModel>> {
        return repository.loadAllItems()
    }

    fun loadFiltered(id:String): LiveData<MutableList<ItemsModel>> {
        return repository.loadFilterd(id)
    }
}
