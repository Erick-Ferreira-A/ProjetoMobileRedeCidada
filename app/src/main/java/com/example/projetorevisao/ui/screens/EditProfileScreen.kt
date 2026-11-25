package com.example.projetorevisao.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projetorevisao.data.session.LocalProfile
import com.example.projetorevisao.data.session.LocalProfileStore
import com.example.projetorevisao.data.session.ageFromBirthDate
import com.example.projetorevisao.ui.designsystem.RcPrimaryButton
import com.example.projetorevisao.ui.designsystem.RcSource
import com.example.projetorevisao.ui.designsystem.RcSourceBadge
import com.example.projetorevisao.ui.designsystem.RcTextButton
import com.example.projetorevisao.ui.theme.RcSpacing
import com.example.projetorevisao.ui.theme.RcTheme
import kotlinx.coroutines.launch

/** Só dígitos, no máximo [max]. */
private fun String.digits(max: Int) = filter(Char::isDigit).take(max)

/** "24051998" → "24/05/1998" enquanto digita. */
internal fun formatBirthDate(raw: String): String {
    val d = raw.digits(8)
    return buildString {
        d.forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }
}

/** "11987654321" → "(11) 98765-4321". */
internal fun formatPhone(raw: String): String {
    val d = raw.digits(11)
    return when {
        d.length <= 2 -> d
        d.length <= 7 -> "(${d.take(2)}) ${d.drop(2)}"
        else -> "(${d.take(2)}) ${d.drop(2).dropLast(4)}-${d.takeLast(4)}"
    }
}

/** "05866000" → "05866-000". */
internal fun formatCep(raw: String): String {
    val d = raw.digits(8)
    return if (d.length <= 5) d else "${d.take(5)}-${d.drop(5)}"
}

/**
 * Editar perfil: foto, dados pessoais e endereço.
 * Os dados ficam neste aparelho até o servidor aceitar esses campos (ver LocalProfileStore).
 */
@Composable
fun EditProfileScreen(
    accountName: String?,
    accountEmail: String?,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    LocalProfileStore.init(context)
    val stored by LocalProfileStore.profile.collectAsState()
    val scope = rememberCoroutineScope()

    var name by rememberSaveable { mutableStateOf(stored.displayName.ifBlank { accountName.orEmpty() }) }
    var birth by rememberSaveable { mutableStateOf(stored.birthDate) }
    var phone by rememberSaveable { mutableStateOf(stored.phone) }
    var cep by rememberSaveable { mutableStateOf(stored.cep) }
    var street by rememberSaveable { mutableStateOf(stored.street) }
    var number by rememberSaveable { mutableStateOf(stored.number) }
    var complement by rememberSaveable { mutableStateOf(stored.complement) }
    var district by rememberSaveable { mutableStateOf(stored.district) }
    var city by rememberSaveable { mutableStateOf(stored.city) }
    var avatarPath by rememberSaveable { mutableStateOf(stored.avatarPath) }
    var avatarVersion by remember { mutableStateOf(0) }

    val birthError = birth.isNotEmpty() && (birth.length < 10 || ageFromBirthDate(birth) == null)
    val underAge = ageFromBirthDate(birth)?.let { it < 18 } == true
    val nameError = name.isBlank()

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            LocalProfileStore.importAvatar(context, uri)?.let {
                avatarPath = it
                avatarVersion++
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(RcSpacing.md),
        verticalArrangement = Arrangement.spacedBy(RcSpacing.md)
    ) {
        // Foto
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box { androidx.compose.runtime.key(avatarVersion) { Avatar(name = name, avatarPath = avatarPath, size = 88) } }
            Spacer(Modifier.width(RcSpacing.md))
            Column {
                RcTextButton(if (avatarPath == null) "Escolher foto" else "Trocar foto", onClick = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                })
                if (avatarPath != null) RcTextButton("Remover foto", onClick = { avatarPath = null })
            }
        }

        SectionTitle("Dados pessoais")
        Field("Nome", name, { name = it }, error = if (nameError) "Informe como quer ser chamado." else null, capitalize = true)
        Field(
            label = "Data de nascimento",
            value = birth,
            onChange = { birth = formatBirthDate(it) },
            keyboard = KeyboardType.Number,
            placeholder = "dd/mm/aaaa",
            error = when {
                birthError -> "Use o formato dd/mm/aaaa."
                underAge -> "O app é para maiores de 18 anos."
                else -> null
            },
            help = ageFromBirthDate(birth)?.takeIf { !underAge }?.let { "$it anos" }
        )
        Field("Telefone", phone, { phone = formatPhone(it) }, keyboard = KeyboardType.Phone, placeholder = "(11) 98765-4321")
        Field("E-mail", accountEmail.orEmpty(), {}, enabled = false, help = "O e-mail da conta não pode ser alterado aqui.")

        SectionTitle("Endereço")
        Text(
            "Usado para mostrar alertas do seu bairro. Nunca aparece para outras pessoas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Field("CEP", cep, { cep = formatCep(it) }, keyboard = KeyboardType.Number, placeholder = "00000-000")
        Field("Rua", street, { street = it }, capitalize = true)
        Row(horizontalArrangement = Arrangement.spacedBy(RcSpacing.sm)) {
            Box(Modifier.weight(0.4f)) { Field("Número", number, { number = it.take(8) }, keyboard = KeyboardType.Number) }
            Box(Modifier.weight(0.6f)) { Field("Complemento", complement, { complement = it }, capitalize = true) }
        }
        Field("Bairro", district, { district = it }, capitalize = true)
        Field("Cidade", city, { city = it }, capitalize = true, placeholder = "São Paulo")

        SectionTitle("Documentos")
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(RcSpacing.md), verticalArrangement = Arrangement.spacedBy(RcSpacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CPF/CNPJ e RG", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                    RcSourceBadge(RcSource.ComingSoon)
                }
                Text(
                    "A verificação de identidade vai usar documentos para dar mais peso aos relatos de quem se identificou. " +
                        "Ela depende de atualizar a política de privacidade, que hoje diz que o app não coleta documentos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(value = "", onValueChange = {}, enabled = false, label = { Text("CPF ou CNPJ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = "", onValueChange = {}, enabled = false, label = { Text("RG") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Text(
            "Salvo neste aparelho. Sincronizar com a sua conta: em breve.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            RcTextButton("Cancelar", onClick = onCancel)
            Spacer(Modifier.width(RcSpacing.xs))
            RcPrimaryButton(
                text = "Salvar",
                enabled = !nameError && !birthError && !underAge,
                onClick = {
                    LocalProfileStore.save(
                        LocalProfile(
                            displayName = name.trim(), birthDate = birth, phone = phone, cep = cep,
                            street = street.trim(), number = number, complement = complement.trim(),
                            district = district.trim(), city = city.trim(), avatarPath = avatarPath
                        )
                    )
                    onSaved()
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.padding(bottom = RcSpacing.xl))
    }
}

@Composable
private fun SectionTitle(text: String) = Text(
    text,
    style = MaterialTheme.typography.titleLarge,
    color = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.padding(top = RcSpacing.xs).semantics { heading() }
)

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboard: KeyboardType = KeyboardType.Text,
    placeholder: String? = null,
    error: String? = null,
    help: String? = null,
    enabled: Boolean = true,
    capitalize: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = (error ?: help)?.let { msg ->
            { Text(msg, color = if (error != null) RcTheme.colors.critical else MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboard,
            capitalization = if (capitalize) KeyboardCapitalization.Words else KeyboardCapitalization.None
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    )
}
