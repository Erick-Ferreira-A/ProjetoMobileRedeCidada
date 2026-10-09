package com.example.projetorevisao.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.theme.*

@Composable
fun CitizenLoginScreen(
    onLoginSuccess: () -> Unit = {}
) {
    var emailOrPhone by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SurfaceDarkHeader, ObsidianBackground)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorderGlow, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brand Logo Badge
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = AccentCyan.copy(alpha = 0.15f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(AccentCyan, AccentPurpleAI)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛡️", fontSize = 36.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Citizen SP",
                    color = TextExpressivePrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Rede Colaborativa de Segurança Urbana",
                    color = TextExpressiveSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = emailOrPhone,
                    onValueChange = { emailOrPhone = it },
                    label = { Text("E-mail ou Celular", color = TextExpressiveSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextExpressivePrimary,
                        unfocusedTextColor = TextExpressivePrimary,
                        focusedContainerColor = SurfaceDarkElevated,
                        unfocusedContainerColor = SurfaceDarkElevated,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorderGlow
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = cpf,
                    onValueChange = { cpf = it },
                    label = { Text("CPF (Validação Anti-Fake)", color = TextExpressiveSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextExpressivePrimary,
                        unfocusedTextColor = TextExpressivePrimary,
                        focusedContainerColor = SurfaceDarkElevated,
                        unfocusedContainerColor = SurfaceDarkElevated,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = SurfaceBorderGlow
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRoseCritical),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "ENTRAR NO CITIZEN SP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextExpressivePrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "🔒 Seus dados são protegidos por criptografia e validados via GOV.BR / SSP-SP",
                    color = TextExpressiveMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCitizenLoginScreen() {
    CitizenSPTheme {
        CitizenLoginScreen()
    }
}
