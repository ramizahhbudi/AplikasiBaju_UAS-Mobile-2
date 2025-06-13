package com.kelompok3.aplikasibaju.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kelompok3.aplikasibaju.Model.*
import com.kelompok3.aplikasibaju.ViewModel.AddressViewModel
import com.kelompok3.aplikasibaju.ViewModel.WilayahViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import android.util.Log // Import Log
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun AddAddressScreen(
    onBack: () -> Unit
) {
    val addressVM: AddressViewModel = viewModel()
    val wilayahVM: WilayahViewModel = viewModel()

    val provinces by wilayahVM.provinces.observeAsState(emptyList())
    val regencies by wilayahVM.regencies.observeAsState(emptyList())
    val districts by wilayahVM.districts.observeAsState(emptyList())
    val villages by wilayahVM.villages.observeAsState(emptyList())

    var selectedProv by remember { mutableStateOf<Province?>(null) }
    var selectedKab by remember { mutableStateOf<Regency?>(null) }
    var selectedKec by remember { mutableStateOf<District?>(null) }
    var selectedKel by remember { mutableStateOf<Village?>(null) }

    var alamatDetail by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var nomorTelepon by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        wilayahVM.fetchProvinces()
        Log.d("AddAddressScreen", "Fetching provinces on screen load.")
    }

    // Trigger fetch regencies when province changes
    LaunchedEffect(selectedProv) {
        selectedProv?.id?.let {
            wilayahVM.fetchRegencies(it)
            Log.d("AddAddressScreen", "Fetching regencies for province ID: $it")
        }
    }

    // Trigger fetch districts when regency changes
    LaunchedEffect(selectedKab) {
        selectedKab?.id?.let {
            wilayahVM.fetchDistricts(it)
            Log.d("AddAddressScreen", "Fetching districts for regency ID: $it")
        }
    }

    // Trigger fetch villages when district changes
    LaunchedEffect(selectedKec) {
        selectedKec?.id?.let {
            wilayahVM.fetchVillages(it)
            Log.d("AddAddressScreen", "Fetching villages for district ID: $it")
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues()) // Menambahkan padding status bar
            .padding(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Tambah Alamat", style = MaterialTheme.typography.headlineSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nomorTelepon,
            onValueChange = { nomorTelepon = it },
            label = { Text("Nomor Telepon") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuBox("Provinsi", provinces, selectedProv) {
            selectedProv = it
            // Reset kabupaten, kecamatan, kelurahan saat provinsi berubah
            selectedKab = null
            selectedKec = null
            selectedKel = null
        }

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox("Kabupaten", regencies, selectedKab) {
            selectedKab = it
            // Reset kecamatan, kelurahan saat kabupaten berubah
            selectedKec = null
            selectedKel = null
        }

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox("Kecamatan", districts, selectedKec) {
            selectedKec = it
            // Reset kelurahan saat kecamatan berubah
            selectedKel = null
        }

        Spacer(modifier = Modifier.height(8.dp))

        DropdownMenuBox("Kelurahan", villages, selectedKel) {
            selectedKel = it
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = alamatDetail,
            onValueChange = { alamatDetail = it },
            label = { Text("Detail Alamat (Contoh: Nama Jalan, No. Rumah)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (selectedKel != null && alamatDetail.isNotBlank() && nama.isNotBlank() && nomorTelepon.isNotBlank()) {
                    val address = Address(
                        id = System.currentTimeMillis(), // Pastikan ID unik
                        name = nama,
                        phone = nomorTelepon,
                        detail = alamatDetail,
                        province = selectedProv?.nama.orEmpty(),
                        regency = selectedKab?.nama.orEmpty(),
                        district = selectedKec?.nama.orEmpty(),
                        village = selectedKel?.nama.orEmpty()
                    )
                    addressVM.addAddress(address)
                    Log.d("AddAddressScreen", "Attempting to add address: ${address.name}")
                    onBack()
                } else {
                    Log.w("AddAddressScreen", "Not all required address fields are filled.")
                }
            },
            enabled = selectedKel != null && alamatDetail.isNotBlank() && nama.isNotBlank() && nomorTelepon.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Simpan Alamat")
        }
    }
}

@Composable
fun <T> DropdownMenuBox(
    label: String,
    items: List<T>?,
    selectedItem: T?,
    onItemSelected: (T) -> Unit
) where T : Any {
    val safeItems = items ?: emptyList()
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.medium)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(text = selectedItem?.toString() ?: "Pilih $label", color = Color.DarkGray)
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Dropdown icon",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            safeItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
