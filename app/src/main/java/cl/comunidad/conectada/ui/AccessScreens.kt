package cl.comunidad.conectada.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cl.comunidad.conectada.domain.RegistrationValidator
import cl.comunidad.conectada.domain.Role

@Composable
fun AccessScreen(state: CommunityState, model: CommunityViewModel) {
    val context = LocalContext.current
    val googleClientId = remember(context) {
        val id =
            context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (id == 0) null else context.getString(id).takeIf { it.isNotBlank() }
    }
    var register by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var home by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    val errors = if (register && attempted) RegistrationValidator.validate(
        name,
        home,
        email,
        password
    ) else emptyMap()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandMark(Modifier.size(36.dp))
            Text(
                androidx.compose.ui.res.stringResource(cl.comunidad.conectada.R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        HorizontalDivider(color = Ink)
        Text(
            if (register) "Crea tu cuenta" else "Entra a tu comunidad",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Box(Modifier
            .width(48.dp)
            .height(6.dp)
            .background(Cobalt))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                shape = Square,
                selected = !register,
                onClick = { register = false; attempted = false; password = "" },
                label = { Text("Ingresar") },
                enabled = !state.busy
            )
            FilterChip(
                shape = Square,
                selected = register,
                onClick = { register = true; attempted = false; password = "" },
                label = { Text("Crear cuenta") },
                enabled = !state.busy
            )
        }
        if (register) {
            InputField(
                name,
                { if (it.length <= 60) name = it },
                "Tu nombre",
                errors["name"],
                !state.busy
            )
            InputField(
                home,
                { if (it.length <= 12) home = it },
                "Vivienda · por ejemplo 311A",
                errors["home"],
                !state.busy
            )
        }
        OutlinedTextField(
            email,
            { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.busy,
            isError = errors["email"] != null,
            supportingText = { errors["email"]?.let { Text(it) } })
        OutlinedTextField(
            password,
            { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !state.busy,
            isError = errors["password"] != null,
            supportingText = { errors["password"]?.let { Text(it) } })
        Button(
            shape = Square,
            onClick = {
                attempted = true
                if (!register || RegistrationValidator.validate(name, home, email, password)
                        .isEmpty()
                ) {
                    model.authenticate(register, name, home, email, password)
                    password = ""
                }
            },
            enabled = state.configured && !state.busy && (register || (email.isNotBlank() && password.isNotEmpty())),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
        ) {
            Text(if (register) "Crear mi cuenta" else "Entrar a mi comunidad")
        }
        if (!register && state.configured) TextButton(
            shape = Square,
            onClick = { model.resetPassword(email) },
            enabled = !state.busy
        ) { Text("Olvidé mi contraseña") }
        if (state.configured && googleClientId != null) {
            OutlinedButton(
                shape = Square,
                onClick = {
                    model.signInWithGoogle {
                        val option = GetSignInWithGoogleOption.Builder(googleClientId).build()
                        val request =
                            GetCredentialRequest.Builder().addCredentialOption(option).build()
                        try {
                            val credential = CredentialManager.create(context)
                                .getCredential(context, request).credential
                            require(
                                credential is CustomCredential &&
                                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) { "No se recibió una cuenta de Google válida." }
                            GoogleIdTokenCredential.createFrom(credential.data).idToken
                        } catch (_: GetCredentialCancellationException) {
                            null
                        }
                    }
                },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continuar con Google") }
        }
        if (!state.configured) EmptyState(
            "Estamos preparando tu comunidad",
            "La conexión de cuentas todavía no está configurada. Puedes explorar la aplicación con datos de ejemplo."
        )
        OutlinedButton(
            shape = Square,
            onClick = model::enterDemo,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Explorar demostración") }
        Text(
            "Tu vivienda puede tener varios residentes. Cada persona utiliza su propia cuenta.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )
    }
}

@Composable
fun MembershipScreen(state: CommunityState, model: CommunityViewModel) {
    val user = state.resident!!
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            "Tu cuenta está creada.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        if (user.name.isBlank() || user.home.isBlank()) {
            Text("Completa tu perfil para continuar.", color = Muted)
            ProfileEditor(user.name, user.home, state.busy, model::saveProfile)
        } else EmptyState(
            "Esperando acceso a la comunidad",
            "Administración debe asociar tu cuenta a una comunidad. No necesitas volver a registrarte."
        )
        SelectionContainerText("Identificador de cuenta: ${user.uid}")
        Button(
            shape = Square,
            onClick = model::refreshSession,
            enabled = !state.busy
        ) { Text("Comprobar mi acceso") }
        TextButton(
            shape = Square,
            onClick = model::signOut,
            enabled = !state.busy
        ) { Text("Cerrar sesión") }
    }
}

@Composable
private fun SelectionContainerText(text: String) {
    androidx.compose.foundation.text.selection.SelectionContainer {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )
    }
}

@Composable
fun ProfileScreen(state: CommunityState, model: CommunityViewModel, onNotifications: () -> Unit) {
    val user = state.resident!!
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Surface(color = Cobalt) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    user.name,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("Vivienda #${user.home}", color = CobaltWash)
                Text(
                    if (user.role == Role.ADMIN) "Administrador" else "Residente",
                    color = CobaltWash,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Text(
            "Así te ven tus vecinos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        ProfileEditor(user.name, user.home, state.busy, model::saveProfile)
        OutlinedButton(
            shape = Square,
            onClick = onNotifications,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Habilitar notificaciones") }
        Text(
            "Tu rol y comunidad los gestiona administración.",
            style = MaterialTheme.typography.bodySmall,
            color = Muted
        )
        TextButton(
            shape = Square,
            onClick = model::signOut,
            enabled = !state.busy
        ) { Text(if (state.demo) "Salir de la demostración" else "Cerrar sesión") }
    }
}

@Composable
private fun ProfileEditor(
    initialName: String,
    initialHome: String,
    busy: Boolean,
    onSave: (String, String) -> Unit
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var home by rememberSaveable(initialHome) { mutableStateOf(initialHome) }
    val errors = RegistrationValidator.validate(name, home, "valid@example.com", "123456")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InputField(
            name,
            { if (it.length <= 60) name = it },
            "Nombre mostrado",
            errors["name"],
            !busy
        )
        InputField(home, { if (it.length <= 12) home = it }, "Vivienda", errors["home"], !busy)
        Button(
            shape = Square,
            onClick = { onSave(name, home) },
            enabled = !busy && errors.isEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Guardar perfil") }
    }
}

@Composable
private fun InputField(
    value: String,
    onValue: (String) -> Unit,
    label: String,
    error: String?,
    enabled: Boolean
) {
    OutlinedTextField(
        value,
        onValue,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        isError = error != null,
        supportingText = { if (error != null) Text(error) })
}
