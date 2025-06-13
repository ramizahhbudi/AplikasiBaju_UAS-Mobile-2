package com.kelompok3.aplikasibaju.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavHostController
import com.kelompok3.aplikasibaju.ViewModel.AddressViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log // Mengganti androidx.media3.common.util.Log
import android.widget.Toast
import androidx.compose.material.icons.filled.Clear
import coil.compose.AsyncImage // Menggunakan coil.compose.AsyncImage
import coil.request.ImageRequest // Menggunakan coil.request.ImageRequest
import coil.transform.CircleCropTransformation // Menggunakan CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kelompok3.aplikasibaju.Model.UserData

@Composable
fun ProfileScreen(navController: NavHostController) {
    val addressViewModel: AddressViewModel = viewModel()
    val addresses by addressViewModel.addressList.observeAsState(emptyList())
    val userData = remember { mutableStateOf<UserData?>(null) }
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }

    var profileImageUri by remember { mutableStateOf<String?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            profileImageUri = it.toString()
        }
    }
    LaunchedEffect(FirebaseAuth.getInstance().currentUser?.uid) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
            dbRef.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(UserData::class.java)
                userData.value = user
                if (user != null) {
                    // Inisialisasi edited fields saat data user dimuat
                    editedUsername = user.username
                    editedEmail = user.email
                    editedPhone = user.no_hp
                }
                Log.d("ProfileScreen", "User data loaded: ${user?.username}")
            }.addOnFailureListener {
                Log.e("ProfileScreen", "Error loading user data", it)
            }
        }
    }
    LaunchedEffect(FirebaseAuth.getInstance().currentUser?.uid) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid
        userId?.let {
            addressViewModel.loadAddresses(it)
            Log.d("ProfileScreen", "Loading addresses for user $it")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF4C3026),
                                    Color(0xFFF5EBDD)
                                )
                            ),
                            shape = RoundedCornerShape(
                                bottomStart = 32.dp,
                                bottomEnd = 32.dp
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileImageUri)
                                    .crossfade(true)
                                    .transformations(CircleCropTransformation())
                                    .build(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = userData.value?.username ?: "Memuat...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Informasi Pribadi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = {
                                if (!isEditing) {
                                    // Inisialisasi edited fields saat memulai edit
                                    editedUsername = userData.value?.username ?: ""
                                    editedEmail = userData.value?.email ?: ""
                                    editedPhone = userData.value?.no_hp ?: ""
                                }
                                isEditing = !isEditing
                            }) {
                                Text(if (isEditing) "Batal" else "Edit")
                            }
                        }

                        // Nama Pengguna
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nama Pengguna",
                                modifier = Modifier.padding(end = 12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nama Pengguna",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editedUsername,
                                        onValueChange = { editedUsername = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        text = userData.value?.username ?: "Memuat...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        // Email
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                modifier = Modifier.padding(end = 12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editedEmail,
                                        onValueChange = { editedEmail = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium,
                                        enabled = false
                                    )
                                } else {
                                    Text(
                                        text = userData.value?.email ?: "Memuat...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Nomor Handphone",
                                modifier = Modifier.padding(end = 12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Nomor Handphone",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isEditing) {
                                    OutlinedTextField(
                                        value = editedPhone,
                                        onValueChange = { editedPhone = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                } else {
                                    Text(
                                        text = userData.value?.no_hp ?: "Memuat...",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        if (isEditing) {
                            Button(
                                onClick = {
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                    if (uid != null) {
                                        val updatedData = mapOf(
                                            "username" to editedUsername,
                                            "email" to editedEmail,
                                            "no_hp" to editedPhone
                                        )
                                        FirebaseDatabase.getInstance().getReference("users")
                                            .child(uid)
                                            .updateChildren(updatedData)
                                            .addOnSuccessListener {
                                                userData.value = userData.value?.copy(
                                                    username = editedUsername,
                                                    email = editedEmail,
                                                    no_hp = editedPhone
                                                )
                                                isEditing = false
                                                Toast.makeText(context, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                                Log.d("ProfileScreen", "User profile updated successfully.")
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
                                                Log.e("ProfileScreen", "Failed to update user profile: ${e.message}", e)
                                            }
                                    } else {
                                        Log.e("ProfileScreen", "User not authenticated, cannot update profile.")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Simpan Perubahan")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Alamat",
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Daftar Alamat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Button(
                                onClick = { navController.navigate("add_address") },
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Tambah",
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(end = 4.dp)
                                )
                                Text(
                                    text = "Tambah",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (addresses.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada alamat tersimpan",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            addresses.forEach { address ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                        },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .weight(1f)
                                        ) {
                                            Text(
                                                text = address.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = address.phone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = address.detail,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = "${address.village}, ${address.district}, ${address.regency}, ${address.province}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                addressViewModel.deleteAddress(address.id)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Hapus Alamat",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        BottomMenu(
            selected = "profile",
            onItemClick = { menu ->
                when (menu) {
                    "home" -> navController.navigate("home")
                    "cart" -> navController.navigate("cart")
                    "order" -> navController.navigate("order")
                    "profile" -> {} // Stay on profile
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
