package com.kelompok3.aplikasibaju.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.kelompok3.aplikasibaju.Model.ItemsModel
import com.kelompok3.aplikasibaju.ViewModel.CartViewModel
import com.kelompok3.aplikasibaju.ui.theme.AplikasiBajuTheme
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.background
import java.util.Locale

class DetailItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val item = try {
            intent.getSerializableExtra("item") as? ItemsModel
        } catch (e: Exception) {
            Log.e("DetailItemActivity", "Error retrieving item from intent: ${e.message}", e)
            null
        }

        setContent {
            AplikasiBajuTheme {
                val navController = rememberNavController()
                val cartViewModel: CartViewModel = viewModel()

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (item != null) {
                        DetailItemScreen(
                            item = item,
                            onBack = { finish() },
                            navController = navController,
                            cartViewModel = cartViewModel
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Data produk tidak tersedia. Harap kembali.", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailItemScreen(
    item: ItemsModel,
    onBack: () -> Unit,
    navController: NavHostController,
    cartViewModel: CartViewModel = viewModel()
) {
    // Inisialisasi selectedModel dengan model pertama jika ada, atau string kosong
    var selectedModel by remember { mutableStateOf(item.model.firstOrNull() ?: "") }
    val snackbarHostState = remember { SnackbarHostState() }
    var quantity by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentPrice = item.price
                    Text(
                        text = "Total\nRp ${String.format(Locale.getDefault(), "%,d", currentPrice * quantity)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            // Validasi: pastikan varian dipilih dan kuantitas valid
                            if (selectedModel.isNotEmpty() && quantity > 0) {
                                cartViewModel.addToCart(item, selectedModel)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Produk ditambahkan ke keranjang!")
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Pilih varian dan/atau kuantitas terlebih dahulu!")
                                }
                            }
                        },
                        enabled = quantity > 0 && selectedModel.isNotEmpty(), // Tombol aktif jika kuantitas > 0 dan model dipilih
                        modifier = Modifier
                            .height(48.dp)
                    ) {
                        Text("Tambah ke Keranjang")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Galeri Gambar
            if (item.picUrl.isNotEmpty()) {
                LazyRow(modifier = Modifier.height(200.dp)) {
                    items(item.picUrl) { url ->
                        val painter = rememberAsyncImagePainter(model = url)
                        Image(
                            painter = painter,
                            contentDescription = item.title ?: "Product Image",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tidak ada gambar tersedia", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(item.title ?: "Nama Produk Tidak Tersedia", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rp ${String.format(Locale.getDefault(), "%,d", item.price ?: 0)}", style = MaterialTheme.typography.headlineSmall)
                Text("⭐ ${item.rating ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Jumlah", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                    Text("-")
                }
                Text(quantity.toString(), modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                IconButton(onClick = { quantity++ }) {
                    Text("+")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Deskripsi Produk", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    item.description ?: "Deskripsi tidak tersedia.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (item.model.isNotEmpty()) {
                Text("Varian Tersedia", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    item.model.forEach { modelName ->
                        val isSelected = modelName == selectedModel
                        Button(
                            onClick = { selectedModel = modelName },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(modelName)
                        }
                    }
                }
            }
        }
    }
}
