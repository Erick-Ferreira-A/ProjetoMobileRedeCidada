package com.example.projetorevisao.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projetorevisao.ui.components.CitizenIcons
import com.example.projetorevisao.ui.components.RedeCidadaLogo
import com.example.projetorevisao.ui.theme.BrandBlue
import com.example.projetorevisao.ui.theme.BrandBlueHero
import com.example.projetorevisao.ui.theme.BrandNavyDark
import com.example.projetorevisao.ui.theme.BrandOrange
import com.example.projetorevisao.ui.theme.CitizenSPTheme
import com.example.projetorevisao.ui.theme.CitizenStatusColors

@Composable
fun CitizenOnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }

    if (step == 0) {
        OnboardingWelcomeStep(
            onNext = { step = 1 }
        )
    } else {
        OnboardingLocationStep(
            onBack = { step = 0 },
            onAllowLocation = onFinishOnboarding,
            onSkip = onFinishOnboarding
        )
    }
}

@Composable
private fun OnboardingWelcomeStep(
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandNavyDark,
                        BrandBlue,
                        BrandNavyDark
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Hero Identity from Figma 192:440
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                RedeCidadaLogo(
                    size = 92.dp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            Text(
                text = "Rede Cidadã",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sua rede de segurança comunitária integrada às APIs de São Paulo",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3 Pillars from Figma 192:440
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OnboardingPillarCard(
                icon = CitizenIcons.Notifications,
                iconTint = BrandOrange,
                title = "Alertas em Tempo Real",
                description = "Receba notificações instantâneas de incidentes próximos, clima severo (CGE/INMET) e Metrô SP."
            )
            OnboardingPillarCard(
                icon = CitizenIcons.Groups,
                iconTint = Color(0xFF38BDF8),
                title = "Comunidade Ativa e Anônima",
                description = "Compartilhe relatos com anonimato garantido por padrão e ajude a proteger seus vizinhos."
            )
            OnboardingPillarCard(
                icon = CitizenIcons.PoliceShield,
                iconTint = Color(0xFF34D399),
                title = "Transparência Policial & IA",
                description = "Acompanhe o status do chamado (Reportado ➔ No Local ➔ Controlado) e rotas seguras com Google Cloud."
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom CTA + Step indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Começar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = CitizenIcons.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OnboardingDotsIndicator(activeStep = 0)
        }
    }
}

@Composable
private fun OnboardingPillarCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconTint.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingLocationStep(
    onBack: () -> Unit,
    onAllowLocation: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top back row (Figma 192:124)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = CitizenIcons.ArrowBack,
                    contentDescription = "Voltar",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Voltar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(BrandBlueHero.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CitizenIcons.LocationPin,
                    contentDescription = null,
                    tint = BrandBlueHero,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Permita sua Localização",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Para enviar alertas relevantes da sua região em São Paulo, precisamos acessar sua localização com segurança.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Benefits Card (Figma 192:143)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LocationBenefitRow("Alertas precisos de incidentes num raio de 500m a 5km")
                    LocationBenefitRow("Integração com CGE-SP (Alagamentos), SSP-SP e Metrô SP")
                    LocationBenefitRow("Cálculo de Rota Segura desviando de ruas com ocorrências")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy Guarantee Box (Figma 192:192)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CitizenStatusColors.verifiedBadgeBg,
                border = BorderStroke(1.dp, CitizenStatusColors.safe.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = CitizenIcons.Lock,
                        contentDescription = null,
                        tint = CitizenStatusColors.safe,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sua privacidade é protegida",
                            style = MaterialTheme.typography.labelLarge,
                            color = CitizenStatusColors.safe,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Seus dados nunca são vendidos e todos os seus relatos na comunidade podem ser enviados de forma 100% anônima.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onAllowLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = CitizenIcons.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Permitir Localização",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "Pular por Agora",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OnboardingDotsIndicator(activeStep = 1, darkBackground = false)
        }
    }
}

@Composable
private fun LocationBenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = CitizenIcons.VerifiedCheck,
            contentDescription = null,
            tint = CitizenStatusColors.safe,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OnboardingDotsIndicator(
    activeStep: Int,
    darkBackground: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isSelected = index == activeStep
            val activeColor = if (darkBackground) BrandOrange else BrandBlue
            val inactiveColor = if (darkBackground) {
                Color.White.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isSelected) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}

@Preview(showBackground = true, name = "Onboarding Welcome - Light")
@Composable
fun PreviewCitizenOnboardingWelcome() {
    CitizenSPTheme(darkTheme = false) {
        CitizenOnboardingScreen(onFinishOnboarding = {})
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Onboarding Location - Dark"
)
@Composable
fun PreviewCitizenOnboardingLocationDark() {
    CitizenSPTheme(darkTheme = true) {
        OnboardingLocationStep(
            onBack = {},
            onAllowLocation = {},
            onSkip = {}
        )
    }
}
