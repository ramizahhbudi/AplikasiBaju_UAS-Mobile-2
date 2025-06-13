package com.kelompok3.aplikasibaju.LoginSignup

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import android.util.Log

@Composable
fun LoginScreen(navController: NavHostController, onLoginSuccess: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var forgotPasswordDialogBox by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE5E0D3)) // Pastikan warna ini ada
            .padding(16.dp)
            .verticalScroll(scrollState)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp)) // Biar ada jarak atas

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Login", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            Toast.makeText(context, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = Firebase.auth.currentUser
                                    val userEmail = user?.email ?: ""
                                    Log.d("LoginScreen", "Login successful for ${userEmail}")
                                    Toast.makeText(context, "Login berhasil!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(userEmail)
                                } else {
                                    Log.e("LoginScreen", "Login failed: ${task.exception?.message}")
                                    Toast.makeText(
                                        context,
                                        task.exception?.message ?: "Login gagal. Periksa email/password Anda.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5E0D3))
                ) {
                    Text("LOGIN")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (forgotPasswordDialogBox) {
                    var resetEmail by remember { mutableStateOf("") }

                    AlertDialog(
                        title = { Text("Lupa Password") },
                        text = {
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it },
                                label = { Text("Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (resetEmail.isNotBlank()) {
                                    Firebase.auth.sendPasswordResetEmail(resetEmail)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    context, "Check email to reset password",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                forgotPasswordDialogBox = false
                                            } else {
                                                Toast.makeText(
                                                    context, task.exception?.message ?: "Registered email not found",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e("LoginScreen", "Password reset failed: ${task.exception?.message}")
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter your registered email",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }) { Text("Submit") }
                        },
                        dismissButton = {
                            TextButton(onClick = { forgotPasswordDialogBox = false }) {
                                Text("Cancel")
                            }
                        },
                        onDismissRequest = { forgotPasswordDialogBox = false }
                    )
                }

                TextButton(onClick = { forgotPasswordDialogBox = true }) {
                    Text("Lupa Password?")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                }) {
                    Text("Belum Punya Akun? Signup")
                }
            }
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}
