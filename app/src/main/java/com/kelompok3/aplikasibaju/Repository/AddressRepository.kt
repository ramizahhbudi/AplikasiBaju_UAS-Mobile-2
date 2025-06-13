package com.kelompok3.aplikasibaju.repository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelompok3.aplikasibaju.Model.Address
import android.util.Log // Import Log

class AddressRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("address")

    fun getAddresses(
        userId: String,
        onResult: (List<Address>) -> Unit
    ) {
        dbRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val addressList = mutableListOf<Address>()
                for (addressSnap in snapshot.children) {
                    val address = addressSnap.getValue(Address::class.java)
                    address?.let { addressList.add(it) }
                }
                onResult(addressList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("AddressRepository", "Failed to load addresses for user $userId: ${error.message}", error.toException())
                onResult(emptyList())
            }
        })
    }
}
