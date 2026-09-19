package com.oxclub.dailynews.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AuthGate() {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (user != null) {
        NewsApp()
    } else {
        AuthScreen(auth)
    }
}

@Composable
private fun AuthScreen(auth: FirebaseAuth) {
    var createAccount by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "DailyNews",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            if (createAccount) "Create your account"
            else "Welcome back"
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                error = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                error = null
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Enter your email and password."
                    return@Button
                }

                loading = true
                error = null

                val task =
                    if (createAccount) {
                        auth.createUserWithEmailAndPassword(
                            email.trim(),
                            password
                        )
                    } else {
                        auth.signInWithEmailAndPassword(
                            email.trim(),
                            password
                        )
                    }

                task.addOnCompleteListener {
                    loading = false

                    if (!it.isSuccessful) {
                        error =
                            it.exception?.localizedMessage
                                ?: "Authentication failed."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (createAccount)
                        "Create account"
                    else
                        "Login"
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                createAccount = !createAccount
                error = null
            }
        ) {
            Text(
                if (createAccount)
                    "Already have an account? Login"
                else
                    "New here? Create an account"
            )
        }

        Spacer(Modifier.height(8.dp))

        Text("or")

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                loading = true
                error = null

                auth.signInAnonymously()
                    .addOnCompleteListener {
                        loading = false

                        if (!it.isSuccessful) {
                            error =
                                it.exception?.localizedMessage
                                    ?: "Anonymous sign-in failed."
                        }
                    }
            },
            enabled = !loading
        ) {
            Text("Continue anonymously")
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))

            Text(
                error!!,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
