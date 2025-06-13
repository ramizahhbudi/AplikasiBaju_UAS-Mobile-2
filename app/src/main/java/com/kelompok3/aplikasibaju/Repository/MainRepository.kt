package com.kelompok3.aplikasibaju.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.kelompok3.aplikasibaju.Model.CategoryModel
import com.kelompok3.aplikasibaju.Model.ItemsModel
import android.util.Log // Import Log

class MainRepository {
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()
        val ref = firebaseDatabase.getReference("Category")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryModel>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(CategoryModel::class.java)
                    item?.let { lists.add(it) }
                }
                listData.value = lists
                Log.d("MainRepository", "Categories loaded: ${lists.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Failed to load categories: ${error.message}", error.toException())
                listData.value = mutableListOf()
            }

        })
        return listData
    }

    fun loadAllItems(): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("Items")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        // Pastikan ID item diisi dari kunci Firebase
                        val itemId = childSnapshot.key ?: list.id
                        lists.add(list.copy(id = itemId))
                    }
                }
                listData.value = lists
                Log.d("MainRepository", "All items loaded: ${lists.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Failed to load all items: ${error.message}", error.toException())
                listData.value = mutableListOf()
            }
        })
        return listData
    }

    fun loadFilterd(id:String): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()
        val ref = firebaseDatabase.getReference("Items")
        val query: Query = ref.orderByChild("categoryId").equalTo(id)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list != null) {
                        // Pastikan ID item diisi dari kunci Firebase
                        val itemId = childSnapshot.key ?: list.id
                        lists.add(list.copy(id = itemId))
                    }
                }
                listData.value = lists
                Log.d("MainRepository", "Filtered items loaded for category $id: ${lists.size} items")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainRepository", "Failed to load filtered items by category: ${error.message}", error.toException())
                listData.value = mutableListOf()
            }
        })
        return listData
    }
}
