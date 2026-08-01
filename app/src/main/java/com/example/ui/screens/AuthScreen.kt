package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R

@Composable
fun AuthScreen(
    onLoginSuccess: (name: String, email: String) -> Unit,
    onGuestLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isSignUpMode by remember { mutableStateOf(false) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    val cyanAccent = Color(0xFF00D2FF)
    val magentaPink = Color(0xFFFF2A85)
    val darkBgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF030712),
            Color(0xFF081226),
            Color(0xFF02050E)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBgGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Main Auth Form Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Brand Logo (User Uploaded DA Duty Accepter Logo)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B1B36))
                        .border(2.5.dp, cyanAccent, CircleShape)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo_1784727011307),
                        contentDescription = "DA Duty Accepter Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title and Subtitle (Matching Screenshot)
                if (!isSignUpMode) {
                    Text(
                        text = "DUTY ACCEPTER",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.5.sp,
                        fontSize = 26.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "S I G N   I N",
                        style = MaterialTheme.typography.titleMedium,
                        color = cyanAccent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 6.sp,
                        fontSize = 15.sp
                    )
                } else {
                    Text(
                        text = "CREATE ACCOUNT",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        fontSize = 24.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "J O I N   T H E   N E T W O R K",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFFA855F7),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Sign Up Name Field
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Full Name", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Full Name",
                                tint = cyanAccent
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanAccent,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF081225),
                            unfocusedContainerColor = Color(0xFF081225),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_name_field")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Email Address Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    placeholder = { Text("Email Address", color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AlternateEmail,
                            contentDescription = "Email",
                            tint = cyanAccent
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cyanAccent,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF081225),
                        unfocusedContainerColor = Color(0xFF081225),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = if (isSignUpMode) "Password (min 6 chars)" else "Password",
                            color = Color(0xFF64748B)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password",
                            tint = cyanAccent
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = cyanAccent
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cyanAccent,
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedContainerColor = Color(0xFF081225),
                        unfocusedContainerColor = Color(0xFF081225),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field")
                )

                // Confirm Password Field (Sign Up Mode)
                if (isSignUpMode) {
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            errorMessage = null
                        },
                        placeholder = { Text("Confirm Password", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Confirm Password",
                                tint = cyanAccent
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle confirm password",
                                    tint = cyanAccent
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanAccent,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF081225),
                            unfocusedContainerColor = Color(0xFF081225),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_field")
                    )
                }

                // Forgot Password Link (Exact match to screenshot - Magenta/Pink, right aligned)
                if (!isSignUpMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = magentaPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { showResetDialog = true }
                                .padding(4.dp)
                                .testTag("forgot_password_link")
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary CTA Action Button
                Button(
                    onClick = {
                        val cleanEmail = emailInput.trim()
                        val cleanPassword = passwordInput.trim()

                        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
                            errorMessage = "Please enter a valid email address"
                            return@Button
                        }
                        if (cleanPassword.isBlank()) {
                            errorMessage = "Please enter your password"
                            return@Button
                        }

                        if (isSignUpMode) {
                            val cleanName = nameInput.trim()
                            if (cleanName.isBlank()) {
                                errorMessage = "Please enter your full name"
                                return@Button
                            }
                            if (cleanPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                                return@Button
                            }
                            if (passwordInput != confirmPasswordInput) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }
                            onLoginSuccess(cleanName, cleanEmail)
                        } else {
                            if (cleanEmail.equals("rp567082@gmail.com", ignoreCase = true) && cleanPassword == "@Ram9663") {
                                onLoginSuccess("Ram", cleanEmail)
                            } else if (cleanEmail.isNotBlank() && cleanPassword.isNotBlank()) {
                                val derivedName = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                                onLoginSuccess(derivedName, cleanEmail)
                            } else {
                                errorMessage = "Invalid credentials. Please enter email: rp567082@gmail.com & password: @Ram9663"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignUpMode) Color(0xFF4C1D95) else cyanAccent,
                        contentColor = if (isSignUpMode) Color.White else Color(0xFF040814)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("auth_main_cta_button")
                ) {
                    Text(
                        text = if (isSignUpMode) "CREATE ACCOUNT" else "SIGN IN",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Switch Sign In / Sign Up toggle link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account? " else "Don't have an account? ",
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (isSignUpMode) "Sign In" else "Sign Up",
                        color = cyanAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable {
                                isSignUpMode = !isSignUpMode
                                errorMessage = null
                            }
                            .padding(4.dp)
                            .testTag("auth_toggle_mode_btn")
                    )
                }
            }

            // Bottom Social Links ("Connect With Us")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 20.dp)
            ) {
                Text(
                    text = "Connect With Us",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Website Globe
                    SocialIconBubble(
                        icon = Icons.Default.Language,
                        contentDescription = "Website",
                        onClick = { Toast.makeText(context, "Opening Website...", Toast.LENGTH_SHORT).show() }
                    )

                    // Telegram Send Icon
                    SocialIconBubble(
                        icon = Icons.Default.Send,
                        contentDescription = "Telegram",
                        onClick = { Toast.makeText(context, "Opening Telegram...", Toast.LENGTH_SHORT).show() }
                    )

                    // WhatsApp Phone Icon
                    SocialIconBubble(
                        icon = Icons.Default.Phone,
                        contentDescription = "WhatsApp",
                        onClick = { Toast.makeText(context, "Opening WhatsApp Support...", Toast.LENGTH_SHORT).show() }
                    )

                    // Instagram Camera Icon
                    SocialIconBubble(
                        icon = Icons.Default.CameraAlt,
                        contentDescription = "Instagram",
                        onClick = { Toast.makeText(context, "Opening Instagram...", Toast.LENGTH_SHORT).show() }
                    )
                }
            }
        }
    }

    // Reset Password Modal Dialog (Exact match to screenshot 2)
    if (showResetDialog) {
        var resetEmail by remember { mutableStateOf(emailInput) }

        Dialog(onDismissRequest = { showResetDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("Email", color = Color(0xFF64748B)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cyanAccent,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF070E1A),
                            unfocusedContainerColor = Color(0xFF070E1A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_password_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Mail Icon",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Also check your Spam/Junk folder in your mail app.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showResetDialog = false }
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (resetEmail.isNotBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Password reset link sent to $resetEmail! Please check inbox and spam folder.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showResetDialog = false
                                } else {
                                    Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "SEND RESET",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialIconBubble(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val cyanAccent = Color(0xFF00D2FF)

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFF0A1528))
            .border(1.dp, Color(0xFF1E293B), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = cyanAccent,
            modifier = Modifier.size(22.dp)
        )
    }
}
