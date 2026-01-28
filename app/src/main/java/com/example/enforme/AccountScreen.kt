package com.example.enforme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class BankOption(val name: String, val cbnCode: String)

private val BANKS = listOf(
    BankOption("Access Bank", "044"),
    BankOption("First Bank", "011"),
    BankOption("GTBank", "058"),
    BankOption("UBA", "033"),
    BankOption("Union Bank", "032"),
    BankOption("Wema Bank", "035"),
    BankOption("Zenith Bank", "057"),
    BankOption("Fidelity Bank", "070"),
    BankOption("Sterling Bank", "232"),
    BankOption("Stanbic IBTC", "039"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankPickerField(
    label: String,
    selectedBankName: String,
    isEditing: Boolean,
    banks: List<BankOption>,
    onBankSelected: (BankOption) -> Unit
) {
    if (!isEditing) {
        ProfileField(label = label, value = selectedBankName.ifBlank { "Not set" })
        return
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selectedBankName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            banks.forEach { bank ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = bank.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onBankSelected(bank)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onSignOut: () -> Unit
) {
    val uiState by profileViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            profileViewModel.onMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                ProfileHeader(uiState.displayName, uiState.email)
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Personal Details
            item {
                ProfileCard(
                    title = "Personal Details",
                    isEditing = uiState.isEditing,
                    onToggleEdit = { profileViewModel.onToggleEditMode() }
                ) {
                    EditableProfileField(
                        label = "Display Name",
                        value = uiState.displayName,
                        isEditing = uiState.isEditing,
                        onValueChange = { profileViewModel.onDisplayNameChange(it) }
                    )
                    EditableProfileField(
                        label = "Phone Number",
                        value = uiState.phoneNumber,
                        isEditing = uiState.isEditing,
                        onValueChange = { profileViewModel.onPhoneNumberChange(it) },
                        keyboardType = KeyboardType.Phone
                    )
                    ProfileField(label = "Email", value = uiState.email)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Payment Details
            item {
                ProfileCard(
                    title = "Payment Details",
                    isEditing = uiState.isEditing,
                    onToggleEdit = { profileViewModel.onToggleEditMode() } // optional, but clearer UX
                ) {
                    BankPickerField(
                        label = "Bank",
                        selectedBankName = uiState.bankName,
                        isEditing = uiState.isEditing,
                        banks = BANKS,
                        onBankSelected = { profileViewModel.onBankSelected(it.name, it.cbnCode) }
                    )

                    EditableProfileField(
                        label = "Bank Account Number",
                        value = uiState.bankAccountNumber,
                        isEditing = uiState.isEditing,
                        onValueChange = { profileViewModel.onBankAccountChange(it) },
                        keyboardType = KeyboardType.Number,
                        isSensitive = true
                    )

                    Text(
                        text = "Your payment details are stored securely and never shared.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Button(
                    onClick = { onSignOut() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Sign Out")
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, email: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Profile Avatar",
                modifier = Modifier.fillMaxSize().padding(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = name.ifEmpty { "Welcome!" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileCard(
    title: String,
    isEditing: Boolean = false,
    onToggleEdit: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                onToggleEdit?.let {
                    IconButton(onClick = it) {
                        Icon(
                            if (isEditing) Icons.Default.Save else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit"
                        )
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun EditableProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isSensitive: Boolean = false
) {
    if (isEditing) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Done
            )
        )
    } else {
        val displayValue = if (isSensitive && value.isNotEmpty()) {
            "•••• ${value.takeLast(4)}"
        } else {
            value.ifEmpty { "Not set" }
        }
        ProfileField(label, displayValue)
    }
}
