package com.kelompok3.aplikasibaju.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kelompok3.aplikasibaju.Model.Address
import com.kelompok3.aplikasibaju.repository.AddressRepository

class AddressViewModel : ViewModel() {
    private val _addressList = MutableLiveData<List<Address>>(emptyList())
    val addressList: LiveData<List<Address>> get() = _addressList

    private val repository = AddressRepository()

    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    fun loadAddresses(uid: String) {
        repository.getAddresses(uid) { list ->
            _addressList.postValue(list)
        }
    }

    fun addAddress(address: Address) {
        val current = _addressList.value.orEmpty().toMutableList()
        current.add(address)
        _addressList.value = current

        userId?.let { uid ->
            val addressId = address.id.toString()
            FirebaseDatabase.getInstance().reference
                .child("address")
                .child(uid)
                .child(addressId)
                .setValue(address)
        }
    }

    fun selectAddress(addressId: Long) {
        _addressList.value = _addressList.value?.map {
            it.copy(isSelected = it.id == addressId)
        }

        userId?.let { uid ->
            _addressList.value?.forEach { address ->
                val path = "address/$uid/${address.id}/isSelected"
                FirebaseDatabase.getInstance().reference
                    .child(path)
                    .setValue(address.isSelected)
            }
        }
    }

    fun deleteAddress(addressId: Long) {
        val current = _addressList.value.orEmpty().toMutableList()
        val updated = current.filterNot { it.id == addressId }
        _addressList.value = updated

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().reference
            .child("address")
            .child(uid)
            .child(addressId.toString())
            .removeValue()
    }
}