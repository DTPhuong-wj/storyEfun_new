package com.example.storyefun.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.storyefun.R
import com.example.storyefun.data.repository.AuthRepository
import com.example.storyefun.viewModel.RegisterViewModel
import com.example.storyefun.viewModel.RegisterUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RegisterScreen(navController: NavController) {
    // Khởi tạo thủ công AuthRepository và RegisterViewModel
    val authRepository = AuthRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
    val viewModel: RegisterViewModel = viewModel(factory = RegisterViewModelFactory(authRepository))
    val uiState by viewModel.uiState.collectAsState()

    // Xử lý điều hướng
    LaunchedEffect(uiState.navigateToHome, uiState.navigateToLogin) {
        if (uiState.navigateToHome) {
            navController.navigate("home") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            viewModel.resetNavigation()
        } else if (uiState.navigateToLogin) {
            navController.navigate("login")
            viewModel.resetNavigation()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.screen),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Hello,",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 60.dp, start = 20.dp)
        )
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 55.sp,
            modifier = Modifier.padding(top = 100.dp, start = 20.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.Start)
                )
            }
            OutlinedTextField(
                value = uiState.username,
                onValueChange = { viewModel.updateUsername(it) },
                label = { Text("Username") },
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.updateEmail(it) },
                label = { Text("Gmail") },
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.updatePassword(it) },
                label = { Text("Password") },
                shape = RoundedCornerShape(20.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                label = { Text("Confirm Password") },
                shape = RoundedCornerShape(20.dp),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = { viewModel.register() },
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Register", color = Color.White)
                }
            }
            TextButton(
                onClick = { viewModel.navigateToLogin() },
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text("Already have an account? Log in here!", color = Color(0xFF1E90FF))
            }
        }
    }
}

// ViewModel Factory để khởi tạo thủ công


class RegisterViewModelFactory(
    private val authRepository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}