package com.suborganizer.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suborganizer.android.ui.components.BrandGradient
import com.suborganizer.android.ui.components.GradientButton
import com.suborganizer.android.ui.theme.AmberSoft
import com.suborganizer.android.ui.theme.BorderDark
import com.suborganizer.android.ui.theme.IndigoAccent
import com.suborganizer.android.ui.theme.Muted
import com.suborganizer.android.ui.theme.Rose
import com.suborganizer.android.ui.theme.SubOrganizerTheme

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        BrandMark()
        Spacer(Modifier.height(28.dp))
        Text(
            text = if (state.isSignUpMode) "Create Account" else "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )
        Text(
            text = if (state.isSignUpMode) "Start tracking in seconds — free tier included." else "Log in to your subscriptions dashboard.",
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
        )

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Rose, style = MaterialTheme.typography.bodyMedium)
        }
        state.info?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = AmberSoft, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))
        GradientButton(
            text = if (state.isSignUpMode) "Create Sentinel Account" else "Sign In Securely",
            onClick = { viewModel.submit(email.trim(), password) },
            loading = state.loading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { viewModel.toggleMode() }, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (state.isSignUpMode) "Already have an account? Log In" else "Don't have an account? Sign Up Free",
                color = Color(0xFFA5B4FC),
            )
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(BrandGradient, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(com.suborganizer.android.R.drawable.ic_shield_mark),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(31.dp),
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = IndigoAccent,
    unfocusedBorderColor = BorderDark,
    focusedLabelColor = IndigoAccent,
    unfocusedLabelColor = Muted,
    cursorColor = IndigoAccent,
)

@Preview
@Composable
private fun LoginScreenPreview() {
    SubOrganizerTheme {
        LoginScreen(onLoggedIn = {})
    }
}
