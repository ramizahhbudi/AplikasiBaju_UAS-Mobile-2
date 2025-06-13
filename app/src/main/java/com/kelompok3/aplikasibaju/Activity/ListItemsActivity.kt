package com.kelompok3.aplikasibaju.Activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import com.kelompok3.aplikasibaju.ui.ListItemsScreen
import com.kelompok3.aplikasibaju.ViewModel.MainViewModel // Pastikan MainViewModel ada
import com.kelompok3.aplikasibaju.ui.theme.AplikasiBajuTheme

class ListItemsActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val categoryId = intent.getStringExtra("id") ?: ""
        val categoryTitle = intent.getStringExtra("title") ?: "Daftar Produk"

        setContent {
            AplikasiBajuTheme {
                Surface {
                    ListItemsScreen(viewModel, categoryId, categoryTitle)
                }
            }
        }
    }
}
