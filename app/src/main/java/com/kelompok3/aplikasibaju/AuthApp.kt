package com.kelompok3.aplikasibaju

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.kelompok3.aplikasibaju.LoginSignup.LoginScreen
import com.kelompok3.aplikasibaju.LoginSignup.SignupScreen
import com.kelompok3.aplikasibaju.ui.AddAddressScreen
import com.kelompok3.aplikasibaju.ui.CartScreen
import com.kelompok3.aplikasibaju.ui.HomeScreen
import com.kelompok3.aplikasibaju.ui.OrderScreen
import com.kelompok3.aplikasibaju.ui.ProfileScreen

@Composable
fun AuthApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            val newIsLoggedIn = currentUser != null
            if (newIsLoggedIn != isLoggedIn) {
                isLoggedIn = newIsLoggedIn
                if (isLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    val onLoginSuccess: (String) -> Unit = { email ->
    }

    val onLogout: () -> Unit = {
        FirebaseAuth.getInstance().signOut()
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "home" else "login"
    ) {
        composable(route = "login") {
            LoginScreen(navController, onLoginSuccess)
        }
        composable(route = "signup") {
            SignupScreen(navController)
        }
        composable(route = "home") {
            HomeScreen(navController, onLogout)
        }
        composable(route = "cart") {
            CartScreen(navController = navController)
        }
        composable(route = "order") {
            OrderScreen(navController = navController)
        }
        composable(route = "profile") {
            ProfileScreen(navController = navController)
        }
        composable(route = "add_address") {
            AddAddressScreen(onBack = { navController.popBackStack() })
        }
    }
}