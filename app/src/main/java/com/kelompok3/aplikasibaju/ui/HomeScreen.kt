package com.kelompok3.aplikasibaju.ui

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.kelompok3.aplikasibaju.Activity.DetailItemActivity
import com.kelompok3.aplikasibaju.Model.CategoryModel
import com.kelompok3.aplikasibaju.Model.ItemsModel
import com.kelompok3.aplikasibaju.ViewModel.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok3.aplikasibaju.R
import kotlinx.coroutines.launch
import java.util.Locale // Import Locale untuk String.format

@Composable
fun HomeScreen(navController: NavHostController, onLogout: () -> Unit) {
    val viewModel: MainViewModel = viewModel()
    val context = LocalContext.current
    val categories = remember { mutableStateListOf<CategoryModel>() }
    val allItems = remember { mutableStateListOf<ItemsModel>() }
    var showCategoryLoading by remember { mutableStateOf(true) }
    var showAllItemsLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMenu by remember { mutableStateOf("home") }

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedCategoryTitle by remember { mutableStateOf("Semua Produk") }


    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadCategory().observeForever { fetchedCategories ->
            categories.clear()
            categories.addAll(fetchedCategories)
            showCategoryLoading = false
            Log.d("HomeScreen", "Categories loaded: ${fetchedCategories.size}")
        }

        viewModel.loadAllItems().observeForever { fetchedItems ->
            allItems.clear()
            allItems.addAll(fetchedItems)
            showAllItemsLoading = false
            Log.d("HomeScreen", "All items loaded: ${fetchedItems.size}")
        }
    }

    LaunchedEffect(selectedCategoryId, categories) {
        selectedCategoryTitle = categories.find { it.id.toString() == selectedCategoryId }?.title ?: "Semua Produk"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E0D3))
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Welcome My Bro", color = Color.Black)
                                Text(
                                    "Pilih Outfit Kece Kalian😎",
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(onClick = {
                                Firebase.auth.signOut()
                                coroutineScope.launch {
                                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                                }
                                onLogout()
                            }) {
                                Text("Logout")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Icon",
                                    tint = Color.Gray
                                )
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF2F2F2),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }

                item {
                    Text(
                        text = "Official Brand",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    )
                }

                item {
                    if (showCategoryLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        CategoryList(categories) { category ->
                            selectedCategoryId = category?.id?.toString()
                            selectedCategoryTitle = category?.title ?: "Semua Produk"
                        }
                    }
                }

                item {
                    Text(
                        text = "Brand: $selectedCategoryTitle",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    )
                }

                item {
                    if (showAllItemsLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val filteredItems = allItems.filter { item ->
                            val itemCategoryId = item.categoryId ?: ""
                            (selectedCategoryId == null || selectedCategoryId!!.isEmpty() || itemCategoryId == selectedCategoryId) &&
                                    (searchQuery.isEmpty() || item.title.contains(searchQuery, ignoreCase = true))
                        }
                        ListAllItems(filteredItems)
                    }
                }
            }
        }

        BottomMenu(
            selected = selectedMenu,
            onItemClick = { menu ->
                selectedMenu = menu
                when (menu) {
                    "home" -> navController.navigate("home") { popUpTo("home") { inclusive = false } }
                    "cart" -> navController.navigate("cart") { popUpTo("home") { inclusive = false } }
                    "order" -> navController.navigate("order") { popUpTo("home") { inclusive = false } }
                    "profile" -> navController.navigate("profile") { popUpTo("home") { inclusive = false } }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CategoryList(
    categories: SnapshotStateList<CategoryModel>,
    onCategorySelected: (CategoryModel?) -> Unit
) {
    var selectedIndex by remember { mutableStateOf(-1) } // -1 untuk "Semua"

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        item {
            CategoryItem(
                CategoryModel(id = -1, title = "Semua", picUrl = ""), // ID -1 untuk "Semua"
                isSelected = selectedIndex == -1,
                onItemClick = {
                    selectedIndex = -1
                    onCategorySelected(null) // Mengirim null untuk kategori "Semua"
                }
            )
        }

        items(categories.size) { index ->
            val category = categories[index]
            CategoryItem(
                item = category,
                isSelected = selectedIndex == index,
                onItemClick = {
                    selectedIndex = index
                    onCategorySelected(category)
                }
            )
        }
    }
}

@Composable
fun CategoryItem(item: CategoryModel, isSelected: Boolean, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onItemClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (item.picUrl.isNotEmpty()) {
            val painter = rememberAsyncImagePainter(model = item.picUrl)

            Box(
                modifier = Modifier
                    .size(if (isSelected) 60.dp else 50.dp)
                    .background(
                        color = if (isSelected) colorResource(id = R.color.darkBrown) else colorResource(id = R.color.lightBrown),
                        shape = RoundedCornerShape(100.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painter,
                    contentDescription = item.title,
                    modifier = Modifier.size(if (isSelected) 40.dp else 35.dp),
                    contentScale = ContentScale.Inside,
                    colorFilter = if (isSelected) ColorFilter.tint(Color.White) else ColorFilter.tint(Color.Black)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(if (isSelected) 60.dp else 50.dp)
                    .background(
                        color = if (isSelected) colorResource(id = R.color.darkBrown) else colorResource(id = R.color.lightBrown),
                        shape = RoundedCornerShape(100.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (item.id == -1) { // Periksa ID integer
                    Text("All", color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
                } else {
                    Text(item.title, color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = item.title,
            color = colorResource(id = R.color.grey),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BottomMenu(
    selected: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val homeIcon = painterResource(id = R.drawable.btn_1)
    val cartIcon = painterResource(id = R.drawable.btn_2)
    val orderIcon = painterResource(id = R.drawable.btn_4)
    val profileIcon = painterResource(id = R.drawable.btn_5)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.darkBrown))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        BottomMenuItem(homeIcon, "Home", selected == "home") { onItemClick("home") }
        BottomMenuItem(cartIcon, "Cart", selected == "cart") { onItemClick("cart") }
        BottomMenuItem(orderIcon, "Order", selected == "order") { onItemClick("order") }
        BottomMenuItem(profileIcon, "Profile", selected == "profile") { onItemClick("profile") }
    }
}

@Composable
fun BottomMenuItem(icon: Painter, text: String, selected: Boolean, onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onItemClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = text,
            tint = if (selected) Color.Yellow else Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            color = if (selected) Color.Yellow else Color.White,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ListAllItems(items: List<ItemsModel>) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        items.forEach { item ->
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
                    val imageUrl = item.picUrl.firstOrNull()
                    val painter = rememberAsyncImagePainter(model = imageUrl)

                    Image(
                        painter = painter,
                        contentDescription = item.title ?: "Product Image",
                        modifier = Modifier.size(80.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title ?: "Nama Produk Tidak Tersedia", fontWeight = FontWeight.Bold)
                        Text("Rp ${String.format(Locale.getDefault(), "%,d", item.price ?: 0)}", color = Color.Gray) // Format dengan Locale
                        Text("⭐ ${item.rating ?: "N/A"}")
                    }
                }
            }
        }
    }
}
