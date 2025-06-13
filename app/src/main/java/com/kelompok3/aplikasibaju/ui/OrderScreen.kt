package com.kelompok3.aplikasibaju.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok3.aplikasibaju.ViewModel.CartViewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderScreen(
    navController: NavHostController,
    cartViewModel: CartViewModel = viewModel()
) {
    val orders by cartViewModel.orders.observeAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        cartViewModel.refreshOrders()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 56.dp)
        ) {
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada pesanan yang berhasil.",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = "Riwayat Pesanan Anda",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) // Menggunakan HorizontalDivider
                    }
                    items(orders) { order ->
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("ID Pesanan: ${order.id}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Item:", style = MaterialTheme.typography.bodyLarge)
                            order.items.forEach { cartItem ->
                                Text("  - ${cartItem.item.title} (${cartItem.selectedModel}) x${cartItem.quantity}",
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Total Pembayaran: Rp ${String.format(Locale.getDefault(), "%,d", order.totalAmount)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Waktu Pesanan: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.orderTime))}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

         BottomMenu(
             selected = "order",
             onItemClick = { menu ->
                 when (menu) {
                     "home" -> navController.navigate("home")
                     "cart" -> navController.navigate("cart")
                     "order" -> {} // tetap di sini
                     "profile" -> navController.navigate("profile")
                 }
             },
             modifier = Modifier.align(Alignment.BottomCenter)
         )
    }
}
