package com.kelompok3.aplikasibaju.ui

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.kelompok3.aplikasibaju.Activity.DetailItemActivity
import com.kelompok3.aplikasibaju.Model.ItemsModel
import com.kelompok3.aplikasibaju.ViewModel.MainViewModel
import java.util.Locale // Import Locale untuk String.format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListItemsScreen(viewModel: MainViewModel, categoryId: String, categoryTitle: String) {
    val itemsState by viewModel.loadFiltered(categoryId).observeAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = categoryTitle) },
                navigationIcon = {
                    val context = LocalContext.current
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (itemsState.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak ada produk ditemukan", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(itemsState) { item ->
                    ProductCard(item)
                }
            }
        }
    }
}

@Composable
fun ProductCard(item: ItemsModel) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                val intent = Intent(context, DetailItemActivity::class.java).apply {
                    putExtra("item", item)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = item.picUrl.firstOrNull() ?: "",
                contentDescription = item.title ?: "Product Image",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title ?: "Nama Produk Tidak Tersedia", fontWeight = FontWeight.Bold)
                Text("Rp ${String.format(Locale.getDefault(), "%,d", item.price ?: 0)}", color = Color.Gray)
                Text("⭐ ${item.rating ?: "N/A"}")
            }
        }
    }
}
