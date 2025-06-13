package com.kelompok3.aplikasibaju.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kelompok3.aplikasibaju.Model.CartItemModel
import com.kelompok3.aplikasibaju.Model.ItemsModel
import com.kelompok3.aplikasibaju.Model.OrderModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class CartViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems = MutableStateFlow<List<CartItemModel>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _orders = MutableLiveData<List<OrderModel>>()
    val orders: LiveData<List<OrderModel>> get() = _orders

    private var cartListener: ValueEventListener? = null
    private var ordersListener: ValueEventListener? = null

    init {
        setupCartListener()
        setupOrdersListener()
    }

    override fun onCleared() {
        super.onCleared()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            cartListener?.let {
                firebaseDatabase.getReference("users").child(userId).child("cart").removeEventListener(it)
            }
            ordersListener?.let {
                firebaseDatabase.getReference("users").child(userId).child("orders").removeEventListener(it)
            }
        }
    }

    private fun setupCartListener() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "User not authenticated for cart listener. Cart will be empty.")
            _cartItems.value = emptyList()
            return
        }

        val userCartRef = firebaseDatabase.getReference("users").child(userId).child("cart")

        cartListener?.let { userCartRef.removeEventListener(it) }

        cartListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewModelScope.launch {
                    val tempCartItemsList = mutableListOf<CartItemModel>()
                    val childrenCount = snapshot.childrenCount.toInt()
                    var fetchedCount = 0

                    if (childrenCount == 0) {
                        _cartItems.value = emptyList()
                        Log.d("CartViewModel", "Cart is empty for user $userId.")
                        return@launch
                    }

                    for (childSnapshot in snapshot.children) {
                        val cartItem = childSnapshot.getValue(CartItemModel::class.java)
                        if (cartItem == null) {
                            Log.w("CartViewModel", "Skipping null CartItem from Firebase snapshot for key: ${childSnapshot.key}")
                            fetchedCount++
                            if (fetchedCount == childrenCount) {
                                _cartItems.value = tempCartItemsList.toList()
                            }
                            continue
                        }

                        val firebaseId = childSnapshot.key ?: ""
                        // Pastikan item memiliki ID item yang valid
                        if (cartItem.item.id.isNullOrEmpty()) {
                            Log.w("CartViewModel", "CartItem has null or empty item ID for key: ${childSnapshot.key}. Skipping.")
                            fetchedCount++
                            if (fetchedCount == childrenCount) {
                                _cartItems.value = tempCartItemsList.toList()
                            }
                            continue
                        }

                        launch {
                            try {
                                val itemSnapshot = firebaseDatabase.getReference("Items").child(cartItem.item.id).get().await()
                                val item = itemSnapshot.getValue(ItemsModel::class.java)
                                if (item != null) {
                                    val updatedCartItem = cartItem.copy(item = item, firebaseId = firebaseId)
                                    synchronized(tempCartItemsList) {
                                        tempCartItemsList.add(updatedCartItem)
                                    }
                                } else {
                                    Log.w("CartViewModel", "Item details were null for ID: ${cartItem.item.id}. Check 'Items' node in Firebase or data consistency.")
                                }
                            } catch (e: Exception) {
                                Log.e("CartViewModel", "Error fetching item details for cart item ${cartItem.item.id}: ${e.message}", e)
                            } finally {
                                fetchedCount++
                                if (fetchedCount == childrenCount) {
                                    _cartItems.value = tempCartItemsList.toList().sortedBy { it.item.title }
                                    Log.d("CartViewModel", "Cart items updated. Total: ${tempCartItemsList.size}")
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartViewModel", "Failed to load cart items: ${error.message}", error.toException())
                _cartItems.value = emptyList()
            }
        }
        userCartRef.addValueEventListener(cartListener!!)
    }

    private fun setupOrdersListener() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "User not authenticated for orders listener. Orders will be empty.")
            _orders.value = emptyList()
            return
        }

        val userOrdersRef = firebaseDatabase.getReference("users").child(userId).child("orders")

        ordersListener?.let { userOrdersRef.removeEventListener(it) }

        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = mutableListOf<OrderModel>()
                for (childSnapshot in snapshot.children) {
                    val order = childSnapshot.getValue(OrderModel::class.java)
                    // Pastikan ID order diisi dari kunci Firebase
                    order?.let {
                        val orderIdFromFirebase = childSnapshot.key ?: ""
                        ordersList.add(it.copy(id = orderIdFromFirebase))
                    }
                }
                _orders.value = ordersList.sortedByDescending { it.orderTime }
                Log.d("CartViewModel", "Orders updated. Total: ${ordersList.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CartViewModel", "Failed to load orders: ${error.message}", error.toException())
                _orders.value = emptyList()
            }
        }
        userOrdersRef.addValueEventListener(ordersListener!!)
    }

    fun refreshOrders() {
        setupOrdersListener()
    }

    fun updateItemQuantity(cartItem: CartItemModel, newQuantity: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "Cannot update quantity: User not authenticated.")
            return
        }

        if (newQuantity <= 0) {
            removeItem(cartItem)
            return
        }

        val cartRef = firebaseDatabase.getReference("users/$userId/cart")
        cartRef.child(cartItem.firebaseId).child("quantity").setValue(newQuantity)
            .addOnSuccessListener {
                Log.d("CartViewModel", "Item quantity updated successfully for ${cartItem.item.title} to $newQuantity")
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Failed to update item quantity for ${cartItem.item.title}", exception)
            }
    }

    fun removeItem(cartItem: CartItemModel) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "Cannot remove item: User not authenticated.")
            return
        }

        val cartRef = firebaseDatabase.getReference("users/$userId/cart")
        cartRef.child(cartItem.firebaseId).removeValue()
            .addOnSuccessListener {
                Log.d("CartViewModel", "Item removed successfully: ${cartItem.item.title}")
                // Listener Firebase akan otomatis memperbarui _cartItems
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Failed to remove item ${cartItem.item.title}", exception)
            }
    }

    fun getTotalAmount(): Int {
        return _cartItems.value.sumOf { it.item.price * it.quantity }
    }

    // Fungsi Checkout
    fun checkout() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "User not authenticated for checkout. Checkout aborted.")
            return
        }

        val currentCartItems = _cartItems.value
        if (currentCartItems.isEmpty()) {
            Log.d("CartViewModel", "Cart is empty, cannot checkout.")
            return
        }

        val order = OrderModel(
            id = UUID.randomUUID().toString(),
            items = currentCartItems,
            totalAmount = getTotalAmount(),
            orderTime = System.currentTimeMillis()
        )

        val ordersRef = firebaseDatabase.getReference("users/$userId/orders")
        // Gunakan push() untuk membuat ID unik di Firebase
        val newOrderRef = ordersRef.push()
        val orderId = newOrderRef.key ?: UUID.randomUUID().toString() // Fallback ID
        val orderWithFirebaseId = order.copy(id = orderId)

        newOrderRef.setValue(orderWithFirebaseId)
            .addOnSuccessListener {
                Log.d("CartViewModel", "Order created successfully with ID: ${orderWithFirebaseId.id}")
                clearCart()
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Failed to create order: ${exception.message}", exception)
            }
    }

    private fun clearCart() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "User not authenticated, cannot clear cart.")
            return
        }
        val cartRef = firebaseDatabase.getReference("users/$userId/cart")

        cartRef.removeValue()
            .addOnSuccessListener {
                Log.d("CartViewModel", "Cart cleared successfully for user $userId.")
            }
            .addOnFailureListener { exception ->
                Log.e("CartViewModel", "Failed to clear cart for user $userId: ${exception.message}", exception)
            }
    }

    fun addToCart(item: ItemsModel, selectedModel: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("CartViewModel", "Cannot add to cart: User not authenticated.")
            return
        }
        if (selectedModel.isEmpty()) {
            Log.e("CartViewModel", "Cannot add to cart: Model variant not selected.")
            return
        }

        val cartRef = firebaseDatabase.getReference("users/$userId/cart")

        cartRef.orderByChild("item/id").equalTo(item.id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var itemFoundAndUpdated = false
                    for (childSnapshot in snapshot.children) {
                        val existingCartItem = childSnapshot.getValue(CartItemModel::class.java)
                        // Periksa tidak hanya item ID tetapi juga selectedModel
                        if (existingCartItem != null && existingCartItem.selectedModel == selectedModel) {
                            // Item dengan ID dan model yang sama sudah ada, perbarui kuantitasnya
                            val newQuantity = existingCartItem.quantity + 1
                            cartRef.child(childSnapshot.key!!).child("quantity").setValue(newQuantity)
                                .addOnSuccessListener {
                                    Log.d("CartViewModel", "Quantity updated for existing item: ${item.title} (${selectedModel}) to $newQuantity")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("CartViewModel", "Failed to update quantity for existing item ${item.title} (${selectedModel})", exception)
                                }
                            itemFoundAndUpdated = true
                            break
                        }
                    }

                    if (!itemFoundAndUpdated) {
                        val newCartItemRef = cartRef.push() // Gunakan push() untuk ID unik
                        val firebaseId = newCartItemRef.key

                        if (firebaseId == null) {
                            Log.e("CartViewModel", "Failed to generate Firebase ID for new cart item.")
                            return
                        }

                        val cartItem = CartItemModel(
                            item = item,
                            selectedModel = selectedModel,
                            quantity = 1,
                            firebaseId = firebaseId
                        )

                        newCartItemRef.setValue(cartItem)
                            .addOnSuccessListener {
                                Log.d("CartViewModel", "New item added to cart: ${item.title} (${selectedModel})")
                            }
                            .addOnFailureListener { exception ->
                                Log.e("CartViewModel", "Failed to add new item to cart ${item.title} (${selectedModel})", exception)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartViewModel", "Failed to check existing items in cart: ${error.message}", error.toException())
                }
            })
    }
}
